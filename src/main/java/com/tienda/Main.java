package com.tienda;

import com.tienda.database.FirestoreConfig;
import com.tienda.ui.VentanaInventario;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Inicializar Firestore primero
        System.out.println("=".repeat(50));
        System.out.println("INICIANDO SISTEMA DE INVENTARIO");
        System.out.println("=".repeat(50));

        FirestoreConfig.inicializar();

        if (!FirestoreConfig.estaConectado()) {
            System.err.println("\n❌ NO SE PUDO CONECTAR A FIRESTORE");
            System.err.println("La aplicación se cerrará.");

            JOptionPane.showMessageDialog(null,
                    "❌ Error de conexión con Firestore\n\n" +
                            "Verifica que:\n" +
                            "1. El archivo serviceAccountKey.json esté en src/main/resources/\n" +
                            "2. El archivo sea válido (descargado de Firebase Console)\n" +
                            "3. Tengas conexión a Internet\n\n" +
                            "La aplicación se cerrará.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        System.out.println("=".repeat(50));

        // Establecer el look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("⚠ No se pudo establecer el Look and Feel del sistema");
        }

        // Ejecutar la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            VentanaInventario ventana = new VentanaInventario();
            ventana.setVisible(true);
        });

        // Hook para cerrar la conexión al salir
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=".repeat(50));
            System.out.println("CERRANDO APLICACIÓN");
            FirestoreConfig.cerrarConexion();
            System.out.println("=".repeat(50));
        }));
    }
}