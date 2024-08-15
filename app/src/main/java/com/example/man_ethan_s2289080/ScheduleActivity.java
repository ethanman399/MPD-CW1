//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private static final String RACE_SCHEDULE_URL = "http://ergast.com/api/f1/current";
    private static final String TAG = "ScheduleActivity";

    private ListView scheduleListView;
    private TextView lastUpdateTextView;
    private Button backToMainButton;

    private ArrayList<Race> raceList = new ArrayList<>();
    private RaceAdapter raceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        scheduleListView = findViewById(R.id.scheduleListView);
        lastUpdateTextView = findViewById(R.id.lastUpdateTextView);
        backToMainButton = findViewById(R.id.backToMainButton);

        // Initialize the adapter and set it to the ListView
        raceAdapter = new RaceAdapter(this, raceList);
        scheduleListView.setAdapter(raceAdapter);

        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Race selectedRace = raceList.get(position);

                double latitude = selectedRace.getLatitude();
                double longitude = selectedRace.getLongitude();

                Log.d("ScheduleActivity", "Selected race: " + selectedRace.getCircuitName() + " Latitude: " + latitude + " Longitude: " + longitude);

                Intent intent = new Intent(ScheduleActivity.this, MapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("circuitName", selectedRace.getCircuitName());
                startActivity(intent);
            }
        });

        // Fetch race data asynchronously
        new FetchDataTask().execute(RACE_SCHEDULE_URL);

        // Navigate to MainActivity when the button is clicked
        backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try {
                result = fetchDataFromUrl(params[0]);
            } catch (IOException e) {
                Log.e(TAG, "Error fetching race data from URL, loading fallback data", e);
                result = loadFallbackData("f1_race_schedule.xml");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseRaceSchedule(result);
                updateLastUpdateTime();

                // Initialize and set the adapter after data is fetched and parsed
                raceAdapter = new RaceAdapter(ScheduleActivity.this, raceList);
                scheduleListView.setAdapter(raceAdapter);

                // Ensure that the data is populated before calling findNextRaceIndex
                if (raceAdapter != null) {
                    raceAdapter.notifyDataSetChanged();
                }
            } else {
                lastUpdateTextView.setText("Error: Could not load data.");
            }
        }

        private String fetchDataFromUrl(String urlString) throws IOException {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set timeouts for the connection and the read operation
            connection.setConnectTimeout(10000);  // 10 seconds to establish a connection
            connection.setReadTimeout(15000); // 15 seconds to read data from the connection

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } finally {
                connection.disconnect();
            }
            return result.toString();
        }

        private String loadFallbackData(String fileName) {
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error loading fallback data", e);
            }
            Log.d(TAG, "Loaded fallback data");
            return result.toString();
        }

        private void parseRaceSchedule(String xmlData) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(xmlData));

                int eventType = parser.getEventType();
                Race currentRace = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagName.equals("Race")) {
                                currentRace = new Race();
                                currentRace.setRound(Integer.parseInt(parser.getAttributeValue(null, "round")));
                            } else if (currentRace != null) {
                                if (tagName.equals("RaceName")) {
                                    parser.next();
                                    currentRace.setRaceName(parser.getText());
                                } else if (tagName.equals("CircuitName")) {
                                    parser.next();
                                    currentRace.setCircuitName(parser.getText());
                                } else if (tagName.equals("Date")) {
                                    parser.next();
                                    currentRace.setDate(parser.getText());
                                } else if (tagName.equals("Time")) {
                                    parser.next();
                                    currentRace.setTime(parser.getText());
                                } else if (tagName.equals("Location")) {
                                    String lat = parser.getAttributeValue(null, "lat");
                                    String lng = parser.getAttributeValue(null, "long");
                                    if (lat != null && lng != null) {
                                        currentRace.setLatitude(Double.parseDouble(lat));
                                        currentRace.setLongitude(Double.parseDouble(lng));
                                    }
                                } else if (tagName.equals("Locality")) {
                                    parser.next();
                                    currentRace.setLocality(parser.getText());
                                } else if (tagName.equals("Country")) {
                                    parser.next();
                                    currentRace.setCountry(parser.getText());
                                }
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            if (tagName.equals("Race") && currentRace != null) {
                                raceList.add(currentRace);
                                currentRace = null;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing XML data", e);
                lastUpdateTextView.setText("Error: Could not parse data.");
            }
        }

        private void updateLastUpdateTime() {
            String currentTime = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(new Date());
            lastUpdateTextView.setText("Last updated: " + currentTime);
        }
    }
}
