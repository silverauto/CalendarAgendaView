package com.mstest.eventscalendar.agenda;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Date;

public class AgendaViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private Date mDate;

    public AgendaViewHolder(View itemView) {
        super(itemView);
    }

    public void setDate(Context context, Date date) {
        mContext = context;
        mDate = date;
    }

    public Date getDate() {
        return mDate;
    }

    Context getContext() {
        return mContext;
    }
}
