package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OnlineRecordAdapter extends RecyclerView.Adapter<OnlineRecordAdapter.SongListHolder>  {
    private final RecordObject[] records;
    private final LayoutInflater mInflater;
    public OnlineRecordAdapter(Context context, RecordObject[] records) {
        mInflater = LayoutInflater.from(context);
        this.records = records;
    }

    @NonNull
    @Override
    public OnlineRecordAdapter.SongListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.song_item, viewGroup, false);
        return new OnlineRecordAdapter.SongListHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OnlineRecordAdapter.SongListHolder holder, int i) {
        RecordObject s = records[i];
        // Add the data to the view holder.
        holder.songName = s.Artist + "-" + s.Name;
        holder.NameView.setText(s.Name);
        holder.timeStamp = s.timeStamp;
        holder.ArtistView.setText(s.timeStamp);
        holder.UIDD = s.UID;
        holder.userName = s.userName;
        holder.CountView.setText(s.userName);
    }

    @Override
    public int getItemCount() {
        return records.length;
    }

    class SongListHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final TextView NameView;
        public final TextView ArtistView;
        public final TextView CountView;
        public String songName;
        public String timeStamp;
        public String userName;
        public int UIDD;

        public SongListHolder(View itemView) {
            super(itemView);
            NameView = itemView.findViewById(R.id.name);
            ArtistView = itemView.findViewById(R.id.artist);
            CountView = itemView.findViewById(R.id.count);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), ListenRecordActivity.class);
            String[] tmp = songName.split("-");
            intent.putExtra("MusicName", tmp[1]);
            intent.putExtra("ArtistName", tmp[0]);
            intent.putExtra("UID", UIDD);
            intent.putExtra("mode", "other");
            v.getContext().startActivity(intent);
        }
    }
}
