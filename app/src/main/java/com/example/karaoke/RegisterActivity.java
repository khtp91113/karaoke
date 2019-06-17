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

import org.json.JSONArray;

public class RegisterActivity extends AppCompatActivity implements  android.support.v4.app.LoaderManager.LoaderCallbacks<String> {

    private EditText username;
    private  EditText password;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        registerButton = findViewById(R.id.button4);
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
        registerButton.setClickable(true);
        if (s != null) {
            if (s.contains("MySQL Error")) {
                Toast.makeText(this, s, Toast.LENGTH_SHORT);
            }
            else if (s.contains("UID=")) {
                Log.d("Returnmsg0","asd"+s.substring(4).replace("\n", "").replace("\r", "")+"asd");


                int UID = Integer.parseInt(s.substring(4).replace("\n", "").replace("\r", ""));


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

    public void register(View view) {
        // Check the status of the network connection.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }
        String mQuery = "http://"+getResources().getString(R.string.server_host)+"/CreateAccount";
        String uKey = "username=";
        String pKey = "passwd=";
        mQuery= mQuery+"?"+ uKey + username.getText() +"&" + pKey +password.getText();

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()
                && mQuery.length() != 0) {

            Bundle queryBundle = new Bundle();
            queryBundle.putString("queryString", mQuery);
            registerButton.setClickable(false);
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
}
