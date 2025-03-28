package com.example.moneymap;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddExpenseActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextName, editTextSum;
    private Button buttonSaveExpense, buttonCancel;
    private CalendarView calendarView;
    private List<Category> categories = new ArrayList<>();

    private String selectedMonth;
    private String selectedYear;
    private String selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        Intent intent = getIntent();
        selectedMonth = intent.getStringExtra("month");
        selectedYear = intent.getStringExtra("year");

        String[] monthsArray = getResources().getStringArray(R.array.months);

        int monthIndex = -1;
        for (int i = 0; i < monthsArray.length; i++) {
            if (monthsArray[i].equals(selectedMonth)) {
                monthIndex = i;
                break;
            }
        }

        Calendar calendar = Calendar.getInstance();

        if (monthIndex != -1) {
            calendar.set(Calendar.MONTH, monthIndex);
            calendar.set(Calendar.YEAR, Integer.parseInt(selectedYear));
        } else {
            Log.e("AddExpenseActivity", "Luna selectată nu a fost găsită în array-ul de luni");
            calendar.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        }

        calendarView = findViewById(R.id.calendarView);
        calendarView.setDate(calendar.getTimeInMillis(), false, true);



        // Initialize views
        spinnerCategory = findViewById(R.id.spinner_category);
        editTextName = findViewById(R.id.editText_name);
        editTextSum = findViewById(R.id.editText_sum);
        buttonSaveExpense = findViewById(R.id.button_save_expense);
        buttonCancel = findViewById(R.id.button_cancel);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDay = String.valueOf(dayOfMonth);
            selectedMonth = getRomanianMonthName(month + 1);
            selectedYear = String.valueOf(year);
        });

        // Load categories
        loadCategories();

        buttonSaveExpense.setOnClickListener(v -> saveExpense());
        buttonCancel.setOnClickListener(v -> finish());
    }

    private String getRomanianMonthName(int month) {
        String[] romanianMonths = {
                "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
                "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
        };
        return romanianMonths[month - 1];
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

                        //dummy
                        categories.add(new Category("", "Alege o categorie", "", ""));

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

                        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
                        spinnerCategory.setAdapter(categoryAdapter);
                        spinnerCategory.setSelection(0);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Eroare la incarcarea categoriilor", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Auth", "No user is authenticated");
            Toast.makeText(this, "User-ul nu e autentificat", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveExpense() {
        String name = editTextName.getText().toString().trim();
        String sumText = editTextSum.getText().toString().trim();
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

        // Validate input
        if (name.isEmpty() || sumText.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Completati toate campurile", Toast.LENGTH_SHORT).show();
            return;
        }
        if(selectedCategory==spinnerCategory.getItemAtPosition(0))
        {
            Toast.makeText(this, "Alegeti o categorie valida!", Toast.LENGTH_SHORT).show();
            return;
        }

        Float sum;
        try {
            sum = Float.parseFloat(sumText);
            if (sum <= 0) {
                Toast.makeText(this, "Suma trebuie sa fie mai mare ca 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Suma invalida", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // mapam variabilele cu atr din db
            Map<String, Object> expense = new HashMap<>();
            expense.put("categoryId", selectedCategory.getId());
            expense.put("name", name);
            expense.put("sum", sum);
            expense.put("day", selectedDay);
            expense.put("month", selectedMonth);
            expense.put("year", selectedYear);
            expense.put("timestamp", System.currentTimeMillis());

            // salvam expense in db
            db.collection("users").document(userId).collection("expenses")
                    .add(expense)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Expense added successfully with ID: " + documentReference.getId());
                        Toast.makeText(this, "Cheltuiala a fost adaugata cu succes", Toast.LENGTH_SHORT).show();

                        // pasam obiectul la ExpenseActivity
                        Expense newExpense = new Expense(name, sum, selectedDay, selectedMonth, selectedYear);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("newExpense", newExpense);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding expense", e);
                        Toast.makeText(this, "Cheltuiala nu a putut fi adaugata", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User-ul nu e autentificat", Toast.LENGTH_SHORT).show();
        }
    }
}