package com.example.testvk;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.util.Pools;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
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

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 95tox on 27.02.2016.
 */
public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {

    private final Context mContext;
    Atachment post;
    String type;
    Long dur;
    String title;
    int count;
    public AdapterAttachment(Context context,Atachment post) {
        this.mContext = context;
        this.post = post;
        type=post.type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v=null;
        if(type.equals("video")) {
             v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_video, viewGroup, false);
        }
        else if(type.equals("photo"))
        {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_photos, viewGroup, false);
        } else if(type.equals("poll"))
        {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pool, viewGroup, false);
        }
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
        Display display = ((WindowManager) MainActivity.context.getSystemService(MainActivity.context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if(type.equals("photo")) {

                Picasso.with(mContext)
                        .load(post.photo.src_big)
                        .error(mContext.getDrawable(R.drawable.comment))
                        .placeholder(mContext.getDrawable(R.drawable.heart))
                        .into(videoViewHolder.photoImage);
            }

        else if(type.equals("video"))
        {
            float tmpHight=(width-20)/1.5f;
            int neededHight=(int)tmpHight;
            Picasso.with(mContext)
                    .load(post.video.image_big)
                    .error(mContext.getDrawable(R.drawable.comment))
                    .placeholder(mContext.getDrawable(R.drawable.heart))
                    .resize(width-20,neededHight)
                    .into(videoViewHolder.videoPrev);
            videoViewHolder.durationTextView.setText(getDuration(post.video.duration));
            videoViewHolder.titleTextView.setText(post.video.title);
        } else if(type.equals("poll"))
        {
            videoViewHolder.titlePoolTextView.setText(post.poll.question);
            ArrayList<VkPollAnswer>  pollAnswer= VkPoll.getPollAnswers(post.poll.answers_json);
            LinearLayout.LayoutParams lpView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpView.setMargins(10,5,10,0);
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
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {



        private ImageView videoPrev;
        private TextView  durationTextView;
        private TextView  titleTextView;
        private ImageView photoImage;
        private LinearLayout layout;
        private TextView titlePoolTextView;
        public ViewHolder(View itemView) {
            super(itemView);

            if(type.equals("video")) {
                videoPrev = (ImageView) itemView.findViewById(R.id.videoPrevImageView);
                durationTextView = (TextView) itemView.findViewById(R.id.durationTextView);
                titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            }
           else if(type.equals("photo"))
            {
                photoImage=(ImageView)itemView.findViewById(R.id.photoOnWallImagView);
            }
            else if(type.equals("poll"))
            {
                layout=(LinearLayout)itemView.findViewById(R.id.pollLayout);
                titlePoolTextView=(TextView)itemView.findViewById(R.id.titlePoolTextView);

            }

        }


    }
}

