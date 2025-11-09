package com.tienda.dao;

import com.tienda.database.ConexionDB;
import com.tienda.modelo.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre_categoria";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"),
                        rs.getString("descripcion")
                );
                categoria.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                categorias.add(categoria);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }

        return categorias;
    }

    // Agregar nueva categoría
    public boolean agregar(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre_categoria, descripcion) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoria.getNombreCategoria());
            pstmt.setString(2, categoria.getDescripcion());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al agregar categoría: " + e.getMessage());
            return false;
        }
    }

    // Obtener categoría por ID
    public Categoria obtenerPorId(int id) {
        String sql = "SELECT * FROM categorias WHERE id_categoria = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Categoria categoria = new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"),
                        rs.getString("descripcion")
                );
                categoria.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                return categoria;
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener categoría: " + e.getMessage());
        }

        return null;
    }
}