package com.example.galeria;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Galeria extends Activity implements View.OnClickListener {
    Button btnAtras, btnAdelante;
    ImageView iv;
    TextView txtTitulo;

    int imagenes[];
    String nombres[] = {"Flash", "Superman", "Mario Bros"};
    int imagenActual = 0;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.galeria);

        imagenes = new int[]{
                R.drawable.flash,
                R.drawable.superman,
                R.drawable.mario
        };

        btnAtras = findViewById(R.id.btnatras);
        btnAdelante = findViewById(R.id.btnadelante);
        iv = findViewById(R.id.imagenx);

        btnAdelante.setOnClickListener(this);
        btnAtras.setOnClickListener(this);

        // Crear e insertar el título arriba de la imagen
        txtTitulo = new TextView(this);
        txtTitulo.setTextSize(22);
        txtTitulo.setTextColor(Color.BLACK);
        txtTitulo.setGravity(Gravity.CENTER);
        txtTitulo.setPadding(0, 20, 0, 20);

        ViewGroup contenedor = (ViewGroup) iv.getParent();
        if (contenedor != null) {
            int indice = contenedor.indexOfChild(iv);
            contenedor.addView(txtTitulo, indice);
        }

        actualizarVista();
    }

    private void actualizarVista() {
        iv.setImageDrawable(getDrawable(imagenes[imagenActual]));
        txtTitulo.setText("Galería: " + nombres[imagenActual]);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnAtras)) {
            if (imagenActual <= 0) {
                imagenActual = imagenes.length - 1;
            } else {
                imagenActual = imagenActual - 1;
            }
            actualizarVista();
        }

        if (v.equals(btnAdelante)) {
            if (imagenActual >= imagenes.length - 1) {
                imagenActual = 0;
            } else {
                imagenActual = imagenActual + 1;
            }
            actualizarVista();
        }
    }
}