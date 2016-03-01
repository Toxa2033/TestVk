package com.example.testvk;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final static int OWNER_ID=-56546810; //айди группы
    final static int COUNT_ALL=30; //количество всех получаемых постов
    static int COUNT_POLL=99; //количество постов получаемых, если выбранны опросы
     int COUNT_OFFSET=0; //сдвиг, для последующей подгрузки постов
    final static int DOMAIN=56546810; //айди группы без "-" в начали
    ArrayList<WallMessage> arrayWall=new ArrayList<>(); //список постов
    AdapterPost adapter;
    RecyclerView recyclerView;
    public static Context context;
    public static String titleGroup;
    public static String urlPostr; //урл постера
    SwipeRefreshLayout refreshLayout;
    boolean isLoading;  //идет загрузка или нет
    boolean isOnlyPool; //выбраны только опросы или нет

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context=this;
        refreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_container);
        recyclerView =(RecyclerView)findViewById(R.id.view);
        adapter=new AdapterPost(MainActivity.this, arrayWall);
       final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        new GetWallPost().execute();

        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                arrayWall.clear();
                adapter.notifyDataSetChanged();
                COUNT_OFFSET=0;
                new GetWallPost().execute();

            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                refreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
                int visibleItemCount = llm.getChildCount();//смотрим сколько элементов на экране
                int totalItemCount = llm.getItemCount();//сколько всего элементов
                int firstVisibleItems = llm.findFirstVisibleItemPosition();//какая позиция первого элемента

                if (!isLoading) {//проверяем, грузим мы что-то или нет
                    if ((visibleItemCount + firstVisibleItems) >= totalItemCount) {
                        isLoading = true;
                        new GetWallPost().execute();
                        }

                    }
                }



            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(MainActivity.this,"Вы успешно авторизовались", Toast.LENGTH_SHORT).show();
                reCreateActivity();
            }
            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this,"Вы не авторизовались",Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




        //получаем посты
    class GetWallPost extends AsyncTask<Void,Void,ArrayList<WallMessage>>
    {
        @Override
        protected ArrayList<WallMessage> doInBackground(Void... params) {
            VKRequest gr=VKApi.groups().getById(VKParameters.from(VKApiConst.GROUP_ID, DOMAIN)); //получаем название и аву группы
            isLoading=true;

            gr.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    try {
                    JSONArray js = response.json.getJSONArray("response");
                    JSONObject obj=js.getJSONObject(0);
                    titleGroup=obj.optString("name");
                    urlPostr=obj.optString("photo_100");
                    } catch (Exception e) {
                        String s = e.toString();
                    }
                }
            });

            //в зависимости от переменной получаем либо 30 либо 99 постов
            VKRequest request;
                if(!isOnlyPool) {
                    request = VKApi.wall().get(VKParameters.from(VKApiConst.FIELDS, "",
                            VKApiConst.OWNER_ID, OWNER_ID, VKApiConst.COUNT, COUNT_ALL, VKApiConst.EXTENDED, 0, VKApiConst.OFFSET, COUNT_OFFSET));
                }
            else { request = VKApi.wall().get(VKParameters.from(VKApiConst.FIELDS, "",
                        VKApiConst.OWNER_ID, OWNER_ID,VKApiConst.COUNT, COUNT_POLL, VKApiConst.EXTENDED, 0, VKApiConst.OFFSET, COUNT_OFFSET));}
            COUNT_OFFSET+=(isOnlyPool ? COUNT_POLL : COUNT_ALL);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    refreshLayout.setRefreshing(true); //включаем анимацию
                    try {
                        JSONObject r = response.json.getJSONObject("response");
                        long count = r.getLong("count");
                        JSONArray j = r.getJSONArray("items");
                        for (int i = 0; i < (isOnlyPool ? COUNT_POLL : COUNT_ALL); i++) { //если выбраны опросы то используется count_poll
                            JSONObject k = j.getJSONObject(i);
                            WallMessage wall = WallMessage.parse(k);
                            //в зависимости от переменной либо фильтруем данные на предмет опросов либо все подрят добавляем
                            if(isOnlyPool) {
                                if(!wall.attachments.isEmpty()) {
                                    for (Atachment item: wall.attachments) {
                                       if (item.type.equals("poll"))
                                            arrayWall.add(wall);
                                        Log.e("error", i+"");
                                    }
                               }
                            }
                            else {
                                arrayWall.add(wall);
                            }

                        }
                               adapter.notifyDataSetChanged();
                                isLoading=false;
                                refreshLayout.setRefreshing(false); //выключаем анимацию
                    } catch (Exception e) {
                        String s = e.toString();
                        Log.e("error", s);
                    }
                }
            });

            return arrayWall;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<WallMessage> aVoid) {
            super.onPostExecute(aVoid);

        }
    }





    public static String unescape(String text){
        if(text==null)
            return null;
        return text.replace("&amp;", "&").replace("&quot;", "\"").replace("<br>", "\n").replace("&gt;", ">").replace("&lt;", "<")
                .replace("<br/>", "\n").replace("&ndash;","-").trim();
        //Баг в API
        //amp встречается в сообщении, br в Ответах тип comment_photo, gt lt на стене - баг API, ndash в статусе когда аудио транслируется
        //quot в тексте сообщения из LongPoll - то есть в уведомлении
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!VKSdk.isLoggedIn()) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.logout, menu);
        }
        getMenuInflater().inflate(R.menu.pool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            VKSdk.login(MainActivity.this, new String[]{VKScope.GROUPS, VKScope.WALL});

        }
            if(id==R.id.poll)
        {
            if(!isOnlyPool) {
                isOnlyPool = true;
                COUNT_OFFSET=0;
            }
            else {
                isOnlyPool=false;
                COUNT_OFFSET=0;
            }
            arrayWall.clear();
            adapter.notifyDataSetChanged();
            new GetWallPost().execute();
        }
        if(id==R.id.logout)
        {
            VKSdk.logout();
            reCreateActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    void reCreateActivity()
    {
        Intent i = new Intent(this,MainActivity.class);
        finish();
        startActivity(i);
    }
}
