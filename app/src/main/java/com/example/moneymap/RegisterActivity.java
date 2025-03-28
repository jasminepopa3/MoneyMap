package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private EditText emailField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //redirect to login if user has account
        TextView loginLink = findViewById(R.id.loginLinkText);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //intent to open LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                // Start the login activity
                startActivity(intent);
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Verifică dacă câmpurile sunt goale
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return; // Oprește execuția metodei dacă un câmp este gol
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Register", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        //set default avatar
                        String userId = user.getUid();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("avatarResId", R.drawable.ic_account); // Default avatar

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Default avatar set"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error setting default avatar", e));

                        addDefaultCategories(user);
                        updateUI(user);

                    } else {
                        Log.w("Register", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void addDefaultCategories(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Log.d("Firestore", "Adding default categories for user: " + userId);

            // Lista de categorii predefinite cu culori
            Map<String, String> defaultCategories = new HashMap<>();
            defaultCategories.put("Alimente", "#F7C9B6"); // peach
            defaultCategories.put("Divertisment", "#E3B23C"); // mustard yellow
            defaultCategories.put("Igiena", "#A44A3F"); // rust red

            // Obține luna și anul curent
            Calendar calendar = Calendar.getInstance();
            String currentMonth = new java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(calendar.getTime());
            String currentYear = String.valueOf(calendar.get(Calendar.YEAR));

            // Adaugă fiecare categorie în subcolecția "categories"
            for (Map.Entry<String, String> entry : defaultCategories.entrySet()) {
                String categoryName = entry.getKey();
                String color = entry.getValue();

                Map<String, Object> category = new HashMap<>();
                category.put("name", categoryName);
                category.put("description", ""); // Descriere goală pentru categorii predefinite
                category.put("color", color); // Adaugă culoarea

                Log.d("Firestore", "Adding category: " + categoryName + " with color: " + color);

                db.collection("users").document(userId).collection("categories")
                        .add(category)
                        .addOnSuccessListener(documentReference -> {
                            String categoryId = documentReference.getId();
                            Log.d("Firestore", "Categorie adăugată: " + categoryName + " with ID: " + categoryId);

                            // Adaugă un buget implicit pentru luna și anul curent
                            Map<String, Object> budget = new HashMap<>();
                            budget.put("month", currentMonth);
                            budget.put("year", currentYear);
                            budget.put("budget", 0); // Buget implicit

                            db.collection("users").document(userId).collection("categories")
                                    .document(categoryId).collection("budgets")
                                    .document(currentYear + "_" + currentMonth)
                                    .set(budget)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Buget implicit adăugat pentru categoria: " + categoryName);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Eroare la adăugarea bugetului implicit pentru categoria: " + categoryName, e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Eroare la adăugarea categoriei: " + categoryName, e);
                        });
            }
        } else {
            Log.e("Firestore", "User is null. Cannot add default categories.");
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}