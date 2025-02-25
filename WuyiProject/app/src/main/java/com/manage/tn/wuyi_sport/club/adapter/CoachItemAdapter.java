package com.manage.tn.wuyi_sport.club.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.ImageView;

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

public class CoachItemAdapter extends SingleTypeAdapter<CoachItem> {
    public CoachItemAdapter(@NonNull Context context) {
        super(context, R.layout.coach_item_view);
    }

    @Override
    public void convert(int position, RecyclerViewHolder holder, CoachItem bean) {
        Button guanZhuBtn=(Button)holder.get(R.id.guanZhuBtn);
        guanZhuBtn.setActivated(true);
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
        holder.setText(R.id.remark,DateUtil.isEmpty(bean.getRemark())?"":bean.getRemark());
        holder.setText(R.id.item_vc,"浏览量 "+bean.getVisitNum());
        holder.setText(R.id.favorNum,bean.getFavorNum()+" 万");
        holder.setText(R.id.focusNum,"粉丝 "+bean.getFocusNum()+"万");

    }
}
