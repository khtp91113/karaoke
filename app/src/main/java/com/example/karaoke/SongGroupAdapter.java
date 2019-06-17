package com.example.karaoke;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

public class SongGroupAdapter extends
        RecyclerView.Adapter<SongGroupAdapter.SongGroupHolder>  {
    private final String[] mSongGroupList;
    private final String[] mApis;
    private final String[] mKeys;
    private final String[][] mGroupContent;
    private final LayoutInflater mInflater;
    private Context context;
    public SongGroupAdapter(Context context, String[] songGroupList,String[][] content,String[] apis, String[] parakeys) {
        mInflater = LayoutInflater.from(context);
        this.mSongGroupList = songGroupList;
        this.mGroupContent = content;
        this.context = context;
        this.mApis = apis;
        this.mKeys = parakeys;
    }

    @Override
    public SongGroupAdapter.SongGroupHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.song_group_item, parent, false);
        return new SongGroupHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(SongGroupHolder holder, int i) {
        // Retrieve the data for that position.
        String mCurrent = mSongGroupList[i];
        // Add the data to the view holder.
        holder.titleView.setText(mCurrent);
        SongGroupContentAdapter mAdapter = new SongGroupContentAdapter(context,mGroupContent[i],mApis[i],mKeys[i]);
        holder.recyclerView.setAdapter(mAdapter);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false));
    }

    @Override
    public int getItemCount() {
        return mSongGroupList.length;
    }

    class SongGroupHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final TextView titleView;
        public final RecyclerView recyclerView;
        final SongGroupAdapter mAdapter;
        Typeface titleFont = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/cfont.ttf");

        public SongGroupHolder(View itemView, SongGroupAdapter adapter) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title);
            recyclerView = itemView.findViewById(R.id.buttons);

            titleView.setTypeface(titleFont);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
