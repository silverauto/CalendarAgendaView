package com.mstest.eventscalendar.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mstest.eventscalendar.R;

/**
 * Created by silverauto on 2017/5/13.
 */

public class DividerItemDecorationItem extends RecyclerView.ItemDecoration{
    private final Paint mDividerPaint;
    private final int mDividerHeight;

    public DividerItemDecorationItem(Context context) {
        int dividerHeight = context.getResources().getDimensionPixelOffset(R.dimen.agenda_divider_height);
        int dividerColor = ContextCompat.getColor(context, R.color.divider_color);
        mDividerHeight = dividerHeight;
        mDividerPaint = new Paint();
        mDividerPaint.setColor(dividerColor);
        mDividerPaint.setStrokeWidth(mDividerHeight);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int dividerTop;
        for (int i = 0; i < parent.getChildCount(); i++) {
            dividerTop = parent.getChildAt(i).getTop() - mDividerHeight / 2;
            c.drawLine(0, dividerTop, parent.getWidth(), dividerTop, mDividerPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) > 0) {
            outRect.top = mDividerHeight;
        }
    }
}
