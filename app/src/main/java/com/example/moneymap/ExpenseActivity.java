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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpenseActivity extends AppCompatActivity {
    private Spinner spinnerMonth, spinnerYear;
    private ProgressBar progressBar;
    private TextView textExpenses;
    private Button buttonAddExpense;
    private List<GroupedExpense> groupedExpensesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private String selectedMonth, selectedYear;
    private Map<String, Category> categoryCache = new HashMap<>();



    private ActivityResultLauncher<Intent> addExpenseLauncher;
    private ActivityResultLauncher<Intent> expenseDetailsLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

        // views
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        textExpenses = findViewById(R.id.text_expenses);
        buttonAddExpense = findViewById(R.id.button_add_expense);
        progressBar = findViewById(R.id.progress_bar);


        // launcher pt add expense
        addExpenseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        updateExpenses();
                    }
                });

        // launcher pt expense details
        expenseDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        updateExpenses();
                    }
                });

        // spinner pentru luni
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        //generarea anilor
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear - 2; year <= currentYear + 5; year++) {
            years.add(String.valueOf(year));
        }

        // spinner pentru ani
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // valori default pentru luna si an
        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonthIndex);

        // index-ul anului curent
        int yearIndex = years.indexOf(String.valueOf(currentYear));
        if (yearIndex >= 0) {
            spinnerYear.setSelection(yearIndex);
        }

        selectedMonth = spinnerMonth.getSelectedItem().toString();
        selectedYear = spinnerYear.getSelectedItem().toString();

        // RecyclerView si Adapter
        recyclerView = findViewById(R.id.recycler_view_expenses);
        adapter = new ExpenseAdapter(groupedExpensesList, this, selectedMonth, selectedYear, expenseDetailsLauncher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Listener pt selectarea lunii
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newMonth = parent.getItemAtPosition(position).toString();
                if (!newMonth.equals(selectedMonth)) {
                    selectedMonth = newMonth;
                    updateExpenses();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nimic
            }
        });

        // Listener pt selectarea anului
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newYear = parent.getItemAtPosition(position).toString();
                if (!newYear.equals(selectedYear)) {
                    selectedYear = newYear;
                    updateExpenses();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nimic
            }
        });

        // Add Expense Button
        buttonAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseActivity.this, AddExpenseActivity.class);
            intent.putExtra("month", selectedMonth);
            intent.putExtra("year", selectedYear);
            addExpenseLauncher.launch(intent);
        });

        // incarcam categoriile
        loadCategoriesIntoCache();
    }


    private void loadCategoriesIntoCache() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // loading bar
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textExpenses.setVisibility(View.GONE);

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
                        //incarcam expenses cand am incarcat categoriile
                        updateExpenses();
                    })
                    .addOnFailureListener(e -> {

                        Log.e("Firestore", "Error loading categories", e);
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
                        //hide loading bar
                        progressBar.setVisibility(View.GONE);
                        textExpenses.setVisibility(View.VISIBLE);
                        textExpenses.setText("Eroare la incarcarea categoriilor");
                    });
        }
    }

    private void updateExpenses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            //loading bar
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            textExpenses.setVisibility(View.GONE);

            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("Firestore", "Expenses fetched: " + queryDocumentSnapshots.size());

                        if (queryDocumentSnapshots.isEmpty()) {
                            //ascundem loading bar-ul daca nu avem cheltuieli pt data selectata
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            textExpenses.setVisibility(View.VISIBLE);
                            textExpenses.setText("Nicio cheltuiala pentru " + selectedMonth + " " + selectedYear);
                            groupedExpensesList.clear();
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        //folosim map pentru a grupa cheltuielile pe categorii
                        Map<String, GroupedExpense> groupedExpenses = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String categoryId = document.getString("categoryId");
                            double sum = document.getDouble("sum");
                            String name = document.getString("name");
                            String day=document.getString("day");

                            if (categoryId != null) {
                                Category category = categoryCache.get(categoryId);
                                if (category != null) {
                                    // facem un Expense obj
                                    Expense expense = new Expense(name, sum, day,selectedMonth, selectedYear);

                                    //daca e o categorie care inca nu apare in map facem un nou GroupedExpense obj
                                    //folosind id-ul categ
                                    if (!groupedExpenses.containsKey(categoryId)) {
                                        GroupedExpense groupedExpense = new GroupedExpense(category);
                                        groupedExpense.addExpense(expense);
                                        groupedExpenses.put(categoryId, groupedExpense);
                                    } else {
                                        //daca deja categ exista in map doar adaugam expense
                                        groupedExpenses.get(categoryId).addExpense(expense);
                                    }
                                }
                            }
                        }

                        groupedExpensesList.clear();
                        groupedExpensesList.addAll(groupedExpenses.values());
                        adapter.updateSelectedDate(selectedMonth, selectedYear); // Update cu luna si anul selectat
                        adapter.notifyDataSetChanged();

                        //hide loading bar
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        textExpenses.setVisibility(View.VISIBLE);
                        textExpenses.setText("Cheltuieli pentru " + selectedMonth + " " + selectedYear);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading expenses", e);
                        textExpenses.setText("Eroare la afisarea cheltuielilor");
                        //hide loading bar
                        progressBar.setVisibility(View.GONE);
                        textExpenses.setVisibility(View.VISIBLE);
                    });
        } else {
            textExpenses.setText("User-ul nu este logat");
            //hide loading bar
            progressBar.setVisibility(View.GONE);
            textExpenses.setVisibility(View.VISIBLE);
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
            //profile clicked
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            //Logout click
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