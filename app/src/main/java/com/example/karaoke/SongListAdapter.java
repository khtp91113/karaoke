package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListHolder>  {
    private final SongListActivity.SongObject[] songs;
    private final LayoutInflater mInflater;
    public SongListAdapter(Context context, SongListActivity.SongObject[] songs) {
        mInflater = LayoutInflater.from(context);
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.song_item, viewGroup, false);

        return new SongListHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListHolder holder, int i) {
        SongListActivity.SongObject s = songs[i];
        // Add the data to the view holder.
        holder.songName = s.Name;
        holder.NameView.setText(s.Name);
        holder.artist = s.Artist;
        holder.ArtistView.setText(s.Artist);
        holder.CountView.setText(Integer.toString(s.Count));
        holder.UID = s.UID;
    }

    @Override
    public int getItemCount() {
        return songs.length;
    }

    class SongListHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final TextView NameView;
        public final TextView ArtistView;
        public final TextView CountView;
        public String songName;
        public String artist;
        public int UID;

        Typeface titleFont = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/cfont.ttf");
        Typeface artistFont = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/cfont.ttf");
        Typeface countFont = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/cfont.ttf");

        public SongListHolder(View itemView) {
            super(itemView);
            NameView = itemView.findViewById(R.id.name);
            ArtistView = itemView.findViewById(R.id.artist);
            CountView = itemView.findViewById(R.id.count);

            NameView.setTypeface(titleFont);
            ArtistView.setTypeface(artistFont);
            CountView.setTypeface(countFont);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(),MusicActivity.class);
            intent.putExtra("MusicName",songName);
            intent.putExtra("ArtistName",artist);
            SharedPreferences pref = v.getContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            int UIDD=pref.getInt("UID", 0);
            intent.putExtra("UID",UIDD);
            v.getContext().startActivity(intent);
        }
    }
}
