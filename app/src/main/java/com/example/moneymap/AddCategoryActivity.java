package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText editTextCategoryName;
    private EditText editTextCategoryDescription;
    private Button buttonSaveCategory;
    private Button[] colorButtons; // Array to hold color buttons
    private String selectedColor = "#FF5733"; // Default color (Orange)
    private Button lastSelectedButton; // Track the last selected button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Referințe la elementele din layout
        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        editTextCategoryDescription = findViewById(R.id.editTextCategoryDescription);
        buttonSaveCategory = findViewById(R.id.buttonSaveCategory);

        // Initialize color buttons
        colorButtons = new Button[]{
                findViewById(R.id.buttonColor1),
                findViewById(R.id.buttonColor2),
                findViewById(R.id.buttonColor3),
                findViewById(R.id.buttonColor4),
                findViewById(R.id.buttonColor5)
        };

        // Set the default selected button (first color)
        lastSelectedButton = colorButtons[0];
        lastSelectedButton.setBackgroundResource(R.drawable.selected_color_border);

        // Listener pentru butoanele de culoare
        for (int i = 0; i < colorButtons.length; i++) {
            final int index = i;
            colorButtons[i].setOnClickListener(v -> {
                // Remove the border from the last selected button
                lastSelectedButton.setBackgroundResource(R.drawable.button_background); // Reset to default background

                // Set the selected color
                switch (index) {
                    case 0:
                        selectedColor = "#FF5733"; // Orange
                        break;
                    case 1:
                        selectedColor = "#33FF57"; // Green
                        break;
                    case 2:
                        selectedColor = "#3357FF"; // Blue
                        break;
                    case 3:
                        selectedColor = "#FF33A1"; // Pink
                        break;
                    case 4:
                        selectedColor = "#A133FF"; // Purple
                        break;
                }

                // Highlight the selected button with a border
                colorButtons[index].setBackgroundResource(R.drawable.selected_color_border);

                // Update the last selected button
                lastSelectedButton = colorButtons[index];
            });
        }

        // Listener pentru butonul de salvare
        buttonSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = editTextCategoryName.getText().toString().trim();
        String description = editTextCategoryDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Creează un map cu datele categoriei
            Map<String, Object> category = new HashMap<>();
            category.put("name", name);
            category.put("description", description);
            category.put("color", selectedColor); // Adaugă culoarea selectată

            // Adaugă categoria în Firestore
            db.collection("users").document(userId).collection("categories")
                    .add(category)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Category added with ID: " + documentReference.getId());
                        Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Închide activitatea și revine la CategoryActivity
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding category", e);
                        Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}