package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private double totalExpenses = 0.0;
    private double totalBudget = 0.0;

    private int currentMonthIndex;
    private int currentYear;

    private ProgressBar loadingBar;
    private int loadingCounter = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

        loadingBar = findViewById(R.id.loadingBar);
        CalendarView calendarView = findViewById(R.id.calendarView);

        Calendar calendar = Calendar.getInstance();
        currentMonthIndex = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            if (month != currentMonthIndex || year != currentYear) {
                currentMonthIndex = month;
                currentYear = year;
                fetchTotalExpenses();
                fetchUserBudget();
            }
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

        // total cheltuieli
        fetchTotalExpenses();
        // bugetul
        fetchUserBudget();
    }

    private void fetchTotalExpenses() {
        loadingBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String[] monthsArray = getResources().getStringArray(R.array.months);
            String currentMonth = monthsArray[currentMonthIndex];
            String currentYearStr = String.valueOf(currentYear);

            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("month", currentMonth)
                    .whereEqualTo("year", currentYearStr)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        totalExpenses = 0.0;

                        // suma cheltuieli
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Double sum = document.getDouble("sum");
                            if (sum != null) {
                                totalExpenses += sum;
                            }
                        }

                        checkLoadingComplete();
                        updateRatioUI();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeActivity", "Error loading expenses", e);
                        Toast.makeText(this, "Eroare la încărcarea cheltuielilor", Toast.LENGTH_SHORT).show();

                    });
        } else {
            Toast.makeText(this, "User-ul nu este logat", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserBudget() {
        loadingBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Category> categories = new ArrayList<>(); // Listă locală pentru categorii
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String color = document.getString("color");

                            if (name != null && color != null) {
                                Category category = new Category(id, name, description, color);
                                categories.add(category);
                            }
                        }

                        //bugetele pentru toate categoriile
                        loadBudgetsForCategories(categories);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadBudgetsForCategories(List<Category> categories) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String[] monthsArray = getResources().getStringArray(R.array.months);
            String currentMonth = monthsArray[currentMonthIndex];
            String currentYearStr = String.valueOf(currentYear);

            totalBudget=0.0;
            for (Category category : categories) {
                db.collection("users").document(userId).collection("categories")
                        .document(category.getId()).collection("budgets")
                        .document(currentYearStr + "_" + currentMonth)
                        .get()
                        .addOnSuccessListener(budgetDocument -> {
                            if (budgetDocument.exists()) {
                                Double budget = budgetDocument.getDouble("budget");
                                if (budget != null) {
                                    totalBudget += budget;
                                }
                            }
                            checkLoadingComplete();
                            updateRatioUI();

                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error loading budget", e);
                        });
            }
        }
    }

    //verificam daca au fost calculate ambele valori
    private void checkLoadingComplete() {
        loadingCounter--;
        if (loadingCounter == 0) {
            loadingBar.setVisibility(View.GONE);
            updateRatioUI();
            loadingCounter = 2;
        }
    }

    // functie pentru update UI
    private void updateRatioUI() {
        TextView textExpensesBudget = findViewById(R.id.textExpensesBudget);
        if (textExpensesBudget != null) {
            String formattedRatio = String.format("%.2f/%.2f RON", totalExpenses, totalBudget);

            textExpensesBudget.setText(formattedRatio);
        }
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
