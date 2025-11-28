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
    private static Firestore db = null;
    private static boolean inicializado = false;

    // Inicializar Firebase
    public static void inicializar() {
        if (inicializado) {
            return;
        }

        try {
            // Opción 1: Desde archivo en resources
            InputStream serviceAccount = ConexionDB.class
                    .getClassLoader()
                    .getResourceAsStream("firebase-credentials.json");

            // Opción 2: Desde archivo en disco (si prefieres esta opción)
            // FileInputStream serviceAccount = new FileInputStream("ruta/a/firebase-credentials.json");

            if (serviceAccount == null) {
                throw new IOException("No se encontró el archivo firebase-credentials.json en resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            db = FirestoreClient.getFirestore();
            inicializado = true;

            System.out.println("✓ Conexión exitosa a Firebase Firestore");

        } catch (IOException e) {
            System.err.println("✗ Error al inicializar Firebase");
            System.err.println("  Verifica que:");
            System.err.println("  1. El archivo firebase-credentials.json esté en src/main/resources/");
            System.err.println("  2. Las credenciales sean válidas");
            e.printStackTrace();
        }
    }

    // Obtener instancia de Firestore
    public static Firestore getFirestore() {
        if (!inicializado) {
            inicializar();
        }
        return db;
    }

    // Verificar si está inicializado
    public static boolean estaConectado() {
        return inicializado && db != null;
    }
}