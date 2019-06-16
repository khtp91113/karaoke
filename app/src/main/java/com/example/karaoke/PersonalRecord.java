package com.example.karaoke;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class PersonalRecord extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private RecyclerView recycler_view;
    private PersonalRecordAdapter adapter;
    private ArrayList<String> PersonalMusicName = new ArrayList<>();
    private ArrayList<String> MusicName = new ArrayList<>();
    private ArrayList<String> Artist = new ArrayList<>();
    private ArrayList<String> UID = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        PersonalMusicName = intent.getStringArrayListExtra("PersonalMusicName_EXTRA");
        MusicName = intent.getStringArrayListExtra("MusicName_EXTRA");
        Artist = intent.getStringArrayListExtra("Artist_EXTRA");
        UID = intent.getStringArrayListExtra("UID_EXTRA");


        // 連結元件
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        // 設置RecyclerView為列表型態
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        // 設置格線
        recycler_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 將資料交給adapter
        adapter = new PersonalRecordAdapter(PersonalMusicName,MusicName,Artist,UID);
        // 設置adapter給recycler_view
        recycler_view.setAdapter(adapter);

    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        String queryString = "";

        if (bundle != null) {
            queryString = bundle.getString("queryString");
        }

        return new QueryLoader (this,queryString);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}