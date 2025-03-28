package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123; // Cod pentru rezultatul autentificării
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inițializează Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurează Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ID client Firebase
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Pornește autentificarea cu Google
        findViewById(R.id.buttonGoogle).setOnClickListener(v -> signInWithGoogle());

        // Register basic
        findViewById(R.id.buttonRegister).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login basic
        findViewById(R.id.buttonLogin).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w("GoogleSignIn", "Autentificare eșuată", e);
            Toast.makeText(this, "Autentificare eșuată: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Autentificare reușită
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Autentificat ca: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        checkIfUserExists(user);

                        // După autentificare reușită, mergi către HomeActivity
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Opțional: închide activitatea curentă pentru a nu permite revenirea la ea
                    } else {
                        // Autentificare eșuată
                        Toast.makeText(this, "Autentificare eșuată", Toast.LENGTH_SHORT).show();
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
                Log.d("Culoare:",color);

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

    private void addUserAndDefaultCategories(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Adaugă documentul utilizatorului în colecția "users"
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("name", user.getDisplayName());

            db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Utilizator adăugat: " + userId);
                        // Adaugă categoriile predefinite
                        addDefaultCategories(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Eroare la adăugarea utilizatorului", e);
                    });
        }
    }

    private void checkIfUserExists(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Verifică dacă documentul utilizatorului există
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            // Dacă documentul nu există, înseamnă că utilizatorul este nou
                            // Adaugă documentul utilizatorului și categoriile predefinite
                            addUserAndDefaultCategories(user);
                        } else {
                            Log.d("Firestore", "Utilizatorul există deja. Nu se adaugă categorii predefinite.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Eroare la verificarea existenței utilizatorului", e);
                    });
        }
    }

}