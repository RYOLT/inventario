package com.tienda.dao;

import com.tienda.database.ConexionDB;
import com.tienda.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    // Crear producto con validación de FK
    public boolean agregarProducto(Producto producto) {
        String sql = "INSERT INTO productos (nombre_producto, descripcion, precio_unitario, " +
                "stock_actual, stock_minimo, id_categoria, id_proveedor, codigo_barras, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, producto.getNombreProducto());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioUnitario());
            pstmt.setInt(4, producto.getStockActual());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setInt(6, producto.getIdCategoria());
            pstmt.setInt(7, producto.getIdProveedor());
            pstmt.setString(8, producto.getCodigoBarras());
            pstmt.setBoolean(9, producto.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                // Obtener el ID generado
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    producto.setIdProducto(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            if (e.getMessage().contains("foreign key")) {
                System.err.println("  Verifica que la categoría y proveedor existan");
            }
        }
        return false;
    }

    // Obtener todos los productos con JOIN para mostrar nombres de categoría y proveedor
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.activo = true
                ORDER BY p.nombre_producto
                """;

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto producto = crearProductoDesdeResultSet(rs);
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    // Buscar productos por nombre con JOIN
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.nombre_producto LIKE ? AND p.activo = true
                ORDER BY p.nombre_producto
                """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Producto producto = crearProductoDesdeResultSet(rs);
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
        }

        return productos;
    }

    // Buscar por categoría
    public List<Producto> buscarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.id_categoria = ? AND p.activo = true
                ORDER BY p.nombre_producto
                """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCategoria);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Producto producto = crearProductoDesdeResultSet(rs);
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por categoría: " + e.getMessage());
        }

        return productos;
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosStockBajo() {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.stock_actual <= p.stock_minimo AND p.activo = true
                ORDER BY p.stock_actual
                """;

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto producto = crearProductoDesdeResultSet(rs);
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }

        return productos;
    }

    // Obtener producto por ID
    public Producto obtenerProductoPorId(int id) {
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.id_producto = ?
                """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return crearProductoDesdeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }

        return null;
    }

    // Actualizar producto
    public boolean actualizarProducto(Producto producto) {
        String sql = """
                UPDATE productos 
                SET nombre_producto = ?, descripcion = ?, precio_unitario = ?, 
                    stock_actual = ?, stock_minimo = ?, id_categoria = ?, 
                    id_proveedor = ?, codigo_barras = ?, activo = ?
                WHERE id_producto = ?
                """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, producto.getNombreProducto());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setDouble(3, producto.getPrecioUnitario());
            pstmt.setInt(4, producto.getStockActual());
            pstmt.setInt(5, producto.getStockMinimo());
            pstmt.setInt(6, producto.getIdCategoria());
            pstmt.setInt(7, producto.getIdProveedor());
            pstmt.setString(8, producto.getCodigoBarras());
            pstmt.setBoolean(9, producto.isActivo());
            pstmt.setInt(10, producto.getIdProducto());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // Eliminar producto (soft delete - no elimina físicamente)
    public boolean eliminarProducto(int id) {
        String sql = "UPDATE productos SET activo = false WHERE id_producto = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // Buscar por código de barras
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        String sql = """
                SELECT p.*, c.nombre_categoria, pr.nombre_proveedor
                FROM productos p
                INNER JOIN categorias c ON p.id_categoria = c.id_categoria
                INNER JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor
                WHERE p.codigo_barras = ? AND p.activo = true
                """;

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoBarras);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return crearProductoDesdeResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por código de barras: " + e.getMessage());
        }

        return null;
    }

    // Actualizar stock de un producto
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        String sql = "UPDATE productos SET stock_actual = ? WHERE id_producto = ?";

        try (Connection conn = ConexionDB.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setInt(2, idProducto);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    // Obtener total de productos activos
    public int contarProductosActivos() {
        String sql = "SELECT COUNT(*) as total FROM productos WHERE activo = true";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar productos: " + e.getMessage());
        }

        return 0;
    }

    // Obtener valor total del inventario
    public double obtenerValorTotalInventario() {
        String sql = "SELECT SUM(precio_unitario * stock_actual) as total FROM productos WHERE activo = true";

        try (Connection conn = ConexionDB.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular valor del inventario: " + e.getMessage());
        }

        return 0.0;
    }

    // Método auxiliar privado para crear objeto Producto desde ResultSet
    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        Producto producto = new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre_producto"),
                rs.getString("descripcion"),
                rs.getDouble("precio_unitario"),
                rs.getInt("stock_actual"),
                rs.getInt("stock_minimo"),
                rs.getInt("id_categoria"),
                rs.getInt("id_proveedor"),
                rs.getString("codigo_barras"),
                rs.getBoolean("activo")
        );

        producto.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        producto.setUltimaActualizacion(rs.getTimestamp("ultima_actualizacion"));
        producto.setNombreCategoria(rs.getString("nombre_categoria"));
        producto.setNombreProveedor(rs.getString("nombre_proveedor"));

        return producto;
    }
}