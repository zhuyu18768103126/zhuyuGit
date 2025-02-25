package com.manage.tn.wuyi_sport.base.refresh;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.danmo.commonapi.base.Constant;
import com.danmo.commonapi.bean.BaseResponse;
import com.danmo.commonutil.DateUtil;
import com.danmo.commonutil.recyclerview.adapter.base.RecyclerViewHolder;
import com.danmo.commonutil.recyclerview.adapter.multitype.HeaderFooterAdapter;
import com.manage.tn.wuyi_sport.R;
import com.manage.tn.wuyi_sport.base.BaseFragment;
import com.manage.tn.wuyi_sport.base.ViewHolder;
import com.manage.tn.wuyi_sport.base.refresh.FooterProvider;
import com.manage.tn.wuyi_sport.util.fastcolck.SingleClick;


import java.util.List;

import okhttp3.internal.Util;

/**
 * 具有下拉刷新和上拉加载的Fragment
 */
public abstract class RefreshRecyclerFragment extends
        BaseFragment {
    // 请求状态 - 下拉刷新 还是 加载更多
    public static final String POST_HEADER = "load_header";
    public static final String POST_LOAD_MORE = "load_more";
    public static final String POST_REFRESH = "refresh";
    public static final String POST_MID = "mid";
    // 当前状态
    private static final int STATE_NORMAL = 0;      // 正常
    private static final int STATE_NO_MORE = 1;     // 正在
    private static final int STATE_LOADING = 2;     // 加载
    private static final int STATE_REFRESH = 3;     // 刷新
    // 分页加载
    protected int pageIndex = 0;                      // 当面页码
    protected int pageCount = 20;                     // 每页个数
    protected RecyclerView mRecyclerView;
    // 适配器
    protected HeaderFooterAdapter mAdapter;
    protected FooterProvider mFooterProvider;
    //    protected boolean isFirstAddFooter = true;

    private int mState = STATE_NORMAL;
    // View
    public SwipeRefreshLayout mRefreshLayout;
    // 状态
    private boolean refreshEnable = true;           // 是否允许刷新
    private boolean loadMoreEnable = false;          // 是否允许加载
    private Boolean isRequestShowed = true;
    public static int SCROLL_STATE_DOWN = 2;
    public static int SCROLL_STATE_UP = 1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_refresh_recycler;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {

        // 适配器
        mAdapter = new HeaderFooterAdapter();
        mFooterProvider = new FooterProvider(getContext()) {
            @Override
            public void needLoadMore(RecyclerViewHolder holder) {
                 loadMore();
            }
        };
        mFooterProvider.setFooterNormal();
        mAdapter.registerFooter(new Footer(), mFooterProvider);
        // refreshLayout
        mRefreshLayout = holder.get(R.id.refresh_layout);
        mRefreshLayout.setProgressViewOffset(false, -20, 80);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mRefreshLayout.setEnabled(true);
        // RecyclerView
        mRecyclerView = holder.get(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(getRecyclerViewLayoutManager());
        mRecyclerView.setOnScrollListener(mOnScrollListener);
        lazyLoad();
        setAdapterRegister(getContext(), mRecyclerView, mAdapter);
        // 监听 RefreshLayout 下拉刷新
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(POST_HEADER);
                refresh(POST_REFRESH);
                refresh(POST_MID);
            }
        });

    }

    protected  void lazyLoad(){
        initData(mAdapter);
    }
    protected void refresh(String freshType) {
        if (!refreshEnable) return;
        if (freshType.equals(POST_REFRESH)) {
            pageIndex = 0;
            String uuid = request(pageIndex * pageCount, pageCount);
            if(null!=uuid){
                mPostTypes.put(uuid, POST_REFRESH);
                pageIndex++;
                mState = STATE_REFRESH;
            }else{
                mState = STATE_NORMAL;
                mRefreshLayout.setRefreshing(false);
            }

        } else if (freshType.equals(POST_HEADER)) {
            loadHeader();
        } else if (freshType.equals(POST_MID)) {
            loadMiddle();
        }
    }

    protected void loadMore() {
            if (!loadMoreEnable) return;
            if (mState == STATE_NO_MORE) return;
            String uuid = request(pageIndex * pageCount, pageCount);
            loadMoreEnable=false;
            if(uuid!=null){
                mPostTypes.put(uuid, POST_LOAD_MORE);
                pageIndex++;
                mState = STATE_LOADING;
                mFooterProvider.setFooterLoading();
            }

    }

    protected void loadHeader() {
        String uuid = requestHeader();
        if(null!=uuid){
            mPostTypes.put(uuid, POST_HEADER);
        }

    }

    protected void loadMiddle() {
        String uuid = requestMiddle();
        if(null!=uuid){
            mPostTypes.put(uuid, POST_MID);
        }

    }


    @Override
    protected void httpOnResult(BaseResponse event) {
        String postType = (String) mPostTypes.get(event.getUuid());
        if(null!=postType){
            if(event.getCode()== Constant.SUCCESS){
                if (postType.equals(POST_HEADER)) {
                    onLoadHeader(event, mAdapter);
                } else if (postType.equals(POST_MID)) {
                    onLoadMiddle(event, mAdapter);
                } else if (postType.equals(POST_LOAD_MORE)) {
                    onLoadMore(event);
                } else if (postType.equals(POST_REFRESH)) {
                    onRefresh(event);
                }
            }else{
                onError(event);
            }
            mPostTypes.remove(event.getUuid());
        }
    }



    protected void onRefresh(BaseResponse event) {
        mState = STATE_NORMAL;
        mRefreshLayout.setRefreshing(false);
        onRefresh(event, mAdapter);
    }

    protected void onLoadMore(BaseResponse event) {

        if(event.getRows() instanceof List){
            if (((List)event.getRows()).size() < pageCount) {
                mState = STATE_NO_MORE;
                mFooterProvider.setFooterNormal();
            } else {
                mState = STATE_NORMAL;
                mFooterProvider.setFooterNormal();
            }
        }
        if(!loadMoreEnable){
            onLoadMore(event, mAdapter);
            loadMoreEnable=true;
        }

    }

    protected void onError(BaseResponse event) {
        mState = STATE_NORMAL;  // 状态重置为正常，以便可以重试，否则进入异常状态后无法再变为正常状态
        String postType = (String) mPostTypes.get(event.getUuid());
        if (postType.equals(POST_LOAD_MORE)) {
            mFooterProvider.setFooterError(new View.OnClickListener() {
                @SingleClick
                @Override
                public void onClick(View v) {
                    pageIndex--;
                    loadMore();
                }
            });
        } else if (postType.equals(POST_REFRESH)) {
            mRefreshLayout.setRefreshing(false);
            mFooterProvider.setFooterNormal();
        }
        onError(event, postType);
    }

    public void setRefreshEnable(boolean refreshEnable) {
        this.refreshEnable = refreshEnable;
        mRefreshLayout.setEnabled(refreshEnable);
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        this.loadMoreEnable = loadMoreEnable;

    }

    public void quickToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }



    /**
     * 通知导航栏显示或隐藏
     */
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int mScrollState = recyclerView.getScrollState();
            if (mScrollState == RecyclerView.SCROLL_STATE_DRAGGING || mScrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                if (dy > 50) {//up -> hide
                    if (isRequestShowed) {
                      //  EventBus.getDefault().post(new EventBusMsg(SCROLL_STATE_UP));
                        isRequestShowed = false;
                    }
                } else if (dy < -50) {//down -> show
                    if (!isRequestShowed) {
                      //  EventBus.getDefault().post(new EventBusMsg(SCROLL_STATE_DOWN));
                        isRequestShowed = true;
                    }
                }
            }
        }
    };

    //--- 需要继承类处理的部分 ----------------------------------------------------------------------

    /**
     * 加载数初始化数据，可以从缓存或者其他地方加载，
     * 如果没有初始数据，一般调用 loadMore() 即可。
     *
     * @param adapter 适配器
     */
    public abstract void initData(HeaderFooterAdapter adapter);

    /**
     * 为 RecyclerView 的 Adapter 注册数据类型
     * 例如： adapter.register(Bean.class, new BeanProvider(getContext()));
     *
     * @param context      上下文
     * @param recyclerView RecyclerView
     * @param adapter      Adapter
     */
    protected abstract void setAdapterRegister(Context context, android.support.v7.widget.RecyclerView recyclerView,
                                               HeaderFooterAdapter adapter);

    /**
     * 获取 RecyclerView 的 LayoutManager
     * 例如： return new LinerLayoutManager(context);
     *
     * @return LayoutManager
     */
    @NonNull
    protected abstract RecyclerView.LayoutManager getRecyclerViewLayoutManager();

    /**
     * 请求数据，并返回请求的 uuid
     * 例如：return mDiycode.getTopicsList(null, mNodeId, offset, limit);
     *
     * @param offset 偏移量
     * @param limit  请求数量
     * @return uuid
     */
    @NonNull
    protected abstract String request(int offset, int limit);

    /**
     * 请求recyclerView的头部数据，并返回请求的 uuid
     * 例如：return mDiycode.getTopicsList(null, mNodeId, offset, limit);
     *
     * @return uuid
     */
    @NonNull
    protected abstract String requestHeader();

    /**
     * 请求recyclerView的中间部分内容数据，并返回请求的 uuid
     * 例如：return mDiycode.getTopicsList(null, mNodeId, offset, limit);
     *
     * @return uuid
     */
    @NonNull
    protected abstract String requestMiddle();


    /**
     * 数据刷新成功的回调，由于不同页面可能要对数据进行处理，例如重新排序，清理掉一些无效数据等，所以由子类自己实现，
     * 如果不需要特殊处理，一般像下面这样写就行:
     * adapter.clearDatas();
     * adapter.addDatas(event.geiBean());
     *
     * @param event   Event
     * @param adapter Adapter
     */
    protected abstract void onRefresh(BaseResponse event, HeaderFooterAdapter adapter);

    /**
     * 数据加载成功时调用，如果不需要对数据进行特殊处理，这样写就行：
     * adapter.addDatas(event.getBean());
     *
     * @param event   Event
     * @param adapter Adapter
     */
    protected abstract void onLoadMore(BaseResponse event, HeaderFooterAdapter adapter);

    /**
     * 给列表增加头部，如幻灯片等.
     * adapter.addDatas(event.getBean());
     *
     * @param event   Event
     * @param adapter Adapter
     */
    protected abstract void onLoadHeader(BaseResponse event, HeaderFooterAdapter adapter);

    /**
     * 给列表增加中间部分.
     * adapter.addDatas(event.getBean());
     *
     * @param event   Event
     * @param adapter Adapter
     */
    protected abstract void onLoadMiddle(BaseResponse event, HeaderFooterAdapter adapter);

    /**
     * 数据加载错误时调用，你可以在这里获取错误类型并进行处理，如果不需要特殊处理，弹出一个 toast 提醒用户即可。
     * if (postType.equals(POST_LOAD_MORE)) {
     * toast("加载更多失败");
     * } else if (postType.equals(POST_REFRESH)) {
     * toast("刷新数据失败");
     * }
     *
     * @param event
     * @param postType
     */
    protected abstract void onError(BaseResponse event, String postType);


}