package com.kisen.slidecard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/6 下午2:48
 */
public class SlideCardPager extends ViewGroup {

    /**
     * 滑动判定距离
     */
    private static final float MOVE_SIZE = 100;
    /**
     * 当前显示卡片位置
     */
    private int mCurrentPos = -1;
    /**
     * 卡片适配器
     */
    private CardAdapter mCardAdapter;
    /**
     * UI刷新观察者
     */
    private CardDataObserver mDefaultObserver = new CardDataObserver();
    /**
     * 卡片回收池
     */
    private CardRecyclePool mCardRecyclePool = new CardRecyclePool();
    /**
     * 用户手势按下位置
     */
    private float mDownX;
    /**
     * 用户判断当前是否处于滑动中，切换操作在一次滑动中只执行一次
     */
    private boolean canMoveToScroll = true;
    /**
     * 卡片切换效果实现类
     */
    private CardTransforms mCardTransforms;
    /**
     * 卡片切换监听
     */
    private List<OnCardChangeListener> mCardChangeListeners;
    /**
     * 卡片选中状态拦截器
     */
    private ItemSelectedInterceptor mItemSelectInterceptor;
    /**
     * 卡片位置，left top right bottom
     */
    private int[] mChildSize = new int[4];

    public SlideCardPager(Context context) {
        this(context, null);
    }

