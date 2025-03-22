package com.example.moneymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String AVATAR_KEY = "avatar_id";
    private ImageView currentAvatar;
    private Button buttonEditProfilePicture;

    private static final int AVATAR_SELECTION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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

        // avatar curent
        int savedAvatar = sharedPreferences.getInt(AVATAR_KEY, R.drawable.ic_account);
        currentAvatar.setImageResource(savedAvatar);

        // edit profile picture
        buttonEditProfilePicture.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AvatarSelectionActivity.class);
            startActivityForResult(intent, AVATAR_SELECTION_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AVATAR_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            // primim id-ul de la SelectAvatarActivity
            int selectedAvatar = data.getIntExtra("selectedAvatar", R.drawable.ic_account);

            // update
            currentAvatar.setImageResource(selectedAvatar);

            // save
            sharedPreferences.edit().putInt(AVATAR_KEY, selectedAvatar).apply();

            // update toolbar
            ToolbarUtils.updateToolbarIcon(this, selectedAvatar);

            // salvam in bd
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("avatarResId", selectedAvatar);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}