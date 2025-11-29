package com.tienda.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.InputStream;


public class FirestoreConfig {

    private static Firestore db = null;
    private static boolean inicializado = false;
    private static String projectId = "";

    /**
     * Inicializa la conexión con Firestore
     * @return true si la inicialización fue exitosa
     */
    public static boolean inicializar() {
        if (inicializado && db != null) {
            System.out.println("✓ Firestore ya está inicializado");
            return true;
        }

        try {
            // Cargar las credenciales desde el archivo JSON
            InputStream serviceAccount = FirestoreConfig.class
                    .getClassLoader()
                    .getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                System.err.println("✗ Error: No se encontró el archivo serviceAccountKey.json");
                System.err.println("  Colócalo en: src/main/resources/serviceAccountKey.json");
                return false;
            }

            // Configurar las opciones de Firebase
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            // Inicializar Firebase App (solo si no está inicializado)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✓ Firebase App inicializado correctamente");
            }

            // Obtener instancia de Firestore
            db = FirestoreClient.getFirestore();
            projectId = FirebaseApp.getInstance().getOptions().getProjectId();
            inicializado = true;

            System.out.println("✓ Conexión exitosa a Firestore");
            System.out.println("  Project ID: " + projectId);

            return true;

        } catch (Exception e) {
            System.err.println("✗ Error al inicializar Firestore: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la instancia de Firestore
     * @return Instancia de Firestore o null si no está inicializado
     */
    public static Firestore getDatabase() {
        if (!inicializado) {
            System.err.println("⚠️ Firestore no está inicializado. Llamando a inicializar()...");
            inicializar();
        }
        return db;
    }

    /**
     * Verifica si Firestore está conectado y funcionando
     * @return true si está conectado
     */
    public static boolean estaConectado() {
        if (!inicializado) {
            inicializar();
        }
        return db != null && inicializado;
    }

    /**
     * Obtiene el ID del proyecto de Firebase
     * @return Project ID
     */
    public static String getProjectId() {
        return projectId;
    }

    /**
     * Cierra la conexión con Firestore
     */
    public static void cerrar() {
        if (inicializado) {
            try {
                // Firebase no requiere cerrar explícitamente la conexión
                // pero podemos limpiar las referencias
                db = null;
                inicializado = false;
                System.out.println("✓ Conexión Firestore cerrada");
            } catch (Exception e) {
                System.err.println("✗ Error al cerrar Firestore: " + e.getMessage());
            }
        }
    }

    /**
     * Método de prueba de conexión
     * @return true si la prueba es exitosa
     */
    public static boolean probarConexion() {
        try {
            if (!inicializado) {
                inicializar();
            }

            if (db != null) {
                // Intentar leer una colección para verificar la conexión
                db.collection("productos").limit(1).get().get();
                System.out.println("✓ Prueba de conexión exitosa");
                System.out.println("  Base de datos: Firestore Cloud");
                System.out.println("  Project ID: " + projectId);
                return true;
            }
        } catch (Exception e) {
            System.err.println("✗ Prueba de conexión fallida");
            System.err.println("  Verifica que:");
            System.err.println("  1. El archivo serviceAccountKey.json exista");
            System.err.println("  2. Las credenciales sean válidas");
            System.err.println("  3. Tengas conexión a Internet");
            System.err.println("  4. El proyecto de Firebase esté activo");
            e.printStackTrace();
        }
        return false;
    }


}