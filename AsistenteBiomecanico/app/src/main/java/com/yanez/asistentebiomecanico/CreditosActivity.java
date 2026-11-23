package com.yanez.asistentebiomecanico;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CreditosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditos);

        Button btnVolver = findViewById(R.id.btnVolverCreditos);
        btnVolver.setOnClickListener(v -> finish());
    }
}