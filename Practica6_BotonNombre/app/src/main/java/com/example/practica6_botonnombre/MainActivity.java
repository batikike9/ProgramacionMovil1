package com.example.practica6_botonnombre; // Asegúrate de conservar tu package original si difiere

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    LinearLayout contenedorPrincipal;
    Button btnMostrarNombre, btnLimpiar;
    TextView txtNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Contenedor principal centrado
        contenedorPrincipal = new LinearLayout(this);
        contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);
        contenedorPrincipal.setGravity(Gravity.CENTER);
        contenedorPrincipal.setBackgroundColor(Color.parseColor("#F5F5F7"));
        contenedorPrincipal.setPadding(40, 40, 40, 40);

        // Botón principal
        btnMostrarNombre = new Button(this);
        btnMostrarNombre.setText("Presiona para ver mi nombre");
        btnMostrarNombre.setTextSize(18);

        // Botón para reiniciar/ocultar
        btnLimpiar = new Button(this);
        btnLimpiar.setText("Reiniciar");
        btnLimpiar.setTextSize(16);
        btnLimpiar.setVisibility(View.GONE); // Empieza oculto para no estorbar

        // TextView del nombre
        txtNombre = new TextView(this);
        txtNombre.setTextSize(26);
        txtNombre.setTextColor(Color.parseColor("#1C1C1E"));
        txtNombre.setGravity(Gravity.CENTER);
        txtNombre.setPadding(0, 40, 0, 40);

        // Mostrar nombre y habilitar el botón de reiniciar
        btnMostrarNombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtNombre.setText("Víctor Enrique Yáñez Medina");
                btnLimpiar.setVisibility(View.VISIBLE);
            }
        });

        // Ocultar nombre y volver al inicio
        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtNombre.setText("");
                btnLimpiar.setVisibility(View.GONE);
            }
        });

        // Agregar elementos
        contenedorPrincipal.addView(btnMostrarNombre);
        contenedorPrincipal.addView(txtNombre);
        contenedorPrincipal.addView(btnLimpiar);

        setContentView(contenedorPrincipal);
    }
}