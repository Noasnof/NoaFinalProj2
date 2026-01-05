package com.example.noafinalproj2;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noafinalproj2.gemini.GeminiCallback;
import com.example.noafinalproj2.gemini.GeminiManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView tvAnswer;
    private Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvAnswer = findViewById(R.id.tvAnswer);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        private void initialization () {
            // initialize

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this)); // to be vertical

            adapter = new RecordAdapter(this, MainActivity.records);
            recyclerView.setAdapter(adapter);
        }


    }
}












