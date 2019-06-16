package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SongGroupContentAdapter  extends
        RecyclerView.Adapter<SongGroupContentAdapter.SongGroupContentHolder>   {

    private final LayoutInflater mInflater;
    private final String[] mContent;
    private final   String mApi;

    private final   String mKey;
    public SongGroupContentAdapter(Context context, String[] content,String api,String key) {
        mInflater = LayoutInflater.from(context);
        this.mContent = content;
        this.mApi = api;
        this.mKey = key;
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
        holder.mquery = "http://"+ holder.context.getResources().getString(R.string.server_host) + "/" + mApi +"?"+ mKey +"="+mCurrent;
        holder.title = mCurrent;
    }

    @Override
    public int getItemCount() {
        return mContent.length;
    }

    class SongGroupContentHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final Button mButton;
        public final Context context;
        public String mquery;
        public String title;
        final SongGroupContentAdapter mAdapter;
        public SongGroupContentHolder(View itemView, SongGroupContentAdapter adapter) {
            super(itemView);
            mButton = itemView.findViewById(R.id.button2);
            this.mAdapter = adapter;
            this.context = itemView.getContext();

            mButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        //TODO open song list
            Intent intent = new Intent(context, SongListActivity.class);
            intent.putExtra("query",mquery);
            intent.putExtra("title",title);
            v.getContext().startActivity(intent);

        }

    }

}
