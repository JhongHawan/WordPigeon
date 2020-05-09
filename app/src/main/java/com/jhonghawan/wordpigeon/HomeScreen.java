package com.jhonghawan.wordpigeon;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeScreen extends AppCompatActivity {
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        button = (Button) findViewById(R.id.translate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChooseModel();
            }
        });
    }

    public void openChooseModel(){
        Intent intent = new Intent(this,ChooseModel.class);
        startActivity(intent);
    }
}
