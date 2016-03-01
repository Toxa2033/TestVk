package com.example.testvk;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;


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



        //раньше  было разделение по лэйаутам, но в один прекрасный момент это перестало работать
            // пришлось выкручиваться
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v=null;
       // if(type.equals("video")) {
             v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_attachments, viewGroup, false);
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



    @Override
    public void onBindViewHolder(final ViewHolder videoViewHolder, int i) {
        VKAccessToken token=VKAccessToken.currentToken();
        type = post.get(i).type;
        Display display = ((WindowManager) MainActivity.context.getSystemService(MainActivity.context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        
        
        //Смотрим тип и в зависимости от него отображаем или скрываем вьюхи и выводим данные 
        if(type.equals("photo")) {

            controlPhoto(View.VISIBLE,videoViewHolder);
            controlVideo(View.GONE, videoViewHolder);
            controlPoll(View.GONE,videoViewHolder);

            Picasso.with(mContext)
                        .load(post.get(i).photo.src_big)
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
                    .resize(width-20,neededHight)
                    .into(videoViewHolder.videoPrev);
            videoViewHolder.durationTextView.setText(getDuration(post.get(i).video.duration));
            videoViewHolder.titleTextView.setText(post.get(i).video.title);
        }

        else if(type.equals("poll"))
        {

            controlPhoto(View.GONE, videoViewHolder);
            controlVideo(View.GONE, videoViewHolder);
            controlPoll(View.VISIBLE,videoViewHolder);

            ArrayList<Long>answerIDs=new ArrayList<>(); //список id ответов
            videoViewHolder.titlePoolTextView.setText(post.get(i).poll.question);
            ArrayList<VkPollAnswer>  pollAnswer= VkPoll.getPollAnswers(post.get(i).poll.answers_json);
            LinearLayout.LayoutParams lpView = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);



            lpView.setMargins(10, 5, 10, 0);

            //смотрим злогинен, проверяю  голосовали ранее
            if(VKSdk.isLoggedIn()) {
                if(new VkPoll().checkYouVote(post.get(i).poll.id,mContext))
                {
                    //выводим результаты опроса
                    viewResultPoll(pollAnswer,videoViewHolder,lpView);
                }
                else {
                            //выводим радио буттоны для голосования
                    for(int j=0; j<pollAnswer.size(); j++) {
                        RadioButton rb = new RadioButton(mContext);
                        rb.setText(pollAnswer.get(j).text);
                        rb.setId(j);
                        videoViewHolder.radioGroup.addView(rb, lpView);
                        answerIDs.add(pollAnswer.get(j).id);
                    }
                    videoViewHolder.setDateForVote(post.get(i).poll.id,answerIDs,post.get(i).poll.owner_id,pollAnswer,lpView,videoViewHolder);
                }

            }
            else {
                //предлогаем залогинеться
                videoViewHolder.voteButton.setVisibility(View.GONE);
                TextView tv = new TextView(mContext);
                tv.setText("Для просмотра результатов опроса авторизуйтесь");
                tv.setTextSize(15);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setLayoutParams(lpView);

                videoViewHolder.layout.addView(tv, lpView);
            }

        }
        }



    void viewResultPoll(ArrayList<VkPollAnswer>  pollAnswer, ViewHolder view, LinearLayout.LayoutParams lpView)
    {
        view.voteButton.setVisibility(View.GONE);
        for (VkPollAnswer answer:pollAnswer) {
            TextView tv = new TextView(mContext);
            tv.setText(answer.text);
            tv.setGravity(Gravity.LEFT);
            tv.setLayoutParams(lpView);
            view.layout.addView(tv);

            ProgressBar pr = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
            pr.setMax(100);
            pr.setProgress(answer.rate);

            TextView prText = new TextView(mContext);
            prText.setText(answer.rate + "% (" + answer.votes + ")");
            prText.setGravity(Gravity.CENTER_HORIZONTAL);

            view.layout.addView(prText, lpView);
            view.layout.addView(pr, lpView);
        }
    }


    //Управление видимостью вьюх
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
        view.radioGroup.setVisibility(variosVisible);
        view.voteButton.setVisibility(variosVisible);
    }

    void controlPhoto(int variosVisible, ViewHolder view)
    {
        view.photoImage.setVisibility(variosVisible);
    }



        //продлжительность видео
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private ImageView videoPrev;
        private TextView durationTextView;
        private TextView titleTextView;
        private ImageView photoImage;
        private LinearLayout layout;
        private TextView titlePoolTextView;
        private ImageView play;
        private RadioGroup radioGroup;
        private Button voteButton;

        long poll_id;
        ArrayList<Long> answer_id;
        long owner_id;
        ArrayList<VkPollAnswer>  pollAnswer;
        LinearLayout.LayoutParams lpView;
        ViewHolder view;
        public ViewHolder(View itemView) {
            super(itemView);

            videoPrev = (ImageView) itemView.findViewById(R.id.videoPrevImageView);
            durationTextView = (TextView) itemView.findViewById(R.id.durationTextView);
            titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
            play = (ImageView) itemView.findViewById(R.id.playImage);
            photoImage = (ImageView) itemView.findViewById(R.id.photoImage);

            layout = (LinearLayout) itemView.findViewById(R.id.layout);
            titlePoolTextView = (TextView) itemView.findViewById(R.id.qestionTextView);
            radioGroup = (RadioGroup) itemView.findViewById(R.id.radioGroup);
            voteButton = (Button) itemView.findViewById(R.id.voteButton);
            voteButton.setOnClickListener(this);

        }
        void setDateForVote(long poll_id, ArrayList<Long> answer_id,long owner_id,ArrayList<VkPollAnswer>  pollAnswer,
                            LinearLayout.LayoutParams lpView, ViewHolder view)
        {
            this.poll_id=poll_id;
            this.answer_id=answer_id;
            this.owner_id=owner_id;
            this.pollAnswer=pollAnswer;
            this.lpView=lpView;
            this.view=view;
        }

        class Vote extends AsyncTask<Integer, Void, Void>
        {

            @Override
            protected Void doInBackground(Integer... params) {
                //голосуем
                new VkPoll().vote(poll_id, answer_id.get(params[0]), owner_id, mContext);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //выводим результаты
                new VkPoll().voted(poll_id,mContext);
                voteButton.setVisibility(View.GONE);
                radioGroup.setVisibility(View.GONE);
                viewResultPoll(pollAnswer, view, lpView);

            }
        }
            //отлавливаем нажатие на кнопку и голосуем в отдельном потоке
        @Override
        public void onClick(View v) {
            int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            new Vote().execute(checkedRadioButtonId);
        }
    }
    }

