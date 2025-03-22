package com.example.moneymap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ToolbarUtils {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String AVATAR_KEY = "avatar_id";

    public static void setupToolbar(Activity activity, Toolbar toolbar) {
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            appCompatActivity.setSupportActionBar(toolbar);

            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
                appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            // incarcam avatarul din firestore
            loadAvatarFromFirestore(activity);
        }
    }

    private static void loadAvatarFromFirestore(Activity activity) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int avatarResId = documentSnapshot.getLong("avatarResId").intValue();
                        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
                        sharedPreferences.edit().putInt(AVATAR_KEY, avatarResId).apply();
                        activity.invalidateOptionsMenu(); // Refresh the menu
                    }
                });
    }

    public static void updateToolbarIcon(Activity activity, int avatarResId) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        sharedPreferences.edit().putInt(AVATAR_KEY, avatarResId).apply();

        activity.runOnUiThread(() -> {
            Log.d("ToolbarUtils", "Invalidating options menu to refresh toolbar icon");
            activity.invalidateOptionsMenu();
        });
    }
    public static void loadToolbarIcon(Activity activity, Menu menu) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        int avatarResId = sharedPreferences.getInt(AVATAR_KEY, R.drawable.ic_account); // Default icon

        MenuItem accountItem = menu.findItem(R.id.action_account);
        if (accountItem != null) {
            accountItem.setIcon(avatarResId);
        }
    }
}