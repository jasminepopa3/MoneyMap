package com.example.moneymap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextName, editTextSum;
    private Button buttonSaveExpense, buttonCancel;
    private List<Category> categories = new ArrayList<>();

    private String selectedMonth;
    private String selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Retrieve the selected month and year from the Intent
        Intent intent = getIntent();
        selectedMonth = intent.getStringExtra("month");
        selectedYear = intent.getStringExtra("year");

        // Initialize views
        spinnerCategory = findViewById(R.id.spinner_category);
        editTextName = findViewById(R.id.editText_name);
        editTextSum = findViewById(R.id.editText_sum);
        buttonSaveExpense = findViewById(R.id.button_save_expense);
        buttonCancel = findViewById(R.id.button_cancel);

        // Load categories from Firebase
        loadCategories();

        // Set save expense button click listener
        buttonSaveExpense.setOnClickListener(v -> saveExpense());

        // Set cancel button click listener
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadCategories() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d("Auth", "Authenticated user: " + userId);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("Firestore", "Number of categories fetched: " + queryDocumentSnapshots.size());
                        categories.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String description = document.getString("description");
                            String color = document.getString("color");

                            if (name != null && color != null) {
                                categories.add(new Category(id, name, description, color));
                                Log.d("Firestore", "Category fetched: " + name + " with ID: " + id);
                            }
                        }

                        // Set the category adapter
                        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
                        spinnerCategory.setAdapter(categoryAdapter);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Auth", "No user is authenticated");
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveExpense() {
        String name = editTextName.getText().toString().trim();
        String sumText = editTextSum.getText().toString().trim();
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

        // Validate inputs
        if (name.isEmpty() || sumText.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Float sum;
        try {
            sum = Float.parseFloat(sumText);
            if (sum <= 0) {
                Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a map for the expense with proper field names including categoryId
            Map<String, Object> expense = new HashMap<>();
            expense.put("categoryId", selectedCategory.getId());
            expense.put("name", name);
            expense.put("sum", sum);
            expense.put("month", selectedMonth);
            expense.put("year", selectedYear);
            expense.put("timestamp", System.currentTimeMillis());

            // Save the expense in Firestore
            db.collection("users").document(userId).collection("expenses")
                    .add(expense)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Expense added successfully with ID: " + documentReference.getId());
                        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();

                        // Create an Expense object to return
                        Expense newExpense = new Expense(selectedCategory, name, sum, selectedMonth, selectedYear);

                        // Pass the expense object back to ExpenseActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newExpense", newExpense);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding expense", e);
                        Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}