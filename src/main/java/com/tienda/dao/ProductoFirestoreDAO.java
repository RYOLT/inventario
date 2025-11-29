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
//            System.out.println("🔍 Intentando obtener productos de Firestore...");

            QuerySnapshot querySnapshot = db.collection("productos")
                    .whereEqualTo("activo", true)
                    .get()
                    .get();

//            System.out.println("📊 Documentos encontrados: " + querySnapshot.size());

            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                System.out.println("📦 Documento ID: " + doc.getId());
//                System.out.println("   Datos: " + doc.getData());

                Producto producto = documentToProducto(doc);
                productos.add(producto);
            }

//            System.out.println("✅ Total productos cargados: " + productos.size());

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
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
        try {
            System.out.println("🔄 Convirtiendo documento: " + doc.getId());

            Producto producto = new Producto();

            // Obtener campos con manejo de nulos
            String nombreProducto = doc.getString("nombre_producto");
            if (nombreProducto == null) nombreProducto = doc.getString("nombre");
            producto.setNombreProducto(nombreProducto);
            System.out.println("   Nombre: " + nombreProducto);

            producto.setDescripcion(doc.getString("descripcion"));

            // Precio
            Object precioObj = doc.get("precio_unitario");
            if (precioObj == null) precioObj = doc.get("precio");
            double precio = 0.0;
            if (precioObj instanceof Number) {
                precio = ((Number) precioObj).doubleValue();
            }
            producto.setPrecioUnitario(precio);
            System.out.println("   Precio: " + precio);

            // Stock actual
            Object stockObj = doc.get("stock_actual");
            if (stockObj == null) stockObj = doc.get("stock");
            int stock = 0;
            if (stockObj instanceof Number) {
                stock = ((Number) stockObj).intValue();
            }
            producto.setStockActual(stock);
            System.out.println("   Stock: " + stock);

            // Stock mínimo
            Object stockMinObj = doc.get("stock_minimo");
            if (stockMinObj == null) stockMinObj = doc.get("stockMin");
            int stockMin = 0;
            if (stockMinObj instanceof Number) {
                stockMin = ((Number) stockMinObj).intValue();
            }
            producto.setStockMinimo(stockMin);

            // ID Categoría
            Object catObj = doc.get("id_categoria");
            if (catObj == null) catObj = doc.get("idCategoria");
            int idCat = 0;
            if (catObj instanceof Number) {
                idCat = ((Number) catObj).intValue();
            }
            producto.setIdCategoria(idCat);

            // ID Proveedor
            Object provObj = doc.get("id_proveedor");
            if (provObj == null) provObj = doc.get("idProveedor");
            int idProv = 0;
            if (provObj instanceof Number) {
                idProv = ((Number) provObj).intValue();
            }
            producto.setIdProveedor(idProv);

            producto.setCodigoBarras(doc.getString("codigo_barras"));

            Boolean activo = doc.getBoolean("activo");
            producto.setActivo(activo != null ? activo : true);

            // Guardar el ID del documento
            producto.setIdProducto(doc.getId().hashCode());

            System.out.println("✅ Producto convertido exitosamente");

            return producto;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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