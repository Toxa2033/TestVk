package com.example.testvk;

import android.util.Log;

import com.felipecsl.asymmetricgridview.library.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyException;
import java.util.zip.GZIPInputStream;

/**
 * Created by 95tox on 29.02.2016.
 */
public class Request {
    static final String TAG="Api";

    public static final String BASE_URL="https://api.vk.com/method/";
    public static final String API_VERSION="5.5";
     JSONObject sendRequest(Params params) throws IOException, JSONException, KException {
        return sendRequest(params, false);
    }


    public Request(String access_token, String api_id){
        this.access_token=access_token;
        this.api_id=api_id;
    }

    public void setAccessToken(String access_token){
        this.access_token=access_token;
    }

    String access_token;
    String api_id;
    static boolean enable_compression=true;

    private final static int MAX_TRIES=3;
    private JSONObject sendRequest(Params params, boolean is_post) throws IOException, JSONException, KException {
        String url = getSignedUrl(params, is_post);
        String body="";
        if(is_post)
            body=params.getParamsString();
        Log.i(TAG, "url="+url);
        if(body.length()!=0)
            Log.i(TAG, "body="+body);
        String response="";
        for(int i=1;i<=MAX_TRIES;++i){
            try{
                if(i!=1)
                    Log.i(TAG, "try " + i);
                response = sendRequestInternal(url, body, is_post);
                break;
            }catch(javax.net.ssl.SSLException ex){
                processNetworkException(i, ex);
            }catch(java.net.SocketException ex){
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response="+response);
        JSONObject root=new JSONObject(response);
        checkError(root, url);
        return root;
    }

    private void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if(i==MAX_TRIES)
            throw ex;
    }

    private String sendRequestInternal(String url, String body, boolean is_post) throws IOException {
        HttpURLConnection connection=null;
        try{
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoOutput(is_post);
            connection.setDoInput(true);
            connection.setRequestMethod(is_post?"POST":"GET");
            if(enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");
            if(is_post)
                connection.getOutputStream().write(body.getBytes("UTF-8"));
            int code=connection.getResponseCode();
            Log.i(TAG, "code="+code);
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code==-1)
                throw new WrongResponseCodeException("Network error");
            //может стоит проверить на код 200
            //on error can also read error stream from connection.
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if(enc!=null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            String response= convertStreamToString(is);
            return response;
        }
        finally{
            if(connection!=null)
                connection.disconnect();
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        InputStreamReader r = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1;)
                sw.write(buffer, 0, n);
        }
        finally{
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.toString();
    }

    private String getSignedUrl(Params params, boolean is_post) {
        params.put("access_token", access_token);
        if(!params.contains("v"))
            params.put("v", API_VERSION);

        String args = "";
        if(!is_post)
            args=params.getParamsString();

        return BASE_URL+params.method_name+"?"+args;
    }


    /*** utils methods***/
    private void checkError(JSONObject root, String url) throws JSONException,KException {
        if(!root.isNull("error")){
            JSONObject error=root.getJSONObject("error");
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code==17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
        if(!root.isNull("execute_errors")){
            JSONArray errors=root.getJSONArray("execute_errors");
            if(errors.length()==0)
                return;
            //only first error is processed if there are multiple
            JSONObject error=errors.getJSONObject(0);
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url);
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            if (code==17)
                e.redirect_uri = error.optString("redirect_uri");
            throw e;
        }
    }
}
