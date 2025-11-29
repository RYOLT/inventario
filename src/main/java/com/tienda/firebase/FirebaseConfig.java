package com.tienda.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.IOException;
import java.io.InputStream;

public class FirebaseConfig {
    private static Firestore firestore;
    private static boolean inicializado = false;

    public static synchronized void inicializar() {
        // Verificar si ya está inicializado
        if (inicializado) {
            System.out.println("ℹ️ Firebase ya está inicializado");
            return;
        }

        try {
            // Cargar el archivo de credenciales desde resources
            InputStream serviceAccount = FirebaseConfig.class
                    .getResourceAsStream("/serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new IOException("❌ No se encontró serviceAccountKey.json en resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Solo inicializar si no hay ninguna app
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase inicializado correctamente");
            } else {
                System.out.println("ℹ️ Firebase ya estaba inicializado previamente");
            }

            firestore = FirestoreClient.getFirestore();
            inicializado = true;

        } catch (IOException e) {
            System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Firestore getFirestore() {
        if (!inicializado) {
            inicializar();
        }
        return firestore;
    }
}