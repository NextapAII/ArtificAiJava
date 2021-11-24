package com.nextap.artificai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class updatetheapp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updatetheapp);
        getSupportActionBar().hide();
        findViewById(R.id.update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nextap.artificai"));
                startActivity(i);
            }
        });
    }
}