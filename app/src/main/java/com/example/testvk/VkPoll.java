package com.example.testvk;

import android.content.Context;
import android.os.AsyncTask;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


public class VkPoll implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;
    public String question;
    public long owner_id;
    public Long created;
    public Long votes;
    public Long answer_id;
    public String answers_json;
    public boolean anonymous;
    public Long topic_id;//if poll is attached to topic

    public static VkPoll parse(JSONObject o) throws NumberFormatException, JSONException {
        VkPoll v = new VkPoll();
        v.id = o.getLong("id");
        v.question = MainActivity.unescape(o.getString("question"));
        if(o.has("owner_id"))
            v.owner_id = o.getLong("owner_id");
        if(o.has("created"))
            v.created = o.optLong("created");
        if(o.has("votes"))
            v.votes = o.optLong("votes");
        if(o.has("answer_id"))
            v.answer_id = o.optLong("answer_id");
        if(o.has("answers"))
            v.answers_json = o.getJSONArray("answers").toString();
        if(o.has("anonymous"))
            v.anonymous = o.getString("anonymous").equals("1");
        return v;
    }

    public static ArrayList<VkPollAnswer> getPollAnswers(String answers_json) {
        ArrayList<VkPollAnswer> answers = new ArrayList<VkPollAnswer>();
        try {
            JSONArray array = new JSONArray(answers_json);
            for(int i=0; i<array.length(); ++i){
                if(array.get(i) instanceof JSONObject == false)
                    continue;
                JSONObject o = (JSONObject)array.get(i);
                VkPollAnswer pa = new VkPollAnswer();
                pa.id = o.getLong("id");
                pa.votes = o.getInt("votes");
                pa.text = MainActivity.unescape(o.getString("text"));
                pa.rate = o.getInt("rate");
                answers.add(pa);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return answers;
    }



    //http://vk.com/dev/polls.getVoters
    public ArrayList<User> getPollVoters(long poll_id, long owner_id, Collection<Long> answer_ids, /*Long count, Long offset,*/ String fields) throws JSONException, IOException, KException {
        Params params = new Params("polls.getVoters");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        params.put("answer_ids", arrayToString(answer_ids));
        //params.put("count", count);
       // params.put("offset", offset);
        params.put("fields", fields);
        JSONObject root = new Request(VKAccessToken.currentToken().accessToken, VKAccessToken.currentToken().userId).sendRequest(params);
        JSONArray response=root.optJSONArray("response");//массив ответов
        JSONObject object = (JSONObject)response.get(0);
        JSONObject array2 = object.optJSONObject("users");
        JSONArray array=array2.optJSONArray("items");
        //TODO for others answer_ids
        return User.parseUsers(array);
    }


    <T> String arrayToString(Collection<T> items) {
        if(items==null)
            return null;
        String str_cids = "";
        for (Object item:items){
            if(str_cids.length()!=0)
                str_cids+=',';
            str_cids+=item;
        }
        return str_cids;
    }

  public  boolean checkYouVote(long pool_id, Context context)
    {
        if(VKSdk.isLoggedIn()) {
            File file=new File(context.getFilesDir()+"/"+ VKAccessToken.currentToken().userId);
            boolean exist=file.isFile()&&file.exists();
            if(exist) {
                try {
                    // открываем поток для чтения
                    BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(VKAccessToken.currentToken().userId)));
                    String str = "";
                    // читаем содержимое
                    while ((str = br.readLine()) != null) {
                        if(String.valueOf(pool_id).equals(str)) //если в проголосовавших есть то тру
                        {
                            return true;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        return false;
    }




   public boolean voted(long pool_id,Context context)
    {

        if(VKSdk.isLoggedIn()) {
         //   File file=new File(context.getFilesDir()+"/"+VKAccessToken.currentToken().userId);
           // boolean exist=file.isFile()&&file.exists();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(VKAccessToken.currentToken().userId)));
                String str = "";
                String tmp="";
                // читаем содержимое
                while ((str = br.readLine()) != null) {
                    tmp+=str+"\n";
                }
                // открываем поток для чтения
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(VKAccessToken.currentToken().userId, context.MODE_PRIVATE)));
                // пишем данные

                bw.write(tmp+String.valueOf(pool_id));
                bw.close();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private  int addPollVote(long poll_id, long answer_id, long owner_id, long topic_id, String captcha_key, String captcha_sid) throws JSONException, IOException, KException {
        Params params = new Params("polls.addVote");
        params.put("owner_id", owner_id);
        params.put("poll_id", poll_id);
        if(topic_id!=0)
            params.put("board", topic_id);
        params.put("answer_id", answer_id);
        //addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = new Request(VKAccessToken.currentToken().accessToken,VKAccessToken.currentToken().userId).sendRequest(params);
        return root.getInt("response");
    }


    public boolean vote(long poll_id,long answer_id,long owner_id, Context context)
    {
        try {
            if(addPollVote(poll_id,answer_id,owner_id,0,null,null)==1)
            {
                return true;
               // if(voted(poll_id,context))
               // return true;
            }
        }
        catch (Exception e)
        {
            String s =e.toString();
        }
        return false;
    }

}