package com.passionatecode.swagger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import twitter4j.Status;

public class WinnersAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final ArrayList<Status> winners;

    public WinnersAdapter(Context context, ArrayList<Status> winners) {
        inflater = LayoutInflater.from(context);
        this.winners = winners;
    }

    @Override
    public int getCount() {
        return winners.size();
    }

    @Override
    public Status getItem(int position) {
        return winners.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.swag_winner, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Status item = getItem(position);
        holder.name.setText(item.getUser().getName());
        holder.screenName.setText(item.getUser().getScreenName());

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.winner_name)
        TextView name;
        @InjectView(R.id.winner_screen_name)
        TextView screenName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
