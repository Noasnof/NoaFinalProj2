package com.example.noafinalproj2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class CreatePlayer extends AppCompatActivity implements View.OnClickListener {

    EditText edName;
    Button btnFinish;
    Player p;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_player);

        edName=findViewById(R.id.edName);
        name=edName.toString();

        p = new Player(name);
        btnFinish=findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        
        
        finish();
    }
}