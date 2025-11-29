package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Proveedor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProveedorDAO {
    private static final String COLLECTION_NAME = "proveedores";
    private Firestore firestore;

    public ProveedorDAO() {
        this.firestore = FirestoreConfig.getFirestore();
    }

    /**
     * Obtener todos los proveedores
     */
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .orderBy("nombreProveedor")
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null) {
                    proveedores.add(proveedor);
                }
            }

            System.out.println("✓ Se obtuvieron " + proveedores.size() + " proveedores");

        } catch (Exception e) {
            System.err.println("✗ Error al obtener proveedores: " + e.getMessage());
            e.printStackTrace();
        }

        return proveedores;
    }

    /**
     * Agregar nuevo proveedor
     */
    public boolean agregar(Proveedor proveedor) {
        try {
            Map<String, Object> data = proveedorToMap(proveedor);

            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Guardar el ID generado
            proveedor.setIdProveedor(docRef.getId().hashCode());

            System.out.println("✓ Proveedor agregado con ID: " + docRef.getId());
            return true;

        } catch (Exception e) {
            System.err.println("✗ Error al agregar proveedor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener proveedor por ID
     */
    public Proveedor obtenerPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProveedor", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProveedor(querySnapshot.getDocuments().get(0));
            }

        } catch (Exception e) {
            System.err.println("✗ Error al obtener proveedor: " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualizar proveedor
     */
    public boolean actualizar(Proveedor proveedor) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProveedor", proveedor.getIdProveedor())
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();
                Map<String, Object> data = proveedorToMap(proveedor);

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .set(data);

                writeResult.get();
                System.out.println("✓ Proveedor actualizado");
                return true;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al actualizar proveedor: " + e.getMessage());
        }

        return false;
    }

    /**
     * Eliminar proveedor
     */
    public boolean eliminar(int id) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idProveedor", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .delete();

                writeResult.get();
                System.out.println("✓ Proveedor eliminado");
                return true;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al eliminar proveedor: " + e.getMessage());
            if (e.getMessage().contains("FAILED_PRECONDITION")) {
                System.err.println("  Puede que haya productos asociados a este proveedor");
            }
        }

        return false;
    }

    /**
     * Buscar proveedores por nombre
     */
    public List<Proveedor> buscarPorNombre(String nombre) {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null &&
                        proveedor.getNombreProveedor().toLowerCase().contains(nombre.toLowerCase())) {
                    proveedores.add(proveedor);
                }
            }

        } catch (Exception e) {
            System.err.println("✗ Error al buscar proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    /**
     * Contar productos de un proveedor
     */
    public int contarProductos(int idProveedor) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("productos")
                    .whereEqualTo("idProveedor", idProveedor)
                    .whereEqualTo("activo", true)
                    .get();

            return future.get().size();

        } catch (Exception e) {
            System.err.println("✗ Error al contar productos: " + e.getMessage());
        }

        return 0;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, Object> proveedorToMap(Proveedor proveedor) {
        Map<String, Object> data = new HashMap<>();
        data.put("idProveedor", proveedor.getIdProveedor());
        data.put("nombreProveedor", proveedor.getNombreProveedor());
        data.put("telefono", proveedor.getTelefono());
        data.put("email", proveedor.getEmail());
        data.put("direccion", proveedor.getDireccion());
        data.put("ciudad", proveedor.getCiudad());
        data.put("pais", proveedor.getPais());
        data.put("fechaRegistro", System.currentTimeMillis());
        return data;
    }

    private Proveedor documentToProveedor(DocumentSnapshot document) {
        try {
            Proveedor proveedor = new Proveedor();
            proveedor.setIdProveedor(document.getLong("idProveedor") != null ?
                    document.getLong("idProveedor").intValue() : 0);
            proveedor.setNombreProveedor(document.getString("nombreProveedor"));
            proveedor.setTelefono(document.getString("telefono"));
            proveedor.setEmail(document.getString("email"));
            proveedor.setDireccion(document.getString("direccion"));
            proveedor.setCiudad(document.getString("ciudad"));
            proveedor.setPais(document.getString("pais"));
            return proveedor;
        } catch (Exception e) {
            System.err.println("✗ Error al convertir documento a proveedor: " + e.getMessage());
            return null;
        }
    }
}