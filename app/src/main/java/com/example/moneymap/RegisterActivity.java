package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
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
            defaultCategories.put("Alimente", "#FF5733"); // Orange
            defaultCategories.put("Divertisment", "#33FF57"); // Green
            defaultCategories.put("Igiena", "#3357FF"); // Blue

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
                            Log.d("Firestore", "Categorie adăugată: " + categoryName + " with ID: " + documentReference.getId());
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