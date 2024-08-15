//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DriverAdapter extends ArrayAdapter<Driver> {

    public DriverAdapter(Context context, ArrayList<Driver> drivers) {
        super(context, 0, drivers);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Driver driver = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_driver, parent, false);
        }

        // Lookup view for data population
        ImageView trophyImageView = convertView.findViewById(R.id.trophyImageView);
        TextView driverNameTextView = convertView.findViewById(R.id.driverNameTextView);
        TextView driverInfoTextView = convertView.findViewById(R.id.driverInfoTextView);

        // Populate the data into the template view using the data object
        driverNameTextView.setText(driver.getGivenName() + " " + driver.getFamilyName());
        driverInfoTextView.setText("Points: " + driver.getPoints() + " | Wins: " + driver.getWins());

        // Display the appropriate trophy for 1st, 2nd, and 3rd place
        if (driver.getPosition().equals("1")) {
            trophyImageView.setImageResource(R.drawable.gold_cup);
            trophyImageView.setVisibility(View.VISIBLE);
        } else if (driver.getPosition().equals("2")) {
            trophyImageView.setImageResource(R.drawable.silver_cup);
            trophyImageView.setVisibility(View.VISIBLE);
        } else if (driver.getPosition().equals("3")) {
            trophyImageView.setImageResource(R.drawable.bronze_cup);
            trophyImageView.setVisibility(View.VISIBLE);
        } else {
            trophyImageView.setVisibility(View.GONE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}

