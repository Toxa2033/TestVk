package com.example.testvk;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.support.v4.util.Pools;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created by 95tox on 27.02.2016.
 */
public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {

    private final Context mContext;
    ArrayList<Atachment> post;
    String type;
    Long dur;
    String title;
    int count;
    public AdapterAttachment(Context context,ArrayList<Atachment> post) {
        this.mContext = context;
        this.post = post;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v=null;
       // if(type.equals("video")) {
             v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_video, viewGroup, false);
     /*   }
        else if(type.equals("photo"))
        {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_photos, viewGroup, false);
        } else if(type.equals("poll"))
        {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pool, viewGroup, false);
        }*/
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    //TODO: Стал по одной картинке выводить, пофиксить
    //TODO: добавить кнопку авторизации и парсить только опросы

    @Override
    public void onBindViewHolder(final ViewHolder videoViewHolder, int i) {
        VKAccessToken token=VKAccessToken.currentToken();
        type = post.get(i).type;
        Display display = ((WindowManager) MainActivity.context.getSystemService(MainActivity.context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if(type.equals("photo")) {

            controlPhoto(View.VISIBLE,videoViewHolder);
            controlVideo(View.GONE, videoViewHolder);
            controlPoll(View.GONE,videoViewHolder);

            Picasso.with(mContext)
                        .load(post.get(i).photo.src_big)
                        .error(mContext.getDrawable(R.drawable.comment))
                        .placeholder(mContext.getDrawable(R.drawable.heart))
                        .into(videoViewHolder.photoImage);
            }

        else if(type.equals("video"))
        {
            controlPhoto(View.GONE,videoViewHolder);
            controlVideo(View.VISIBLE, videoViewHolder);
            controlPoll(View.GONE,videoViewHolder);

            float tmpHight=(width-20)/1.5f;
            int neededHight=(int)tmpHight;
            Picasso.with(mContext)
                    .load(post.get(i).video.image_big)
                    .error(mContext.getDrawable(R.drawable.comment))
                    .placeholder(mContext.getDrawable(R.drawable.heart))
                    .resize(width-20,neededHight)
                    .into(videoViewHolder.videoPrev);
            videoViewHolder.durationTextView.setText(getDuration(post.get(i).video.duration));
            videoViewHolder.titleTextView.setText(post.get(i).video.title);
        }

        else if(type.equals("poll"))
        {
            controlPhoto(View.GONE,videoViewHolder);
            controlVideo(View.GONE, videoViewHolder);
            controlPoll(View.VISIBLE,videoViewHolder);

            videoViewHolder.titlePoolTextView.setText(post.get(i).poll.question);
            ArrayList<VkPollAnswer>  pollAnswer= VkPoll.getPollAnswers(post.get(i).poll.answers_json);
            LinearLayout.LayoutParams lpView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpView.setMargins(10, 5, 10, 0);
            if(VKSdk.isLoggedIn()) {
                for (VkPollAnswer answer:pollAnswer) {
                    TextView tv = new TextView(mContext);
                    tv.setText(answer.text);
                    tv.setGravity(Gravity.LEFT);
                    tv.setLayoutParams(lpView);
                    videoViewHolder.layout.addView(tv);


                    ProgressBar pr = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
                    pr.setMax(100);
                    pr.setProgress(answer.rate);

                    TextView prText = new TextView(mContext);
                    prText.setText(answer.rate + "% (" + answer.votes + ")");
                    prText.setGravity(Gravity.CENTER_HORIZONTAL);

                    videoViewHolder.layout.addView(prText,lpView);
                    videoViewHolder.layout.addView(pr,lpView);


                }

            }
            else {
                TextView tv = new TextView(mContext);
                tv.setText("Для просмотра результатов опроса авторизуйтесь");
                tv.setTextSize(15);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lpView);

                videoViewHolder.layout.addView(tv, lpView);
            }


        }


    }



    class GetVoters extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {

            
            return null;
        }
    }

    void controlVideo(int variosVisible, ViewHolder view) //View.GONE example
    {
        view.play.setVisibility(variosVisible);
        view.titleTextView.setVisibility(variosVisible);
        view.videoPrev.setVisibility(variosVisible);
        view.durationTextView.setVisibility(variosVisible);
    }

    void controlPoll(int variosVisible, ViewHolder view)
    {
        view.titlePoolTextView.setVisibility(variosVisible);
        view.layout.setVisibility(variosVisible);
    }

    void controlPhoto(int variosVisible, ViewHolder view)
    {
        view.photoImage.setVisibility(variosVisible);
    }

    String getDuration(long duration)
    {
        String result="";

        double hour=Math.floor(duration/60);
        double minut=Long.valueOf(duration)-hour*60;
        String tempMinut=(int)minut+"";
        if(String.valueOf((int)minut).toCharArray().length<=1)
        {
            tempMinut="0"+(int)minut;
        }
        result=(int)hour+":"+tempMinut;
        return result;
    }

    @Override
    public int getItemCount() {
        return post.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {



        private ImageView videoPrev;
        private TextView  durationTextView;
        private TextView  titleTextView;
        private ImageView photoImage;
        private LinearLayout layout;
        private TextView titlePoolTextView;
        private ImageView play;
        public ViewHolder(View itemView) {
            super(itemView);

                videoPrev = (ImageView) itemView.findViewById(R.id.videoPrevImageView);
                durationTextView = (TextView) itemView.findViewById(R.id.durationTextView);
                titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
                play=(ImageView)itemView.findViewById(R.id.playImage);
                photoImage=(ImageView)itemView.findViewById(R.id.photoImage);

                layout=(LinearLayout)itemView.findViewById(R.id.layout);
                titlePoolTextView=(TextView)itemView.findViewById(R.id.qestionTextView);



        }


    }
}

