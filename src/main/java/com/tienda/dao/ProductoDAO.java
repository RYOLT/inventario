package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.ConexionDB;
import com.tienda.modelo.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ProductoDAO {
    private static final String COLLECTION_NAME = "productos";
    private Firestore firestore;

    public ProductoDAO() {
        this.firestore = ConexionDB.getFirestore();
    }

    // Agregar producto
    public boolean agregarProducto(Producto producto) {
        try {
            Map<String, Object> productoMap = productoToMap(producto);

            // Generar ID automático
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            producto.setIdProducto(docRef.getId().hashCode()); // Generar ID numérico
            productoMap.put("id_producto", producto.getIdProducto());
            productoMap.put("docId", docRef.getId()); // Guardar ID del documento

            ApiFuture<WriteResult> result = docRef.set(productoMap);
            result.get(); // Esperar a que se complete

            System.out.println("✅ Producto agregado: " + docRef.getId());
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
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .orderBy("nombre_producto")
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null) {
                    productos.add(producto);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    // Buscar productos por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();

        try {
            // Firestore no soporta LIKE, buscamos todos y filtramos en Java
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null &&
                        producto.getNombreProducto().toLowerCase().contains(nombre.toLowerCase())) {
                    productos.add(producto);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar productos: " + e.getMessage());
        }

        return productos;
    }

    // Buscar por categoría
    public List<Producto> buscarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", idCategoria)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null) {
                    productos.add(producto);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar por categoría: " + e.getMessage());
        }

        return productos;
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosStockBajo() {
        List<Producto> productos = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null && producto.getStockActual() <= producto.getStockMinimo()) {
                    productos.add(producto);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos con stock bajo: " + e.getMessage());
        }

        return productos;
    }

    // Obtener producto por ID
    public Producto obtenerProductoPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProducto(querySnapshot.getDocuments().get(0));
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener producto por ID: " + e.getMessage());
        }

        return null;
    }

    // Actualizar producto
    public boolean actualizarProducto(Producto producto) {
        try {
            // Buscar el documento por id_producto
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", producto.getIdProducto())
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();
                Map<String, Object> productoMap = productoToMap(producto);
                productoMap.put("docId", docId);

                ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .set(productoMap);

                result.get();
                System.out.println("✅ Producto actualizado: " + docId);
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar producto: " + e.getMessage());
        }

        return false;
    }

    // Eliminar producto (soft delete)
    public boolean eliminarProducto(int id) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .update("activo", false);

                result.get();
                System.out.println("✅ Producto eliminado (soft delete): " + docId);
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar producto: " + e.getMessage());
        }

        return false;
    }

    // Buscar por código de barras
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("codigo_barras", codigoBarras)
                    .whereEqualTo("activo", true)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProducto(querySnapshot.getDocuments().get(0));
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar por código de barras: " + e.getMessage());
        }

        return null;
    }

    // Actualizar solo el stock
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", idProducto)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .update("stock_actual", nuevoStock);

                result.get();
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar stock: " + e.getMessage());
        }

        return false;
    }

    // Contar productos activos
    public int contarProductosActivos() {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            return query.get().size();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al contar productos: " + e.getMessage());
        }

        return 0;
    }

    // Obtener valor total del inventario
    public double obtenerValorTotalInventario() {
        double total = 0.0;

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Double precio = document.getDouble("precio_unitario");
                Long stock = document.getLong("stock_actual");

                if (precio != null && stock != null) {
                    total += precio * stock;
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al calcular valor del inventario: " + e.getMessage());
        }

        return total;
    }

    // Convertir Producto a Map para Firebase
    private Map<String, Object> productoToMap(Producto producto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id_producto", producto.getIdProducto());
        map.put("nombre_producto", producto.getNombreProducto());
        map.put("descripcion", producto.getDescripcion());
        map.put("precio_unitario", producto.getPrecioUnitario());
        map.put("stock_actual", producto.getStockActual());
        map.put("stock_minimo", producto.getStockMinimo());
        map.put("id_categoria", producto.getIdCategoria());
        map.put("id_proveedor", producto.getIdProveedor());
        map.put("codigo_barras", producto.getCodigoBarras());
        map.put("activo", producto.isActivo());
        map.put("nombre_categoria", producto.getNombreCategoria());
        map.put("nombre_proveedor", producto.getNombreProveedor());
        map.put("timestamp", FieldValue.serverTimestamp());
        return map;
    }

    // Convertir DocumentSnapshot a Producto
    private Producto documentToProducto(DocumentSnapshot document) {
        try {
            Producto producto = new Producto();
            producto.setIdProducto(document.getLong("id_producto").intValue());
            producto.setNombreProducto(document.getString("nombre_producto"));
            producto.setDescripcion(document.getString("descripcion"));
            producto.setPrecioUnitario(document.getDouble("precio_unitario"));
            producto.setStockActual(document.getLong("stock_actual").intValue());
            producto.setStockMinimo(document.getLong("stock_minimo").intValue());
            producto.setIdCategoria(document.getLong("id_categoria").intValue());
            producto.setIdProveedor(document.getLong("id_proveedor").intValue());
            producto.setCodigoBarras(document.getString("codigo_barras"));
            producto.setActivo(document.getBoolean("activo"));
            producto.setNombreCategoria(document.getString("nombre_categoria"));
            producto.setNombreProveedor(document.getString("nombre_proveedor"));
            return producto;
        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento: " + e.getMessage());
            return null;
        }
    }
}