package com.example.finanzplaner;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hier verknüpfst du die Activity mit deinem Home-Layout:
        setContentView(R.layout.activity_home);
    }
}