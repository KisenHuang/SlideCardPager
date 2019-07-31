package com.kisen.slidecard.transforms;

import android.animation.TimeInterpolator;
import android.view.View;

import com.kisen.slidecard.SlideCardPager;

/**
 * description:
 * author: Kisenhuang
 * email: Kisenhuang@163.com
 * time: 2019/7/31 下午11:14
 */
public class SectorCardTransforms implements SlideCardPager.CardTransforms {

    private static final int ANGLE = 15;
    private static final float ALPHA = 0.7f;
    private static final float SCALE = 0.9f;

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
        view.setRotation(0);
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

    private void hide(SlideCardPager.CardHolder holder, int currentState, int oldState, float percent) {
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

    @Override
    public float[] getPivotPointOnMeasureFinish(View view) {
        return new float[]{view.getMeasuredWidth() / 2, view.getMeasuredHeight()};
    }

    @Override
    public long getDuration(int currentState, int oldState) {
        return 400;
    }

    @Override
    public int makeGroupHeight(int groupHeight, SlideCardPager slideCardPager) {
        int appendHeight = 0;
        for (int i = 0; i < slideCardPager.getChildCount(); i++) {
            SlideCardPager.CardHolder cardHolder = slideCardPager.getCardHolderByGroupPos(i);
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
    public int makeGroupWidth(int groupWidth, SlideCardPager slideCardPager) {
        int appendWidth = 0;
        for (int i = 0; i < slideCardPager.getChildCount(); i++) {
            SlideCardPager.CardHolder cardHolder = slideCardPager.getCardHolderByGroupPos(i);
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
