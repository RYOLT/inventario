package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
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
        this.firestore = FirestoreConfig.getFirestore();
    }

    /**
     * Agregar un nuevo producto a Firestore
     */
    public boolean agregarProducto(Producto producto) {
        try {
            // Crear el documento con datos del producto
            Map<String, Object> data = productoToMap(producto);

            // Agregar el producto (Firestore genera el ID automáticamente)
            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Guardar el ID generado en el producto
            producto.setIdProducto(docRef.getId().hashCode()); // Convertir String ID a int

            System.out.println("✓ Producto agregado con ID: " + docRef.getId());
            return true;

        } catch (Exception e) {
            System.err.println("✗ Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener todos los productos activos
     */
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();

        try {
            // Consulta para obtener solo productos activos
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .orderBy("nombreProducto")
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null) {
                    productos.add(producto);
                }
            }

            System.out.println("✓ Se obtuvieron " + productos.size() + " productos");

        } catch (Exception e) {
            System.err.println("✗ Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Buscar productos por nombre
     */
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();

        try {
            // Firestore no tiene búsqueda LIKE, así que obtenemos todos y filtramos
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null &&
                        producto.getNombreProducto().toLowerCase().contains(nombre.toLowerCase())) {
                    productos.add(producto);
                }
            }

            System.out.println("✓ Búsqueda completada: " + productos.size() + " productos encontrados");

        } catch (Exception e) {
            System.err.println("✗ Error al buscar productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Buscar productos por categoría
     */
    public List<Producto> buscarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .whereEqualTo("idCategoria", idCategoria)
                    .orderBy("nombreProducto")
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null) {
                    productos.add(producto);
                }
            }

            System.out.println("✓ Productos por categoría: " + productos.size());

        } catch (Exception e) {
            System.err.println("✗ Error al buscar por categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<Producto> obtenerProductosStockBajo() {
        List<Producto> productos = new ArrayList<>();

        try {
            // Obtener todos los productos activos y filtrar por stock
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Producto producto = documentToProducto(document);
                if (producto != null && producto.isBajoStock()) {
                    productos.add(producto);
                }
            }

            // Ordenar por stock actual
            productos.sort((p1, p2) -> Integer.compare(p1.getStockActual(), p2.getStockActual()));

            System.out.println("✓ Productos con stock bajo: " + productos.size());

        } catch (Exception e) {
            System.err.println("✗ Error al obtener productos con stock bajo: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Obtener producto por ID del documento
     */
    public Producto obtenerProductoPorId(String docId) {
        try {
            ApiFuture<DocumentSnapshot> future = firestore.collection(COLLECTION_NAME)
                    .document(docId)
                    .get();

            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return documentToProducto(document);
            } else {
                System.out.println("⚠ Producto no encontrado con ID: " + docId);
            }

        } catch (Exception e) {
            System.err.println("✗ Error al obtener producto: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtener producto por ID (busca en todos los documentos)
     */
    public Producto obtenerProductoPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProducto", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProducto(querySnapshot.getDocuments().get(0));
            }

        } catch (Exception e) {
            System.err.println("✗ Error al obtener producto por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualizar un producto existente
     */
    public boolean actualizarProducto(Producto producto) {
        try {
            // Buscar el documento por idProducto
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProducto", producto.getIdProducto())
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();
                Map<String, Object> data = productoToMap(producto);

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .set(data);

                writeResult.get(); // Esperar a que termine
                System.out.println("✓ Producto actualizado correctamente");
                return true;
            } else {
                System.err.println("⚠ Producto no encontrado para actualizar");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar producto (soft delete - marca como inactivo)
     */
    public boolean eliminarProducto(int id) {
        try {
            // Buscar el documento
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProducto", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                // Actualizar solo el campo activo
                Map<String, Object> updates = new HashMap<>();
                updates.put("activo", false);

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .update(updates);

                writeResult.get();
                System.out.println("✓ Producto marcado como inactivo");
                return true;
            } else {
                System.err.println("⚠ Producto no encontrado para eliminar");
                return false;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buscar producto por código de barras
     */
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("codigoBarras", codigoBarras)
                    .whereEqualTo("activo", true)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProducto(querySnapshot.getDocuments().get(0));
            }

        } catch (Exception e) {
            System.err.println("✗ Error al buscar por código de barras: " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualizar stock de un producto
     */
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProducto", idProducto)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                Map<String, Object> updates = new HashMap<>();
                updates.put("stockActual", nuevoStock);

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .update(updates);

                writeResult.get();
                return true;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al actualizar stock: " + e.getMessage());
        }

        return false;
    }

    /**
     * Contar productos activos
     */
    public int contarProductosActivos() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            return future.get().size();

        } catch (Exception e) {
            System.err.println("✗ Error al contar productos: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Obtener valor total del inventario
     */
    public double obtenerValorTotalInventario() {
        double total = 0.0;

        try {
            List<Producto> productos = obtenerTodosLosProductos();
            for (Producto p : productos) {
                total += p.getPrecioUnitario() * p.getStockActual();
            }
        } catch (Exception e) {
            System.err.println("✗ Error al calcular valor del inventario: " + e.getMessage());
        }

        return total;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convertir Producto a Map para Firestore
     */
    private Map<String, Object> productoToMap(Producto producto) {
        Map<String, Object> data = new HashMap<>();
        data.put("idProducto", producto.getIdProducto());
        data.put("nombreProducto", producto.getNombreProducto());
        data.put("descripcion", producto.getDescripcion());
        data.put("precioUnitario", producto.getPrecioUnitario());
        data.put("stockActual", producto.getStockActual());
        data.put("stockMinimo", producto.getStockMinimo());
        data.put("idCategoria", producto.getIdCategoria());
        data.put("idProveedor", producto.getIdProveedor());
        data.put("codigoBarras", producto.getCodigoBarras());
        data.put("activo", producto.isActivo());
        data.put("nombreCategoria", producto.getNombreCategoria());
        data.put("nombreProveedor", producto.getNombreProveedor());
        data.put("fechaRegistro", System.currentTimeMillis());
        data.put("ultimaActualizacion", System.currentTimeMillis());
        return data;
    }

    /**
     * Convertir DocumentSnapshot a Producto
     */
    private Producto documentToProducto(DocumentSnapshot document) {
        try {
            Producto producto = new Producto();
            producto.setIdProducto(document.getLong("idProducto") != null ?
                    document.getLong("idProducto").intValue() : 0);
            producto.setNombreProducto(document.getString("nombreProducto"));
            producto.setDescripcion(document.getString("descripcion"));
            producto.setPrecioUnitario(document.getDouble("precioUnitario") != null ?
                    document.getDouble("precioUnitario") : 0.0);
            producto.setStockActual(document.getLong("stockActual") != null ?
                    document.getLong("stockActual").intValue() : 0);
            producto.setStockMinimo(document.getLong("stockMinimo") != null ?
                    document.getLong("stockMinimo").intValue() : 0);
            producto.setIdCategoria(document.getLong("idCategoria") != null ?
                    document.getLong("idCategoria").intValue() : 0);
            producto.setIdProveedor(document.getLong("idProveedor") != null ?
                    document.getLong("idProveedor").intValue() : 0);
            producto.setCodigoBarras(document.getString("codigoBarras"));
            producto.setActivo(document.getBoolean("activo") != null ?
                    document.getBoolean("activo") : true);
            producto.setNombreCategoria(document.getString("nombreCategoria"));
            producto.setNombreProveedor(document.getString("nombreProveedor"));

            return producto;
        } catch (Exception e) {
            System.err.println("✗ Error al convertir documento a producto: " + e.getMessage());
            return null;
        }
    }
}