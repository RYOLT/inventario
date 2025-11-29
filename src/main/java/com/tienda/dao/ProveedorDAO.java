package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Proveedor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * DAO para gestionar Proveedores en Firebase Firestore
 * Reemplaza la versión con MySQL/JDBC
 */
public class ProveedorDAO {

    private static final String COLLECTION_NAME = "proveedores";
    private Firestore db;

    public ProveedorDAO() {
        this.db = FirestoreConfig.getDatabase();
        if (this.db == null) {
            System.err.println("⚠️ Error: Firestore no está inicializado en ProveedorDAO");
        }
    }

    /**
     * Obtener todos los proveedores ordenados por nombre
     */
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return proveedores;
        }

        try {
            // Consulta a Firestore ordenada por nombre_proveedor
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .orderBy("nombre_proveedor", Query.Direction.ASCENDING)
                    .get();

            // Obtener los documentos
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Convertir cada documento a objeto Proveedor
            for (QueryDocumentSnapshot document : documents) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null) {
                    proveedores.add(proveedor);
                }
            }

            System.out.println("✓ Se obtuvieron " + proveedores.size() + " proveedores de Firestore");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener proveedores: " + e.getMessage());
            e.printStackTrace();
        }

        return proveedores;
    }

    /**
     * Agregar nuevo proveedor
     */
    public boolean agregar(Proveedor proveedor) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Crear mapa con los datos del proveedor
            Map<String, Object> data = new HashMap<>();
            data.put("nombre_proveedor", proveedor.getNombreProveedor());
            data.put("telefono", proveedor.getTelefono() != null ? proveedor.getTelefono() : "");
            data.put("email", proveedor.getEmail() != null ? proveedor.getEmail() : "");
            data.put("direccion", proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
            data.put("ciudad", proveedor.getCiudad() != null ? proveedor.getCiudad() : "");
            data.put("pais", proveedor.getPais() != null ? proveedor.getPais() : "");
            data.put("fecha_registro", Timestamp.now());

            // Agregar documento a Firestore
            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Asignar el ID generado
            String firestoreId = docRef.getId();
            proveedor.setIdProveedor(firestoreId.hashCode());

            // Guardar el ID de Firestore en el documento
            docRef.update("firestore_id", firestoreId);

            System.out.println("✓ Proveedor agregado con ID: " + firestoreId);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener proveedor por ID
     */
    public Proveedor obtenerPorId(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            // Buscar por el campo id_proveedor
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                return documentToProveedor(documents.get(0));
            }

            System.out.println("⚠️ No se encontró proveedor con ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener proveedor por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtener proveedor por su ID de Firestore (String)
     */
    public Proveedor obtenerPorFirestoreId(String firestoreId) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(firestoreId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return documentToProveedor(document);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener proveedor: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualizar proveedor existente
     */
    public boolean actualizar(Proveedor proveedor) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Buscar el documento por id_proveedor
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", proveedor.getIdProveedor())
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró el proveedor para actualizar");
                return false;
            }

            // Obtener el ID del documento
            String docId = documents.get(0).getId();

            // Preparar datos a actualizar
            Map<String, Object> updates = new HashMap<>();
            updates.put("nombre_proveedor", proveedor.getNombreProveedor());
            updates.put("telefono", proveedor.getTelefono() != null ? proveedor.getTelefono() : "");
            updates.put("email", proveedor.getEmail() != null ? proveedor.getEmail() : "");
            updates.put("direccion", proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
            updates.put("ciudad", proveedor.getCiudad() != null ? proveedor.getCiudad() : "");
            updates.put("pais", proveedor.getPais() != null ? proveedor.getPais() : "");

            // Actualizar el documento
            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .update(updates);

            writeResult.get();

            System.out.println("✓ Proveedor actualizado: " + proveedor.getNombreProveedor());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar proveedor
     */
    public boolean eliminar(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Buscar el documento por id_proveedor
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró el proveedor para eliminar");
                return false;
            }

            String docId = documents.get(0).getId();

            // VERIFICAR si hay productos con este proveedor
            ApiFuture<QuerySnapshot> productosConProveedor = db.collection("productos")
                    .whereEqualTo("id_proveedor", id)
                    .limit(1)
                    .get();

            if (!productosConProveedor.get().isEmpty()) {
                System.err.println("❌ No se puede eliminar: hay productos asociados a este proveedor");
                return false;
            }

            // Eliminar el documento
            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .delete();

            writeResult.get();

            System.out.println("✓ Proveedor eliminado con ID: " + id);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Buscar proveedores por nombre
     */
    public List<Proveedor> buscarPorNombre(String nombre) {
        List<Proveedor> proveedores = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return proveedores;
        }

        try {
            // En Firestore, búsqueda case-sensitive
            // Para búsqueda más flexible, convertir a minúsculas
            String nombreBusqueda = nombre.toLowerCase();

            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Filtrar manualmente (Firestore no tiene LIKE nativo)
            for (QueryDocumentSnapshot document : documents) {
                String nombreProveedor = document.getString("nombre_proveedor");
                if (nombreProveedor != null &&
                        nombreProveedor.toLowerCase().contains(nombreBusqueda)) {
                    Proveedor proveedor = documentToProveedor(document);
                    if (proveedor != null) {
                        proveedores.add(proveedor);
                    }
                }
            }

            System.out.println("✓ Se encontraron " + proveedores.size() + " proveedores");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar proveedores: " + e.getMessage());
            e.printStackTrace();
        }

        return proveedores;
    }

    /**
     * Contar productos de un proveedor
     */
    public int contarProductos(int idProveedor) {
        if (db == null) {
            return 0;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection("productos")
                    .whereEqualTo("id_proveedor", idProveedor)
                    .whereEqualTo("activo", true)
                    .get();

            return future.get().size();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al contar productos: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Contar cuántos proveedores hay en total
     */
    public int contarProveedores() {
        if (db == null) {
            return 0;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            return future.get().size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al contar proveedores: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Método auxiliar: Convierte DocumentSnapshot a objeto Proveedor
     */
    private Proveedor documentToProveedor(DocumentSnapshot document) {
        try {
            String firestoreId = document.getId();

            String nombreProveedor = document.getString("nombre_proveedor");
            String telefono = document.getString("telefono");
            String email = document.getString("email");
            String direccion = document.getString("direccion");
            String ciudad = document.getString("ciudad");
            String pais = document.getString("pais");
            Timestamp fechaRegistro = document.getTimestamp("fecha_registro");

            int idProveedor = document.contains("id_proveedor")
                    ? document.getLong("id_proveedor").intValue()
                    : firestoreId.hashCode();

            Proveedor proveedor = new Proveedor(
                    idProveedor,
                    nombreProveedor != null ? nombreProveedor : "",
                    telefono != null ? telefono : "",
                    email != null ? email : "",
                    direccion != null ? direccion : "",
                    ciudad != null ? ciudad : "",
                    pais != null ? pais : ""
            );

            if (fechaRegistro != null) {
                proveedor.setFechaRegistro(
                        new java.sql.Timestamp(fechaRegistro.toDate().getTime())
                );
            }

            return proveedor;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento a Proveedor: " + e.getMessage());
            return null;
        }
    }

    /**
     * Inicializar proveedores de ejemplo
     */
    public void inicializarProveedoresEjemplo() {
        if (contarProveedores() > 0) {
            System.out.println("⚠️ Ya existen proveedores en Firestore");
            return;
        }

        Object[][] proveedoresEjemplo = {
                {"Tech Solutions SA", "555-0001", "ventas@techsol.com", "Av. Tecnología 123", "Ciudad de México", "México"},
                {"Importadora Global", "555-0002", "info@impglobal.com", "Calle Comercio 456", "Guadalajara", "México"},
                {"Distribuidora Nacional", "555-0003", "contacto@distnac.com", "Blvd. Industrial 789", "Monterrey", "México"},
                {"Alimentos del Norte", "555-0004", "pedidos@alinorte.com", "Zona Industrial 321", "Tijuana", "México"},
                {"Textiles Premium", "555-0005", "ventas@textprem.com", "Parque Industrial Sur", "Puebla", "México"}
        };

        int contador = 0;
        for (Object[] prov : proveedoresEjemplo) {
            Proveedor proveedor = new Proveedor(
                    (String) prov[0], // nombre
                    (String) prov[1], // telefono
                    (String) prov[2], // email
                    (String) prov[3], // direccion
                    (String) prov[4], // ciudad
                    (String) prov[5]  // pais
            );
            if (agregar(proveedor)) {
                contador++;
            }
        }

        System.out.println("✓ Se agregaron " + contador + " proveedores de ejemplo");
    }
}