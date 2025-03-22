package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
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
    private TextView textCategoryTitle, textEmptyState;

    private CalendarView calendarView;
    private Button buttonShowMonthExpenses;
    private String selectedDay;
    private String selectedMonth;
    private String selectedYear;
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
        textEmptyState = findViewById(R.id.text_empty_state);
        calendarView = findViewById(R.id.calendarView);
        buttonShowMonthExpenses = findViewById(R.id.button_show_month_expenses);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryName = getIntent().getStringExtra("categoryName");
        categoryId = getIntent().getStringExtra("categoryId");

        //initializam data
        Calendar calendar = Calendar.getInstance();
        selectedDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        selectedMonth = getRomanianMonthName(calendar.get(Calendar.MONTH) + 1);
        selectedYear = String.valueOf(calendar.get(Calendar.YEAR));


        textCategoryTitle.setText("Cheltuieli pentru " + categoryName + " - " + selectedDay + " " + selectedMonth + " " + selectedYear);

        // calendar
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // update pentru selectiile user-ului
            selectedDay = String.valueOf(dayOfMonth);
            selectedMonth = getRomanianMonthName(month + 1);
            selectedYear = String.valueOf(year);

            textCategoryTitle.setText("Cheltuieli pentru " + categoryName + " - " + selectedDay + " " + selectedMonth + " " + selectedYear);
            fetchExpensesForCategory();
        });

        // Initialize adapter
        expenseList = new ArrayList<>();
        adapter = new ExpenseListAdapter(expenseList, this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(position -> {
            Expense expenseToDelete = expenseList.get(position);
            deleteExpense(expenseToDelete, position);
        });

        // buton expenses pentru toata luna
        buttonShowMonthExpenses.setOnClickListener(v -> fetchExpensesForMonth());

        fetchExpensesForCategory();
    }

    // numele de luni in romana
    private String getRomanianMonthName(int month) {
        String[] romanianMonths = {
                "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
                "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
        };
        return romanianMonths[month - 1];
    }

    private void fetchExpensesForCategory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // fetch expenses din firebase
            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("categoryId", categoryId)
                    .whereEqualTo("day", selectedDay)
                    .whereEqualTo("month", selectedMonth) // Use Romanian month name
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {


                        List<Expense> updatedExpenseList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            double sum = document.getDouble("sum");
                            String day = document.getString("day");
                            String month = document.getString("month");
                            String year = document.getString("year");

                            updatedExpenseList.add(new Expense(name, sum, day, month, year));
                        }

                        // Update adapter
                        adapter.updateExpenses(updatedExpenseList);
                        adapter.notifyDataSetChanged();

                        // verificam daca avem cheltuieli pentru ziua selctata
                        if (updatedExpenseList.isEmpty()) {
                            textEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            textEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Eroare la incarcarea cheltuielilor", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User-ul nu e autentificat", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchExpensesForMonth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            // fetch expenses din firebase
            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("categoryId", categoryId)
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Debugging: Log the number of documents fetched
                        Log.d("ExpenseDetails", "Number of expenses fetched: " + queryDocumentSnapshots.size());


                        textCategoryTitle.setText("Cheltuieli pentru " + categoryName + " - "+ selectedMonth + " " + selectedYear);

                        List<Expense> updatedExpenseList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            double sum = document.getDouble("sum");
                            String day = document.getString("day");
                            String month = document.getString("month");
                            String year = document.getString("year");

                            updatedExpenseList.add(new Expense(name, sum, day, month, year));
                        }

                        //update adapter
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
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(this, "Eroare la incarcarea cheltuielilor", Toast.LENGTH_SHORT).show();
                    });
        } else {

            Toast.makeText(this, "User-ul nu e autentificat", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteExpense(Expense expense, int position) {
        // Pop-up pentru delete
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

    private void proceedWithDeletion(Expense expense, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("name", expense.getName())
                    .whereEqualTo("sum", expense.getSum())
                    .whereEqualTo("day", expense.getDay())
                    .whereEqualTo("month", expense.getMonth())
                    .whereEqualTo("year", expense.getYear())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            db.collection("users").document(userId).collection("expenses")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // stergem cheltuiala din lista si update adapter
                                        expenseList.remove(position);
                                        adapter.notifyItemRemoved(position);

                                        Toast.makeText(this, "Cheltuiala stearsa", Toast.LENGTH_SHORT).show();

                                        if (expenseList.isEmpty()) {
                                            textEmptyState.setVisibility(View.VISIBLE);
                                            recyclerView.setVisibility(View.GONE);
                                        }

                                        Intent resultIntent = new Intent();
                                        setResult(RESULT_OK, resultIntent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Eroare la stergerea cheltuielii", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Cheltuiala selectata nu exista in baza de date", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Meniu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d("ExpenseActivity", "onCreateOptionsMenu called");
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
        Log.d("ExpenseActivity", "onResume called");
        invalidateOptionsMenu();
    }
}