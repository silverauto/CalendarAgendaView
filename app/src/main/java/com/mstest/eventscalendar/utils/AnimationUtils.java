package com.mstest.eventscalendar.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public final class AnimationUtils {
    public final static int DEFAULT_DURATION = 250;

    public static void expandOrCollapseView(final View targetView, final int startHeight, final int endHeight) {
        if(targetView == null || !targetView.isAttachedToWindow()) {
            return;
        }
        
        targetView.getLayoutParams().height = startHeight;
        targetView.setVisibility(View.VISIBLE);
        Animation animation = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int animatedHeight = interpolatedTime == 1 ? endHeight : startHeight + (int)((float)(endHeight - startHeight) * interpolatedTime);
                targetView.getLayoutParams().height = animatedHeight;
                targetView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(DEFAULT_DURATION);
        targetView.startAnimation(animation);
    }
}
