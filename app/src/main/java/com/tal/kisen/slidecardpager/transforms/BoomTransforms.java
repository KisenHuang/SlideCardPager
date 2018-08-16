package com.tal.kisen.slidecardpager.transforms;

import android.animation.TimeInterpolator;
import android.view.View;

import com.tal.kisen.slidecardpager.SlideCardPager;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午7:53
 */

public class BoomTransforms implements SlideCardPager.CardTransforms {

    @Override
    public void transforms(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {

    }

    @Override
    public TimeInterpolator getInterpolator(int currentState, int oldState) {
        return null;
    }

    @Override
    public float[] getPivotPointOnMeasureFinish(View view) {
        return new float[2];
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return 0;
    }

    @Override
    public int makeGroupHeight(int groupHeight, SlideCardPager slideCardPager) {
        return 0;
    }

    @Override
    public int makeGroupWidth(int groupWidth, SlideCardPager slideCardPager) {
        return 0;
    }

    @Override
    public int[] calculateLayout(View child, int state, int[] size) {
        return new int[4];
    }
}
