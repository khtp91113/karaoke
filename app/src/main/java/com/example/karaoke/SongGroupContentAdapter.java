package com.example.karaoke;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SongGroupContentAdapter  extends
        RecyclerView.Adapter<SongGroupContentAdapter.SongGroupContentHolder>  {

    private final LayoutInflater mInflater;
    private final String[] mContent;
    public SongGroupContentAdapter(Context context, String[] content) {
        mInflater = LayoutInflater.from(context);
        this.mContent = content;
    }
    @NonNull
    @Override
    public SongGroupContentHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.song_categories_button, parent, false);
        return new SongGroupContentHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongGroupContentHolder holder, int i) {
        // Retrieve the data for that position.
        String mCurrent = mContent[i];
        // Add the data to the view holder.
        holder.mButton.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mContent.length;
    }

    class SongGroupContentHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final Button mButton;
        final SongGroupContentAdapter mAdapter;
        public SongGroupContentHolder(View itemView, SongGroupContentAdapter adapter) {
            super(itemView);
            mButton = itemView.findViewById(R.id.button2);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        //TODO open song list
        }
    }

}
