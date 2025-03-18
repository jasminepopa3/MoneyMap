package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpenseActivity extends AppCompatActivity {
    private Spinner spinnerMonth, spinnerYear;
    private TextView textExpenses, textTotalExpenses;
    private Button buttonAddExpense;
    private List<Expense> expensesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private String selectedMonth, selectedYear;
    private Map<String, Category> categoryCache = new HashMap<>(); // Cache for categories

    // Declare the launcher for handling the result of AddExpenseActivity
    private ActivityResultLauncher<Intent> addExpenseLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Initialize Views
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        textExpenses = findViewById(R.id.text_expenses);
        buttonAddExpense = findViewById(R.id.button_add_expense);

        // Initialize the RecyclerView and its Adapter
        recyclerView = findViewById(R.id.recycler_view_expenses);
        adapter = new ExpenseAdapter(expensesList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize the activity result launcher
        addExpenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Handle the result if the expense was added successfully
                        Intent data = result.getData();
                        if (data != null) {
                            Expense newExpense = (Expense) data.getSerializableExtra("newExpense");
                            if (newExpense != null) {
                                expensesList.add(newExpense);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        // Configure Spinner for months
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Generate years dynamically (similar to BudgetActivity)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear - 2; year <= currentYear + 5; year++) {
            years.add(String.valueOf(year));
        }

        // Configure Spinner for years
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Set default values for month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonthIndex);

        // Find the index of current year in our years list
        int yearIndex = years.indexOf(String.valueOf(currentYear));
        if (yearIndex >= 0) {
            spinnerYear.setSelection(yearIndex);
        }

        selectedMonth = spinnerMonth.getSelectedItem().toString();
        selectedYear = spinnerYear.getSelectedItem().toString();

        // Listener for Month selection
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newMonth = parent.getItemAtPosition(position).toString();
                if (!newMonth.equals(selectedMonth)) { // Ensure it's a new selection
                    selectedMonth = newMonth;
                    updateExpenses();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

// Listener for Year selection
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newYear = parent.getItemAtPosition(position).toString();
                if (!newYear.equals(selectedYear)) { // Ensure it's a new selection
                    selectedYear = newYear;
                    updateExpenses();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Add Expense Button Click
        buttonAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseActivity.this, AddExpenseActivity.class);
            intent.putExtra("month", selectedMonth);
            intent.putExtra("year", selectedYear);
            addExpenseLauncher.launch(intent);
        });

        // Load all categories into cache first
        loadCategoriesIntoCache();
    }


    private void loadCategoriesIntoCache() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        categoryCache.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String color = document.getString("color");

                            if (name != null && color != null) {
                                Category category = new Category(id, name, description, color);
                                categoryCache.put(id, category);
                            }
                        }
                        // Now that we have all categories cached, we can load expenses
                        updateExpenses();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateExpenses() {
        textExpenses.setText("Loading expenses for " + selectedMonth + " " + selectedYear + "...");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("Firestore", "Expenses fetched: " + queryDocumentSnapshots.size());

                        // Check if there are no expenses
                        if (queryDocumentSnapshots.isEmpty()) {
                            textExpenses.setText("No expenses for " + selectedMonth + " " + selectedYear);
                            expensesList.clear();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        Set<String> expenseIds = new HashSet<>(); // Track added expense IDs

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String expenseId = document.getId(); // Get unique ID
                            if (expenseIds.contains(expenseId)) {
                                continue; // Skip duplicate entry
                            }
                            expenseIds.add(expenseId); // Mark as added

                            String categoryId = document.getString("categoryId");
                            String name = document.getString("name");
                            double sum = document.getDouble("sum");

                            if (name != null && categoryId != null) {
                                // Get category from cache
                                Category category = categoryCache.get(categoryId);
                                if (category != null) {
                                    Expense expense = new Expense(category, name, (float) sum, selectedMonth, selectedYear);
                                    expensesList.add(expense);
                                }
                            }
                        }

                        // Update UI after all expenses are processed
                        adapter.notifyDataSetChanged();
                        textExpenses.setText("Expenses for " + selectedMonth + " " + selectedYear);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading expenses", e);
                        textExpenses.setText("Error loading expenses");
                    });
        } else {
            textExpenses.setText("User is not authenticated");
        }
    }


//    private void updateTotalExpenses() {
//        float total = 0;
//        for (Expense expense : expensesList) {
//            total += expense.getSum();
//        }
//        textTotalExpenses.setText(String.format("Total: $%.2f", total));
//    }
}