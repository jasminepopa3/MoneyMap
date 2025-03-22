package com.example.moneymap;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AvatarSelectionActivity extends AppCompatActivity {

    private GridView gridViewAvatars;
    private List<Integer> avatarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gridViewAvatars = findViewById(R.id.gridViewAvatars);

        // lista de avatare
        avatarList = new ArrayList<>();
        avatarList.add(R.drawable.avatar_1);
        avatarList.add(R.drawable.avatar_2);
        avatarList.add(R.drawable.avatar_3);


        AvatarAdapter adapter = new AvatarAdapter(this, avatarList);
        gridViewAvatars.setAdapter(adapter);

        // selectare avatar
        gridViewAvatars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedAvatar = avatarList.get(position);

                // trimitem update la ProfileActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedAvatar", selectedAvatar);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}