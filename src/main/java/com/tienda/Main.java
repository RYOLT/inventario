package com.tienda;

import com.tienda.ui.VentanaInventario;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Establecer el look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Ejecutar la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            VentanaInventario ventana = new VentanaInventario();
            ventana.setVisible(true);
        });
    }
}