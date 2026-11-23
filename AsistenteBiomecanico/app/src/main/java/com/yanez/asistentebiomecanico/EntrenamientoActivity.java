package com.yanez.asistentebiomecanico;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntrenamientoActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private PreviewView previewView;
    private OverlayEsqueletoView overlayEsqueleto;
    private LinearLayout bannerAlerta, layoutDescanso, layoutResumenFinal, contenedorTarjetasResumen;
    private TextView tvAlerta, tvContador, tvInfoSerie, tvEjercicioActivo, tvPesoActivo;
    private TextView tvSiguienteSerie, tvSegundosDescanso, tvTituloResumen, tvSubtituloResumen;

    private PoseLandmarker poseLandmarker;
    private EvaluadorBiomecanico evaluador;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private TextToSpeech tts;
    private boolean ttsListo = false;
    private CountDownTimer timerDescanso;

    private static class BloqueSerie {
        String clave;
        String nombre;
        int metaReps;
        double pesoKg;
        int descansoSeg;
        int numSerieEnEjercicio;
        int totalSeriesEjercicio;
    }

    private final List<BloqueSerie> colaSeries = new ArrayList<>();
    private final List<String[]> resultadosSesion = new ArrayList<>();
    private int indiceBloqueActual = 0;
    private String nombreRutina = "";
    private boolean esRutina = false;

    private boolean enDescanso = false;
    private boolean sesionFinalizada = false;
    private String ultimoMensajeVoz = "";
    private long tiempoUltimoVoz = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

        previewView = findViewById(R.id.previewView);
        overlayEsqueleto = findViewById(R.id.overlayEsqueleto);
        bannerAlerta = findViewById(R.id.bannerAlerta);
        tvAlerta = findViewById(R.id.tvAlerta);
        tvContador = findViewById(R.id.tvContador);
        tvInfoSerie = findViewById(R.id.tvInfoSerie);
        tvEjercicioActivo = findViewById(R.id.tvEjercicioActivo);
        tvPesoActivo = findViewById(R.id.tvPesoActivo);

        layoutDescanso = findViewById(R.id.layoutDescanso);
        tvSiguienteSerie = findViewById(R.id.tvSiguienteSerie);
        tvSegundosDescanso = findViewById(R.id.tvSegundosDescanso);

        layoutResumenFinal = findViewById(R.id.layoutResumenFinal);
        contenedorTarjetasResumen = findViewById(R.id.contenedorTarjetasResumen);
        tvTituloResumen = findViewById(R.id.tvTituloResumen);
        tvSubtituloResumen = findViewById(R.id.tvSubtituloResumen);

        Button btnCancelar = findViewById(R.id.btnCancelarSerie);
        Button btnTerminar = findViewById(R.id.btnTerminarSerie);
        Button btnOmitir = findViewById(R.id.btnOmitirDescanso);
        Button btnVolverMenu = findViewById(R.id.btnVolverMenuResumen);

        cargarColaDeEntrenamiento();

        evaluador = new EvaluadorBiomecanico();
        cameraExecutor = Executors.newSingleThreadExecutor();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(new Locale("es", "MX"));
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(new Locale("es", "ES"));
                }
                tts.setSpeechRate(0.95f);
                ttsListo = true;
            }
        });

        actualizarEncabezados();

        btnCancelar.setOnClickListener(v -> {
            sesionFinalizada = true;
            detenerRecursosCamara();
            finish();
        });

        btnTerminar.setOnClickListener(v -> concluirSerieActual());

        btnOmitir.setOnClickListener(v -> {
            if (timerDescanso != null) timerDescanso.cancel();
            finalizarDescanso();
        });

        btnVolverMenu.setOnClickListener(v -> finish());

        inicializarMediaPipe();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void cargarColaDeEntrenamiento() {
        String rutinaJsonStr = getIntent().getStringExtra("rutinaItemsJson");
        nombreRutina = getIntent().getStringExtra("nombreRutina");

        if (rutinaJsonStr != null && nombreRutina != null) {
            esRutina = true;
            try {
                JSONArray items = new JSONArray(rutinaJsonStr);
                for (int i = 0; i < items.length(); i++) {
                    JSONObject obj = items.getJSONObject(i);
                    String clave = obj.getString("ejercicioClave");
                    int series = obj.getInt("series");
                    int reps = obj.getInt("reps");
                    double peso = obj.getDouble("peso");
                    int descanso = obj.getInt("descanso");

                    for (int s = 1; s <= series; s++) {
                        BloqueSerie b = new BloqueSerie();
                        b.clave = clave;
                        b.nombre = GestorDatos.obtenerInfoEjercicio(clave)[0];
                        b.metaReps = reps;
                        b.pesoKg = peso;
                        b.descansoSeg = descanso;
                        b.numSerieEnEjercicio = s;
                        b.totalSeriesEjercicio = series;
                        colaSeries.add(b);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (colaSeries.isEmpty()) {
            esRutina = false;
            String clave = getIntent().getStringExtra("ejercicioClave");
            if (clave == null) clave = "laterales";
            BloqueSerie b = new BloqueSerie();
            b.clave = clave;
            b.nombre = GestorDatos.obtenerInfoEjercicio(clave)[0];
            b.metaReps = getIntent().getIntExtra("metaReps", 10);
            b.pesoKg = getIntent().getDoubleExtra("pesoKg", 10.0);
            b.descansoSeg = 30;
            b.numSerieEnEjercicio = 1;
            b.totalSeriesEjercicio = 1;
            colaSeries.add(b);
        }
    }

    private BloqueSerie bloqueActual() {
        return colaSeries.get(indiceBloqueActual);
    }

    private void actualizarEncabezados() {
        BloqueSerie b = bloqueActual();
        tvEjercicioActivo.setText(b.nombre);
        tvPesoActivo.setText("Carga: " + b.pesoKg + " kg");
        tvContador.setText(evaluador.contador + " / " + b.metaReps);
        if (esRutina) {
            tvInfoSerie.setText("RUTINA: " + nombreRutina.toUpperCase()
                    + " · BLOQUE " + (indiceBloqueActual + 1) + "/" + colaSeries.size()
                    + " (SERIE " + b.numSerieEnEjercicio + "/" + b.totalSeriesEjercicio + ")");
        } else {
            tvInfoSerie.setText("ENTRENAMIENTO INDIVIDUAL");
        }
    }

    private void concluirSerieActual() {
        if (sesionFinalizada) return;
        sesionFinalizada = true;

        BloqueSerie b = bloqueActual();
        int repsLogradas = evaluador.contador;
        String[] diag = GestorDatos.generarDiagnostico(b.clave, repsLogradas, b.metaReps);

        String tituloBloque = b.nombre + (esRutina ? " · Serie " + b.numSerieEnEjercicio + "/" + b.totalSeriesEjercicio : "");
        String metricas = repsLogradas + " / " + b.metaReps + " reps  ·  " + b.pesoKg + " kg";
        resultadosSesion.add(new String[]{tituloBloque, metricas, diag[0], diag[1]});

        if (indiceBloqueActual + 1 < colaSeries.size()) {
            sesionFinalizada = false;
            iniciarPantallaDescanso(b.descansoSeg);
        } else {
            detenerRecursosCamara();
            guardarYMostrarResumenFinal();
        }
    }

    private GradientDrawable crearTarjetaRedondeada(String colorHex, float radioDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(colorHex));
        gd.setCornerRadius(radioDp * getResources().getDisplayMetrics().density);
        return gd;
    }

    private void guardarYMostrarResumenFinal() {
        JSONArray bloquesJson = new JSONArray();
        String diagPrincipal = "";

        for (int i = 0; i < resultadosSesion.size(); i++) {
            String[] res = resultadosSesion.get(i);
            if (i == 0) diagPrincipal = res[2];
            try {
                JSONObject bObj = new JSONObject();
                bObj.put("subtitulo", res[0]);
                bObj.put("metricas", res[1]);
                bloquesJson.put(bObj);
            } catch (Exception ignored) {}
        }

        String tituloRegistro = esRutina
                ? "RUTINA: " + nombreRutina.toUpperCase()
                : bloqueActual().nombre;

        GestorDatos.guardarRegistroEstructurado(this, tituloRegistro, esRutina, bloquesJson, diagPrincipal);

        layoutResumenFinal.setVisibility(View.VISIBLE);
        tvTituloResumen.setText(esRutina ? "RUTINA COMPLETADA" : "RESUMEN DE ENTRENAMIENTO");
        tvSubtituloResumen.setText(tituloRegistro + " · Guardado en Registros");

        contenedorTarjetasResumen.removeAllViews();
        for (String[] res : resultadosSesion) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(crearTarjetaRedondeada("#131F37", 14));
            card.setPadding(28, 24, 28, 24);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);

            TextView tvT = new TextView(this);
            tvT.setText(res[0]);
            tvT.setTextColor(Color.parseColor("#38BDF8"));
            tvT.setTextSize(16f);
            tvT.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvM = new TextView(this);
            tvM.setText(res[1]);
            tvM.setTextColor(Color.parseColor("#10B981"));
            tvM.setTextSize(14f);
            tvM.setTypeface(null, android.graphics.Typeface.BOLD);
            tvM.setPadding(0, 8, 0, 10);

            TextView tvD = new TextView(this);
            tvD.setText("Diagnóstico: " + res[2] + "\n\nRecomendación:\n" + res[3]);
            tvD.setTextColor(Color.parseColor("#E2E8F0"));
            tvD.setTextSize(13f);

            card.addView(tvT);
            card.addView(tvM);
            card.addView(tvD);
            contenedorTarjetasResumen.addView(card);
        }

        hablar("Entrenamiento completado y guardado", true);
    }

    private void detenerRecursosCamara() {
        try {
            if (cameraProvider != null) cameraProvider.unbindAll();
            if (cameraExecutor != null && !cameraExecutor.isShutdown()) cameraExecutor.shutdownNow();
        } catch (Exception ignored) {}
    }

    private void iniciarPantallaDescanso(int segundos) {
        enDescanso = true;
        layoutDescanso.setVisibility(View.VISIBLE);
        BloqueSerie siguiente = colaSeries.get(indiceBloqueActual + 1);
        tvSiguienteSerie.setText("Siguiente: " + siguiente.nombre + " (Serie " + siguiente.numSerieEnEjercicio + "/" + siguiente.totalSeriesEjercicio + ")");
        hablar("Descansa " + segundos + " segundos", true);

        timerDescanso = new CountDownTimer(segundos * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvSegundosDescanso.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                finalizarDescanso();
            }
        }.start();
    }

    private void finalizarDescanso() {
        enDescanso = false;
        layoutDescanso.setVisibility(View.GONE);
        indiceBloqueActual++;
        evaluador.reiniciar();
        actualizarEncabezados();
        hablar("Siguiente serie: " + bloqueActual().nombre, true);
    }

    private void inicializarMediaPipe() {
        try {
            BaseOptions baseOptions = BaseOptions.builder()
                    .setModelAssetPath("pose_landmarker_lite.task")
                    .build();

            PoseLandmarker.PoseLandmarkerOptions options = PoseLandmarker.PoseLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener(this::procesarResultadoPose)
                    .build();

            poseLandmarker = PoseLandmarker.createFromOptions(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error cargando IA: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analizarFotograma);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Toast.makeText(this, "Error cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analizarFotograma(@NonNull ImageProxy imageProxy) {
        if (poseLandmarker == null || enDescanso || sesionFinalizada) {
            imageProxy.close();
            return;
        }

        try {
            Bitmap bitmap = imageProxy.toBitmap();
            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
            poseLandmarker.detectAsync(mpImage, SystemClock.uptimeMillis());
        } catch (Exception ignored) {
        } finally {
            imageProxy.close();
        }
    }

    private void procesarResultadoPose(PoseLandmarkerResult result, MPImage input) {
        if (enDescanso || sesionFinalizada) return;
        if (result.landmarks() != null && !result.landmarks().isEmpty()) {
            overlayEsqueleto.actualizarLandmarks(result.landmarks().get(0));
            boolean repOk = evaluador.evaluar(bloqueActual().clave, result.landmarks().get(0));
            runOnUiThread(() -> {
                if (!sesionFinalizada) {
                    actualizarUI(repOk);
                }
            });
        }
    }

    private void actualizarUI(boolean repCompletada) {
        BloqueSerie b = bloqueActual();
        tvAlerta.setText(evaluador.alerta.toUpperCase());
        bannerAlerta.setBackgroundColor(evaluador.colorSemaforo);
        tvContador.setText(evaluador.contador + " / " + b.metaReps);

        if (repCompletada) {
            if (evaluador.contador >= b.metaReps) {
                concluirSerieActual();
            } else {
                hablar("Bien, " + evaluador.contador, true);
            }
        } else if (evaluador.alerta.startsWith("Error") || evaluador.alerta.startsWith("Peligro")) {
            hablar(evaluador.alerta, false);
        }
    }

    private void hablar(String texto, boolean forzar) {
        if (!ttsListo || tts == null) return;
        long ahora = System.currentTimeMillis();

        if (!forzar && (ahora - tiempoUltimoVoz) < 3000) return;

        if (forzar || !texto.equals(ultimoMensajeVoz)) {
            ultimoMensajeVoz = texto;
            tiempoUltimoVoz = ahora;
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "TTS_" + ahora);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        }
    }

    @Override
    protected void onDestroy() {
        sesionFinalizada = true;
        if (timerDescanso != null) timerDescanso.cancel();
        detenerRecursosCamara();
        if (poseLandmarker != null) {
            try { poseLandmarker.close(); } catch (Exception ignored) {}
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}