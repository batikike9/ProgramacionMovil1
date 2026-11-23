package com.yanez.asistentebiomecanico;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button tabInstrucciones, tabRutinas, tabRegistros;
    private LinearLayout vistaInstrucciones, vistaRegistros;
    private ScrollView vistaRutinas;

    private ImageView ivEjercicio;
    private TextView tvTituloEjercicio, tvZonaEquipo, tvPreparacion, tvEjecucion, tvConsejos;
    private EditText etCargaKg, etRepsMeta;
    private String claveSeleccionada = "laterales";

    private EditText etNombreRutina, etRutinaSeries, etRutinaReps, etRutinaPeso, etRutinaDescanso;
    private Spinner spinnerRutinaEjercicio;
    private LinearLayout contenedorColaTemp, contenedorListaRutinas;
    private JSONArray colaEjerciciosTemp = new JSONArray();

    private LinearLayout contenedorListaRegistros;

    private final String[] claves = {"laterales", "sentadilla", "biceps"};
    private final String[] nombres = {"Elevaciones Laterales", "Sentadilla Libre (Squat)", "Curl de Bíceps"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabInstrucciones = findViewById(R.id.tabInstrucciones);
        tabRutinas = findViewById(R.id.tabRutinas);
        tabRegistros = findViewById(R.id.tabRegistros);

        vistaInstrucciones = findViewById(R.id.vistaInstrucciones);
        vistaRutinas = findViewById(R.id.vistaRutinas);
        vistaRegistros = findViewById(R.id.vistaRegistros);

        Button btnCreditos = findViewById(R.id.btnCreditosHeader);
        btnCreditos.setOnClickListener(v -> startActivity(new Intent(this, CreditosActivity.class)));

        tabInstrucciones.setOnClickListener(v -> cambiarPestana("INSTRUCCIONES"));
        tabRutinas.setOnClickListener(v -> cambiarPestana("RUTINAS"));
        tabRegistros.setOnClickListener(v -> cambiarPestana("REGISTROS"));

        configurarVistaInstrucciones();
        configurarVistaRutinas();
        configurarVistaRegistros();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarListaRutinas();
        cargarListaRegistros();
    }

    // Helper para crear tarjetas con esquinas redondeadas y borde sutil
    private GradientDrawable crearTarjeta(String colorFondoHex, String colorBordeHex, float radioDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(colorFondoHex));
        gd.setCornerRadius(radioDp * getResources().getDisplayMetrics().density);
        if (colorBordeHex != null) {
            gd.setStroke((int) (1 * getResources().getDisplayMetrics().density), Color.parseColor(colorBordeHex));
        }
        return gd;
    }

    private void cambiarPestana(String pestana) {
        vistaInstrucciones.setVisibility(View.GONE);
        vistaRutinas.setVisibility(View.GONE);
        vistaRegistros.setVisibility(View.GONE);

        int colorInactivo = Color.parseColor("#131F37");
        int colorActivo = Color.parseColor("#2563EB");
        int textoInactivo = Color.parseColor("#94A3B8");

        tabInstrucciones.setBackgroundColor(colorInactivo);
        tabRutinas.setBackgroundColor(colorInactivo);
        tabRegistros.setBackgroundColor(colorInactivo);
        tabInstrucciones.setTextColor(textoInactivo);
        tabRutinas.setTextColor(textoInactivo);
        tabRegistros.setTextColor(textoInactivo);

        if (pestana.equals("INSTRUCCIONES")) {
            vistaInstrucciones.setVisibility(View.VISIBLE);
            tabInstrucciones.setBackgroundColor(colorActivo);
            tabInstrucciones.setTextColor(Color.WHITE);
        } else if (pestana.equals("RUTINAS")) {
            vistaRutinas.setVisibility(View.VISIBLE);
            tabRutinas.setBackgroundColor(colorActivo);
            tabRutinas.setTextColor(Color.WHITE);
            cargarListaRutinas();
        } else {
            vistaRegistros.setVisibility(View.VISIBLE);
            tabRegistros.setBackgroundColor(colorActivo);
            tabRegistros.setTextColor(Color.WHITE);
            cargarListaRegistros();
        }
    }

    private void configurarVistaInstrucciones() {
        Spinner spinnerHome = findViewById(R.id.spinnerEjerciciosHome);
        ivEjercicio = findViewById(R.id.ivEjercicio);
        tvTituloEjercicio = findViewById(R.id.tvTituloEjercicio);
        tvZonaEquipo = findViewById(R.id.tvZonaEquipo);
        tvPreparacion = findViewById(R.id.tvPreparacion);
        tvEjecucion = findViewById(R.id.tvEjecucion);
        tvConsejos = findViewById(R.id.tvConsejos);
        etCargaKg = findViewById(R.id.etCargaKg);
        etRepsMeta = findViewById(R.id.etRepsMeta);
        Button btnComenzar = findViewById(R.id.btnComenzarIndividual);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombres);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerHome.setAdapter(adapter);
        spinnerHome.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                claveSeleccionada = claves[position];
                String[] info = GestorDatos.obtenerInfoEjercicio(claveSeleccionada);
                tvTituloEjercicio.setText(info[0]);
                tvZonaEquipo.setText(info[1]);
                tvPreparacion.setText(info[2]);
                tvEjecucion.setText(info[3]);
                tvConsejos.setText(info[4]);

                if (claveSeleccionada.equals("sentadilla")) {
                    ivEjercicio.setImageResource(R.drawable.img_sentadilla);
                } else if (claveSeleccionada.equals("biceps")) {
                    ivEjercicio.setImageResource(R.drawable.img_biceps);
                } else {
                    ivEjercicio.setImageResource(R.drawable.img_laterales);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnComenzar.setOnClickListener(v -> {
            double peso = 10.0;
            int reps = 10;
            try { peso = Double.parseDouble(etCargaKg.getText().toString()); } catch (Exception ignored) {}
            try { reps = Integer.parseInt(etRepsMeta.getText().toString()); } catch (Exception ignored) {}

            Intent intent = new Intent(this, EntrenamientoActivity.class);
            intent.putExtra("ejercicioClave", claveSeleccionada);
            intent.putExtra("metaReps", reps);
            intent.putExtra("pesoKg", peso);
            startActivity(intent);
        });
    }

    private void configurarVistaRutinas() {
        etNombreRutina = findViewById(R.id.etNombreRutina);
        spinnerRutinaEjercicio = findViewById(R.id.spinnerRutinaEjercicio);
        etRutinaSeries = findViewById(R.id.etRutinaSeries);
        etRutinaReps = findViewById(R.id.etRutinaReps);
        etRutinaPeso = findViewById(R.id.etRutinaPeso);
        etRutinaDescanso = findViewById(R.id.etRutinaDescanso);
        contenedorColaTemp = findViewById(R.id.contenedorColaTemp);
        contenedorListaRutinas = findViewById(R.id.contenedorListaRutinas);

        Button btnAgregarCola = findViewById(R.id.btnAgregarEjercicioCola);
        Button btnGuardarRutina = findViewById(R.id.btnGuardarRutina);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, nombres);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerRutinaEjercicio.setAdapter(adapter);

        renderizarColaTemporal();

        btnAgregarCola.setOnClickListener(v -> {
            int pos = spinnerRutinaEjercicio.getSelectedItemPosition();
            int series = 2, reps = 8, descanso = 15;
            double peso = 10.0;
            try { series = Integer.parseInt(etRutinaSeries.getText().toString()); } catch (Exception ignored) {}
            try { reps = Integer.parseInt(etRutinaReps.getText().toString()); } catch (Exception ignored) {}
            try { peso = Double.parseDouble(etRutinaPeso.getText().toString()); } catch (Exception ignored) {}
            try { descanso = Integer.parseInt(etRutinaDescanso.getText().toString()); } catch (Exception ignored) {}

            try {
                JSONObject item = new JSONObject();
                item.put("ejercicioClave", claves[pos]);
                item.put("series", series);
                item.put("reps", reps);
                item.put("peso", peso);
                item.put("descanso", descanso);
                colaEjerciciosTemp.put(item);
                renderizarColaTemporal();
                Toast.makeText(this, "Ejercicio añadido a la cola", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
        });

        btnGuardarRutina.setOnClickListener(v -> {
            String nom = etNombreRutina.getText().toString().trim();
            if (nom.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre de la rutina", Toast.LENGTH_SHORT).show();
                return;
            }
            if (colaEjerciciosTemp.length() == 0) {
                Toast.makeText(this, "Añade al menos un ejercicio primero", Toast.LENGTH_SHORT).show();
                return;
            }

            GestorDatos.guardarRutinaMulti(this, nom, colaEjerciciosTemp);
            etNombreRutina.setText("");
            colaEjerciciosTemp = new JSONArray();
            renderizarColaTemporal();
            cargarListaRutinas();
            Toast.makeText(this, "Rutina guardada correctamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void renderizarColaTemporal() {
        contenedorColaTemp.removeAllViews();
        if (colaEjerciciosTemp.length() == 0) {
            TextView tv = new TextView(this);
            tv.setText("Cola vacía: añade uno o varios ejercicios antes de guardar.");
            tv.setTextColor(Color.parseColor("#64748B"));
            tv.setTextSize(11f);
            contenedorColaTemp.addView(tv);
            return;
        }

        for (int i = 0; i < colaEjerciciosTemp.length(); i++) {
            try {
                final int idx = i;
                JSONObject it = colaEjerciciosTemp.getJSONObject(i);
                String nomEj = GestorDatos.obtenerInfoEjercicio(it.getString("ejercicioClave"))[0];

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBackground(crearTarjeta("#1E293B", "#334155", 8));
                row.setPadding(20, 14, 20, 14);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 6, 0, 6);
                row.setLayoutParams(lp);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tv.setText((i + 1) + ". " + nomEj + " (" + it.getInt("series") + "x" + it.getInt("reps") + " · " + it.getDouble("peso") + "kg)");
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(12f);

                TextView btnQuitar = new TextView(this);
                btnQuitar.setText("QUITAR");
                btnQuitar.setTextColor(Color.parseColor("#EF4444"));
                btnQuitar.setTextSize(11f);
                btnQuitar.setTypeface(null, android.graphics.Typeface.BOLD);
                btnQuitar.setOnClickListener(v -> {
                    JSONArray nueva = new JSONArray();
                    for (int j = 0; j < colaEjerciciosTemp.length(); j++) {
                        if (j != idx) {
                            try { nueva.put(colaEjerciciosTemp.get(j)); } catch (Exception ignored) {}
                        }
                    }
                    colaEjerciciosTemp = nueva;
                    renderizarColaTemporal();
                });

                row.addView(tv);
                row.addView(btnQuitar);
                contenedorColaTemp.addView(row);
            } catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // LISTA DE RUTINAS CON SUB-TARJETAS POR EJERCICIO
    // =========================================================================
    private void cargarListaRutinas() {
        contenedorListaRutinas.removeAllViews();
        JSONArray rutinas = GestorDatos.obtenerRutinas(this);

        if (rutinas.length() == 0) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No tienes rutinas guardadas. Diseña una arriba.");
            tvVacio.setTextColor(Color.parseColor("#64748B"));
            tvVacio.setPadding(8, 12, 8, 12);
            contenedorListaRutinas.addView(tvVacio);
            return;
        }

        for (int i = 0; i < rutinas.length(); i++) {
            try {
                JSONObject r = rutinas.getJSONObject(i);
                final int index = i;
                String nombreRut = r.getString("nombre");
                JSONArray items = r.optJSONArray("items");
                if (items == null) continue;

                // Tarjeta Contenedora Principal (#131F37)
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(crearTarjeta("#131F37", "#1E293B", 14));
                card.setPadding(24, 22, 24, 22);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 18);
                card.setLayoutParams(params);

                // Encabezado de la rutina
                LinearLayout headerRut = new LinearLayout(this);
                headerRut.setOrientation(LinearLayout.HORIZONTAL);
                headerRut.setGravity(Gravity.CENTER_VERTICAL);
                headerRut.setPadding(0, 0, 0, 14);

                TextView tvNom = new TextView(this);
                tvNom.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvNom.setText(nombreRut.toUpperCase());
                tvNom.setTextColor(Color.parseColor("#38BDF8"));
                tvNom.setTextSize(16f);
                tvNom.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvBadge = new TextView(this);
                tvBadge.setText(items.length() + (items.length() == 1 ? " ejercicio" : " ejercicios"));
                tvBadge.setTextColor(Color.parseColor("#94A3B8"));
                tvBadge.setTextSize(11f);
                tvBadge.setBackground(crearTarjeta("#0F172A", "#334155", 8));
                tvBadge.setPadding(16, 6, 16, 6);

                headerRut.addView(tvNom);
                headerRut.addView(tvBadge);
                card.addView(headerRut);

                // Sub-tarjetas internas (#1E293B) para cada ejercicio de la rutina
                for (int k = 0; k < items.length(); k++) {
                    JSONObject it = items.getJSONObject(k);
                    String ejNom = GestorDatos.obtenerInfoEjercicio(it.getString("ejercicioClave"))[0];

                    LinearLayout subCard = new LinearLayout(this);
                    subCard.setOrientation(LinearLayout.VERTICAL);
                    subCard.setBackground(crearTarjeta("#1E293B", "#334155", 10));
                    subCard.setPadding(20, 14, 20, 14);
                    LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    subParams.setMargins(0, 0, 0, 10);
                    subCard.setLayoutParams(subParams);

                    TextView tvSubTitle = new TextView(this);
                    tvSubTitle.setText((k + 1) + ". " + ejNom);
                    tvSubTitle.setTextColor(Color.WHITE);
                    tvSubTitle.setTextSize(13.5f);
                    tvSubTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView tvSubMeta = new TextView(this);
                    tvSubMeta.setText(it.getInt("series") + " series  ·  "
                            + it.getInt("reps") + " reps  ·  "
                            + it.getDouble("peso") + " kg  ·  Descanso: "
                            + it.getInt("descanso") + "s");
                    tvSubMeta.setTextColor(Color.parseColor("#94A3B8"));
                    tvSubMeta.setTextSize(12f);
                    tvSubMeta.setPadding(0, 4, 0, 0);

                    subCard.addView(tvSubTitle);
                    subCard.addView(tvSubMeta);
                    card.addView(subCard);
                }

                // Fila de botones de acción
                LinearLayout filaBtns = new LinearLayout(this);
                filaBtns.setOrientation(LinearLayout.HORIZONTAL);
                filaBtns.setPadding(0, 8, 0, 0);

                Button btnPlay = new Button(this);
                LinearLayout.LayoutParams pPlay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.4f);
                btnPlay.setLayoutParams(pPlay);
                btnPlay.setText("INICIAR RUTINA");
                btnPlay.setBackgroundColor(Color.parseColor("#10B981"));
                btnPlay.setTextColor(Color.WHITE);
                btnPlay.setTypeface(null, android.graphics.Typeface.BOLD);
                btnPlay.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EntrenamientoActivity.class);
                    intent.putExtra("nombreRutina", nombreRut);
                    intent.putExtra("rutinaItemsJson", items.toString());
                    startActivity(intent);
                });

                Button btnDel = new Button(this);
                LinearLayout.LayoutParams pDel = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
                pDel.setMargins(16, 0, 0, 0);
                btnDel.setLayoutParams(pDel);
                btnDel.setText("BORRAR");
                btnDel.setBackgroundColor(Color.parseColor("#EF4444"));
                btnDel.setTextColor(Color.WHITE);
                btnDel.setTypeface(null, android.graphics.Typeface.BOLD);
                btnDel.setOnClickListener(v -> {
                    GestorDatos.eliminarRutina(this, index);
                    cargarListaRutinas();
                });

                filaBtns.addView(btnPlay);
                filaBtns.addView(btnDel);
                card.addView(filaBtns);

                contenedorListaRutinas.addView(card);
            } catch (Exception ignored) {}
        }
    }

    private void configurarVistaRegistros() {
        contenedorListaRegistros = findViewById(R.id.contenedorListaRegistros);
        Button btnLimpiar = findViewById(R.id.btnLimpiarRegistros);
        btnLimpiar.setOnClickListener(v -> {
            GestorDatos.limpiarHistorial(this);
            cargarListaRegistros();
            Toast.makeText(this, "Historial borrado", Toast.LENGTH_SHORT).show();
        });
    }

    // =========================================================================
    // HISTORIAL DE SESIONES CON SUB-TARJETAS DIFERENCIADAS POR BLOQUE
    // =========================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void cargarListaRegistros() {
        contenedorListaRegistros.removeAllViews();
        JSONArray historial = GestorDatos.obtenerHistorial(this);

        if (historial.length() == 0) {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay registros todavía. Completa una serie o rutina para guardarla.");
            tvVacio.setTextColor(Color.parseColor("#64748B"));
            tvVacio.setPadding(8, 24, 8, 24);
            contenedorListaRegistros.addView(tvVacio);
            return;
        }

        TextView tvHint = new TextView(this);
        tvHint.setText("← Desliza una tarjeta a la izquierda para eliminarla");
        tvHint.setTextColor(Color.parseColor("#64748B"));
        tvHint.setTextSize(11f);
        tvHint.setPadding(4, 0, 4, 12);
        contenedorListaRegistros.addView(tvHint);

        for (int i = historial.length() - 1; i >= 0; i--) {
            try {
                final int indexReal = i;
                JSONObject s = historial.getJSONObject(i);

                FrameLayout swipeWrapper = new FrameLayout(this);
                LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                wrapParams.setMargins(0, 0, 0, 16);
                swipeWrapper.setLayoutParams(wrapParams);

                // Fondo rojo de eliminación
                FrameLayout fondoRojo = new FrameLayout(this);
                fondoRojo.setBackground(crearTarjeta("#EF4444", null, 14));
                fondoRojo.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                ImageButton btnBasurero = new ImageButton(this);
                btnBasurero.setImageResource(android.R.drawable.ic_menu_delete);
                btnBasurero.setBackgroundColor(Color.TRANSPARENT);
                btnBasurero.setColorFilter(Color.WHITE);
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                        180, FrameLayout.LayoutParams.MATCH_PARENT);
                iconParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                btnBasurero.setLayoutParams(iconParams);
                btnBasurero.setOnClickListener(v -> {
                    GestorDatos.eliminarSerie(this, indexReal);
                    cargarListaRegistros();
                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                });
                fondoRojo.addView(btnBasurero);

                // Tarjeta Principal de Registro (#131F37)
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(crearTarjeta("#131F37", "#1E293B", 14));
                card.setPadding(24, 20, 24, 20);
                card.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

                // Cabecera: Título + Fecha
                LinearLayout headerReg = new LinearLayout(this);
                headerReg.setOrientation(LinearLayout.HORIZONTAL);
                headerReg.setGravity(Gravity.CENTER_VERTICAL);
                headerReg.setPadding(0, 0, 0, 12);

                boolean esRut = s.optBoolean("esRutina", false);
                TextView tvEj = new TextView(this);
                tvEj.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvEj.setText(s.optString("titulo", s.optString("ejercicio", "Sesión")));
                tvEj.setTextColor(esRut ? Color.parseColor("#38BDF8") : Color.parseColor("#10B981"));
                tvEj.setTextSize(15f);
                tvEj.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvFecha = new TextView(this);
                tvFecha.setText(s.optString("fecha"));
                tvFecha.setTextColor(Color.parseColor("#94A3B8"));
                tvFecha.setTextSize(11f);
                tvFecha.setBackground(crearTarjeta("#0F172A", "#334155", 6));
                tvFecha.setPadding(12, 4, 12, 4);

                headerReg.addView(tvEj);
                headerReg.addView(tvFecha);
                card.addView(headerReg);

                // Sub-tarjetas internas (#1E293B) para cada serie o ejercicio realizado
                JSONArray bloques = s.optJSONArray("bloques");
                if (bloques != null && bloques.length() > 0) {
                    for (int b = 0; b < bloques.length(); b++) {
                        JSONObject bObj = bloques.getJSONObject(b);

                        LinearLayout subItem = new LinearLayout(this);
                        subItem.setOrientation(LinearLayout.VERTICAL);
                        subItem.setBackground(crearTarjeta("#1E293B", "#334155", 10));
                        subItem.setPadding(18, 12, 18, 12);
                        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        subP.setMargins(0, 0, 0, (b < bloques.length() - 1) ? 8 : 0);
                        subItem.setLayoutParams(subP);

                        TextView tvSubT = new TextView(this);
                        tvSubT.setText(bObj.optString("subtitulo"));
                        tvSubT.setTextColor(Color.WHITE);
                        tvSubT.setTextSize(13f);
                        tvSubT.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView tvSubM = new TextView(this);
                        tvSubM.setText(bObj.optString("metricas"));
                        tvSubM.setTextColor(Color.parseColor("#38BDF8"));
                        tvSubM.setTextSize(12f);
                        tvSubM.setPadding(0, 3, 0, 0);

                        subItem.addView(tvSubT);
                        subItem.addView(tvSubM);
                        card.addView(subItem);
                    }
                } else {
                    // Compatibilidad con registros anteriores
                    LinearLayout subItem = new LinearLayout(this);
                    subItem.setOrientation(LinearLayout.VERTICAL);
                    subItem.setBackground(crearTarjeta("#1E293B", "#334155", 10));
                    subItem.setPadding(18, 12, 18, 12);

                    TextView tvDatos = new TextView(this);
                    String detalle = s.has("detalle")
                            ? s.getString("detalle")
                            : "Reps: " + s.optInt("reps") + " / " + s.optInt("meta") + "  ·  Carga: " + s.optDouble("peso") + " kg";
                    tvDatos.setText(detalle);
                    tvDatos.setTextColor(Color.parseColor("#E2E8F0"));
                    tvDatos.setTextSize(12.5f);
                    subItem.addView(tvDatos);
                    card.addView(subItem);
                }

                // Gesto táctil de deslizamiento hacia la izquierda
                final float[] startX = {0f};
                final float maxDesplazamiento = -180f;

                card.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX[0] = event.getRawX() - v.getTranslationX();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            float deltaX = event.getRawX() - startX[0];
                            if (deltaX <= 0 && deltaX >= maxDesplazamiento) {
                                v.setTranslationX(deltaX);
                            }
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (v.getTranslationX() < maxDesplazamiento / 2f) {
                                v.animate().translationX(maxDesplazamiento).setDuration(150).start();
                            } else {
                                v.animate().translationX(0f).setDuration(150).start();
                            }
                            return true;
                    }
                    return false;
                });

                swipeWrapper.addView(fondoRojo);
                swipeWrapper.addView(card);
                contenedorListaRegistros.addView(swipeWrapper);
            } catch (Exception ignored) {}
        }
    }
}