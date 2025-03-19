package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

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

        // Găsește butonul "CATEGORII" și setează un OnClickListener
        Button buttonCategories = findViewById(R.id.buttonCategories);
        buttonCategories.setOnClickListener(v -> {
            // Creează un intent pentru a naviga către CategoryActivity
            Intent intent = new Intent(HomeActivity.this, CategoryActivity.class);
            startActivity(intent);
        });

        // Găsește butonul "BUGET" și setează un OnClickListener
        Button buttonBudget = findViewById(R.id.buttonBudget);
        buttonBudget.setOnClickListener(v -> {
            // Creează un intent pentru a naviga către CategoryActivity
            Intent intent = new Intent(HomeActivity.this, BudgetActivity.class);
            startActivity(intent);
        });

        //butonul de cheltuieli
        Button buttonExpenses = findViewById(R.id.buttonExpenses); // Get the button
        buttonExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ExpenseActivity.class);
            startActivity(intent);
        });
    }

    //meniu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (ToolbarUtils.handleOptionsItemSelected(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
