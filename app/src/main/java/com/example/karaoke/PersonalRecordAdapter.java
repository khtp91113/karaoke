package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;


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
            Typeface titleFont = Typeface.createFromAsset(itemView.getContext().getAssets(),"fonts/cfont.ttf");
            txtItem1.setTypeface(titleFont);
            btnRemove.setTypeface(titleFont);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mPosition = getLayoutPosition();
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ListenRecordActivity.class);
                    intent.putExtra("MusicName", mData1.get(mPosition));
                    intent.putExtra("ArtistName", mArtistList.get(mPosition));
                    intent.putExtra("UID",mUID);
                    intent.putExtra("mode","self");
                    context.startActivity(intent);
                }
            });
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mPosition = getLayoutPosition();
                    //local file
                    String recordPath  = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + mArtistList.get(mPosition) + "-" + mData1.get(mPosition)  +  "_record.wav";
                    File f = new File(recordPath);
                    if (f.exists() == true)
                        f.delete();

                    // http server ftp server
                    try {
                        new RemovePersonalRecordTask().execute("http://140.116.245.248:5000", mData1.get(mPosition), mArtistList.get(mPosition), "140.116.245.248", "21", "ftpuser", "12345678", String.valueOf(mUID)).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    removeItem();
                }
            });
        }
    }

    public void removeItem(){
        Handler handler = PersonalRecord.handler;
        handler.sendEmptyMessage(0);
        return;
    }
}
