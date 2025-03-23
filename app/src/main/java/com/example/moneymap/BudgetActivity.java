package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity implements CategoryBudgetAdapter.OnBudgetSetListener {

    private RecyclerView recyclerViewCategories;
    private CategoryBudgetAdapter categoryBudgetAdapter;
    private List<Category> categories = new ArrayList<>();
    private Spinner spinnerMonth, spinnerYear;
    private String selectedMonth, selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

        // Referințe la elementele din layout
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);

        // Configurează Spinner-ul pentru luni
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Generează lista de ani dinamic
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear; year <= 2050; year++) {
            years.add(String.valueOf(year));
        }

        // Configurează Spinner-ul pentru ani
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Setează valorile implicite pentru lună și an
        selectedMonth = spinnerMonth.getSelectedItem().toString();
        selectedYear = spinnerYear.getSelectedItem().toString();

        // Configurează RecyclerView
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryBudgetAdapter = new CategoryBudgetAdapter(categories, this);
        recyclerViewCategories.setAdapter(categoryBudgetAdapter);

        // Încarcă categoriile din Firestore
        loadCategoriesWithBudget();

        // Listener pentru Spinner-ul de luni
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = parent.getItemAtPosition(position).toString();
                loadCategoriesWithBudget(); // Reîncarcă categoriile cu bugetul pentru luna selectată
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nu face nimic
            }
        });

        // Listener pentru Spinner-ul de ani
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                loadCategoriesWithBudget(); // Reîncarcă categoriile cu bugetul pentru anul selectat
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nu face nimic
            }
        });
    }

    @Override
    public void onBudgetSet(Category category, double budget) {
        // Salvează bugetul în Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Creează un map cu bugetul pentru luna și anul selectat
            Map<String, Object> budgetData = new HashMap<>();
            budgetData.put("month", selectedMonth);
            budgetData.put("year", selectedYear);
            budgetData.put("budget", budget);

            // Salvează bugetul în subcolecția "budgets" a categoriei
            db.collection("users").document(userId).collection("categories")
                    .document(category.getId()).collection("budgets")
                    .document(selectedYear + "_" + selectedMonth)
                    .set(budgetData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                        loadCategoriesWithBudget(); // Reîncarcă categoriile
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving budget", e);
                        Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadCategoriesWithBudget() {
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

                        // Încarcă bugetele pentru toate categoriile
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

            for (Category category : categories) {
                db.collection("users").document(userId).collection("categories")
                        .document(category.getId()).collection("budgets")
                        .document(selectedYear + "_" + selectedMonth)
                        .get()
                        .addOnSuccessListener(budgetDocument -> {
                            if (budgetDocument.exists()) {
                                // Bugetul există, încărcați valoarea
                                double budget = budgetDocument.getDouble("budget");
                                category.setBudget(budget);
                            } else {
                                // Bugetul nu există, setați valoarea implicită la 0
                                category.setBudget(0);
                            }

                            // Actualizați adapterul după ce toate bugetele sunt încărcate
                            categoryBudgetAdapter.updateCategories(categories);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error loading budget", e);
                        });
            }
        }
    }

    //meniu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d("ExpenseActivity", "onCreateOptionsMenu called");
        ToolbarUtils.loadToolbarIcon(this, menu); // Load the avatar icon
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // profile click
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
        Log.d("ExpenseActivity", "onResume called");
        invalidateOptionsMenu();
    }
}