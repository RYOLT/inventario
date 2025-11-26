package com.tienda.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConexionDB {
    private static Firestore firestore = null;
    private static boolean inicializado = false;

    // Nombre del archivo de credenciales (debe estar en src/main/resources/)
    private static final String CREDENTIALS_FILE = "inventario-tienda-firebase.json";

    /**
     * Inicializa Firebase y retorna la instancia de Firestore
     */
    public static Firestore getFirestore() {
        if (!inicializado) {
            inicializarFirebase();
        }
        return firestore;
    }

    /**
     * Inicializa la conexión con Firebase
     */
    private static void inicializarFirebase() {
        try {
            // Intentar cargar las credenciales desde resources
            InputStream serviceAccount = ConexionDB.class.getClassLoader()
                    .getResourceAsStream(CREDENTIALS_FILE);

            if (serviceAccount == null) {
                System.err.println("❌ ERROR: No se encontró el archivo de credenciales: " + CREDENTIALS_FILE);
                System.err.println("   Coloca el archivo JSON en: src/main/resources/");
                System.err.println("   Descárgalo desde: Firebase Console → Project Settings → Service Accounts");
                return;
            }

            // Configurar Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Inicializar Firebase (solo si no está inicializado)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase inicializado correctamente");
            }

            // Obtener instancia de Firestore
            firestore = FirestoreClient.getFirestore();
            inicializado = true;

            System.out.println("✅ Conexión a Firestore establecida");
            System.out.println("   Base de datos: Cloud Firestore");
            System.out.println("   Proyecto: " + FirebaseApp.getInstance().getOptions().getProjectId());

        } catch (IOException e) {
            System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifica si Firebase está correctamente inicializado
     */
    public static boolean estaConectado() {
        return inicializado && firestore != null;
    }

    /**
     * Obtiene el ID del proyecto Firebase
     */
    public static String obtenerProyectoId() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance().getOptions().getProjectId();
        }
        return "No inicializado";
    }

    /**
     * Cierra la conexión (opcional, Firebase se cierra automáticamente)
     */
    public static void cerrarConexion() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.getInstance().delete();
                firestore = null;
                inicializado = false;
                System.out.println("✅ Conexión a Firebase cerrada");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cerrar Firebase: " + e.getMessage());
        }
    }

    /**
     * Método de prueba de conexión
     */
    public static boolean probarConexion() {
        try {
            Firestore db = getFirestore();
            if (db != null && estaConectado()) {
                System.out.println("✅ Prueba de conexión exitosa a Firebase");
                System.out.println("   Proyecto ID: " + obtenerProyectoId());
                return true;
            } else {
                System.err.println("❌ Prueba de conexión fallida");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error en prueba de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}