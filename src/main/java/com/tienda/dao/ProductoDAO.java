package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * DAO para gestionar Productos en Firebase Firestore
 * Reemplaza la versión con MySQL/JDBC
 */
public class ProductoDAO {

    private static final String COLLECTION_NAME = "productos";
    private Firestore db;

    public ProductoDAO() {
        this.db = FirestoreConfig.getDatabase();
        if (this.db == null) {
            System.err.println("⚠️ Error: Firestore no está inicializado en ProductoDAO");
        }
    }

    /**
     * Agregar producto con validación de FK
     */
    public boolean agregarProducto(Producto producto) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Verificar que existan la categoría y el proveedor
            if (!existeCategoria(producto.getIdCategoria())) {
                System.err.println("❌ Error: La categoría no existe");
                return false;
            }

            if (!existeProveedor(producto.getIdProveedor())) {
                System.err.println("❌ Error: El proveedor no existe");
                return false;
            }

            // Crear mapa con los datos del producto
            Map<String, Object> data = new HashMap<>();
            data.put("nombre_producto", producto.getNombreProducto());
            data.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
            data.put("precio_unitario", producto.getPrecioUnitario());
            data.put("stock_actual", producto.getStockActual());
            data.put("stock_minimo", producto.getStockMinimo());
            data.put("id_categoria", producto.getIdCategoria());
            data.put("id_proveedor", producto.getIdProveedor());
            data.put("codigo_barras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "");
            data.put("activo", producto.isActivo());
            data.put("fecha_registro", Timestamp.now());
            data.put("ultima_actualizacion", Timestamp.now());

            // Agregar documento a Firestore
            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Asignar el ID generado
            String firestoreId = docRef.getId();
            producto.setIdProducto(firestoreId.hashCode());

            // Guardar el ID de Firestore y numérico en el documento
            Map<String, Object> idUpdate = new HashMap<>();
            idUpdate.put("firestore_id", firestoreId);
            idUpdate.put("id_producto", producto.getIdProducto());
            docRef.update(idUpdate);

            System.out.println("✓ Producto agregado con ID: " + firestoreId);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener todos los productos activos con información de categoría y proveedor
     */
    public List<Producto> obtenerTodosLosProductos() {
        List<Producto> productos = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return productos;
        }

        try {
            // Consulta de productos activos ordenados por nombre
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .orderBy("nombre_producto", Query.Direction.ASCENDING)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Obtener categorías y proveedores una sola vez
            Map<Integer, String> categoriasMap = obtenerCategoriasMap();
            Map<Integer, String> proveedoresMap = obtenerProveedoresMap();

            // Convertir documentos a objetos Producto
            for (QueryDocumentSnapshot document : documents) {
                Producto producto = documentToProducto(document, categoriasMap, proveedoresMap);
                if (producto != null) {
                    productos.add(producto);
                }
            }

            System.out.println("✓ Se obtuvieron " + productos.size() + " productos de Firestore");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Buscar productos por nombre
     */
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> productos = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return productos;
        }

        try {
            String nombreBusqueda = nombre.toLowerCase();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            Map<Integer, String> categoriasMap = obtenerCategoriasMap();
            Map<Integer, String> proveedoresMap = obtenerProveedoresMap();

            // Filtrar por nombre (Firestore no tiene búsqueda LIKE nativa)
            for (QueryDocumentSnapshot document : documents) {
                String nombreProducto = document.getString("nombre_producto");
                if (nombreProducto != null &&
                        nombreProducto.toLowerCase().contains(nombreBusqueda)) {
                    Producto producto = documentToProducto(document, categoriasMap, proveedoresMap);
                    if (producto != null) {
                        productos.add(producto);
                    }
                }
            }

            System.out.println("✓ Se encontraron " + productos.size() + " productos");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Buscar productos por categoría
     */
    public List<Producto> buscarPorCategoria(int idCategoria) {
        List<Producto> productos = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return productos;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", idCategoria)
                    .whereEqualTo("activo", true)
                    .orderBy("nombre_producto", Query.Direction.ASCENDING)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            Map<Integer, String> categoriasMap = obtenerCategoriasMap();
            Map<Integer, String> proveedoresMap = obtenerProveedoresMap();

            for (QueryDocumentSnapshot document : documents) {
                Producto producto = documentToProducto(document, categoriasMap, proveedoresMap);
                if (producto != null) {
                    productos.add(producto);
                }
            }

            System.out.println("✓ Se encontraron " + productos.size() + " productos en la categoría");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar por categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Obtener productos con stock bajo
     */
    public List<Producto> obtenerProductosStockBajo() {
        List<Producto> productos = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return productos;
        }

        try {
            // Firestore no permite comparaciones entre campos en la consulta
            // Necesitamos obtener todos y filtrar manualmente
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            Map<Integer, String> categoriasMap = obtenerCategoriasMap();
            Map<Integer, String> proveedoresMap = obtenerProveedoresMap();

            for (QueryDocumentSnapshot document : documents) {
                Long stockActual = document.getLong("stock_actual");
                Long stockMinimo = document.getLong("stock_minimo");

                // Filtrar productos con stock bajo
                if (stockActual != null && stockMinimo != null &&
                        stockActual <= stockMinimo) {
                    Producto producto = documentToProducto(document, categoriasMap, proveedoresMap);
                    if (producto != null) {
                        productos.add(producto);
                    }
                }
            }

            // Ordenar por stock actual ascendente
            productos.sort((p1, p2) -> Integer.compare(p1.getStockActual(), p2.getStockActual()));

            System.out.println("✓ Se encontraron " + productos.size() + " productos con stock bajo");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos con stock bajo: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    /**
     * Obtener producto por ID
     */
    public Producto obtenerProductoPorId(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                Map<Integer, String> categoriasMap = obtenerCategoriasMap();
                Map<Integer, String> proveedoresMap = obtenerProveedoresMap();
                return documentToProducto(documents.get(0), categoriasMap, proveedoresMap);
            }

            System.out.println("⚠️ No se encontró producto con ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener producto por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualizar producto
     */
    public boolean actualizarProducto(Producto producto) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Verificar que existan la categoría y el proveedor
            if (!existeCategoria(producto.getIdCategoria())) {
                System.err.println("❌ Error: La categoría no existe");
                return false;
            }

            if (!existeProveedor(producto.getIdProveedor())) {
                System.err.println("❌ Error: El proveedor no existe");
                return false;
            }

            // Buscar el documento por id_producto
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", producto.getIdProducto())
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró el producto para actualizar");
                return false;
            }

            String docId = documents.get(0).getId();

            // Preparar datos a actualizar
            Map<String, Object> updates = new HashMap<>();
            updates.put("nombre_producto", producto.getNombreProducto());
            updates.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");
            updates.put("precio_unitario", producto.getPrecioUnitario());
            updates.put("stock_actual", producto.getStockActual());
            updates.put("stock_minimo", producto.getStockMinimo());
            updates.put("id_categoria", producto.getIdCategoria());
            updates.put("id_proveedor", producto.getIdProveedor());
            updates.put("codigo_barras", producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "");
            updates.put("activo", producto.isActivo());
            updates.put("ultima_actualizacion", Timestamp.now());

            // Actualizar el documento
            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .update(updates);

            writeResult.get();

            System.out.println("✓ Producto actualizado: " + producto.getNombreProducto());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar producto (soft delete)
     */
    public boolean eliminarProducto(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró el producto para eliminar");
                return false;
            }

            String docId = documents.get(0).getId();

            // Soft delete: marcar como inactivo
            Map<String, Object> updates = new HashMap<>();
            updates.put("activo", false);
            updates.put("ultima_actualizacion", Timestamp.now());

            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .update(updates);

            writeResult.get();

            System.out.println("✓ Producto eliminado (marcado como inactivo) con ID: " + id);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buscar producto por código de barras
     */
    public Producto buscarPorCodigoBarras(String codigoBarras) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("codigo_barras", codigoBarras)
                    .whereEqualTo("activo", true)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                Map<Integer, String> categoriasMap = obtenerCategoriasMap();
                Map<Integer, String> proveedoresMap = obtenerProveedoresMap();
                return documentToProducto(documents.get(0), categoriasMap, proveedoresMap);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar por código de barras: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualizar solo el stock de un producto
     */
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_producto", idProducto)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró el producto");
                return false;
            }

            String docId = documents.get(0).getId();

            Map<String, Object> updates = new HashMap<>();
            updates.put("stock_actual", nuevoStock);
            updates.put("ultima_actualizacion", Timestamp.now());

            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .update(updates);

            writeResult.get();

            System.out.println("✓ Stock actualizado a: " + nuevoStock);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Contar productos activos
     */
    public int contarProductosActivos() {
        if (db == null) {
            return 0;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();
            return future.get().size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al contar productos: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtener valor total del inventario
     */
    public double obtenerValorTotalInventario() {
        if (db == null) {
            return 0.0;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("activo", true)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            double total = 0.0;

            for (QueryDocumentSnapshot doc : documents) {
                Double precio = doc.getDouble("precio_unitario");
                Long stock = doc.getLong("stock_actual");

                if (precio != null && stock != null) {
                    total += precio * stock;
                }
            }

            return total;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al calcular valor del inventario: " + e.getMessage());
            return 0.0;
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Verificar si existe una categoría
     */
    private boolean existeCategoria(int idCategoria) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("categorias")
                    .whereEqualTo("id_categoria", idCategoria)
                    .limit(1)
                    .get();
            return !future.get().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verificar si existe un proveedor
     */
    private boolean existeProveedor(int idProveedor) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("proveedores")
                    .whereEqualTo("id_proveedor", idProveedor)
                    .limit(1)
                    .get();
            return !future.get().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtener mapa de categorías (ID -> Nombre)
     */
    private Map<Integer, String> obtenerCategoriasMap() {
        Map<Integer, String> map = new HashMap<>();
        try {
            ApiFuture<QuerySnapshot> future = db.collection("categorias").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                Long idLong = doc.getLong("id_categoria");
                String nombre = doc.getString("nombre_categoria");
                if (idLong != null && nombre != null) {
                    map.put(idLong.intValue(), nombre);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener categorías: " + e.getMessage());
        }
        return map;
    }

    /**
     * Obtener mapa de proveedores (ID -> Nombre)
     */
    private Map<Integer, String> obtenerProveedoresMap() {
        Map<Integer, String> map = new HashMap<>();
        try {
            ApiFuture<QuerySnapshot> future = db.collection("proveedores").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                Long idLong = doc.getLong("id_proveedor");
                String nombre = doc.getString("nombre_proveedor");
                if (idLong != null && nombre != null) {
                    map.put(idLong.intValue(), nombre);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener proveedores: " + e.getMessage());
        }
        return map;
    }

    /**
     * Convertir DocumentSnapshot a objeto Producto
     */
    private Producto documentToProducto(DocumentSnapshot document,
                                        Map<Integer, String> categoriasMap,
                                        Map<Integer, String> proveedoresMap) {
        try {
            Long idProductoLong = document.getLong("id_producto");
            String nombreProducto = document.getString("nombre_producto");
            String descripcion = document.getString("descripcion");
            Double precioUnitario = document.getDouble("precio_unitario");
            Long stockActual = document.getLong("stock_actual");
            Long stockMinimo = document.getLong("stock_minimo");
            Long idCategoriaLong = document.getLong("id_categoria");
            Long idProveedorLong = document.getLong("id_proveedor");
            String codigoBarras = document.getString("codigo_barras");
            Boolean activo = document.getBoolean("activo");
            Timestamp fechaRegistro = document.getTimestamp("fecha_registro");
            Timestamp ultimaActualizacion = document.getTimestamp("ultima_actualizacion");

            int idProducto = idProductoLong != null ? idProductoLong.intValue() : document.getId().hashCode();
            int idCategoria = idCategoriaLong != null ? idCategoriaLong.intValue() : 0;
            int idProveedor = idProveedorLong != null ? idProveedorLong.intValue() : 0;

            Producto producto = new Producto(
                    idProducto,
                    nombreProducto != null ? nombreProducto : "",
                    descripcion,
                    precioUnitario != null ? precioUnitario : 0.0,
                    stockActual != null ? stockActual.intValue() : 0,
                    stockMinimo != null ? stockMinimo.intValue() : 0,
                    idCategoria,
                    idProveedor,
                    codigoBarras,
                    activo != null ? activo : true
            );

            // Asignar nombres de categoría y proveedor
            producto.setNombreCategoria(categoriasMap.getOrDefault(idCategoria, "Sin categoría"));
            producto.setNombreProveedor(proveedoresMap.getOrDefault(idProveedor, "Sin proveedor"));

            // Convertir Timestamps
            if (fechaRegistro != null) {
                producto.setFechaRegistro(new java.sql.Timestamp(fechaRegistro.toDate().getTime()));
            }
            if (ultimaActualizacion != null) {
                producto.setUltimaActualizacion(new java.sql.Timestamp(ultimaActualizacion.toDate().getTime()));
            }

            return producto;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento a Producto: " + e.getMessage());
            return null;
        }
    }
}