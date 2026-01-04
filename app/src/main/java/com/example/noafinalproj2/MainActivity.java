package com.example.noafinalproj2;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button btnAgainstPlayer,btnAgainstAi,btnInstruction,btnLogout,btnNewPlayer;
    EditText edGvihim;
    int mode=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnAgainstPlayer=findViewById(R.id.btnAgainstPlayer);
        btnAgainstPlayer.setOnClickListener(this);
        btnInstruction=findViewById(R.id.btnInstruction);
        btnInstruction.setOnClickListener(this);
        btnAgainstAi=findViewById(R.id.btnAgainstAi);
        btnAgainstAi.setOnClickListener(this);
        btnLogout=findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
        edGvihim=findViewById(R.id.edGvihim);
        btnNewPlayer=findViewById(R.id.btnNewPlayer);
        btnNewPlayer.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v==btnAgainstPlayer) {
            Intent i = new Intent(MainActivity.this, SelectActivity.class);
            mode=1;
            i.putExtra("mode",mode);
            startActivity(i);
        }
        if(v==btnAgainstAi)
        {
            Intent intent=new Intent(MainActivity.this,  GameActivity.class);
            mode=1;
            intent.putExtra("mode",mode);
            startActivity(intent);
        }
        if (v==btnInstruction)
        {
            Intent i2=new Intent(MainActivity.this, InstructionActivity.class);
            startActivity(i2);
        }
        if(v==btnLogout) {
            FirebaseAuth.getInstance().signOut();
            finish(); // close the activity
        }
        if(v==btnNewPlayer) {
            Intent i3 = new Intent(MainActivity.this, CreatePlayer.class);
            startActivity(i3);
        }


    }
}