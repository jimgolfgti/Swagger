package com.passionatecode.swagger;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class SwagWinnersFragment extends Fragment {
    private final Random random = new Random();
    private final Set<String> exclusions = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {{
        add("mrandrew");
        add("technottingham");
        add("antenna_uk");
        add("cordiusltd");
        add("justianblount");
        add("cwcs_hosting");
    }};
    private final HashMap<String, Status> candidates = new HashMap<String, Status>();
    private final ArrayList<Status> winners = new ArrayList<Status>();

    private TwitterFactory twitterFactory;
    private WinnersAdapter winnerListAdapter;
    private Status currentWinner;

    @InjectView(R.id.tweet_user_view)
    TextView userTextView;
    @InjectView(R.id.tweet_message_view)
    TextView messageTextView;
    @InjectView(R.id.load_tweets_button)
    Button loadTweetsButton;
    @InjectView(R.id.pick_winner_button)
    Button pickWinnerButton;
    @InjectView(R.id.winner_list_view)
    ListView winnerListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_swag_winners, container, false);
        ButterKnife.inject(this, rootView);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity());
        loadTweetsButton.setEnabled(true);
        loadTweetsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setEnabled(false);
                loadTweets();
            }
        });
        pickWinnerButton.setEnabled(false);
        pickWinnerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pickWinner();
            }
        });
        winnerListAdapter = new WinnersAdapter(this.getActivity(), winners);
        winnerListView.setAdapter(winnerListAdapter);

        Configuration configuration = new ConfigurationBuilder()
                .setOAuthConsumerKey(preferences
                        .getString(Constants.TWITTER_CONSUMER_KEY, null))
                .setOAuthConsumerSecret(preferences
                        .getString(Constants.TWITTER_CONSUMER_SECRET, null))
                .setOAuthAccessToken(preferences
                        .getString(Constants.TWITTER_USER_KEY, null))
                .setOAuthAccessTokenSecret(preferences
                        .getString(Constants.TWITTER_USER_SECRET, null))
                .build();
        twitterFactory = new TwitterFactory(configuration);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void loadTweets() {
        new DownloadTweetsTask(twitterFactory.getInstance()).execute();
    }

    private void tweetsLoaded(List<Status> tweets) {
        Log.d(Constants.APPTAG, "Tweets downloaded: " + tweets.size());
        for (Status tweet : tweets) {
            String user = tweet.getUser().getScreenName();
            if (exclusions.contains(user) || candidates.containsKey(user)) continue;
            candidates.put(user, tweet);
        }
        Toast.makeText(this.getActivity(), candidates.size() == 0
                        ? "No candidates"
                        : "Found " + candidates.size() + " possibilities",
                Toast.LENGTH_LONG
        ).show();

        pickWinnerButton.setEnabled(candidates.size() > 0);
    }

    private class DownloadTweetsTask extends AsyncTask<Void, Void, List<Status>> {
        private final Twitter twitter;

        public DownloadTweetsTask(Twitter twitter) {
            this.twitter = twitter;
        }

        @Override
        protected List<twitter4j.Status> doInBackground(Void... params) {
            Query query = new Query("#TechNott")
                    .count(100)
                    .lang("en")
                    .resultType(Query.ResultType.recent);
            try {
                QueryResult result = twitter.search().search(query);

                return result.getTweets();
            } catch (TwitterException e) {
                Log.e(Constants.APPTAG, "Error downloading");
                Log.d(Constants.APPTAG, Log.getStackTraceString(e));
            }
            return new ArrayList<twitter4j.Status>();
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if (result != null) {
                tweetsLoaded(result);
            }
        }
    }

    private void pickWinner() {
        if (currentWinner != null) {
            winners.add(0, currentWinner);
            winnerListAdapter.notifyDataSetChanged();
        }

        Status[] values = candidates.values().toArray(new Status[]{});
        currentWinner = values[random.nextInt(values.length)];

        User user = currentWinner.getUser();
        candidates.remove(user.getScreenName());

        userTextView.setText(String.format("%s (@%s)", user.getName(), user.getScreenName()));
        messageTextView.setText(currentWinner.getText());

        if (candidates.size() == 0) pickWinnerButton.setEnabled(false);
    }
}

