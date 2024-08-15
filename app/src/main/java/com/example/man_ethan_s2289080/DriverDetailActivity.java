//
// Name                 Ethan Man
// Student ID           S2289080
// Programme of Study   BSc Computing
//

package com.example.man_ethan_s2289080;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DriverDetailActivity extends AppCompatActivity {

    private TextView driverNameTextView;
    private TextView driverDetailsTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);

        driverNameTextView = findViewById(R.id.driverNameTextView);
        driverDetailsTextView = findViewById(R.id.driverDetailsTextView);

        // Enable the back button in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Driver selectedDriver = (Driver) getIntent().getSerializableExtra("driver");

        if (selectedDriver != null) {
            driverNameTextView.setText(selectedDriver.getGivenName() + " " + selectedDriver.getFamilyName());

            // Access the constructor object
            Constructor constructor = selectedDriver.getConstructor();
            String constructorInfo = (constructor != null) ? constructor.toString() : "Unknown";

            driverDetailsTextView.setText(
                    "Date of Birth: " + selectedDriver.getDateOfBirth() + "\n" +
                            "Nationality: " + selectedDriver.getNationality() + "\n" +
                            "Constructor: " + constructorInfo
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


