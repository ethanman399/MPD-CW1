//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class RaceAdapter extends ArrayAdapter<Race> {

    private int nextRaceIndex = -1;
    private static final String TAG = "RaceAdapter";

    public RaceAdapter(Context context, ArrayList<Race> races) {
        super(context, 0, races);
        this.nextRaceIndex = findNextRaceIndex(races);  // Determine the index of the next race
        Log.d(TAG, "Next race index determined: " + nextRaceIndex);  // Log the index of the next race
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Race race = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_race, parent, false);
        }

        // Lookup view for data population
        TextView raceNameTextView = convertView.findViewById(R.id.raceNameTextView);
        TextView raceInfoTextView = convertView.findViewById(R.id.raceInfoTextView);

        // Populate the data into the template view using the data object
        raceNameTextView.setText("Round " + race.getRound() + ": " + race.getRaceName());
        raceInfoTextView.setText("Circuit: " + race.getCircuitName() + " | Location: " + race.getLocality() + ", " + race.getCountry() +
                " | Date: " + race.getDate() + " | Time: " + race.getTimeWithUTC());

        // Log the current race and its position
        Log.d(TAG, "Processing race: " + race.getRaceName() + " at position: " + position);

        // Highlighting logic based on index
        if (position == nextRaceIndex) {
            convertView.setBackgroundColor(Color.YELLOW);  // Highlight the next race in yellow
            Log.d(TAG, "Highlighted race: " + race.getRaceName() + " at position: " + position);
        } else if (race.isPastRace()) {
            convertView.setBackgroundColor(Color.LTGRAY);  // Past races in gray
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);  // Future races in default color
        }

        // Return the completed view to render on screen
        return convertView;
    }

    private int findNextRaceIndex(ArrayList<Race> races) {
        Date now = new Date();
        Log.d(TAG, "Current date: " + now);  // Log the current date
        int closestIndex = -1;  // Initialize to an invalid index
        Date closestDate = null;

        for (int i = 0; i < races.size(); i++) {
            Race race = races.get(i);
            Date raceDate = race.getRaceDate();

            // Log each race's date and comparison
            Log.d(TAG, "Checking race: " + race.getRaceName() + " with date: " + raceDate);

            if (raceDate != null && raceDate.after(now)) {
                Log.d(TAG, race.getRaceName() + " is an upcoming race.");
                if (closestDate == null || raceDate.before(closestDate)) {
                    closestDate = raceDate;
                    closestIndex = i;  // Update the index of the next race
                    Log.d(TAG, "Next race set to: " + race.getRaceName() + " at index: " + closestIndex);
                }
            } else {
                Log.d(TAG, race.getRaceName() + " is not an upcoming race.");
            }
        }

        if (closestIndex == -1) {
            Log.d(TAG, "No upcoming races found.");
        }

        return closestIndex;
    }

}
