package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.ConexionDB;
import com.tienda.modelo.Proveedor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ProveedorDAO {
    private static final String COLLECTION_NAME = "proveedores";
    private Firestore firestore;

    public ProveedorDAO() {
        this.firestore = ConexionDB.getFirestore();
    }

    // Obtener todos los proveedores
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            // Consulta simple sin orderBy
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME).get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null) {
                    proveedores.add(proveedor);
                }
            }

            // Ordenar en Java
            proveedores.sort((p1, p2) -> p1.getNombreProveedor().compareToIgnoreCase(p2.getNombreProveedor()));

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Agregar nuevo proveedor
    public boolean agregar(Proveedor proveedor) {
        try {
            Map<String, Object> proveedorMap = new HashMap<>();

            // Generar ID automático
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            int id = docRef.getId().hashCode();

            proveedorMap.put("id_proveedor", id);
            proveedorMap.put("nombre_proveedor", proveedor.getNombreProveedor());
            proveedorMap.put("telefono", proveedor.getTelefono());
            proveedorMap.put("email", proveedor.getEmail());
            proveedorMap.put("direccion", proveedor.getDireccion());
            proveedorMap.put("ciudad", proveedor.getCiudad());
            proveedorMap.put("pais", proveedor.getPais());
            proveedorMap.put("timestamp", FieldValue.serverTimestamp());
            proveedorMap.put("docId", docRef.getId());

            ApiFuture<WriteResult> result = docRef.set(proveedorMap);
            result.get();

            proveedor.setIdProveedor(id);
            System.out.println("✅ Proveedor agregado: " + docRef.getId());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar proveedor: " + e.getMessage());
            return false;
        }
    }

    // Obtener proveedor por ID
    public Proveedor obtenerPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                return documentToProveedor(querySnapshot.getDocuments().get(0));
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener proveedor: " + e.getMessage());
        }

        return null;
    }

    // Actualizar proveedor
    public boolean actualizar(Proveedor proveedor) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", proveedor.getIdProveedor())
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                Map<String, Object> updates = new HashMap<>();
                updates.put("nombre_proveedor", proveedor.getNombreProveedor());
                updates.put("telefono", proveedor.getTelefono());
                updates.put("email", proveedor.getEmail());
                updates.put("direccion", proveedor.getDireccion());
                updates.put("ciudad", proveedor.getCiudad());
                updates.put("pais", proveedor.getPais());

                ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .update(updates);

                result.get();
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar proveedor: " + e.getMessage());
        }

        return false;
    }

    // Eliminar proveedor
    public boolean eliminar(int id) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_proveedor", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .delete();

                result.get();
                return true;
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar proveedor: " + e.getMessage());
        }

        return false;
    }

    // Buscar proveedores por nombre
    public List<Proveedor> buscarPorNombre(String nombre) {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null &&
                        proveedor.getNombreProveedor().toLowerCase().contains(nombre.toLowerCase())) {
                    proveedores.add(proveedor);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al buscar proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Convertir DocumentSnapshot a Proveedor
    private Proveedor documentToProveedor(DocumentSnapshot document) {
        try {
            Proveedor proveedor = new Proveedor();
            proveedor.setIdProveedor((int) document.getLong("id_proveedor").longValue());
            proveedor.setNombreProveedor(document.getString("nombre_proveedor"));
            proveedor.setTelefono(document.getString("telefono"));
            proveedor.setEmail(document.getString("email"));
            proveedor.setDireccion(document.getString("direccion"));
            proveedor.setCiudad(document.getString("ciudad"));
            proveedor.setPais(document.getString("pais"));
            return proveedor;
        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento de proveedor: " + e.getMessage());
            return null;
        }
    }
}