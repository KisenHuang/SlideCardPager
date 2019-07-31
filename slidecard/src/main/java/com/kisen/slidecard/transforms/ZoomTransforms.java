package com.kisen.slidecard.transforms;

import android.animation.TimeInterpolator;
import android.support.annotation.Size;
import android.view.View;

import com.kisen.slidecard.SlideCardPager;


/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午5:51
 */

public class ZoomTransforms implements SlideCardPager.CardTransforms, TransformsHelper.TransformCall {

    private static final float SCALE = 0.8f;

    @Override
    public void transforms(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        TransformsHelper.transform(holder, currentState, oldState, percent, this);
    }

    @Override
    public void select(SlideCardPager.CardHolder holder, int oldState, float percent) {
        View view = holder.getContentView();
        view.setAlpha(1);

        float scale = (SCALE + (1 - SCALE) * percent);
        view.setScaleX(scale);
        view.setScaleY(scale);

        int targetX = holder.getViewWidth() / 2;

        int sign = oldState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT ? 1 : -1;
        view.setTranslationX(targetX * (1 - percent) * sign);
    }

    @Override
    public void unSelect(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        View view = holder.getContentView();
        view.setAlpha(1);
        int sign = currentState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT ? 1 : -1;
        if (oldState == SlideCardPager.CardState.STATE_SELECTED) {

            float scale = 1 - (1 - SCALE) * percent;
            view.setScaleX(scale);
            view.setScaleY(scale);

            int targetX = holder.getViewWidth() / 2;

            view.setTranslationX(-targetX * (1 - percent) * sign);
        } else {
            view.setScaleX(SCALE);
            view.setScaleY(SCALE);
            view.setTranslationX(0);
        }
    }

    @Override
    public void hide(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        View view = holder.getContentView();
        if (oldState >= 3) {
            view.setAlpha(1);
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
        int[] viewSize = TransformsHelper.getViewMeasureSize(view);
        return new float[]{viewSize[0] / 2, viewSize[1] / 2};
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return TransformsHelper.DURATION;
    }

    @Override
    public int calculateGroupHeight(int groupHeight, SlideCardPager slideCardPager) {
        return TransformsHelper.calculateDefGroupSize(groupHeight);
    }

    @Override
    public int calculateGroupWidth(int groupWidth, SlideCardPager slideCardPager) {
        int appendWidth = 0;
        SlideCardPager.CardHolder currentCardHolder = slideCardPager.getCurrentCardHolder();
        if (currentCardHolder != null)
            appendWidth = (int) (currentCardHolder.getViewWidth() * SCALE);
        return groupWidth + appendWidth;
    }

    @Override
    public int[] calculateLayout(View child, int state, int[] size) {
        float width = child.getMeasuredWidth() * SCALE / 2;
        switch (state) {
            case SlideCardPager.CardState.STATE_SELECTED:
                //do nothing
                break;
            case SlideCardPager.CardState.STATE_UNSELECTED_PRE:
            case SlideCardPager.CardState.STATE_HIDE_LEFT:
                size[0] -= width;
                size[2] -= width;
                break;
            case SlideCardPager.CardState.STATE_UNSELECTED_NEXT:
            case SlideCardPager.CardState.STATE_HIDE_RIGHT:
                size[0] += width;
                size[2] += width;
                break;
        }
        return size;
    }
}
