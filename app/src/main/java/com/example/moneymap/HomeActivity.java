package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        Log.d("HomeActivity", "onCreateOptionsMenu called");
        ToolbarUtils.loadToolbarIcon(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // Profile click
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Logout click
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    //refresh meniu
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("HomeActivity", "onResume called");
        invalidateOptionsMenu();
    }
}
