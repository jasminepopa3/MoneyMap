package com.example.moneymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Typeface;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String AVATAR_KEY = "avatar_id";
    private ImageView currentAvatar;
    private Button buttonEditProfilePicture;
    private Spinner spinnerMonth, spinnerYear;
    private PieChart pieChart;
    private String selectedMonth, selectedYear;
    private Map<String, Category> categoryCache = new HashMap<>();

    private static final int AVATAR_SELECTION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentAvatar = findViewById(R.id.current_avatar);
        buttonEditProfilePicture = findViewById(R.id.button_edit_profile_picture);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        pieChart = findViewById(R.id.pie_chart);

        int savedAvatar = sharedPreferences.getInt(AVATAR_KEY, R.drawable.ic_account);
        currentAvatar.setImageResource(savedAvatar);

        // "Edit Profile Picture" button
        buttonEditProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AvatarSelectionActivity.class);
            startActivityForResult(intent, AVATAR_SELECTION_REQUEST);
        });

        // Set up spinners
        setupSpinners();

        // Load categories into cache
        loadCategoriesIntoCache();
    }

    private void setupSpinners() {
        //month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.months,
                android.R.layout.simple_spinner_item
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // year spinner
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

        Calendar calendar = Calendar.getInstance();
        int currentMonthIndex = calendar.get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonthIndex);
        int yearIndex = years.indexOf(String.valueOf(currentYear));
        if (yearIndex >= 0) {
            spinnerYear.setSelection(yearIndex);
        }

        selectedMonth = spinnerMonth.getSelectedItem().toString();
        selectedYear = spinnerYear.getSelectedItem().toString();

        //listeners
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newMonth = parent.getItemAtPosition(position).toString();
                if (!newMonth.equals(selectedMonth)) {
                    selectedMonth = newMonth;
                    updatePieChart();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newYear = parent.getItemAtPosition(position).toString();
                if (!newYear.equals(selectedYear)) {
                    selectedYear = newYear;
                    updatePieChart();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadCategoriesIntoCache() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
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

                        updatePieChart();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading categories", e);
                    });
        }
    }

    private void updatePieChart() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("expenses")
                    .whereEqualTo("month", selectedMonth)
                    .whereEqualTo("year", selectedYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Map<String, Float> categoryTotals = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String categoryId = document.getString("categoryId");
                            double sum = document.getDouble("sum");

                            if (categoryId != null && categoryCache.containsKey(categoryId)) {
                                categoryTotals.put(categoryId, (float) (categoryTotals.getOrDefault(categoryId, 0f) + sum));
                            }
                        }
                        if (categoryTotals.isEmpty()) {
                            findViewById(R.id.text_no_expenses).setVisibility(View.VISIBLE);
                            pieChart.setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.text_no_expenses).setVisibility(View.GONE);
                            pieChart.setVisibility(View.VISIBLE);

                            //cream pie chart
                            List<PieEntry> entries = new ArrayList<>();
                            List<Integer> colors = new ArrayList<>();

                            for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
                                String categoryId = entry.getKey();
                                Category category = categoryCache.get(categoryId);
                                if (category != null) {
                                    entries.add(new PieEntry(entry.getValue(), category.getName()));
                                    colors.add(Color.parseColor(category.getColor()));
                                }
                            }

                            // Set up pie chart
                            PieDataSet dataSet = new PieDataSet(entries, "");
                            dataSet.setColors(colors);
                            dataSet.setValueTextSize(25f);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setValueLinePart1OffsetPercentage(0f);
                            dataSet.setValueLinePart1Length(0f);
                            dataSet.setValueLinePart2Length(0f);
                            dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);


                            PieData data = new PieData(dataSet);
                            pieChart.setData(data);

                            // pie chart design
                            pieChart.setUsePercentValues(true);
                            pieChart.getDescription().setEnabled(false);
                            pieChart.setDrawHoleEnabled(false);
                            pieChart.setRotationAngle(0);
                            pieChart.setRotationEnabled(true);
                            pieChart.setHighlightPerTapEnabled(true);

                            // legenda
                            Legend legend = pieChart.getLegend();
                            legend.setEnabled(true);
                            legend.setTextSize(15f);
                            legend.setTextColor(Color.WHITE);
                            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
                            legend.setDrawInside(false);
                            legend.setYEntrySpace(17f);

                            pieChart.setDrawEntryLabels(false);

                            Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
                            legend.setTypeface(boldTypeface);
                            data.setValueTypeface(boldTypeface);

                            pieChart.invalidate();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading expenses", e);
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AVATAR_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            int selectedAvatar = data.getIntExtra("selectedAvatar", R.drawable.ic_account); // Default to ic_account
            currentAvatar.setImageResource(selectedAvatar);
            sharedPreferences.edit().putInt(AVATAR_KEY, selectedAvatar).apply();
            ToolbarUtils.updateToolbarIcon(this, selectedAvatar);
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .update("avatarResId", selectedAvatar);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}