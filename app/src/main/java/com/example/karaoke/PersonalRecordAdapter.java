package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class PersonalRecordAdapter extends RecyclerView.Adapter<PersonalRecordAdapter.ViewHolder> {

    private List<String> mData1;
    private List<String> mArtistList;
    private Integer mUID;

    PersonalRecordAdapter(List<String> data1, List<String> data3, Integer data4) {
        mData1 = data1;
        mArtistList = data3;
        mUID = data4;
    }

    @NonNull
    @Override
    public PersonalRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.personal_record_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalRecordAdapter.ViewHolder viewHolder, int i) {
        viewHolder.txtItem1.setText(mData1.get(i));
    }

    @Override
    public int getItemCount() {
        return mData1.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtItem1;
        private Button btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            txtItem1 = (TextView) itemView.findViewById(R.id.txtItem1);
            btnRemove = (Button) itemView.findViewById(R.id.btnRemove);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mPosition = getLayoutPosition();
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ListenRecordActivity.class);
                    intent.putExtra("MusicName", mData1.get(mPosition));
                    intent.putExtra("ArtistName", mArtistList.get(mPosition));
                    intent.putExtra("UID",mUID);
                    context.startActivity(intent);
                }
            });
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mPosition = getLayoutPosition();
                    removeItem(getAdapterPosition());
                    String queryString = "https://140.118.245.248/RemovePersonalSong?/UID="+ mUID +"&PersonalMusicName=" + mData1.get(mPosition);


                    // Hide the keyboard when the button is pushed.
                    InputMethodManager inputManager = (InputMethodManager)
                            v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null) {
                        inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    // Check the status of the network connection.
                    ConnectivityManager connMgr = (ConnectivityManager)
                            v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = null;
                    if (connMgr != null) {
                        networkInfo = connMgr.getActiveNetworkInfo();
                    }

                    // If the network is available, connected, and the search field
                    // is not empty, start a BookLoader AsyncTask.
                    if (networkInfo != null && networkInfo.isConnected()
                            && queryString.length() != 0) {

                        Bundle queryBundle = new Bundle();
                        queryBundle.putString("queryString", queryString);
                        //getSupportLoaderManager().restartLoader(0, queryBundle, this);
                    }
                    // Otherwise update the TextView to tell the user there is no
                    // connection, or no search term.
                    else {
                        Toast.makeText(v.getContext(), "Check your internet connection and try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void removeItem(int position){
        mData1.remove(position);
        notifyItemRemoved(position);
    }
}
