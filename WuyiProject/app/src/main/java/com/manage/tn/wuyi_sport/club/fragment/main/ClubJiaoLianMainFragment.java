package com.manage.tn.wuyi_sport.club.fragment.main;

import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.danmo.commonapi.bean.BaseResponse;
import com.danmo.commonapi.bean.home.club.ClubItem;
import com.manage.tn.wuyi_sport.R;
import com.manage.tn.wuyi_sport.base.BaseActivity;
import com.manage.tn.wuyi_sport.base.BaseFragment;
import com.manage.tn.wuyi_sport.base.HeadFragment;
import com.manage.tn.wuyi_sport.base.ViewHolder;
import com.manage.tn.wuyi_sport.club.fragment.ClubJiaoLianFragment;

public class ClubJiaoLianMainFragment extends BaseFragment {
    ClubItem clubItem;
    @Override
    protected void httpOnResult(BaseResponse response) {
    }

    @Override
    protected void lazyLoad() {
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        clubItem= (ClubItem) getArguments().getSerializable("clubItem");
        lazyLoad();
        FragmentTransaction transaction = ((BaseActivity)mContext).getSupportFragmentManager().beginTransaction();
        HeadFragment headFragment=new HeadFragment();
        headFragment.showTyp=1;
        headFragment.channelType=3;
        headFragment.channelName="教练";
        headFragment.recordId="3";
        transaction.add(R.id.headView3,headFragment,"HeadFragment");
        transaction.commitAllowingStateLoss();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main3;
    }
}
