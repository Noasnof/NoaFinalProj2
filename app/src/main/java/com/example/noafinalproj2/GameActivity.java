package com.example.noafinalproj2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private EditText edAnswer;
    private TextView tvAnswer;
    private RecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        btnSend=findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        edAnswer=findViewById(R.id.edAnswer);
        tvAnswer=findViewById(R.id.tvAnswer);
        String topic=getIntent().getStringExtra("subject");
        tvAnswer.setText(topic);


    }

    @Override
    public void onClick(View v) {
        if ((v==btnSend)){
            String Answer = edAnswer.getText().toString();



        FB.getInstance().setRecord(Answer); }
        initialization();
    }
    private void initialization() {
        // initialize

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // to be vertical

        adapter = new RecordAdapter(this, MainActivity.records);
        recyclerView.setAdapter(adapter);
    }
}