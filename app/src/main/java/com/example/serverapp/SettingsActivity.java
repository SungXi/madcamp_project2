package com.example.serverapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        if (null != findViewById(R.id.settings)) {
            if (null != savedInstanceState)
                return;
            getSupportFragmentManager().beginTransaction().add(R.id.settings, new SettingsFragment()).commit();
        }
    }
}
