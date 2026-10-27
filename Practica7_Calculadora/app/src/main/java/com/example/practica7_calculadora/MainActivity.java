package com.example.practica7_calculadora;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView txtAlumno;
    EditText pantalla;
    int tamLetra = 24;
    Button[] botones = new Button[20];
    Double num1 = null;
    String operacion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Contenedor principal vertical
        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(30, 60, 30, 30);
        principal.setBackgroundColor(Color.parseColor("#263238"));

        // Tu nombre en la parte de arriba
        txtAlumno = new TextView(this);
        txtAlumno.setText("Víctor Enrique Yáñez Medina");
        txtAlumno.setTextSize(20);
        txtAlumno.setTextColor(Color.WHITE);
        txtAlumno.setGravity(Gravity.CENTER);
        txtAlumno.setPadding(0, 0, 0, 20);
        principal.addView(txtAlumno);

        // Pantalla de la calculadora
        pantalla = new EditText(this);
        pantalla.setTextSize(32);
        pantalla.setBackgroundColor(Color.WHITE);
        pantalla.setTextColor(Color.BLACK);
        pantalla.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        pantalla.setPadding(20, 20, 20, 20);
        pantalla.setFocusable(false);
        principal.addView(pantalla);

        // Filas para los botones
        LinearLayout f1 = new LinearLayout(this); f1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout f2 = new LinearLayout(this); f2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout f3 = new LinearLayout(this); f3.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout f4 = new LinearLayout(this); f4.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout f5 = new LinearLayout(this); f5.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(6, 6, 6, 6);

        // Botones del 0 al 9
        for (int i = 0; i <= 9; i++) {
            botones[i] = new Button(this);
            botones[i].setText(String.valueOf(i));
            botones[i].setTextSize(tamLetra);
            botones[i].setLayoutParams(params);
            botones[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String digito = ((Button) v).getText().toString();
                    pantalla.append(digito);
                }
            });
        }

        // Botón Punto (.)
        botones[10] = new Button(this);
        botones[10].setText(".");
        botones[10].setTextSize(tamLetra);
        botones[10].setLayoutParams(params);
        botones[10].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String actual = pantalla.getText().toString();
                if (!actual.contains(".")) {
                    pantalla.append(actual.isEmpty() ? "0." : ".");
                }
            }
        });

        // Botón Suma (+)
        botones[11] = new Button(this);
        botones[11].setText("+");
        botones[11].setTextSize(tamLetra);
        botones[11].setLayoutParams(params);
        botones[11].setOnClickListener(crearOperadorListener("+"));

        // Botón Resta (-)
        botones[12] = new Button(this);
        botones[12].setText("-");
        botones[12].setTextSize(tamLetra);
        botones[12].setLayoutParams(params);
        botones[12].setOnClickListener(crearOperadorListener("-"));

        // Botón Multiplicación (*)
        botones[13] = new Button(this);
        botones[13].setText("*");
        botones[13].setTextSize(tamLetra);
        botones[13].setLayoutParams(params);
        botones[13].setOnClickListener(crearOperadorListener("*"));

        // Botón División (/)
        botones[14] = new Button(this);
        botones[14].setText("/");
        botones[14].setTextSize(tamLetra);
        botones[14].setLayoutParams(params);
        botones[14].setOnClickListener(crearOperadorListener("/"));

        // Botón Igual (=)
        botones[15] = new Button(this);
        botones[15].setText("=");
        botones[15].setTextSize(tamLetra);
        botones[15].setLayoutParams(params);
        botones[15].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularResultado();
            }
        });

        // Botón Limpiar (C)
        botones[16] = new Button(this);
        botones[16].setText("C");
        botones[16].setTextSize(tamLetra);
        botones[16].setLayoutParams(params);
        botones[16].setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pantalla.setText("");
                num1 = null;
                operacion = "";
            }
        });

        // Fila 1: [ C ] [ / ]
        f1.addView(botones[16]);
        f1.addView(botones[14]);

        // Fila 2: [ 7 ] [ 8 ] [ 9 ] [ * ]
        f2.addView(botones[7]);
        f2.addView(botones[8]);
        f2.addView(botones[9]);
        f2.addView(botones[13]);

        // Fila 3: [ 4 ] [ 5 ] [ 6 ] [ - ]
        f3.addView(botones[4]);
        f3.addView(botones[5]);
        f3.addView(botones[6]);
        f3.addView(botones[12]);

        // Fila 4: [ 1 ] [ 2 ] [ 3 ] [ + ]
        f4.addView(botones[1]);
        f4.addView(botones[2]);
        f4.addView(botones[3]);
        f4.addView(botones[11]);

        // Fila 5: [ 0 ] [ . ] [ = ]
        f5.addView(botones[0]);
        f5.addView(botones[10]);
        f5.addView(botones[15]);

        // Agregar filas al contenedor principal
        principal.addView(f1);
        principal.addView(f2);
        principal.addView(f3);
        principal.addView(f4);
        principal.addView(f5);

        setContentView(principal);
    }

    private OnClickListener crearOperadorListener(final String op) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                String texto = pantalla.getText().toString().trim();
                if (!texto.isEmpty()) {
                    num1 = Double.parseDouble(texto);
                    operacion = op;
                    pantalla.setText("");
                }
            }
        };
    }

    private void calcularResultado() {
        String texto = pantalla.getText().toString().trim();
        if (num1 == null || texto.isEmpty() || operacion.isEmpty()) {
            return;
        }

        double num2 = Double.parseDouble(texto);
        double res = 0.0;

        switch (operacion) {
            case "+":
                res = num1 + num2;
                break;
            case "-":
                res = num1 - num2;
                break;
            case "*":
                res = num1 * num2;
                break;
            case "/":
                if (num2 == 0) {
                    Toast.makeText(this, "No se puede dividir entre cero", Toast.LENGTH_SHORT).show();
                    pantalla.setText("");
                    num1 = null;
                    operacion = "";
                    return;
                }
                res = num1 / num2;
                break;
        }

        if (res % 1 == 0) {
            pantalla.setText(String.valueOf((long) res));
        } else {
            pantalla.setText(String.valueOf(res));
        }

        num1 = null;
        operacion = "";
    }
}