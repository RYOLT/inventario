package com.tienda.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Configuración para XAMPP (MySQL)
    private static final String URL = "jdbc:mysql://localhost:3306/inventario_tienda";
    private static final String USUARIO = "root";
    private static final String PASSWORD = ""; // Por defecto XAMPP no tiene contraseña

    private static Connection conexion = null;

    // Obtener conexión
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                // Cargar el driver de MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establecer conexión
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("✓ Conexión exitosa a MySQL (XAMPP)");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Error: Driver de MySQL no encontrado");
            System.err.println("  Asegúrate de tener la dependencia mysql-connector-j en pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Error al conectar con la base de datos MySQL");
            System.err.println("  Verifica que:");
            System.err.println("  1. XAMPP esté ejecutándose");
            System.err.println("  2. MySQL esté iniciado");
            System.err.println("  3. La base de datos 'inventario_tienda' exista");
            System.err.println("  4. Las credenciales sean correctas");
            e.printStackTrace();
        }
        return conexion;
    }

    // Cerrar conexión
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al cerrar la conexión: " + e.getMessage());
        }
    }

    // Metodo de prueba de conexión
    public static boolean probarConexion() {
        try {
            Connection conn = getConexion();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Prueba de conexión exitosa");
                System.out.println("  Base de datos: inventario_tienda");
                System.out.println("  Servidor: localhost:3306");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Prueba de conexión fallida");
            e.printStackTrace();
        }
        return false;
    }
}