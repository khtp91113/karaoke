package com.example.karaoke;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SongGroupAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO change image if already login
        setContentView(R.layout.activity_main);
        String[] categories = getResources().getStringArray(R.array.songcategories);
        String[][] content = new String[categories.length][];
        content[0] = getResources().getStringArray(R.array.languages);
        content[1] = getResources().getStringArray(R.array.singer);
        content[2] = getResources().getStringArray(R.array.leaderboard);
        String[] apis = getResources().getStringArray(R.array.apis);
        String[] keys = getResources().getStringArray(R.array.apis_para_key);
        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new SongGroupAdapter(this,categories,content,apis,keys);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
}


    public void OpenPersonalDetail(View view) {
        //TODO prompt login if not login


        //Go to personal detail if already login
        Intent intent = new Intent(view.getContext(), PersonalDetailActivity.class);
        ActivityOptionsCompat options = (ActivityOptionsCompat) ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(),view.findViewById(R.id.view2),"profile");

        view.getContext().startActivity(intent,options.toBundle());
    }

  }
