package com.passionatecode.swagger;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterLoginFragment extends Fragment {
    static final String TWITTER_CALLBACK_URL = "oauth://swagger";

    @InjectView(R.id.loginWebView)
    WebView loginWebView;

    private Twitter twitter;
    private RequestToken requestToken;
    private SharedPreferences preferences;
    private OnTwitterLoggedInListener callback;

    public interface OnTwitterLoggedInListener {
        public void onTwitterLoggedIn();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_twitter_login, container, false);
        ButterKnife.inject(this, rootView);

        loginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url != null && url.startsWith(TWITTER_CALLBACK_URL))
                    new AfterLoginTask().execute(url);
                else
                    webView.loadUrl(url);
                return true;
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());

        new LoginTask().execute();

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (OnTwitterLoggedInListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTwitterLoggedInListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Function to login twitter
     */
    private void loginToTwitter() {
        Configuration configuration = new ConfigurationBuilder()
                .setOAuthConsumerKey(preferences
                        .getString(Constants.TWITTER_CONSUMER_KEY, null))
                .setOAuthConsumerSecret(preferences
                        .getString(Constants.TWITTER_CONSUMER_SECRET, null))
                .build();

        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();

        try {
            requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
        } catch (TwitterException e) {
            Log.e(Constants.APPTAG, Log.getStackTraceString(e));
        }
    }

    public void handleTwitterCallback(String url) {
        Uri uri = Uri.parse(url);

        // oAuth verifier
        final String verifier = uri.getQueryParameter("oauth_verifier");

        try {
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

            Editor editor = preferences.edit();

            editor.putString(Constants.TWITTER_USER_KEY, accessToken.getToken());
            editor.putString(Constants.TWITTER_USER_SECRET, accessToken.getTokenSecret());
            editor.putBoolean(Constants.TWITTER_LOGGED_IN, true);
            editor.apply();
        } catch (Exception e) {
            Log.e(Constants.APPTAG, Log.getStackTraceString(e));
        }
    }

    class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            loginToTwitter();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (requestToken == null) return;
            loginWebView.loadUrl(requestToken.getAuthenticationURL());
            loginWebView.setVisibility(View.VISIBLE);
            loginWebView.requestFocus(View.FOCUS_DOWN);
        }
    }

    class AfterLoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            loginWebView.clearHistory();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            handleTwitterCallback(params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loginWebView.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Login Successful", Toast.LENGTH_SHORT).show();
            callback.onTwitterLoggedIn();
        }
    }
}

