package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PersonalDetailActivity extends AppCompatActivity {
    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String username=pref.getString("Username", "");
        mTextView = findViewById(R.id.username);
        mTextView.setText(username);
    }

    public void logout(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("UID",0);
        editor.putString("Username","");
        editor.commit();

        Intent intent = new Intent(view.getContext(),MainActivity.class);
        startActivity(intent);
    }

    public void ChangePassword(View view) {
        Intent intent = new Intent(view.getContext(),ChangePasswordActivity.class);
        startActivity(intent);
    }

    public void UploadRecords(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Integer UID = pref.getInt("UID", 21);

        Intent intent = new Intent(view.getContext(),PersonalRecord.class);
        intent.putExtra("UID_EXTRA",UID);
        startActivity(intent);
    }
}
