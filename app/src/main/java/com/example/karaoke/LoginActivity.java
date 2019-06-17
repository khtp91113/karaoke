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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements  android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

    private EditText username;
    private  EditText password;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        loginButton = findViewById(R.id.button4);
    }

    public void OpenRegister(View view) {
        Intent i = new Intent(this,RegisterActivity.class);
        startActivityForResult(i, 0);
    }

    public void login(View view) {
        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        String mQuery = "http://"+getResources().getString(R.string.server_host)+"/Login";
        String uKey = "username=";
        String pKey = "passwd=";
        mQuery= mQuery+"?"+ uKey + username.getText() +"&" + pKey +password.getText();

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && mQuery.length() != 0) {

            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", mQuery);
            loginButton.setClickable(false);
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
        loginButton.setClickable(true);
        if (s != null) {
            if (s.contains("UID=")) {
                int UID = Integer.parseInt(s.substring(4).replace("\n", "").replace("\r", ""));
                if (UID == -1){
                    Toast.makeText(this, "User not found or password error!", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("UID", UID);
                editor.putString("Username",username.getText().toString());
                editor.commit();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                setResult(RESULT_OK, null);
                finish();
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK, null);
                this.finish();
            }
        }
    }
}
