package com.yanez.asistentebiomecanico;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Administra la base de conocimiento biomecánico (instrucciones), diagnósticos ROM
 * y la persistencia local de Rutinas Multi-Ejercicio y Registros Históricos estructurados.
 */
public class GestorDatos {

    private static final String PREFS_NAME = "AsistenteBiomecanicoPrefs";
    private static final String KEY_HISTORIAL = "historial_series";
    private static final String KEY_RUTINAS = "rutinas_guardadas";

    // [0]=Nombre, [1]=Zona/Cámara, [2]=Preparación, [3]=Ejecución, [4]=Consejos
    public static String[] obtenerInfoEjercicio(String clave) {
        switch (clave) {
            case "sentadilla":
                return new String[]{
                        "Sentadilla Libre (Squat)",
                        "Cuádriceps y Glúteos · Cámara: Perfil Lateral",
                        "Colócate de perfil a la cámara con los pies al ancho de los hombros y puntas ligeramente hacia afuera (15°-30°). Mantén el pecho erguido y la mirada al frente.",
                        "1. Inicia flexionando cadera y rodillas simultáneamente.\n2. Desciende controladamente hasta alcanzar al menos 90°-100° en la rodilla.\n3. Empuja con toda la planta del pie para regresar a la vertical.",
                        "• No encorves la zona lumbar hacia adelante.\n• Mantén los talones apoyados en todo momento."
                };
            case "biceps":
                return new String[]{
                        "Curl de Bíceps",
                        "Bíceps Braquial · Cámara: Perfil Lateral",
                        "Ponte de perfil con los brazos extendidos a los costados, codos pegados al torso y abdomen firme.",
                        "1. Flexiona el codo elevando el antebrazo sin adelantar el hombro.\n2. Contrae al máximo arriba (ángulo <= 50°).\n3. Desciende controladamente hasta extender el brazo.",
                        "• Evita balancear el tronco para tomar impulso.\n• Mantén la muñeca neutra."
                };
            case "laterales":
            default:
                return new String[]{
                        "Elevaciones Laterales",
                        "Deltoides Lateral · Cámara: Frontal",
                        "Colócate de frente a la cámara con los pies al ancho de la cadera, torso erguido y brazos relajados a los lados.",
                        "1. Eleva ambos brazos simultáneamente hacia los lados hasta la altura de los hombros (75°-90°).\n2. Mantén una ligera flexión de codo.\n3. Baja de forma controlada.",
                        "• Sube ambos brazos simétricamente.\n• No encogas los trapecios ni balancees la espalda."
                };
        }
    }

    public static String[] generarDiagnostico(String ejercicioClave, int repsValidas, int meta) {
        if (repsValidas == 0) {
            return new String[]{
                    "Sin repeticiones válidas (0/" + meta + "). Se detectaron compensaciones posturales o rango incompleto.",
                    "• Reduce la carga, mantén el tronco estable y completa todo el recorrido articular."
            };
        }
        switch (ejercicioClave) {
            case "sentadilla":
                return new String[]{
                        "Rango funcional alcanzado (" + repsValidas + "/" + meta + " reps). Buen control de flexión de rodilla y estabilidad lumbar.",
                        "• Mantén la cadencia controlada en el descenso y empuja con toda la planta del pie sin despegar talones."
                };
            case "biceps":
                return new String[]{
                        "Contracción concéntrica óptima (" + repsValidas + "/" + meta + " reps). Codo estable sin impulso de espalda.",
                        "• Controla la fase excéntrica (bajada de 2 segundos) para maximizar el estímulo en el bíceps braquial."
                };
            case "laterales":
            default:
                return new String[]{
                        "Abducción bilateral simétrica (" + repsValidas + "/" + meta + " reps). Elevación controlada sin balanceo axial.",
                        "• Evita elevar los hombros hacia las orejas y mantén una semiflexión constante en los codos."
                };
        }
    }

    // Guarda un registro estructurado con arreglo de sub-tarjetas (bloques)
    public static void guardarRegistroEstructurado(Context context, String titulo, boolean esRutina, JSONArray bloques, String diagnostico) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray lista = new JSONArray(prefs.getString(KEY_HISTORIAL, "[]"));
            JSONObject item = new JSONObject();
            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            item.put("fecha", fecha);
            item.put("titulo", titulo);
            item.put("esRutina", esRutina);
            item.put("bloques", bloques);
            item.put("diagnostico", diagnostico);

            lista.put(item);
            prefs.edit().putString(KEY_HISTORIAL, lista.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray obtenerHistorial(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return new JSONArray(prefs.getString(KEY_HISTORIAL, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static void eliminarSerie(Context context, int indexReal) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray lista = new JSONArray(prefs.getString(KEY_HISTORIAL, "[]"));
            JSONArray nueva = new JSONArray();
            for (int i = 0; i < lista.length(); i++) {
                if (i != indexReal) nueva.put(lista.get(i));
            }
            prefs.edit().putString(KEY_HISTORIAL, nueva.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void limpiarHistorial(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_HISTORIAL, "[]").apply();
    }

    public static void guardarRutinaMulti(Context context, String nombreRutina, JSONArray itemsEjercicios) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray rutinas = new JSONArray(prefs.getString(KEY_RUTINAS, "[]"));
            JSONObject r = new JSONObject();
            r.put("nombre", nombreRutina);
            r.put("items", itemsEjercicios);

            rutinas.put(r);
            prefs.edit().putString(KEY_RUTINAS, rutinas.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray obtenerRutinas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            return new JSONArray(prefs.getString(KEY_RUTINAS, "[]"));
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    public static void eliminarRutina(Context context, int index) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        try {
            JSONArray rutinas = new JSONArray(prefs.getString(KEY_RUTINAS, "[]"));
            JSONArray nueva = new JSONArray();
            for (int i = 0; i < rutinas.length(); i++) {
                if (i != index) nueva.put(rutinas.get(i));
            }
            prefs.edit().putString(KEY_RUTINAS, nueva.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}