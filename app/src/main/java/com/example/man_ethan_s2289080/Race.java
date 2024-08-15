//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Race implements Serializable {
    private String raceName;
    private String circuitName;
    private String locality;
    private String country;
    private String date;
    private String time;
    private int round;
    private double latitude;
    private double longitude;

    // Getters and setters
    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getRaceDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date raceDate = dateFormat.parse(this.date + " " + this.time);
            Log.d("Race", "Parsed date for " + raceName + ": " + raceDate);
            return raceDate;
        } catch (ParseException e) {
            Log.e("Race", "Error parsing date for " + raceName + ": " + this.date + " " + this.time, e);
            return null;
        }
    }

    public String getTimeWithUTC() {
        if (this.time != null && this.time.endsWith("Z")) {
            return this.time.replace("Z", " UTC");
        }
        return this.time;
    }

    public boolean isPastRace() {
        Date raceDate = getRaceDate();
        return raceDate != null && raceDate.before(new Date());
    }
}