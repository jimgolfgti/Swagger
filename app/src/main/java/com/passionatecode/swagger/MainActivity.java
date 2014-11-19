package com.passionatecode.swagger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements TwitterLoginFragment.OnTwitterLoggedInListener {
    private SharedPreferences preferences;
    private SwagWinnersFragment swagWinnersFragment;
    private TwitterLoginFragment twitterLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (savedInstanceState == null) {
            try {
                ensureAssetsPresent();
            } catch (IOException e) {
                finish();
                return;
            }
            if (preferences.getBoolean(Constants.TWITTER_LOGGED_IN, false)) {
                swagWinnersFragment = new SwagWinnersFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.container, swagWinnersFragment)
                        .commit();
            } else {
                twitterLoginFragment = new TwitterLoginFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.container, twitterLoginFragment)
                        .commit();
            }
        }
    }

    private void ensureAssetsPresent() throws IOException {
        if (preferences.getBoolean(Constants.ASSETS_IMPORTED, false)) return;

        StringBuilder builder = new StringBuilder();
        InputStream stream = this.getAssets().open("twitter_api.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String str;
        while ((str = reader.readLine()) != null) {
            builder.append(str);
        }

        reader.close();

        String[] consumerKey = builder.toString().split(":", 2);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.TWITTER_CONSUMER_KEY, consumerKey[0]);
        editor.putString(Constants.TWITTER_CONSUMER_SECRET, consumerKey[1]);
        editor.putBoolean(Constants.ASSETS_IMPORTED, true);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTwitterLoggedIn() {
        swagWinnersFragment = new SwagWinnersFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, swagWinnersFragment)
                .commit();
        twitterLoginFragment = null;
    }
}
