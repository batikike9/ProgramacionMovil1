package com.yanez.asistentebiomecanico;

import android.graphics.Color;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

/**
 * Motor de evaluación biomecánica en Java.
 * Migra las heurísticas trigonométricas y máquinas de estado finitas con bloqueo anti-trampas.
 */
public class EvaluadorBiomecanico {

    public int contador = 0;
    public String etapa = "arriba";
    public String alerta = "Listo: Posiciónate frente a la cámara";
    public int colorSemaforo = Color.parseColor("#94A3B8"); // Gris neutro

    private boolean enRepeticion = false;
    private boolean repeticionAnulada = false;
    private double profundidadMinimaActual = 180.0;

    // Índices estándar de MediaPipe Pose (33 landmarks)
    private static final int L_SHOULDER = 11, R_SHOULDER = 12;
    private static final int L_ELBOW = 13, R_ELBOW = 14;
    private static final int L_WRIST = 15, R_WRIST = 16;
    private static final int L_HIP = 23, R_HIP = 24;
    private static final int L_KNEE = 25, R_KNEE = 26;
    private static final int L_ANKLE = 27, R_ANKLE = 28;

    public void reiniciar() {
        contador = 0;
        etapa = "arriba";
        alerta = "Listo: Comienza el movimiento";
        colorSemaforo = Color.parseColor("#94A3B8");
        enRepeticion = false;
        repeticionAnulada = false;
        profundidadMinimaActual = 180.0;
    }

    /**
     * Calcula el ángulo interior en grados en el vértice B dados tres landmarks A, B, C.
     */
    private int calcularAngulo(NormalizedLandmark a, NormalizedLandmark b, NormalizedLandmark c) {
        double radianes = Math.atan2(c.y() - b.y(), c.x() - b.x())
                - Math.atan2(a.y() - b.y(), a.x() - b.x());
        double angulo = Math.abs(radianes * 180.0 / Math.PI);
        if (angulo > 180.0) {
            angulo = 360.0 - angulo;
        }
        return (int) angulo;
    }

    /**
     * Calcula la inclinación de un segmento respecto a la vertical (0° = recto).
     */
    private int anguloInclinacionVertical(NormalizedLandmark p1, NormalizedLandmark p2) {
        double dx = Math.abs(p1.x() - p2.x());
        double dy = Math.abs(p1.y() - p2.y());
        if (dy == 0) return 90;
        return (int) Math.toDegrees(Math.atan(dx / dy));
    }

    /**
     * Evalúa el fotograma actual según el ejercicio seleccionado.
     * Retorna true si se acaba de completar una repetición técnicamente válida.
     */
    public boolean evaluar(String ejercicio, List<NormalizedLandmark> lm) {
        if (lm == null || lm.size() < 29) return false;

        switch (ejercicio) {
            case "sentadilla":
                return evaluarSentadilla(lm);
            case "biceps":
                return evaluarBiceps(lm);
            case "laterales":
            default:
                return evaluarLaterales(lm);
        }
    }

    // 1. EVALUADOR DE ELEVACIONES LATERALES (Simetría bilateral y elevación)
    private boolean evaluarLaterales(List<NormalizedLandmark> lm) {
        int angIzq = calcularAngulo(lm.get(L_HIP), lm.get(L_SHOULDER), lm.get(L_WRIST));
        int angDer = calcularAngulo(lm.get(R_HIP), lm.get(R_SHOULDER), lm.get(R_WRIST));
        int angMedio = (angIzq + angDer) / 2;
        int asimetria = Math.abs(angIzq - angDer);
        int incTorso = anguloInclinacionVertical(lm.get(R_SHOULDER), lm.get(R_HIP));

        boolean posturaInvalida = false;
        boolean repCompletada = false;

        // Reglas anti-trampas: asimetría entre brazos o balanceo de tronco
        if (asimetria > 25) {
            alerta = "Error: Sube ambos brazos parejos";
            colorSemaforo = Color.parseColor("#EF4444");
            repeticionAnulada = true;
            posturaInvalida = true;
        } else if (incTorso > 20) {
            alerta = "Error: No balancees la espalda";
            colorSemaforo = Color.parseColor("#EF4444");
            repeticionAnulada = true;
            posturaInvalida = true;
        }

        // Máquina de estados
        if (angMedio > 35) {
            enRepeticion = true;
            if (!posturaInvalida && !repeticionAnulada) {
                alerta = "Elevando (" + angMedio + "°)...";
                colorSemaforo = Color.parseColor("#38BDF8");
            }
            if (angMedio > (180 - profundidadMinimaActual)) {
                profundidadMinimaActual = 180 - angMedio;
            }
        }

        if (angMedio >= 75 && !repeticionAnulada && !posturaInvalida) {
            etapa = "arriba";
            alerta = "Altura óptima. Baja controlado";
            colorSemaforo = Color.parseColor("#10B981");
        }

        if (angMedio <= 28) {
            if (enRepeticion) {
                if (!repeticionAnulada && (180 - profundidadMinimaActual) >= 70) {
                    contador++;
                    repCompletada = true;
                    alerta = "Repetición válida";
                    colorSemaforo = Color.parseColor("#10B981");
                } else {
                    alerta = "Repetición anulada por técnica";
                    colorSemaforo = Color.parseColor("#EF4444");
                }
                enRepeticion = false;
                repeticionAnulada = false;
                profundidadMinimaActual = 180.0;
            } else if (!posturaInvalida) {
                alerta = "Listo: Eleva los brazos lateralmente";
                colorSemaforo = Color.parseColor("#94A3B8");
            }
            etapa = "abajo";
        }
        return repCompletada;
    }

