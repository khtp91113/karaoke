package com.example.karaoke;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.karaoke.QuerySongListTask;
import com.example.karaoke.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class SongListActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

    private String mQuery;
    private RecyclerView mRecyclerView;
    private  SongListAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        String title  = getIntent().getExtras().getString("title");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        this.mQuery = getIntent().getExtras().getString("query");
        Log.d("querystring", this.mQuery);
        GetSongList();
        mRecyclerView = findViewById(R.id.songlist);
        mAdapter = new SongListAdapter(this,new SongObject[0]);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
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

        return new QuerySongListTask(this, queryString);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        if (s != null) {
            Log.d("Returned JSON", s);
            try {
                JSONArray arr = new JSONArray(s);
                Log.d("Parsed JSON", Integer.toString(arr.length()));
                SongObject[] songs = new SongObject[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    JSONArray a = new JSONArray(arr.getString(i));
                    songs[i] = new SongObject(a);
                }
                mAdapter = new SongListAdapter(this,songs);
                mRecyclerView.setAdapter(mAdapter);
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
        public String Gender;
        public String Lang;
        public int Count;
        public SongObject(JSONArray s) {
            try {
                UID = s.getInt(0);
                Name = s.getString(1);
                Artist = s.getString(5);
                Gender = s.getString(6);
                Lang = s.getString(7);
                Count = s.getInt(8);
            } catch (Exception e) {

            }
        }
    }
}
