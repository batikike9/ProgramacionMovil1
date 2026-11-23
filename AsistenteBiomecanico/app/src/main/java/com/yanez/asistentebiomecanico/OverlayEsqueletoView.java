package com.yanez.asistentebiomecanico;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

/**
 * Capa gráfica transparente superpuesta a CameraX para dibujar los 33 nodos y conexiones.
 */
public class OverlayEsqueletoView extends View {

    private List<NormalizedLandmark> landmarks;
    private final Paint paintLinea;
    private final Paint paintPunto;

    // Pares de conexiones óseas principales del cuerpo humano
    private static final int[][] CONEXIONES = {
            {11, 12}, {11, 13}, {13, 15}, {12, 14}, {14, 16}, // Torso superior y brazos
            {11, 23}, {12, 24}, {23, 24},                     // Tronco y cadera
            {23, 25}, {25, 27}, {24, 26}, {26, 28}            // Piernas
    };

    public OverlayEsqueletoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintLinea = new Paint();
        paintLinea.setColor(Color.parseColor("#38BDF8"));
        paintLinea.setStrokeWidth(7f);
        paintLinea.setAntiAlias(true);

        paintPunto = new Paint();
        paintPunto.setColor(Color.parseColor("#F43F5E"));
        paintPunto.setStyle(Paint.Style.FILL);
        paintPunto.setAntiAlias(true);
    }

    public void actualizarLandmarks(List<NormalizedLandmark> nuevosLandmarks) {
        this.landmarks = nuevosLandmarks;
        postInvalidate(); // Redibujar vista en el hilo de UI
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (landmarks == null || landmarks.size() < 29) return;

        float w = getWidth();
        float h = getHeight();

        // Dibujar conexiones óseas (espejo horizontal para cámara frontal: 1 - x)
        for (int[] par : CONEXIONES) {
            NormalizedLandmark p1 = landmarks.get(par[0]);
            NormalizedLandmark p2 = landmarks.get(par[1]);
            canvas.drawLine((1f - p1.x()) * w, p1.y() * h, (1f - p2.x()) * w, p2.y() * h, paintLinea);
        }

        // Dibujar nodos articulares del cuerpo (índices 11 al 28)
        for (int i = 11; i <= 28; i++) {
            NormalizedLandmark p = landmarks.get(i);
            canvas.drawCircle((1f - p.x()) * w, p.y() * h, 11f, paintPunto);
        }
    }
}