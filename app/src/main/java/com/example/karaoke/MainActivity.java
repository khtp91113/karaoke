package com.example.karaoke;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SongGroupAdapter mAdapter;
    private ImageView smallperson;
    private int UID;
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
        smallperson = findViewById(R.id.imageView1);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        int savedUID=pref.getInt("UID", 0);
        if (savedUID ==0) {
            smallperson.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_black_24dp));
        }else{
            smallperson.setImageDrawable(getResources().getDrawable(R.drawable.ic_person_green_24dp));
        }
        UID = savedUID;
    }


    public void OpenPersonalDetail(View view) {
        //TODO prompt login if not login
        if(UID ==0){
            Intent  intent = new Intent(view.getContext(),LoginActivity.class);
            this.startActivityForResult(intent, 0);
        }else{
            //Go to personal detail if already login
            Intent intent = new Intent(view.getContext(), PersonalDetailActivity.class);
            ActivityOptionsCompat options = (ActivityOptionsCompat) ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(),view.findViewById(R.id.view2),"profile");
            this.startActivityForResult(intent, 0, options.toBundle());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                this.finish();
            }
        }
    }
  }
