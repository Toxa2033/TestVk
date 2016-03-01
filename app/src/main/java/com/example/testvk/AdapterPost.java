package com.example.testvk;

import android.app.FragmentManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.felipecsl.asymmetricgridview.library.Utils;
import com.felipecsl.asymmetricgridview.library.model.AsymmetricItem;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.squareup.picasso.Picasso;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class AdapterPost extends RecyclerView.Adapter<AdapterPost.VideoViewHolder> {

    private final Context mContext;
    ArrayList<WallMessage> post;
    public AdapterPost(Context context, ArrayList<WallMessage> post) {
        this.mContext = context;
        this.post=post;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_post, viewGroup, false);
        return new VideoViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder videoViewHolder, int i) {
        videoViewHolder.setDate(post);
        WallMessage item=post.get(i);

        Picasso.with(mContext)
                .load("https://pp.vk.me/c9236/v9236078/1721/CwafbKzyR-w.jpg")
                .resize(50,50)
                .centerInside()
                .into(videoViewHolder.avatarImage);

        videoViewHolder.nameGroupTextView.setText("АнтиГАИ");

        if(!MainActivity.titleGroup.isEmpty())
        {
            String s=MainActivity.titleGroup+"";
            videoViewHolder.nameGroupTextView.setText(s);
            Picasso.with(mContext)
                    .load(MainActivity.urlPostr)
                    .resize(50, 50)
                    .centerInside()
                    .into(videoViewHolder.avatarImage);
        }

        videoViewHolder.commentImage.setImageDrawable(mContext.getDrawable(R.drawable.comment));
        videoViewHolder.repostImage.setImageDrawable(mContext.getDrawable(R.drawable.repost));
        videoViewHolder.likeImage.setImageDrawable(mContext.getDrawable(R.drawable.heart));


        Date temp = new Date(item.date*1000);

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        videoViewHolder.timeTextView.setText(format.format(temp));
        if(item.text.toCharArray().length>=300)
        {
            videoViewHolder.textTextView.setText(item.text.substring(0,300)+"\n\t...\n Показать полностью");
        }
        else {
            videoViewHolder.textTextView.setText(item.text);
        }


        videoViewHolder.comentCountTextView.setText(String.valueOf(item.comment_count));
        videoViewHolder.repostCountTextView.setText(String.valueOf(item.reposts_count));
        videoViewHolder.likeCountTextView.setText(String.valueOf(item.like_count));


        if(item.attachments.size()>0) {

           // for (Atachment att:item.attachments) {
            final LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                videoViewHolder.recyclerView.setLayoutManager(layoutManager);
                AdapterAttachment adapter = new AdapterAttachment(mContext, item.attachments);
                videoViewHolder.recyclerView.setAdapter(adapter);

           // }
        }

    }


    public class CustomLinearLayoutManager extends LinearLayoutManager {

        private  final String TAG = CustomLinearLayoutManager.class.getSimpleName();

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {

            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);

            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);


                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            try {
                View view = recycler.getViewForPosition(0);//fix 动态添加时报IndexOutOfBoundsException

                if (view != null) {
                    RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

                    int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                            getPaddingLeft() + getPaddingRight(), p.width);

                    int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                            getPaddingTop() + getPaddingBottom(), p.height);

                    view.measure(childWidthSpec, childHeightSpec);
                    measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                    measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                    recycler.recycleView(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

        @Override
        public int getItemCount () {
              return post.size();
        }


        class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


            private ImageView avatarImage;
            private TextView nameGroupTextView;
            private TextView timeTextView;
            private TextView textTextView;
            private TextView comentCountTextView;
            private TextView repostCountTextView;
            private TextView likeCountTextView;
            private ImageView commentImage;
            private ImageView repostImage;
            private ImageView likeImage;
            private ImageView playImageView;
            RecyclerView recyclerView;
            public VideoViewHolder(View itemView) {
                super(itemView);
                avatarImage = (ImageView) itemView.findViewById(R.id.imageAvGroup);
                timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
                textTextView = (TextView) itemView.findViewById(R.id.textPost);
                comentCountTextView = (TextView) itemView.findViewById(R.id.comentCountTextView);
                repostCountTextView = (TextView) itemView.findViewById(R.id.repostCountTextView);
                likeCountTextView = (TextView) itemView.findViewById(R.id.likeCountTextView);
                commentImage = (ImageView) itemView.findViewById(R.id.comentImage);
                repostImage = (ImageView) itemView.findViewById(R.id.repostImage);
                likeImage = (ImageView) itemView.findViewById(R.id.likeImage);
            //    playImageView=(ImageView)itemView.findViewById(R.id.playImageView);
               // gridView= (StaggeredGridView)itemView.findViewById(R.id.grid_view);
                nameGroupTextView=(TextView)itemView.findViewById(R.id.nameGroup);
                recyclerView=(RecyclerView)itemView.findViewById(R.id.recyclePhoto);
                itemView.setOnClickListener(this);
            }

            ArrayList<WallMessage> item;
            void setDate(ArrayList<WallMessage> item)
            {
                this.item=item;
            }

            @Override
            public void onClick(View v) {
                textTextView.setText(item.get(getAdapterPosition()).text);
            }
        }

}
