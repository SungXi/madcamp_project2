package com.example.serverapp.Fragment0;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.serverapp.R;

public class AddressEdit extends AppCompatActivity {
    EditText edit_name, edit_number, edit_email;
    String name, number, email;
    int position;
    Button yes_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);

        position = getIntent().getIntExtra("position", -1);
        name = getIntent().getStringExtra("name");
        number = getIntent().getStringExtra("number");
        email = getIntent().getStringExtra("email");
        System.out.println("at Edit : " + position);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(0);
            actionBar.setTitle("Nice to meet you!");
        }

        edit_name = findViewById(R.id.edit_name);
        edit_number = findViewById(R.id.edit_number);
        edit_email = findViewById(R.id.edit_email);
        yes_button = findViewById(R.id.yes_button);

        edit_name.setText(name);
        edit_number.setText(number);
        edit_email.setText(email);

        yes_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", edit_name.getText().toString());
        resultIntent.putExtra("number", edit_number.getText().toString());
        resultIntent.putExtra("email", edit_email.getText().toString());
        resultIntent.putExtra("position", position);
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}