    public SlideCardPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideCardPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //重置滑动可切换状态
                canMoveToScroll = true;
                //记录点击位置
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                //判断本次滑动是否满足切换
                if (Math.abs(mDownX - x) > MOVE_SIZE) {
                    intercept = true;
                } else
                    intercept = false;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchEvent = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchEvent = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                boolean animRunning = isAnimRunning();
                //触发滑动效果
                //防止点击子控件外的区域导致没有调用onInterceptTouchEvent中ACTION_MOVE的处理，引发滑动错乱
                if (Math.abs(mDownX - x) > MOVE_SIZE && canMoveToScroll && !animRunning) {
                    canMoveToScroll = false;
                    if (mDownX > x) {
                        next();
                    } else {
                        pre();
                    }
                    touchEvent = true;
                } else
                    touchEvent = false;
                break;
        }
        return touchEvent;
    }

    /**
     * 添加卡片状态变化监听，卡片切换时触发
     *
     * @param listener 监听
     */
    public void addCardChangeListener(OnCardChangeListener listener) {
        if (mCardChangeListeners == null)
            mCardChangeListeners = new ArrayList<>();
        mCardChangeListeners.add(listener);
    }

    /**
     * 设置Transforms，卡片切换效果
     *
     * @param transforms 卡片切换效果类
     */
    public void setTransforms(CardTransforms transforms) {
        if (transforms != null && mCardTransforms != transforms) {
            mCardTransforms = transforms;
            removeAllViews();
            populate(0);
        }
    }

    /**
     * 设置切换拦截器，判断当前卡片是否可滑动切换
     *
     * @param interceptor 滑动拦截器
     */
    public void setOnSelectInterceptor(ItemSelectedInterceptor interceptor) {
        mItemSelectInterceptor = interceptor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int groupWidth = 0;
        int groupHeight = 0;
        //根据填充状态判断是否需要动态计算宽度
        boolean needComputeWidth = false;
        boolean needComputeHeight = false;
        if (widthMode == MeasureSpec.EXACTLY)
            groupWidth = MeasureSpec.getSize(widthMeasureSpec);
        else {
            needComputeWidth = true;
        }

        if (heightMode == MeasureSpec.EXACTLY)
            groupHeight = MeasureSpec.getSize(heightMeasureSpec);
        else {
            needComputeHeight = true;
        }

        for (int i = 0; i < getChildCount(); i++) {
            CardHolder holder = getCardHolderByGroupPos(i);
            View childAt = holder.getContentView();
            measureChild(childAt, widthMeasureSpec, heightMeasureSpec);
            holder.measureFinish();
            if (needComputeHeight) {
                if (holder.getViewCardState().state == CardState.STATE_SELECTED) {
                    MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                    groupHeight = childAt.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin
                            + getPaddingTop() + getPaddingBottom();
                }
            }
            if (needComputeWidth) {
                if (holder.getViewCardState().state == CardState.STATE_UNSELECTED_PRE
                        || holder.getViewCardState().state == CardState.STATE_UNSELECTED_NEXT) {
                    MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                    groupWidth = childAt.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
                    groupWidth = 2 * groupWidth;
                }
            }
        }
        if (needComputeWidth)
            groupWidth += getPaddingLeft() + getPaddingRight();
        if (mCardTransforms != null) {
            if (needComputeHeight)
                groupHeight = mCardTransforms.calculateGroupHeight(groupHeight, this);
            if (needComputeWidth)
                groupWidth = mCardTransforms.calculateGroupWidth(groupWidth, this);
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(groupWidth, widthMode),
                MeasureSpec.makeMeasureSpec(groupHeight, heightMode));
    }

    /**
     * 获取当前group中的对应位置的CardHolder
     *
     * @param pos 当前显示的对应位置
     * @return CardHolder
     */
    public CardHolder getCardHolderByGroupPos(int pos) {
        return mCardRecyclePool.getHolderByView(getChildAt(pos));
    }

    /**
     * 获取适配器中对应的CardHolder
     *
     * @param pos 对应位置
     * @return 对应的CardHolder
     */
    public CardHolder getCardHolderByAdapterPos(int pos) {
        return mCardRecyclePool.getHolderByPos(pos);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < getChildCount(); i++) {
            CardHolder holder = getCardHolderByGroupPos(i);
            View childAt = holder.getContentView();
            int state = holder.getViewCardState().state;
            int childWidth = childAt.getMeasuredWidth();
            int childHeight = childAt.getMeasuredHeight();
            switch (state) {
                case CardState.STATE_SELECTED:
                    mChildSize[0] = width / 2 - childWidth / 2;
                    mChildSize[1] = 0;
                    mChildSize[2] = width / 2 + childWidth / 2;
                    mChildSize[3] = childHeight;
                    break;
                case CardState.STATE_UNSELECTED_PRE:
                case CardState.STATE_HIDE_LEFT:
                    mChildSize[0] = width / 2 - childWidth;
                    mChildSize[1] = 0;
                    mChildSize[2] = width / 2;
                    mChildSize[3] = childHeight;
                    break;
                case CardState.STATE_UNSELECTED_NEXT:
                case CardState.STATE_HIDE_RIGHT:
                    mChildSize[0] = width / 2;
                    mChildSize[1] = 0;
                    mChildSize[2] = width / 2 + childWidth;
                    mChildSize[3] = childHeight;
                    break;
            }
            if (mCardTransforms != null)
                mChildSize = mCardTransforms.calculateLayout(childAt, state, mChildSize);
            childAt.layout(mChildSize[0], mChildSize[1], mChildSize[2], mChildSize[3]);
        }
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 后一张，向后滑动
     */
    public void next() {
        populate(mCurrentPos + 1);
    }

    /**
     * 前一张，向前滑动
     */
    public void pre() {
        populate(mCurrentPos - 1);
    }

    /**
     * 动画是否正在执行
     */
    public boolean isAnimRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            CardHolder holder = getCardHolderByGroupPos(i);
            if (holder.isRunningAnim())
                return true;
        }
        return false;
    }

    /**
     * 填充View,改变view状态
     * 核心方法
     *
     * @param pos 要切换到的位置
     */
    void populate(int pos) {
        //拦截器处理
        if (interceptorSelect(pos))
            return;

        if (isAnimRunning())
            return;

        if (mCardAdapter == null)
            return;

        //判断位置合法性
        if (pos < 0 || pos > mCardAdapter.getItemCount() - 1)
            return;

        int adapterItemCount = mCardAdapter.getItemCount();
        int childCount = getChildCount();

        //用于记录滑动方向和填充方式
        int touch = 0;

        //如果填充的位置与当前位置的差值超过1，清空所有重新填充
        if (Math.abs(mCurrentPos - pos) <= 1) {
            if (mCurrentPos > pos) {
                touch = -1;
            } else {
                touch = 1;
            }
        }

        CardHolder selected = null;
        CardHolder needRemoved = null;

        /*改变当前试图的状态*/
        //执行对应动画
        for (int i = 0; i < childCount; i++) {
            CardHolder holder = getCardHolderByGroupPos(i);
            CardState state = holder.getViewCardState();
            switch (touch) {
                case 1:
                case -1:
                    int dPos = pos - state.getGroupPosition();
                    switch (dPos * touch) {
                        case 3:
                        case -3:
                            //超出缓存位置，回收Holder
                            needRemoved = holder;
                            holder.recycle();
                            break;
                        case 2:
                        case -2:
                            //缓存位置的数据，启动隐藏动画
                            holder.hide(true, dPos);
                            if (mCardChangeListeners != null) {
                                for (OnCardChangeListener listener : mCardChangeListeners) {
                                    listener.onCardHide(holder, state.state, state.getGroupPosition());
                                }
                            }
                            break;
                        case 1:
                        case -1:
                            //显示在两侧位置的数据，启动切换动画
                            holder.unSelected(true, dPos);
                            if (mCardChangeListeners != null) {
                                for (OnCardChangeListener listener : mCardChangeListeners) {
                                    listener.onCardUnSelected(holder, state.state, state.getGroupPosition());
                                }
                            }
                            break;
                        case 0:
                            //中间选中状态，启动选中动画
                            holder.selected(true);
                            selected = holder;
                            if (mCardChangeListeners != null) {
                                for (OnCardChangeListener listener : mCardChangeListeners) {
                                    listener.onCardSelected(holder, state.state, state.getGroupPosition());
                                }
                            }
                            break;
                    }
                    break;
                default:
                    //回收无效Holder
                    holder.recycle();
                    break;
            }
        }

        //标记字段是0 ，表示不是左右切换，需要清空所有View并重新添加
        if (touch == 0)
            removeAllViews();

        //移除无用试图
        if (needRemoved != null)
            removeView(needRemoved.getContentView());

        /* 计算当前状态需要处理的数据量 */
        int needSize;//当前位置上需要显示的View数量
        int startPos;//第一个view（包括缓存view）在Adapter中的位置
        if (pos == 0) {
            needSize = 3;
            startPos = 0;
        } else if (pos == adapterItemCount - 1) {
            needSize = 3;
            startPos = adapterItemCount - 1 - 2;
        } else if (pos == 1) {
            needSize = 4;
            startPos = 0;
        } else if (pos == adapterItemCount - 2) {
            needSize = 4;
            startPos = adapterItemCount - 2 - 2;
        } else {
            needSize = 5;
            startPos = pos - 2;
        }

        /* 填充试图 */
        //根据计算得到的数据，初始化holder
        childCount = getChildCount();
        if (childCount == 0) {//初始化操作，相当于touch等于0
            int sp = startPos;
            for (int i = 0; i < needSize; i++) {
                //从recyclePool中获取Holder
                CardHolder cardHolder = generateCardHolder(sp);

                //绑定数据
                mCardAdapter.onBindCardViewHolder(cardHolder, sp);

                addNewItem(cardHolder.getContentView());

                //设置CardView状态
                //该处不执行动画
                if (sp == pos) {
                    cardHolder.selected(false);
                    selected = cardHolder;
                    if (mCardChangeListeners != null) {
                        for (OnCardChangeListener listener : mCardChangeListeners) {
                            listener.onCardSelected(cardHolder, cardHolder.getViewCardState().state, sp);
                        }
                    }
                } else if (Math.abs(pos - sp) == 1) {
                    cardHolder.unSelected(false, pos - sp);
                    if (mCardChangeListeners != null) {
                        for (OnCardChangeListener listener : mCardChangeListeners) {
                            listener.onCardUnSelected(cardHolder, cardHolder.getViewCardState().state, sp);
                        }
                    }
                } else {
                    cardHolder.hide(false, pos - sp);
                    if (mCardChangeListeners != null) {
                        for (OnCardChangeListener listener : mCardChangeListeners) {
                            listener.onCardHide(cardHolder, cardHolder.getViewCardState().state, sp);
                        }
                    }
                }
                cardHolder.getViewCardState().position = sp;
                sp++;
            }
        } else {
            if (touch == 1) {//初始化右侧缓存位置的Holder
                if (adapterItemCount - 1 - pos > 1) {
                    CardHolder cardHolder = generateCardHolder(pos + 2);
                    mCardAdapter.onBindCardViewHolder(cardHolder, pos + 2);
                    addNewItem(cardHolder.getContentView());
                    cardHolder.hide(true, -1);
                    cardHolder.getViewCardState().position = pos + 2;
                    if (mCardChangeListeners != null) {
                        for (OnCardChangeListener listener : mCardChangeListeners) {
                            listener.onCardHide(cardHolder, cardHolder.getViewCardState().state,
                                    cardHolder.getViewCardState().getGroupPosition());
                        }
                    }
                }
            } else {
                if (pos > 1) {//初始化左侧缓存位置的Holder
                    CardHolder cardHolder = generateCardHolder(pos - 2);
                    mCardAdapter.onBindCardViewHolder(cardHolder, pos - 2);
                    addNewItem(cardHolder.getContentView());
                    cardHolder.hide(true, 1);
                    cardHolder.getViewCardState().position = pos - 2;
                    if (mCardChangeListeners != null) {
                        for (OnCardChangeListener listener : mCardChangeListeners) {
                            listener.onCardHide(cardHolder, cardHolder.getViewCardState().state,
                                    cardHolder.getViewCardState().getGroupPosition());
                        }
                    }
                }
            }
        }

        //将选中状态的View移动到View最上层
        if (selected != null)
            bringChildToFront(selected.getContentView());

        mCurrentPos = pos;
    }

    /**
     * 处理拦截器
     *
     * @param currentPos 选中位置
     * @return 是否拦截
     */
    private boolean interceptorSelect(int currentPos) {
        if (mItemSelectInterceptor != null) {
            if (currentPos > -1 && currentPos < mCardAdapter.getItemCount()) {
                CardHolder holder = getCardHolderByAdapterPos(currentPos);
                if (holder != null) {
                    CardState viewCardState = holder.getViewCardState();
                    return mItemSelectInterceptor.interceptor(holder, viewCardState.state,
                            viewCardState.getGroupPosition());
                }
            }
        }
        return false;
    }

    /**
     * 生成一个CardHolder
     * 如果Pool中有闲置CardHolder，则从RecyclePool中获取空闲状态的CardHolder，否则创建一个CardHolder
     *
     * @param pos 获取pool中闲置的CardHolder
     * @return CardHolder
     */
    private CardHolder generateCardHolder(int pos) {
        CardHolder cardHolder;
        CardHolder poolView = mCardRecyclePool.getIdleViewHolder();
        if (poolView != null) {
            cardHolder = poolView;
        } else {
            cardHolder = mCardAdapter.createCardViewHolder(this, pos);
            mCardRecyclePool.addRecycleViewHolder(cardHolder);
        }
        cardHolder.setTransforms(mCardTransforms);
        return cardHolder;
    }

    /**
     * 将CardHolder对应的view填充到Group中
     *
     * @param view CardHolder对应的试图
     */
    private void addNewItem(View view) {
        addView(view, new MarginLayoutParams(view.getLayoutParams()));
    }

    /**
     * 设置Adapter，适配数据
     *
     * @param adapter 适配器
     */
    public void setAdapter(CardAdapter adapter) {
        if (adapter == null)
            return;
        if (mCardAdapter == adapter)
            return;
        CardAdapter oldAdapter = mCardAdapter;
        if (oldAdapter != null) {
            oldAdapter.unregisterObserver(mDefaultObserver);
        }

        adapter.registerObserver(mDefaultObserver);
        mCardAdapter = adapter;

        mCurrentPos = 0;
        mCardAdapter.notifySetDataChanged();
    }

    /**
     * 刷新布局
     */
    private void requestCardView() {
        populate(mCurrentPos);
    }

    /**
     * 刷新指定位置卡片
     *
     * @param position 指定位置
     */
    private void requestItemCard(int position) {
        CardHolder cardHolder = getCardHolderByAdapterPos(position);
        mCardAdapter.onBindCardViewHolder(cardHolder, position);
    }

    /**
     * 切换到指定位置
     *
     * @param pos 显示位置
     */
    public void setCurrent(int pos) {
        populate(pos);
    }

    /**
     * 获取当前CardHolder
     *
     * @return 当前CardHolder
     */
    public CardHolder getCurrentCardHolder() {
        return getCardHolderByAdapterPos(mCurrentPos);
    }

    /**
     * Holder的管理池，用于Holder的回收和复用
     * 其实主要回收的是View，类似于RecyclerView的回收机制，此处简单实现
     */
    private class CardRecyclePool {

        private int poolSize = 5;
        private SparseArray<CardHolder> recyclerPool = new SparseArray<>();

        /**
         * 判断回收池是否已满
         *
         * @return 是否已满
         */
        private boolean isPoolFull() {
            return recyclerPool.size() >= poolSize;
        }

        /**
         * 通过view获取对应CardHolder
         *
         * @param view 需要查找的view
         * @return 查找结果
         */
        private CardHolder getHolderByView(View view) {
            if (view == null)
                return null;
            return recyclerPool.get(view.hashCode());
        }

        /**
         * 通过pos获取对应CardHolder
         *
         * @param pos 在adapter中的位置
         * @return 查找结果
         */
        private CardHolder getHolderByPos(int pos) {
            for (int i = 0; i < recyclerPool.size(); i++) {
                CardHolder holder = recyclerPool.valueAt(i);
                if (holder.getViewCardState().getGroupPosition() == pos) {
                    return holder;
                }
            }
            return null;
        }

        /**
         * 获取闲置的CardHolder
         *
         * @return CardHolder
         */
        @Nullable
        private CardHolder getIdleViewHolder() {
            if (!isPoolFull())
                return null;
            for (int i = 0; i < recyclerPool.size(); i++) {
                CardHolder holder = recyclerPool.valueAt(i);
                if (holder.getViewCardState().isRecycled()) {
                    return holder;
                }
            }
            return null;
        }

        /**
         * 将CardHolder填充到回收池中
         *
         * @param holder 需要回收监控的CardHolder
         */
        private void addRecycleViewHolder(CardHolder holder) {
            View contentView = holder.getContentView();
            if (recyclerPool.indexOfValue(holder) == -1)
                recyclerPool.append(contentView.hashCode(), holder);
        }
    }

    /**
     * 卡片切换效果的核心
     * 用于切换效果的实现，包括onMeasure，onLayout更正操作
     */
    public interface CardTransforms {

        /**
         * 动画执行时Update方法
         * <p>
         * 注意：
         * 执行动画时，所有View的状态已经更新
         * View状态以currentState为准
         * 此处在计算View位置的时候，View的默认位置是当前状态currentState的位置也就是切换之后的位置
         * </p>
         *
         * @param holder       CardHolder 可以从其中获取View信息
         * @param currentState 当前卡片的状态
         * @param oldState     切换之前卡片的状态
         * @param percent      当前动画执行百分比
         */
        void transforms(CardHolder holder, int currentState, int oldState, float percent);

        /**
         * 添加动画时间拦截器
         *
         * @param currentState 当前卡片状态
         * @param oldState     切换之前卡片的状态
         * @return 时间拦截器
         */
        TimeInterpolator getInterpolator(int currentState, int oldState);

        /**
         * 获取View动画处理的中心点
         * 该方法在ViewOnMeasure后调用
         *
         * @param view 动画处理的view
         * @return view绘制结束后动画操作的中心点
         */
        @Size(2)
        float[] getPivotPointOnMeasureFinish(View view);

        /**
         * 获取动画执行时间
         *
         * @param currentState 当前卡片状态
         * @param oldState     卡片切换之前的状态
         */
        long getDuration(int currentState, int oldState);

        /**
         * 根据子布局计算父布局高度
         * 如果父布局设置的是固定高度或match_parent则不会调用该方法
         *
         * @param groupHeight    默认父布局高度
         * @param slideCardPager 父布局
         * @return 计算结果
         */
        int calculateGroupHeight(int groupHeight, SlideCardPager slideCardPager);

        /**
         * 根据子布局计算父布局宽度
         * 如果父布局设置的是固定宽度或match_parent则不会调用该方法
         *
         * @param groupWidth     默认父布局宽度
         * @param slideCardPager 父布局
         * @return 计算结果
         */
        int calculateGroupWidth(int groupWidth, SlideCardPager slideCardPager);

        /**
         * 该方法在父布局的onLayout时调用，需要子View针对自己的情况自己调整size
         *
         * @param child 子View
         * @param state View的状态
         * @param size  view的左上右下
         * @return 修正后的size
         */
        @Size(4)
        int[] calculateLayout(View child, int state, @Size(4) int[] size);
    }

    /**
     * CardPager的又一核心
     * 用于提供数据，绑定数据和更新
     */
    public abstract static class CardAdapter<D> {

        private List<AdapterObserver> mObserver = new ArrayList<>();
        private int mLayoutResId;
        protected List<D> mDatas;
        protected Context mContext;
        protected OnItemClickListener mItemClickListener;

        /**
         * 构造方法
         *
         * @param layoutResId 卡片布局文件ID
         * @param data        数据源
         */
        public CardAdapter(int layoutResId, List<D> data) {
            mLayoutResId = layoutResId;
            mDatas = data == null ? new ArrayList<D>() : data;
        }

        /**
         * 注册数据观察者
         *
         * @param observer 观察者
         */
        void registerObserver(AdapterObserver observer) {
            if (!mObserver.contains(observer))
                mObserver.add(observer);
        }

        /**
         * 注销数据观察者
         *
         * @param observer 观察者
         */
        void unregisterObserver(AdapterObserver observer) {
            mObserver.remove(observer);
        }

        /**
         * 设置数据源,替换之前数据
         *
         * @param list 数据源
         */
        public void setList(List<D> list) {
            if (list != null)
                mDatas = new ArrayList<>(list);
            notifySetDataChanged();
        }

        /**
         * 获取对应位置数据
         *
         * @param position 要获取数据的位置
         * @return 获取结果，如果没有对应位置数据则返回null
         */
        @Nullable
        public D getItemData(int position) {
            if (position < mDatas.size() && position >= 0)
                return mDatas.get(position);
            return null;
        }

        /**
         * 创建CardHolder
         * 子类可自定义处理
         *
         * @param group    父布局
         * @param position 对应位置
         * @return 创建的CardHolder
         */
        @NonNull
        protected CardHolder createCardViewHolder(ViewGroup group, int position) {
            mContext = group.getContext();
            return createDefCardViewHolder(group, position);
        }

        /**
         * 创建默认CardHolder，不可重写
         *
         * @param group    父布局
         * @param position 对应位置
         * @return 创建的CardHolder
         */
        protected final CardHolder createDefCardViewHolder(ViewGroup group, int position) {
            return new CardViewHolder(group, mLayoutResId);
        }

        /**
         * CardHolder绑定数据
         * 默认处理点击事件
         *
         * @param cardViewHolder 需要绑定数据的CardHolder
         * @param position       CardHolder对应的位置，在adapter中的位置
         */
        protected void onBindCardViewHolder(final CardHolder cardViewHolder, final int position) {
            cardViewHolder.getContentView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.onItemClick(CardAdapter.this, v,
                                cardViewHolder.getViewCardState().state, position);
                }
            });
            if (position < mDatas.size())
                convert(cardViewHolder, mDatas.get(position));
        }

        /**
         * 适配数据
         *
         * @param cardViewHolder CardHolder
         * @param data           数据源
         */
        protected abstract void convert(CardHolder cardViewHolder, D data);

        /**
         * 获取数据数量
         *
         * @return 数据数量
         */
        public int getItemCount() {
            return mDatas.size();
        }

        /**
         * 刷新UI，全局刷新
         */
        public final void notifySetDataChanged() {
            for (AdapterObserver observer : mObserver) {
                observer.change();
            }
        }

        /**
         * 刷新指定位置数据
         *
         * @param position 要刷新的位置
         */
        public final void notifyItemChanged(int position) {
            for (AdapterObserver observer : mObserver) {
                observer.notifyItem(position);
            }
        }

        /**
         * 设置CardHolder点击事件
         *
         * @param listener 监听
         */
        public void setOnItemClickListener(OnItemClickListener listener) {
            mItemClickListener = listener;
        }
    }

    /**
     * 观察者模式处理处理UI更新
     */
    public interface AdapterObserver {

        void change();

        void notifyItem(int position);
    }

    /**
     * 默认实现处理，刷新UI
     */
    private class CardDataObserver implements AdapterObserver {

        @Override
        public void change() {
            requestCardView();
        }

        @Override
        public void notifyItem(int position) {
            requestItemCard(position);
        }
    }

    /**
     * 卡片状态管理类
     */
    public static class CardState {

        public static final int STATE_IDLE = -1;
        public static final int STATE_SELECTED = 0;
        public static final int STATE_HIDE_LEFT = 1;
        public static final int STATE_HIDE_RIGHT = 2;
        public static final int STATE_UNSELECTED_PRE = 3;
        public static final int STATE_UNSELECTED_NEXT = 4;

        private int position;
        private int state = STATE_IDLE;

        /**
         * 设置为选中状态
         */
        public void select() {
            state = STATE_SELECTED;
        }

        /**
         * 设置为非选中状态
         */
        public void unSelect(int pre) {
            if (pre > 0)
                state = STATE_UNSELECTED_PRE;
            else
                state = STATE_UNSELECTED_NEXT;
        }

        /**
         * 设置为隐藏状态
         */
        public void hide(int pre) {
            if (pre > 0)
                state = STATE_HIDE_LEFT;
            else
                state = STATE_HIDE_RIGHT;
        }

        /**
         * 获取当前状态
         */
        public int getState() {
            return state;
        }

        /**
         * 设置为闲置状态
         */
        void recycle() {
            state = STATE_IDLE;
        }

        /**
         * 是否可使用
         *
         * @return 是否可使用状态
         */
        boolean isRecycled() {
            return state == STATE_IDLE;
        }

        /**
         * 在父布局中的位置
         *
         * @return 父布局中的位置，默认为-1
         */
        int getGroupPosition() {
            return isRecycled() ? -1 : position;
        }
    }


    /**
     * Holder接口，抽象类
     * 主要功能：用于单个卡片的操作处理
     */
    public static abstract class CardHolder {

        protected CardState mCurrentState = new CardState();
        protected View mContentView;
        protected CardTransforms mCardTransforms;
        protected int viewWidth;

        public CardHolder(@NonNull View content) {
            mContentView = content;
        }

        public CardState getViewCardState() {
            return mCurrentState;
        }

        @NonNull
        public View getContentView() {
            return mContentView;
        }

        private void setTransforms(CardTransforms transforms) {
            mCardTransforms = transforms;
        }

        void measureFinish() {
            viewWidth = mContentView.getMeasuredWidth();
            if (mCardTransforms != null) {
                float[] pivotPoint = mCardTransforms.getPivotPointOnMeasureFinish(mContentView);
                setPivot(pivotPoint);
            }
        }

        private void setPivot(float[] pivot) {
            mContentView.setPivotX(pivot[0]);
            mContentView.setPivotY(pivot[1]);
        }

        abstract void selected(boolean anim);

        abstract void unSelected(boolean anim, int pre);

        abstract void hide(boolean anim, int pre);

        void recycle() {
            mCurrentState.recycle();
        }

        public int getAdapterPosition() {
            return mCurrentState.getGroupPosition();
        }

        public abstract boolean isRunningAnim();

        public abstract int getViewWidth();

        public <T extends View> T getView(int viewId) {
            return mContentView.findViewById(viewId);
        }
    }

    /**
     * 默认ViewHolder类
     * 集成Transforms
     */
    private static class CardViewHolder extends CardHolder {

        private ValueAnimator mValueAnimator;
        private Animator.AnimatorListener mAnimListener = new AnimatorListenerAdapter() {

//            private float[] pivotPoint;

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                resetPivot(pivotPoint);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                pivotPoint = checkPivot();
            }
        };

        CardViewHolder(ViewGroup parent, int viewId) {
            super(LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false));
            mValueAnimator = ObjectAnimator.ofFloat(0, 1);
            mContentView.setClickable(true);
        }

        @Override
        public int getViewWidth() {
            return viewWidth;
        }

        @Override
        public void selected(final boolean anim) {
            int oldState = mCurrentState.state;
            mCurrentState.select();
            int state = mCurrentState.state;
            startAnim(anim, state, oldState);
        }

        @Override
        public void unSelected(boolean anim, int pre) {
            int oldState = mCurrentState.state;
            mCurrentState.unSelect(pre);
            int state = mCurrentState.state;
            startAnim(anim, state, oldState);
        }

        @Override
        public void hide(boolean anim, int pre) {
            int oldState = mCurrentState.state;
            mCurrentState.hide(pre);
            int state = mCurrentState.state;
            startAnim(anim, state, oldState);
        }

        private void startAnim(boolean anim, final int state, final int oldState) {
            if (mCardTransforms == null)
                return;
            if (anim) {
                mValueAnimator.removeAllListeners();
                mValueAnimator.removeAllUpdateListeners();
                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float percent = (float) animation.getAnimatedValue();
                        mCardTransforms.transforms(CardViewHolder.this, state, oldState, percent);
                    }
                });
                mValueAnimator.setInterpolator(mCardTransforms.getInterpolator(state, oldState));
                mValueAnimator.setDuration(mCardTransforms.getDuration(state, oldState));
                mValueAnimator.addListener(mAnimListener);
                mValueAnimator.start();
            } else {
                mCardTransforms.transforms(CardViewHolder.this, state, oldState, 1);
            }
        }

        @Override
        public boolean isRunningAnim() {
            return mValueAnimator.isRunning();
        }

    }

    /**
     * 卡片状态变换（切换）监听
     */
    public interface OnCardChangeListener {

        void onCardSelected(CardHolder card, int state, int position);

        void onCardUnSelected(CardHolder card, int state, int position);

        void onCardHide(CardHolder card, int state, int position);
    }

    /**
     * 卡片点击监听
     */
    public interface OnItemClickListener {
        void onItemClick(CardAdapter adapter, View view, int state, int position);
    }

    /**
     * 卡片状态变换（切换）拦截器
     */
    public interface ItemSelectedInterceptor {
        boolean interceptor(CardHolder holder, int state, int position);
    }
}
