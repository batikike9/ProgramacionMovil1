package com.yanez.practica10_appaudio;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ImageButton btnVaca, btnPerro, btnGato, btnLeon, btnGallo;
    MediaPlayer reproductor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Relacionar los botones
        btnVaca = findViewById(R.id.btnVaca);
        btnPerro = findViewById(R.id.btnPerro);
        btnGato = findViewById(R.id.btnGato);
        btnLeon = findViewById(R.id.btnLeon);
        btnGallo = findViewById(R.id.btnGallo);

        // Botón Vaca
        btnVaca.setOnClickListener(v -> {
            reproducirSonido(R.raw.vaca);
        });

        // Botón Perro
        btnPerro.setOnClickListener(v -> {
            reproducirSonido(R.raw.perro);
        });

        // Botón Gato
        btnGato.setOnClickListener(v -> {
            reproducirSonido(R.raw.gato);
        });

        // Botón León
        btnLeon.setOnClickListener(v -> {
            reproducirSonido(R.raw.leon);
        });

        // Botón Gallo
        btnGallo.setOnClickListener(v -> {
            reproducirSonido(R.raw.gallo);
        });
    }

    // Método para reproducir sonidos
    private void reproducirSonido(int sonido) {
        // Detener sonido anterior
        if (reproductor != null) {
            reproductor.release();
            reproductor = null;
        }

        // Crear nuevo reproductor
        reproductor = MediaPlayer.create(this, sonido);

        if (reproductor != null) {
            reproductor.setOnCompletionListener(mp -> {
                mp.release();
                if (reproductor == mp) {
                    reproductor = null;
                }
            });

            reproductor.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reproductor != null) {
            reproductor.release();
            reproductor = null;
        }
    }
}