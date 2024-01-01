package com.example.helloworld;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button TopButton,BottomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TopButton=findViewById(R.id.top);
        TopButton.setOnClickListener(this);
        BottomButton=findViewById(R.id.bottom);
        BottomButton.setOnClickListener(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.top){
            Toast msg=Toast.makeText(this,"Top Button Clicked",Toast.LENGTH_SHORT);
            msg.show();
        }
        else if(id==R.id.bottom){
            Toast msg=Toast.makeText(this,"Bottom Button Clicked",Toast.LENGTH_SHORT);
            msg.show();
        }
    }
}