package com.tienda.dao;

import com.tienda.database.ConexionDB;
import com.tienda.modelo.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    // Obtener todos los proveedores
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY nombre_proveedor";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Proveedor proveedor = new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getString("ciudad"),
                        rs.getString("pais")
                );
                proveedor.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                proveedores.add(proveedor);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Agregar nuevo proveedor
    public boolean agregar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores (nombre_proveedor, telefono, email, direccion, ciudad, pais) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, proveedor.getNombreProveedor());
            pstmt.setString(2, proveedor.getTelefono());
            pstmt.setString(3, proveedor.getEmail());
            pstmt.setString(4, proveedor.getDireccion());
            pstmt.setString(5, proveedor.getCiudad());
            pstmt.setString(6, proveedor.getPais());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                // Obtener el ID generado
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    proveedor.setIdProveedor(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar proveedor: " + e.getMessage());
        }
        return false;
    }

    // Obtener proveedor por ID
    public Proveedor obtenerPorId(int id) {
        String sql = "SELECT * FROM proveedores WHERE id_proveedor = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Proveedor proveedor = new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getString("ciudad"),
                        rs.getString("pais")
                );
                proveedor.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                return proveedor;
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
        }

        return null;
    }

    // Actualizar proveedor
    public boolean actualizar(Proveedor proveedor) {
        String sql = "UPDATE proveedores SET nombre_proveedor = ?, telefono = ?, email = ?, " +
                "direccion = ?, ciudad = ?, pais = ? WHERE id_proveedor = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proveedor.getNombreProveedor());
            pstmt.setString(2, proveedor.getTelefono());
            pstmt.setString(3, proveedor.getEmail());
            pstmt.setString(4, proveedor.getDireccion());
            pstmt.setString(5, proveedor.getCiudad());
            pstmt.setString(6, proveedor.getPais());
            pstmt.setInt(7, proveedor.getIdProveedor());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            return false;
        }
    }

    // Eliminar proveedor
    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar proveedor: " + e.getMessage());
            // Si falla, probablemente hay productos asociados (FK constraint)
            if (e.getMessage().contains("foreign key")) {
                System.err.println("No se puede eliminar: hay productos asociados a este proveedor");
            }
            return false;
        }
    }

    // Buscar proveedores por nombre
    public List<Proveedor> buscarPorNombre(String nombre) {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores WHERE nombre_proveedor LIKE ? ORDER BY nombre_proveedor";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Proveedor proveedor = new Proveedor(
                        rs.getInt("id_proveedor"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getString("ciudad"),
                        rs.getString("pais")
                );
                proveedor.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                proveedores.add(proveedor);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Contar productos de un proveedor
    public int contarProductos(int idProveedor) {
        String sql = "SELECT COUNT(*) as total FROM productos WHERE id_proveedor = ? AND activo = true";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idProveedor);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar productos: " + e.getMessage());
        }

        return 0;
    }
}