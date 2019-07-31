package com.kisen.slidecard.transforms;

import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * description:
 * author: KisenHuang
 * email: KisenHuang@163.com
 * time: 2018/8/16 下午7:55
 */

public class TransformsHelper {

    public static final int CENTER = 0x00000001;
    public static final int LEFT = 0x00010001;
    public static final int TOP = LEFT << 1;
    public static final int RIGHT = TOP << 1;
    public static final int BOTTOM = RIGHT << 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CENTER, LEFT, TOP, RIGHT, BOTTOM})
    public @interface PointGravity {
    }

    public float[] getPivotPoint(View view, int gravity){
        if ((gravity & CENTER) != 0){

        }
        switch (gravity) {
            case CENTER:break;
            case LEFT:break;
            case TOP:break;
            case RIGHT:break;
            case BOTTOM:break;
        }
        return new float[]{view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2};
    }
}
