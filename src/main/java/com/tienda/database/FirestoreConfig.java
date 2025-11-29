package com.tienda.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirestoreConfig {
    private static Firestore firestore;
    private static boolean inicializado = false;

    /**
     * Inicializa la conexión con Firestore
     *
     * PASOS PARA CONFIGURAR:
     *
     * 1. Ve a: https://console.firebase.google.com/
     * 2. Selecciona tu proyecto
     * 3. Ve a: Configuración del proyecto (ícono engranaje) → Cuentas de servicio
     * 4. Haz clic en "Generar nueva clave privada"
     * 5. Descarga el archivo JSON
     * 6. Coloca el archivo en: src/main/resources/serviceAccountKey.json
     *
     * Si tu archivo tiene otro nombre, cámbialo en SERVICE_ACCOUNT_PATH
     */
    private static final String SERVICE_ACCOUNT_PATH = "src/main/resources/serviceAccountKey.json";

    public static void inicializar() {
        if (inicializado) {
            System.out.println("✓ Firestore ya está inicializado");
            return;
        }

        try {
            // Cargar las credenciales desde el archivo JSON
            InputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH);

            // Configurar las opciones de Firebase
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Inicializar Firebase App
            FirebaseApp.initializeApp(options);

            // Obtener instancia de Firestore
            firestore = FirestoreClient.getFirestore();

            inicializado = true;
            System.out.println("✓ Conexión exitosa a Cloud Firestore");
            System.out.println("  Proyecto: " + FirebaseApp.getInstance().getOptions().getProjectId());

        } catch (IOException e) {
            System.err.println("✗ Error al inicializar Firestore");
            System.err.println("  Verifica que:");
            System.err.println("  1. El archivo JSON de credenciales esté en: " + SERVICE_ACCOUNT_PATH);
            System.err.println("  2. El archivo sea válido (descargado de Firebase Console)");
            System.err.println("  3. Tengas permisos de lectura/escritura en Firestore");
            System.err.println("\n  PASOS PARA OBTENER EL ARCHIVO:");
            System.err.println("  • Ve a: https://console.firebase.google.com/");
            System.err.println("  • Configuración → Cuentas de servicio → Generar clave privada");
            e.printStackTrace();
            inicializado = false;
        } catch (Exception e) {
            System.err.println("✗ Error inesperado al inicializar Firestore");
            e.printStackTrace();
            inicializado = false;
        }
    }

    /**
     * Obtiene la instancia de Firestore
     */
    public static Firestore getFirestore() {
        if (!inicializado) {
            inicializar();
        }
        return firestore;
    }

    /**
     * Verifica si la conexión está activa
     */
    public static boolean estaConectado() {
        return inicializado && firestore != null;
    }

    /**
     * Cierra la conexión
     */
    public static void cerrarConexion() {
        if (inicializado && FirebaseApp.getApps().size() > 0) {
            try {
                firestore.close();
                FirebaseApp.getInstance().delete();
                inicializado = false;
                System.out.println("✓ Conexión a Firestore cerrada");
            } catch (Exception e) {
                System.err.println("✗ Error al cerrar Firestore: " + e.getMessage());
            }
        }
    }

    /**
     * Método de prueba para verificar la conexión
     */
    public static boolean probarConexion() {
        try {
            inicializar();
            if (inicializado) {
                System.out.println("✓ Prueba de conexión exitosa");
                System.out.println("  Base de datos: Cloud Firestore");
                System.out.println("  Proyecto: " + FirebaseApp.getInstance().getOptions().getProjectId());
                return true;
            }
        } catch (Exception e) {
            System.err.println("✗ Prueba de conexión fallida");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Obtiene el ID del proyecto
     */
    public static String getProjectId() {
        if (inicializado) {
            return FirebaseApp.getInstance().getOptions().getProjectId();
        }
        return "No inicializado";
    }
}