package com.tienda.dao;

import com.google.cloud.firestore.*;
import com.tienda.firebase.FirebaseConfig;
import com.tienda.modelo.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ProductoFirestoreDAO {

    private final Firestore db;

    public ProductoFirestoreDAO() {
        this.db = FirebaseConfig.getFirestore();
    }

    // Agregar producto
    public boolean agregarProducto(Producto producto) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("nombre_producto", producto.getNombreProducto());
            data.put("descripcion", producto.getDescripcion());
            data.put("precio_unitario", producto.getPrecioUnitario());
            data.put("stock_actual", producto.getStockActual());
            data.put("stock_minimo", producto.getStockMinimo());
            data.put("id_categoria", producto.getIdCategoria());
            data.put("id_proveedor", producto.getIdProveedor());
            data.put("codigo_barras", producto.getCodigoBarras());
            data.put("activo", true);
            data.put("timestamp", System.currentTimeMillis());

            db.collection("productos").add(data).get();
            System.out.println("✅ Producto agregado a Firestore");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

    // Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();

        try {
            QuerySnapshot querySnapshot = db.collection("productos")
                    .whereEqualTo("activo", true)
                    .get()
                    .get();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(doc);
                productos.add(producto);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    // Actualizar producto
    public boolean actualizarProducto(String docId, Producto producto) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("nombre_producto", producto.getNombreProducto());
            data.put("descripcion", producto.getDescripcion());
            data.put("precio_unitario", producto.getPrecioUnitario());
            data.put("stock_actual", producto.getStockActual());
            data.put("stock_minimo", producto.getStockMinimo());
            data.put("id_categoria", producto.getIdCategoria());
            data.put("id_proveedor", producto.getIdProveedor());
            data.put("codigo_barras", producto.getCodigoBarras());
            data.put("timestamp", System.currentTimeMillis());

            db.collection("productos").document(docId).update(data).get();
            System.out.println("✅ Producto actualizado en Firestore");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    // Eliminar producto
    public boolean eliminarProducto(String docId) {
        try {
            db.collection("productos").document(docId).delete().get();
            System.out.println("✅ Producto eliminado de Firestore");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    // Buscar por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();

        try {
            QuerySnapshot querySnapshot = db.collection("productos")
                    .whereEqualTo("activo", true)
                    .get()
                    .get();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String nombreProducto = doc.getString("nombre_producto");
                if (nombreProducto != null && nombreProducto.toLowerCase().contains(nombre.toLowerCase())) {
                    productos.add(documentToProducto(doc));
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar productos: " + e.getMessage());
        }

        return productos;
    }

    // Productos con stock bajo
    public List<Producto> obtenerProductosStockBajo() {
        List<Producto> productos = new ArrayList<>();

        try {
            QuerySnapshot querySnapshot = db.collection("productos")
                    .whereEqualTo("activo", true)
                    .get()
                    .get();

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(doc);
                if (producto.isBajoStock()) {
                    productos.add(producto);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos con stock bajo: " + e.getMessage());
        }

        return productos;
    }

    // Convertir DocumentSnapshot a Producto
    private Producto documentToProducto(DocumentSnapshot doc) {
        Producto producto = new Producto();
        producto.setNombreProducto(doc.getString("nombre_producto"));
        producto.setDescripcion(doc.getString("descripcion"));

        // Manejo seguro de valores nulos
        Double precio = doc.getDouble("precio_unitario");
        producto.setPrecioUnitario(precio != null ? precio : 0.0);

        Long stockActual = doc.getLong("stock_actual");
        producto.setStockActual(stockActual != null ? stockActual.intValue() : 0);

        Long stockMinimo = doc.getLong("stock_minimo");
        producto.setStockMinimo(stockMinimo != null ? stockMinimo.intValue() : 0);

        Long idCategoria = doc.getLong("id_categoria");
        producto.setIdCategoria(idCategoria != null ? idCategoria.intValue() : 0);

        Long idProveedor = doc.getLong("id_proveedor");
        producto.setIdProveedor(idProveedor != null ? idProveedor.intValue() : 0);

        producto.setCodigoBarras(doc.getString("codigo_barras"));

        Boolean activo = doc.getBoolean("activo");
        producto.setActivo(activo != null ? activo : true);

        // IMPORTANTE: Guardar el ID del documento de Firestore
        producto.setIdProducto(doc.getId().hashCode());
        producto.setCodigoBarras(doc.getId()); // USAR CÓDIGO DE BARRAS PARA GUARDAR EL DOC ID TEMPORALMENTE

        return producto;
    }

    // Obtener producto por ID de documento
    public Producto obtenerProductoPorId(String docId) {
        try {
            DocumentSnapshot doc = db.collection("productos")
                    .document(docId)
                    .get()
                    .get();

            if (doc.exists()) {
                return documentToProducto(doc);
            } else {
                System.err.println("❌ Producto no encontrado con ID: " + docId);
                return null;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener producto por ID: " + e.getMessage());
            return null;
        }
    }
}