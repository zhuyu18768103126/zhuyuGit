package com.manage.tn.wuyi_sport.gym.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.danmo.commonapi.base.Constant;
import com.danmo.commonapi.bean.home.school.CoachItem;
import com.danmo.commonutil.DateUtil;
import com.danmo.commonutil.recyclerview.adapter.base.RecyclerViewHolder;
import com.danmo.commonutil.recyclerview.adapter.singletype.SingleTypeAdapter;
import com.manage.tn.wuyi_sport.R;
import com.manage.tn.wuyi_sport.util.GlideRoundTransform;

public class GymCoachItemAdapter extends SingleTypeAdapter<CoachItem> {

    public GymCoachItemAdapter(@NonNull Context context) {
        super(context, R.layout.gym_coach_item_view);
    }

    @Override
    public void convert(int position, RecyclerViewHolder holder, CoachItem bean) {

        Button orderBtn=(Button)holder.get(R.id.orderBtn);
        orderBtn.setActivated(true);
        ImageView item_image=holder.get(R.id.item_image);
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_placeholder)
                .transform(new GlideRoundTransform(mContext, 8))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(mContext)
                .load(Constant.IMAGE_URL+bean.getPhoto())
                .apply(options)
                .into(item_image);
        holder.setText(R.id.name, DateUtil.isEmpty(bean.getName())?"":bean.getName());
        holder.setText(R.id.goodat,(DateUtil.isEmpty(bean.getBeGoodAt())?"":"("+bean.getBeGoodAt()+")"));
        holder.setText(R.id.coachTypeName,DateUtil.isEmpty(bean.getCoachTypeName())?"擅长: ":"擅长: "+bean.getCoachTypeName());
        holder.setText(R.id.start_value,bean.getScoreNum()+"分");
        ((RatingBar)holder.get(R.id.ratingbar)).setRating((float) bean.getScoreNum());
        holder.setText(R.id.price,"￥"+bean.getPrice().toString());
    }
}
