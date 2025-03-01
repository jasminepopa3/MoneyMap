package com.example.moneymap;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CalendarView calendarView = findViewById(R.id.calendarView);

        // Setează ziua curentă ca fiind selectată
        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();
        calendarView.setDate(currentDate, true, true);

        // Setează un OnDateChangeListener pentru a bloca selecția zilelor
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Ignoră selecția zilei (nu face nimic)
            // Re-setăm ziua curentă ca fiind selectată
            calendarView.setDate(currentDate, true, true);
        });
    }
}
