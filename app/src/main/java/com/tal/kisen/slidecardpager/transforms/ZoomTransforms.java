package com.tal.kisen.slidecardpager.transforms;

import android.animation.TimeInterpolator;
import android.support.annotation.Size;
import android.view.View;

import com.tal.kisen.slidecardpager.SlideCardPager;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午5:51
 */

public class ZoomTransforms implements SlideCardPager.CardTransforms {

    private static final float SCALE = 0.8f;

    @Override
    public void transforms(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        switch (currentState) {
            case SlideCardPager.CardState.STATE_SELECTED:
                select(holder, oldState, percent);
                break;
            case SlideCardPager.CardState.STATE_UNSELECTED_PRE:
            case SlideCardPager.CardState.STATE_UNSELECTED_NEXT:
                unSelect(holder, currentState, oldState, percent);
                break;
            case SlideCardPager.CardState.STATE_HIDE_LEFT:
            case SlideCardPager.CardState.STATE_HIDE_RIGHT:
                hide(holder, currentState, oldState, percent);
                break;
        }
    }

    private void select(SlideCardPager.CardHolder holder, float oldState, float percent) {
        View view = holder.getContentView();
        view.setAlpha(1);

        float scale = (SCALE + (1 - SCALE) * percent);
        view.setScaleX(scale);
        view.setScaleY(scale);

        int targetX = holder.getViewWidth() / 2;

        int sign = oldState == SlideCardPager.CardState.STATE_UNSELECTED_PRE ? 1 : -1;
        view.setTranslationX(-targetX * (1 - percent) * sign);
    }

    private void unSelect(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
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

    private void hide(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
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
        return new float[]{view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2};
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return 400;
    }

    @Override
    public int makeGroupHeight(int groupHeight, SlideCardPager slideCardPager) {
        return groupHeight;
    }

    @Override
    public int makeGroupWidth(int groupWidth, SlideCardPager slideCardPager) {
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
