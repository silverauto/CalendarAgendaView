package com.mstest.eventscalendar.ui;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class RecyclerViewGestureListener implements RecyclerView.OnItemTouchListener {
    private final RecyclerView mRecyclerView;
    private GestureDetectorCompat mGestureDetectorCompat;
    private GestureListener mGestureListener;

    public RecyclerViewGestureListener(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mGestureListener = new GestureListener();
        mGestureDetectorCompat = new GestureDetectorCompat(mRecyclerView.getContext(),
                mGestureListener);
        mGestureDetectorCompat.setOnDoubleTapListener(null);
        mGestureDetectorCompat.setIsLongpressEnabled(false);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public abstract void onItemClick(RecyclerView.ViewHolder viewHolder);

    public abstract void onScrolled();

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private View mPressedView;
        @Override
        public void onShowPress(MotionEvent e) {
            pressItemView(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (itemView != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(itemView);
                onItemClick(viewHolder);
            }
            if(mPressedView == null) {
                // press down and up too quickly (onShowPress not called)
                pressItemView(e);
                unpressItemView();
            } else {
                unpressItemView();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean handled = super.onScroll(e1, e2, distanceX, distanceY);
            unpressItemView();
            onScrolled();
            return handled;
        }

        private void pressItemView(MotionEvent e) {
            unpressItemView();
            View itemView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if(itemView != null) {
                itemView.setPressed(true);
                mPressedView = itemView;
            }
        }

        void unpressItemView() {
            if(mPressedView != null) {
                mPressedView.setPressed(false);
            }
            mPressedView = null;
        }
    }
}
