package com.tal.kisen.slidecardpager.transforms;

import android.view.View;

import com.tal.kisen.slidecardpager.SlideCardPager;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午7:55
 */

public class TransformsHelper {

    public static final int DURATION = 400;

    public static void transform(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent, TransformCall call) {
        if (call == null)
            return;
        switch (currentState) {
            case SlideCardPager.CardState.STATE_SELECTED:
                call.select(holder, oldState, percent);
                break;
            case SlideCardPager.CardState.STATE_UNSELECTED_PRE:
            case SlideCardPager.CardState.STATE_UNSELECTED_NEXT:
                call.unSelect(holder, currentState, oldState, percent);
                break;
            case SlideCardPager.CardState.STATE_HIDE_LEFT:
            case SlideCardPager.CardState.STATE_HIDE_RIGHT:
                call.hide(holder, currentState, oldState, percent);
                break;
        }
    }

    public static int[] getViewMeasureSize(View view){
        return new int[]{view.getMeasuredWidth(), view.getMeasuredHeight()};
    }

    public static int calculateDefGroupSize(int size){
        return size;
    }

    public interface TransformCall {
        void select(SlideCardPager.CardHolder holder, int oldState, float percent);

        void unSelect(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent);

        void hide(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent);
    }
}
