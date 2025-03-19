package com.example.moneymap;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ToolbarUtils {

    public static void setupToolbar(Activity activity, Toolbar toolbar) {
        // Check if the activity is an instance of AppCompatActivity
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;

            // Set the Toolbar as the action bar
            appCompatActivity.setSupportActionBar(toolbar);

            // Remove the app icon from the action bar (optional)
            if (appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().setDisplayShowHomeEnabled(false);
                appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    public static boolean handleOptionsItemSelected(Activity activity, MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            // Handle the account icon click
            Toast.makeText(activity, "Account clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}