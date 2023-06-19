package com.dada.firebasebutton2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.tagmate.CustomButton;
import com.tagmate.TagmateAnalytics;


public class MainActivity extends AppCompatActivity {

    CustomButton button;
    TagmateAnalytics tagmateAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.fireBaseButton);

        tagmateAnalytics = TagmateAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString("KEY_1","VIEW_ITEM_1");

        tagmateAnalytics.logEvent(TagmateAnalytics.Event.VIEW_ITEM,bundle);

//        SharedPreferences sp = ((BaseApp) getApplicationContext()).getSharedPreferences();
//        Log.d("ABC_XYZ", "onCreate: "+sp.getBoolean("SERVER_STATUS", false));

        button.setCustomClickListener(new CustomButton.CustomOnClickListener() {
            @Override
            public void onClick(View var1) {
                Toast.makeText(MainActivity.this,"Custom Button Clicked!!!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}