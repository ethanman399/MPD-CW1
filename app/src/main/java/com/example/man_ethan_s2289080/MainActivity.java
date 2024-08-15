/*  Starter project for Mobile Platform Development in main diet 2023/2024
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    private static final String DRIVER_STANDINGS_URL = "http://ergast.com/api/f1/current/driverStandings";
    private static final String TAG = "MainActivity";

    private ListView standingsListView;
    private TextView lastUpdateTextView;
    private Button viewScheduleButton;

    private ArrayList<Driver> driverList = new ArrayList<>();
    private DriverAdapter driverAdapter;
    private Handler dataUpdateHandler;
    private DataUpdateRunnable dataUpdateRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the handler and start the data update runnable
        dataUpdateHandler = new Handler(Looper.getMainLooper());
        dataUpdateRunnable = new DataUpdateRunnable(this, dataUpdateHandler);
        dataUpdateHandler.post(dataUpdateRunnable);

        standingsListView = findViewById(R.id.standingsListView);
        lastUpdateTextView = findViewById(R.id.lastUpdateTextView);
        viewScheduleButton = findViewById(R.id.viewScheduleButton);

        // Initialize the adapter and set it to the ListView
        driverAdapter = new DriverAdapter(this, driverList);
        standingsListView.setAdapter(driverAdapter);


        standingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Driver selectedDriver = driverList.get(position);
                Intent intent = new Intent(MainActivity.this, DriverDetailActivity.class);
                intent.putExtra("driver", selectedDriver);
                startActivity(intent);
            }
        });


        // Navigate to ScheduleActivity when the button is clicked
        viewScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                startActivity(intent);
            }
        });

        // Fetch driver standings data asynchronously
        new FetchDataTask().execute(DRIVER_STANDINGS_URL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to stop the updates when the activity is destroyed
        dataUpdateHandler.removeCallbacks(dataUpdateRunnable);
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try {
                result = fetchDataFromUrl(params[0]);
            } catch (IOException e) {
                Log.e(TAG, "Error fetching data from URL, loading fallback data", e);
                result = loadFallbackData("f1_current_driverStandings.xml");
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseDriverStandings(result);
                updateLastUpdateTime();

                // Notify the adapter of data changes after parsing is complete
                if (driverAdapter != null) {
                    driverAdapter.notifyDataSetChanged();
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

        private void parseDriverStandings(String xmlData) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(xmlData));

                int eventType = parser.getEventType();
                Driver currentDriver = null;
                Constructor currentConstructor = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagName.equals("DriverStanding")) {
                                currentDriver = new Driver();
                                currentDriver.setPosition(parser.getAttributeValue(null, "position"));
                                currentDriver.setPoints(parser.getAttributeValue(null, "points"));
                                currentDriver.setWins(parser.getAttributeValue(null, "wins"));
                            } else if (currentDriver != null) {
                                if (tagName.equals("GivenName")) {
                                    parser.next();
                                    currentDriver.setGivenName(parser.getText());
                                } else if (tagName.equals("FamilyName")) {
                                    parser.next();
                                    currentDriver.setFamilyName(parser.getText());
                                } else if (tagName.equals("DateOfBirth")) {
                                    parser.next();
                                    currentDriver.setDateOfBirth(parser.getText());
                                } else if (tagName.equals("Nationality") && currentConstructor == null) {  // To ensure this is Driver's nationality
                                    parser.next();
                                    currentDriver.setNationality(parser.getText());
                                } else if (tagName.equals("Constructor")) {
                                    currentConstructor = new Constructor();
                                } else if (currentConstructor != null) {
                                    if (tagName.equals("Name")) {
                                        parser.next();
                                        currentConstructor.setName(parser.getText());
                                    } else if (tagName.equals("Nationality")) {  // To ensure this is Constructor's nationality
                                        parser.next();
                                        currentConstructor.setNationality(parser.getText());
                                    }
                                }
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            if (tagName.equals("Constructor") && currentConstructor != null) {
                                currentDriver.setConstructor(currentConstructor);
                                currentConstructor = null;
                            } else if (tagName.equals("DriverStanding") && currentDriver != null) {
                                driverList.add(currentDriver);
                                currentDriver = null;
                            }
                            break;
                    }
                    eventType = parser.next();
                }

                driverAdapter.notifyDataSetChanged();
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
