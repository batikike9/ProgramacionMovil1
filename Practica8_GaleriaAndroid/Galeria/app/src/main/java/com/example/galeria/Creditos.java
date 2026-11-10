package com.example.galeria;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Creditos extends Activity {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        // Contenedor principal centrado
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.CENTER);
        contenedor.setBackgroundColor(Color.parseColor("#F5F5F7"));
        contenedor.setPadding(40, 40, 40, 40);

        // Título de la sección
        TextView txtTitulo = new TextView(this);
        txtTitulo.setText("CRÉDITOS");
        txtTitulo.setTextSize(26);
        txtTitulo.setTextColor(Color.parseColor("#1C1C1E"));
        txtTitulo.setGravity(Gravity.CENTER);
        txtTitulo.setPadding(0, 0, 0, 30);

        // Subtítulo
        TextView txtRol = new TextView(this);
        txtRol.setText("Desarrollado por:");
        txtRol.setTextSize(18);
        txtRol.setTextColor(Color.parseColor("#636366"));
        txtRol.setGravity(Gravity.CENTER);

        // Tu nombre completo
        TextView txtNombre = new TextView(this);
        txtNombre.setText("Víctor Enrique Yáñez Medina");
        txtNombre.setTextSize(22);
        txtNombre.setTextColor(Color.parseColor("#007AFF"));
        txtNombre.setGravity(Gravity.CENTER);
        txtNombre.setPadding(0, 15, 0, 0);

        contenedor.addView(txtTitulo);
        contenedor.addView(txtRol);
        contenedor.addView(txtNombre);

        // Asignar el contenedor como la vista de la pantalla
        setContentView(contenedor);
    }
}