package com.example.karaoke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity implements  android.support.v4.app.LoaderManager.LoaderCallbacks<String>  {
    private EditText oldpw;
    private  EditText newpw;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        oldpw = findViewById(R.id.editText);
        newpw = findViewById(R.id.editText2);
        submit = findViewById(R.id.button4);
    }

    public void submit(View view) {
        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
       int UID =  pref.getInt("UID",0);
        String mQuery = "http://"+getResources().getString(R.string.server_host)+"/ChangePasswd";
        String uKey = "oldpw=";
        String pKey = "newpd=";
        mQuery= mQuery+"?"+ uKey + oldpw.getText() +"&" + pKey +newpw.getText() + "&UID="+Integer.toString(UID);

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && mQuery.length() != 0) {

            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", mQuery);
            submit.setClickable(false);
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
        submit.setClickable(true);
        Log.d(",CHangepass",s);
        if (s != null) {
            if (s.startsWith("0")) {
                Toast.makeText(this, s, Toast.LENGTH_SHORT);
            }
            else if (s.startsWith("1")) {
                Intent intent = new Intent(this, PersonalDetailActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, s, Toast.LENGTH_SHORT);
            }
        } else {
            Toast.makeText(this, "Bad Connection Please Try Again", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
