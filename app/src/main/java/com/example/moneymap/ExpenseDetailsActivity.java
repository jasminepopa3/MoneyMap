package com.example.moneymap;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseListAdapter adapter;
    private List<Expense> expenseList;
    private String categoryName;
    private TextView textCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        recyclerView = findViewById(R.id.recycler_view_expenses);
        textCategoryTitle = findViewById(R.id.text_category_title);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryName = getIntent().getStringExtra("categoryName");
        expenseList = (List<Expense>) getIntent().getSerializableExtra("expenseList");

        if (expenseList != null) {
            for (Expense expense : expenseList) {
                Log.d("ExpenseDetails", "Cheltuieli: " + expense.getName() + " - " + expense.getSum());
            }
        }

        textCategoryTitle.setText("Cheltuieli pentru " + categoryName);

        adapter = new ExpenseListAdapter(expenseList, this);
        recyclerView.setAdapter(adapter);
    }
}