    // 2. EVALUADOR DE SENTADILLA (Profundidad de rodilla e inclinación lumbar)
    private boolean evaluarSentadilla(List<NormalizedLandmark> lm) {
        int angRodilla = calcularAngulo(lm.get(R_HIP), lm.get(R_KNEE), lm.get(R_ANKLE));
        int incTorso = anguloInclinacionVertical(lm.get(R_SHOULDER), lm.get(R_HIP));

        boolean posturaInvalida = false;
        boolean repCompletada = false;

        if (incTorso > 65) {
            alerta = "Peligro: Pecho arriba, no dobles espalda";
            colorSemaforo = Color.parseColor("#EF4444");
            repeticionAnulada = true;
            posturaInvalida = true;
        }

        if (angRodilla < 140) {
            enRepeticion = true;
            if (!posturaInvalida && !repeticionAnulada) {
                alerta = "Descendiendo (" + angRodilla + "°)...";
                colorSemaforo = Color.parseColor("#38BDF8");
            }
            if (angRodilla < profundidadMinimaActual) {
                profundidadMinimaActual = angRodilla;
            }
        }

        if (angRodilla <= 100 && !repeticionAnulada && !posturaInvalida) {
            etapa = "abajo";
            alerta = "Profundidad óptima. Sube";
            colorSemaforo = Color.parseColor("#10B981");
        }

        if (angRodilla >= 148) {
            if (enRepeticion) {
                if (!repeticionAnulada && profundidadMinimaActual <= 120) {
                    contador++;
                    repCompletada = true;
                    alerta = "Sentadilla válida";
                    colorSemaforo = Color.parseColor("#10B981");
                } else {
                    alerta = "Repetición anulada";
                    colorSemaforo = Color.parseColor("#EF4444");
                }
                enRepeticion = false;
                repeticionAnulada = false;
                profundidadMinimaActual = 180.0;
            } else if (!posturaInvalida) {
                alerta = "Listo: Comienza el descenso";
                colorSemaforo = Color.parseColor("#94A3B8");
            }
            etapa = "arriba";
        }
        return repCompletada;
    }

    // 3. EVALUADOR DE CURL DE BÍCEPS (Flexión de codo y estabilidad de tronco)
    private boolean evaluarBiceps(List<NormalizedLandmark> lm) {
        int angCodo = calcularAngulo(lm.get(R_SHOULDER), lm.get(R_ELBOW), lm.get(R_WRIST));
        int incTorso = anguloInclinacionVertical(lm.get(R_SHOULDER), lm.get(R_HIP));

        boolean posturaInvalida = false;
        boolean repCompletada = false;

        if (incTorso > 22) {
            alerta = "Error: No te impulses con la espalda";
            colorSemaforo = Color.parseColor("#EF4444");
            repeticionAnulada = true;
            posturaInvalida = true;
        }

        if (angCodo < 145) {
            enRepeticion = true;
            if (!posturaInvalida && !repeticionAnulada) {
                alerta = "Flexionando (" + angCodo + "°)...";
                colorSemaforo = Color.parseColor("#38BDF8");
            }
            if (angCodo < profundidadMinimaActual) {
                profundidadMinimaActual = angCodo;
            }
        }

        if (angCodo <= 50 && !repeticionAnulada && !posturaInvalida) {
            etapa = "arriba";
            alerta = "Contracción máxima. Baja";
            colorSemaforo = Color.parseColor("#10B981");
        }

        if (angCodo >= 150) {
            if (enRepeticion) {
                if (!repeticionAnulada && profundidadMinimaActual <= 60) {
                    contador++;
                    repCompletada = true;
                    alerta = "Curl válido";
                    colorSemaforo = Color.parseColor("#10B981");
                } else {
                    alerta = "Repetición anulada";
                    colorSemaforo = Color.parseColor("#EF4444");
                }
                enRepeticion = false;
                repeticionAnulada = false;
                profundidadMinimaActual = 180.0;
            } else if (!posturaInvalida) {
                alerta = "Listo: Flexiona el codo";
                colorSemaforo = Color.parseColor("#94A3B8");
            }
            etapa = "abajo";
        }
        return repCompletada;
    }
}