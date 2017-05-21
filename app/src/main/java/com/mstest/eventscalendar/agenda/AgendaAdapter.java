package com.mstest.eventscalendar.agenda;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mstest.eventscalendar.R;
import com.mstest.eventscalendar.content.CalendarDataStore;
import com.mstest.eventscalendar.model.CalendarData;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaViewHolder> implements CalendarDataStore.DataChangeObserver {

    private final static int VH_TYPE_HEADER = 0;
    private final static int VH_TYPE_NO_EVENT = 1;
    private final static int VH_TYPE_EVENT = 2;

    private Context mContext;
    private final LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;
    private CalendarDataStore mDataStore;


    public AgendaAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setDataStore(CalendarDataStore dataStore) {
        mDataStore = dataStore;
        mDataStore.setDataChangeObserver(this);
    }

    public int getItemViewType(int position) {
        if(mDataStore.isHeaderData(position)) {
            return VH_TYPE_HEADER;
        } else if(mDataStore.getEvent(position) == null) {
            return VH_TYPE_NO_EVENT;
        } else {
            return VH_TYPE_EVENT;
        }
    }

    @Override
    public AgendaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AgendaViewHolder viewHolder;
        if(viewType == VH_TYPE_HEADER) {
            viewHolder = new AgendaHeaderViewHolder(mLayoutInflater.inflate(R.layout.agenda_header, mRecyclerView, false));
        } else if(viewType == VH_TYPE_NO_EVENT) {
            viewHolder = new AgendaNoEventViewHolder(mLayoutInflater.inflate(R.layout.agenda_no_event, mRecyclerView, false));
        } else {
            viewHolder = new AgendaEventViewHolder(mLayoutInflater.inflate(R.layout.agenda_event, mRecyclerView, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AgendaViewHolder holder, int position) {
        CalendarData data = getItem(position);
        if(data == null) {
            return;
        }
        holder.setDate(mContext, data.getDate());
        if(getItemViewType(position) == VH_TYPE_EVENT) {
            ((AgendaEventViewHolder) holder).setEvent(mDataStore.getEvent(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataStore != null ? mDataStore.getTotalDataCount() : 0;
    }

    @Override
    public void onChanged(@CalendarDataStore.DataChangeType int changeType, final int start, final int count) {
        if(changeType == CalendarDataStore.DATA_INSERT) {
            notifyItemRangeInserted(start, count);
        } else if(changeType == CalendarDataStore.DATA_REMOVE){
            notifyItemRangeRemoved(start, count);
        } else if(changeType == CalendarDataStore.DATA_UPDATE){
            notifyItemRangeChanged(start, count);
        }
    }

    public CalendarData getItem(int position) {
        if(position >= mDataStore.getTotalDataCount() || position < 0) {
            return null;
        }
        return mDataStore.getData(position);
    }
}
