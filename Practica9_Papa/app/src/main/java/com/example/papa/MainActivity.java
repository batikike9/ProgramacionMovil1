package com.example.papa;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.content.ClipData;
import android.view.View;
import android.view.DragEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    FrameLayout escenario;
    ImageView piezaDisponible;
    ImageView ojosColocados, narizColocada, peloColocado, bocaColocada, piesColocados;
    Spinner selector;
    Button btnPosiciones;
    TextView resultado, tituloid;

    int piezaActual = 0;
    int tamanoPieza;

    // Coordenadas en la escala de 500x500
    float ojosX = -1, ojosY = -1;
    float narizX = -1, narizY = -1;
    float peloX = -1, peloY = -1;
    float bocaX = -1, bocaY = -1;
    float piesX = -1, piesY = -1;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        escenario = findViewById(R.id.escenario);
        piezaDisponible = findViewById(R.id.piezadisponible);
        selector = findViewById(R.id.selector);
        btnPosiciones = findViewById(R.id.btnposiciones);
        resultado = findViewById(R.id.resultado);
        tituloid = findViewById(R.id.tituloid);

        // Forzar textos en español
        if (tituloid != null) {
            tituloid.setText("Sr. Cara de Papa");
        }
        if (btnPosiciones != null) {
            btnPosiciones.setText("Calificar Posiciones");
        }

        try {
            TextView instrucciones = findViewById(getResources().getIdentifier("instrucciones", "id", getPackageName()));
            if (instrucciones != null) {
                instrucciones.setText("Arrastra la pieza hacia el cuerpo");
            }
        } catch (Exception ignored) {}

        try {
            Typeface fuente = getResources().getFont(R.font.ps);
            if (tituloid != null) tituloid.setTypeface(fuente);
            if (resultado != null) resultado.setTypeface(fuente);
        } catch (Exception ignored) {}

        tamanoPieza = dp(72);

        // Opciones del selector en español
        String[] opciones = {
                "Selecciona una pieza...",
                "Ojos",
                "Nariz",
                "Pelo",
                "Boca",
                "Pies"
        };

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selector.setAdapter(adaptador);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                piezaActual = position;
                piezaDisponible.setVisibility(View.GONE);

                switch (position) {
                    case 1: // Ojos
                        if (ojosColocados != null) {
                            escenario.removeView(ojosColocados);
                            ojosColocados = null;
                            ojosX = -1; ojosY = -1;
                        }
                        asignarPiezaSegura(R.drawable.ojos);
                        break;

                    case 2: // Nariz
                        if (narizColocada != null) {
                            escenario.removeView(narizColocada);
                            narizColocada = null;
                            narizX = -1; narizY = -1;
                        }
                        asignarPiezaSegura(R.drawable.nariz);
                        break;

                    case 3: // Pelo
                        if (peloColocado != null) {
                            escenario.removeView(peloColocado);
                            peloColocado = null;
                            peloX = -1; peloY = -1;
                        }
                        asignarPiezaSegura(obtenerDrawable("pelo", R.drawable.ojos));
                        break;

                    case 4: // Boca
                        if (bocaColocada != null) {
                            escenario.removeView(bocaColocada);
                            bocaColocada = null;
                            bocaX = -1; bocaY = -1;
                        }
                        asignarPiezaSegura(obtenerDrawable("boca", R.drawable.nariz));
                        break;

                    case 5: // Pies
                        if (piesColocados != null) {
                            escenario.removeView(piesColocados);
                            piesColocados = null;
                            piesX = -1; piesY = -1;
                        }
                        asignarPiezaSegura(obtenerDrawable("pies", R.drawable.nariz));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Touch listener para iniciar arrastre
        piezaDisponible.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    if (piezaActual == 0) return false;

                    ClipData datos = ClipData.newPlainText("pieza", String.valueOf(piezaActual));
                    View.DragShadowBuilder sombra = new View.DragShadowBuilder(v);
                    v.startDragAndDrop(datos, sombra, Integer.valueOf(piezaActual), 0);
                    return true;
                }
                return false;
            }
        });

        // Drag listener para el escenario
        escenario.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return event.getLocalState() instanceof Integer;
                    case DragEvent.ACTION_DROP:
                        int tipo = (Integer) event.getLocalState();
                        colocarPieza(event.getX(), event.getY(), tipo);
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                    case DragEvent.ACTION_DRAG_LOCATION:
                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });

        // Botón para calificar las posiciones
        btnPosiciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificarPosiciones();
            }
        });
    }

    private void asignarPiezaSegura(int resId) {
        piezaDisponible.setImageResource(resId);
        piezaDisponible.setVisibility(View.VISIBLE);
    }

    private int obtenerDrawable(String nombre, int respaldo) {
        int id = getResources().getIdentifier(nombre, "drawable", getPackageName());
        return (id != 0) ? id : respaldo;
    }

    private void colocarPieza(float x, float y, int tipo) {
        ImageView nuevaPieza = new ImageView(this);

        switch (tipo) {
            case 1:
                nuevaPieza.setImageResource(R.drawable.ojos);
                break;
            case 2:
                nuevaPieza.setImageResource(R.drawable.nariz);
                break;
            case 3:
                nuevaPieza.setImageResource(obtenerDrawable("pelo", R.drawable.ojos));
                break;
            case 4:
                nuevaPieza.setImageResource(obtenerDrawable("boca", R.drawable.nariz));
                break;
            case 5:
                nuevaPieza.setImageResource(obtenerDrawable("pies", R.drawable.nariz));
                break;
        }

        nuevaPieza.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int ancho = escenario.getWidth();
        if (ancho == 0) ancho = dp(300);

        int izquierda = Math.max(0, Math.min((int) x - tamanoPieza / 2, ancho - tamanoPieza));
        int arriba = Math.max(0, Math.min((int) y - tamanoPieza / 2, ancho - tamanoPieza));

        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(tamanoPieza, tamanoPieza);
        parametros.leftMargin = izquierda;
        parametros.topMargin = arriba;
        escenario.addView(nuevaPieza, parametros);

        float factor = 500f / ancho;
        float centroX = (izquierda + tamanoPieza / 2f) * factor;
        float centroY = (arriba + tamanoPieza / 2f) * factor;

        switch (tipo) {
            case 1:
                ojosColocados = nuevaPieza;
                ojosX = centroX; ojosY = centroY;
                break;
            case 2:
                narizColocada = nuevaPieza;
                narizX = centroX; narizY = centroY;
                break;
            case 3:
                peloColocado = nuevaPieza;
                peloX = centroX; peloY = centroY;
                break;
            case 4:
                bocaColocada = nuevaPieza;
                bocaX = centroX; bocaY = centroY;
                break;
            case 5:
                piesColocados = nuevaPieza;
                piesX = centroX; piesY = centroY;
                break;
        }

        piezaDisponible.setVisibility(View.GONE);
    }

    private void calificarPosiciones() {
        int aciertos = 0;
        StringBuilder reporte = new StringBuilder();

        // 1. Pelo
        if (peloColocado != null) {
            boolean correcto = (peloY >= 40 && peloY <= 150) && (peloX >= 160 && peloX <= 340);
            if (correcto) aciertos++;
            reporte.append("Pelo: ").append(correcto ? "CORRECTO (+2 pts)" : "Desviado").append("\n");
        } else {
            reporte.append("Pelo: No colocado\n");
        }

        // 2. Ojos
        if (ojosColocados != null) {
            boolean correcto = (ojosY >= 130 && ojosY <= 230) && (ojosX >= 170 && ojosX <= 330);
            if (correcto) aciertos++;
            reporte.append("Ojos: ").append(correcto ? "CORRECTO (+2 pts)" : "Desviado").append("\n");
        } else {
            reporte.append("Ojos: No colocados\n");
        }

        // 3. Nariz
        if (narizColocada != null) {
            boolean correcto = (narizY >= 210 && narizY <= 300) && (narizX >= 190 && narizX <= 310);
            if (correcto) aciertos++;
            reporte.append("Nariz: ").append(correcto ? "CORRECTO (+2 pts)" : "Desviada").append("\n");
        } else {
            reporte.append("Nariz: No colocada\n");
        }

        // 4. Boca
        if (bocaColocada != null) {
            boolean correcto = (bocaY >= 280 && bocaY <= 380) && (bocaX >= 180 && bocaX <= 320);
            if (correcto) aciertos++;
            reporte.append("Boca: ").append(correcto ? "CORRECTO (+2 pts)" : "Desviada").append("\n");
        } else {
            reporte.append("Boca: No colocada\n");
        }

        // 5. Pies
        if (piesColocados != null) {
            boolean correcto = (piesY >= 380 && piesY <= 490) && (piesX >= 160 && piesX <= 340);
            if (correcto) aciertos++;
            reporte.append("Pies: ").append(correcto ? "CORRECTO (+2 pts)" : "Desviados").append("\n");
        } else {
            reporte.append("Pies: No colocados\n");
        }

        int calificacion = aciertos * 2;
        reporte.append("\nCalificación: ").append(calificacion).append("/10");
        if (calificacion == 10) {
            reporte.append(" (¡Excelente!)");
        }

        resultado.setText(reporte.toString());
    }

    private int dp(float valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}