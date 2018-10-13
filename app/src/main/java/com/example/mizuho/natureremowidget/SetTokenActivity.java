package com.example.mizuho.natureremowidget;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetTokenActivity extends AppCompatActivity {

    final String FILE_NAME = "token.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_token);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FileRW fileRW = new FileRW(getApplicationContext());
                EditText editText = findViewById(R.id.editText);

                String token = editText.getText().toString();
                fileRW.clearFile(FILE_NAME);
                fileRW.writeFileMessage(FILE_NAME, token);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
