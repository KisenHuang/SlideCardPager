package com.kisen.slidecard.transforms;

import android.animation.TimeInterpolator;
import android.support.annotation.Size;
import android.view.View;

import com.kisen.slidecard.SlideCardPager;


/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/22 下午8:52
 */
public class FanPageTransforms implements SlideCardPager.CardTransforms, TransformsHelper.TransformCall {

    private static final int ANGLE = 15;
    private static final float ALPHA = 0.7f;
    private static final float SCALE = 0.9f;

    @Override
    public void transforms(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        TransformsHelper.transform(holder, currentState, oldState, percent, this);
    }

    @Override
    public void select(SlideCardPager.CardHolder holder, int oldState, float percent) {
        View view = holder.getContentView();
        view.setRotation(0);
        view.setAlpha(1);

        float scale = (SCALE + (1 - SCALE) * percent);
        view.setScaleX(scale);
        view.setScaleY(scale);

        int targetX = holder.getViewWidth() / 2;

        int sign = oldState == SlideCardPager.CardState.STATE_UNSELECTED_PRE ? 1 : -1;
        view.setTranslationX(-targetX * (1 - percent) * sign);
    }

    @Override
    public void unSelect(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        View view = holder.getContentView();
        int sign = currentState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT ? 1 : -1;
        if (oldState == SlideCardPager.CardState.STATE_SELECTED) {
            view.setRotation(ANGLE * percent * sign);
            view.setAlpha(1 - (1 - ALPHA) * percent);

            float scale = 1 - (1 - SCALE) * percent;
            view.setScaleX(scale);
            view.setScaleY(scale);

            int targetX = holder.getViewWidth() / 2;

            view.setTranslationX(-targetX * (1 - percent) * sign);
        } else {
            view.setRotation(ANGLE * sign);
            view.setAlpha(1 - (1 - ALPHA) * percent);
            view.setScaleX(SCALE);
            view.setScaleY(SCALE);
            view.setTranslationX(0);
        }
    }

    @Override
    public void hide(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        View view = holder.getContentView();
        int sign = currentState == SlideCardPager.CardState.STATE_HIDE_LEFT ? -1 : 1;
        view.setRotation(ANGLE * sign);
        if (oldState >= 3) {
            view.setAlpha(ALPHA * (1 - percent));
        } else {
            view.setAlpha(0);
        }
        view.setScaleX(SCALE);
        view.setScaleY(SCALE);
        view.setTranslationX(0);
    }

    @Override
    public TimeInterpolator getInterpolator(int currentState, int oldState) {
        return null;
    }

    @Size(2)
    @Override
    public float[] getPivotPointOnMeasureFinish(View view) {
        return new float[]{view.getMeasuredWidth() / 2, view.getMeasuredHeight()};
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return TransformsHelper.DURATION;
    }

    @Override
    public int calculateGroupHeight(int groupHeight, SlideCardPager touchCardView) {
        int appendHeight = 0;
        for (int i = 0; i < touchCardView.getChildCount(); i++) {
            SlideCardPager.CardHolder cardHolder = touchCardView.getCardHolderByGroupPos(i);
            int state = cardHolder.getViewCardState().getState();
            if (state == SlideCardPager.CardState.STATE_UNSELECTED_PRE
                    || state == SlideCardPager.CardState.STATE_UNSELECTED_NEXT) {
                appendHeight = (int) (Math.sin(ANGLE / 180f * Math.PI) *
                        cardHolder.getContentView().getMeasuredWidth() / 2);
                break;
            }
        }
        return groupHeight + appendHeight;
    }

    @Override
    public int calculateGroupWidth(int groupWidth, SlideCardPager touchCardView) {
        int appendWidth = 0;
        for (int i = 0; i < touchCardView.getChildCount(); i++) {
            SlideCardPager.CardHolder cardHolder = touchCardView.getCardHolderByGroupPos(i);
            int viewCardState = cardHolder.getViewCardState().getState();
            if (viewCardState == SlideCardPager.CardState.STATE_UNSELECTED_PRE
                    || viewCardState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT) {
                appendWidth = (int) (Math.sin(ANGLE / 180f * Math.PI) *
                        cardHolder.getContentView().getMeasuredHeight());
                break;
            }
        }
        return groupWidth + appendWidth * 2;
    }

    @Override
    public int[] calculateLayout(View child, int state, int[] size) {
        return size;
    }
}
