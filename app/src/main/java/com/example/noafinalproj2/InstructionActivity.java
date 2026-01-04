package com.example.noafinalproj2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InstructionActivity extends AppCompatActivity implements View.OnClickListener {

    public Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_instruction);


        btnBack=findViewById(R.id.btnBack) ;
        btnBack.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        finish();
    }
}