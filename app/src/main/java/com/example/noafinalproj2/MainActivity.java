package com.example.noafinalproj2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noafinalproj2.gemini.GeminiCallback;
import com.example.noafinalproj2.gemini.GeminiManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public Button btnAgainstPlayer,btnAgainstAi,btnInstruction,btnLogout,btnNewPlayer;
    EditText edGvihim;

    public String topic;
    public static ArrayList<Record> records;

    FB fb;
    private static final String API_KEY = "AIzaSyCxQCtqMjCtfgMnSPhY1mQ9dRMQ-ihDvRI"; // הכניסי כאן את מפתח ה-API שלך


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
        String TAG = "MainActivity";
        initialization();
    }

    @Override
    public void onClick(View v) {
        if (v==btnAgainstPlayer) {
            Intent i = new Intent(MainActivity.this, SelectActivity.class);


            startActivity(i);
        }
        if(v==btnAgainstAi)
        {
            Intent intent=new Intent(MainActivity.this,  GameActivity.class);

            String prompt ="תן לי נושא למשחק אסוסיאציות";
            //String prompt = "What is the capital of France?";
            GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() ->
                            {
                                topic=response;
                            }
                    );
                }

/*                    @Override
                    public void onError(Throwable e) {
                        //runOnUiThread(() ->System.out.println("שגיאה: " + e.getMessage()));
                        runOnUiThread(() ->Log.e(TAG, "שגיאה: " + e.getMessage()));
                        //Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }*/

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Gemini error", e); // prints full stack trace, not just message
                    topic="Error";
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this,
                            "Error: " + e.getClass().getName() + " / " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }


                @Override
                public void onError(Exception e) {
                    //runOnUiThread(() -> System.out.println("שגיאה: " + e.getMessage()));
                    runOnUiThread(() ->Log.e(TAG, "שגיאה: " + e.getMessage()));
                    topic="Error";
                    //Toast.makeText(InstructionActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                }
            });
            intent.putExtra("subject",topic);
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






    private void initialization() {
    // initialize



    records = new ArrayList<>();
    fb = FB.getInstance();
}
}