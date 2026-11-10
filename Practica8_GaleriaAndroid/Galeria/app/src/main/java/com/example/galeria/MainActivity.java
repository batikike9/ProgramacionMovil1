package com.example.galeria;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.principal);
    }

    public void abregaleria(View v){
        Intent i = new Intent(this, Galeria.class);
        startActivity(i);
    }
    public void abrecreditos(View v){
        Intent i2 = new Intent(this, Creditos.class);
        startActivity(i2);
    }
}