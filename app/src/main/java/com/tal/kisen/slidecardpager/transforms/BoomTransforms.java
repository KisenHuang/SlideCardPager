package com.tal.kisen.slidecardpager.transforms;

import android.animation.TimeInterpolator;
import android.util.Log;
import android.view.View;

import com.tal.kisen.slidecardpager.SlideCardPager;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午7:53
 */

public class BoomTransforms implements SlideCardPager.CardTransforms, TransformsHelper.TransformCall {

    private static float SCALE = 0.8f;
    private static float ROTATION = 15;

    @Override
    public void transforms(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        TransformsHelper.transform(holder, currentState, oldState, percent, this);
    }

    @Override
    public TimeInterpolator getInterpolator(int currentState, int oldState) {
        return null;
    }

    @Override
    public float[] getPivotPointOnMeasureFinish(View view) {
        int[] viewSize = TransformsHelper.getViewMeasureSize(view);
        return new float[]{viewSize[0] / 2, viewSize[1] / 2};
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return  TransformsHelper.DURATION;
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
            appendWidth = currentCardHolder.getViewWidth();
        return groupWidth + appendWidth;
    }

    @Override
    public int[] calculateLayout(View child, int state, int[] size) {
        float width = child.getMeasuredWidth() / 2;
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

    @Override
    public void select(SlideCardPager.CardHolder holder, int oldState, float percent) {
        View view = holder.getContentView();

        int sign = oldState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT ? 1 : -1;
        float targetX = holder.getViewWidth() ;

        float point1 = 0.3f;
        if (percent < point1) {
            //设置缩放
            float per = percent / point1;
            float scale = per * (1 - SCALE) + SCALE;
            view.setScaleX(scale);
            view.setScaleY(scale);

            //设置翻转
//            float rotationY = ROTATION * sign * (1 - per);
//            view.setRotationY(rotationY);

            percent = 0;
        } else {
            view.setScaleX(1);
            view.setScaleY(1);
//            view.setRotationY(0.5f);

            percent = (percent - point1) / (1 - point1);
        }

        view.setTranslationX(targetX * (1 - percent) * sign);
    }

    @Override
    public void unSelect(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
        View view = holder.getContentView();
        int sign = currentState == SlideCardPager.CardState.STATE_UNSELECTED_NEXT ? 1 : -1;
        if (oldState == SlideCardPager.CardState.STATE_SELECTED) {
            float targetX = holder.getViewWidth() ;
            float point1 = 0.3f;
            float point2 = 0.8f;
            if (percent < point1) {
                percent = 0;
            } else {
                if (percent >= point2) {
                    float per = (percent - point2) / 0.2f;
                    float scale = 1 - (1 - SCALE) * per;
                    view.setScaleX(scale);
                    view.setScaleY(scale);
                }
                percent = (percent - point1) / (1 - point1);
            }

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
        view.setScaleX(SCALE);
        view.setScaleY(SCALE);
        view.setTranslationX(0);
    }
}
