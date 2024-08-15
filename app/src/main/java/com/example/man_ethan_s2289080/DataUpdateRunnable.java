//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DataUpdateRunnable implements Runnable {

    private static final String TAG = "DataUpdateRunnable";
    private static final long UPDATE_INTERVAL = 60000; // 2 hours in milliseconds (demo reduced to 10 seconds)

    private final Context context;
    private final Handler handler;

    public DataUpdateRunnable(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            // Perform data fetching and UI updating
            fetchDataAndUpdateUI();

            // Schedule the next update
            handler.postDelayed(this, UPDATE_INTERVAL);

        } catch (Exception e) {
            Log.e(TAG, "Error updating data", e);
        }
    }

    private void fetchDataAndUpdateUI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Fetch the driver standings and race schedule
                    ArrayList<Driver> drivers = fetchDriverStandings();
                    ArrayList<Race> races = fetchRaceSchedule();

                    // Update the UI on the main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Driver standings and race schedule updated (every 2 hours).");
                            Toast.makeText(context, "Data updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching data", e);
                }
            }
        }).start();
    }

    // Method to fetch driver standings
    private ArrayList<Driver> fetchDriverStandings() throws Exception {
        String urlString = "http://ergast.com/api/f1/current/driverStandings";
        String xmlData;

        try {
            xmlData = fetchDataFromUrl(urlString);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching driver standings data from URL, loading fallback data", e);
            xmlData = loadFallbackData("f1_current_driverStandings.xml");
        }

        return parseDriverStandings(xmlData);
    }

    // Method to fetch race schedule
    private ArrayList<Race> fetchRaceSchedule() throws Exception {
        String urlString = "http://ergast.com/api/f1/current";
        String xmlData;

        try {
            xmlData = fetchDataFromUrl(urlString);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching race schedule data from URL, loading fallback data", e);
            xmlData = loadFallbackData("f1_race_schedule.xml");
        }

        return parseRaceSchedule(xmlData);
    }

    // Method to fetch data from URL with timeouts
    private String fetchDataFromUrl(String urlString) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set timeouts for the connection and the read operation
        connection.setConnectTimeout(10000);  // 10 seconds to establish a connection
        connection.setReadTimeout(15000);     // 15 seconds to read data from the connection

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

    // Method to load fallback data from assets
    private String loadFallbackData(String fileName) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading fallback data", e);
        }
        return result.toString();
    }

    // Method to parse driver standings XML data
    private ArrayList<Driver> parseDriverStandings(String xmlData) throws Exception {
        ArrayList<Driver> drivers = new ArrayList<>();
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
                        } else if (tagName.equals("Nationality") && currentConstructor == null) {
                            parser.next();
                            currentDriver.setNationality(parser.getText());
                        } else if (tagName.equals("Constructor")) {
                            currentConstructor = new Constructor();
                        } else if (currentConstructor != null) {
                            if (tagName.equals("Name")) {
                                parser.next();
                                currentConstructor.setName(parser.getText());
                            } else if (tagName.equals("Nationality")) {
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
                        drivers.add(currentDriver);
                        currentDriver = null;
                    }
                    break;
            }
            eventType = parser.next();
        }

        return drivers;
    }

    // Method to parse race schedule XML data
    private ArrayList<Race> parseRaceSchedule(String xmlData) throws Exception {
        ArrayList<Race> races = new ArrayList<>();
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
                        races.add(currentRace);
                        currentRace = null;
                    }
                    break;
            }
            eventType = parser.next();
        }

        return races;
    }
}
