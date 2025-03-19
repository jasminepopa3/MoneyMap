package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class ExpenseDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> expenseList;
    private String categoryName;
    private TextView textCategoryTitle,textEmptyState;

    private String selectedMonth;
    private String selectedYear;
    private Spinner spinnerMonth, spinnerYear;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ToolbarUtils.setupToolbar(this, toolbar);

        recyclerView = findViewById(R.id.recycler_view_expenses);
        textCategoryTitle = findViewById(R.id.text_category_title);
        textEmptyState=findViewById(R.id.text_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);

        categoryName = getIntent().getStringExtra("categoryName");
        categoryId=getIntent().getStringExtra("categoryId");
        expenseList = (List<Expense>) getIntent().getSerializableExtra("expenseList");
        selectedMonth=getIntent().getStringExtra("selectedMonth");
        selectedYear=getIntent().getStringExtra("selectedYear");

        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // spinner pentru luna si an
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYear - 2; year <= currentYear + 5; year++) {
            years.add(String.valueOf(year));
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        if (selectedMonth != null) {
            int monthIndex = monthAdapter.getPosition(selectedMonth);
            spinnerMonth.setSelection(monthIndex);
        }

        if (selectedYear != null) {
            int yearIndex = yearAdapter.getPosition(selectedYear);
            spinnerYear.setSelection(yearIndex);
        }

        if (expenseList != null) {
            for (Expense expense : expenseList) {
                Log.d("ExpenseDetails", "Cheltuieli: " + expense.getName() + " - " + expense.getSum());
            }
        }

        textCategoryTitle.setText("Cheltuieli pentru " + categoryName+ " - "+selectedMonth+" "+selectedYear);

        adapter = new ExpenseListAdapter(expenseList, this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(position -> {
            Expense expenseToDelete = expenseList.get(position);
            deleteExpense(expenseToDelete, position);
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newMonth = parent.getItemAtPosition(position).toString();
                if (!newMonth.equals(selectedMonth)) {
                    selectedMonth = newMonth;
                    fetchExpensesForCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nimic
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newYear = parent.getItemAtPosition(position).toString();
                if (!newYear.equals(selectedYear)) {
                    selectedYear = newYear;
                    fetchExpensesForCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nimic
            }
        });
    }

    private void fetchExpensesForCategory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // fretch la expenses dupa categoryId si luna/anul selectat de user
            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("categoryId", categoryId)
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Expense> updatedExpenseList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            double sum = document.getDouble("sum");
                            String month = document.getString("month");
                            String year = document.getString("year");
                            String categoryId = document.getString("categoryId");
                            updatedExpenseList.add(new Expense(name, sum, month, year));
                        }

                        // update la adapter cu noua lista de expenses
                        adapter.updateExpenses(updatedExpenseList);
                        adapter.notifyDataSetChanged();

                        // verificam daca lista e goala
                        if (updatedExpenseList.isEmpty()) {
                            textEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            textEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }


                        // update la titlu pentru schimbarea lunii/anului
                        textCategoryTitle.setText("Cheltuieli pentru " + categoryName + " - " + selectedMonth + " " + selectedYear);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching expenses", e);
                        Toast.makeText(this, "Eroare la incarcarea cheltuielilor", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteExpense(Expense expense, int position) {
        //pop-up pentru confirmarea stergerii
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirma stergerea cheltuielii");
        builder.setMessage("Esti sigur ca vrei sa stergi cheltuiala inregistrata?\n\n" +
                expense.getName() + ": " + expense.getSum() + " RON");

        builder.setPositiveButton("Sterge", (dialog, id) -> {
            proceedWithDeletion(expense, position);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            dialog.dismiss();
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    // delete cheltuiala
    private void proceedWithDeletion(Expense expense, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("name", expense.getName())
                    .whereEqualTo("sum", expense.getSum())
                    .whereEqualTo("month", expense.getMonth())
                    .whereEqualTo("year", expense.getYear())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("users").document(userId).collection("expenses")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // stergem ob din lista si update adapter
                                        expenseList.remove(position);
                                        adapter.notifyItemRemoved(position);

                                        Toast.makeText(this, "Cheltuiala stearsa", Toast.LENGTH_SHORT).show();

                                        // daca grouped expense devine goala dam update
                                        if (expenseList.isEmpty()) {
                                            textEmptyState.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }

                                        Intent resultIntent = new Intent();
                                        setResult(RESULT_OK, resultIntent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Eroare la stergerea cheltuielii", e);
                                        Toast.makeText(this, "Eroare la stergerea cheltuielii", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Cheltuiala selectata nu exista in baza de date", e);
                        Toast.makeText(this, "Cheltuiala selectata nu exista in baza de date", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    //meniu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            //Profile click
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
}