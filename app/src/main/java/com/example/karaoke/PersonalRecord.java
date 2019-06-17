package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.ArrayList;

public class PersonalRecord extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private RecyclerView recycler_view;
    private PersonalRecordAdapter adapter;
    //private ArrayList<String> PersonalMusicName = new ArrayList<>();
    private String mQuery;

    private ArrayList<String> Artist = new ArrayList<>();
    private ArrayList<String> MusicName = new ArrayList<>();
    private Integer UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_record);

        Intent intent = getIntent();
        //PersonalMusicName = intent.getStringArrayListExtra("PersonalMusicName_EXTRA");
        UID = intent.getIntExtra("UID_EXTRA",21);

        this.mQuery = "http://140.116.245.248:5000/QueryPersonalSongList?UID="+UID;
        GetSongList();


        // 連結元件
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 將資料交給adapter
        //adapter = new PersonalRecordAdapter(MusicName,Artist,UID);
        // 設置adapter給recycler_view
        //recycler_view.setAdapter(adapter);

    }
    public void GetSongList() {
        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && mQuery.length() != 0) {

            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", mQuery);
            getSupportLoaderManager().restartLoader(0, queryBundle, this);
            Toast.makeText(this, "Loading", Toast.LENGTH_SHORT);

        }
        // Otherwise update the TextView to tell the user there is no
        // connection, or no search term.
        else {
            if (mQuery.length() == 0) {
                Toast.makeText(this, "Empty return", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "Bad Connection Please Try Again", Toast.LENGTH_SHORT);
            }
        }

    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle args) {
        String queryString = "";

        if (args != null) {
            queryString = args.getString("queryString");
        }

<<<<<<< HEAD
        return new QuerySongListTask(this, queryString);

=======
        return new QueryLoader(this,queryString);
>>>>>>> be26c9ca4680d5adf78f633a144a814766793acd
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        if (s != null) {
            Log.d("Returned JSON", s);
            try {
                JSONArray arr = new JSONArray(s);
                Log.d("Parsed JSON", Integer.toString(arr.length()));
                PersonalRecord.SongObject[] songs = new PersonalRecord.SongObject[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    JSONArray a = new JSONArray(arr.getString(i));
                    songs[i] = new PersonalRecord.SongObject(a);
                    MusicName.add(songs[i].Name);
                    Artist.add(songs[i].Artist);
                }
                // 將資料交給adapter
                adapter = new PersonalRecordAdapter(MusicName,Artist,UID);
                // 設置adapter給recycler_view
                recycler_view.setAdapter(adapter);
            } catch (Exception e) {

            }
        } else {
            Toast.makeText(this, "Bad Connection Please Try Again", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    public class SongObject {
        public int UID;
        public String Name;
        public String Artist;
        public SongObject(JSONArray s) {
            try {
                UID = s.getInt(0);
                Name = s.getString(1);
                Artist = s.getString(2);
            } catch (Exception e) {

            }
        }
    }
}