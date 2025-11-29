package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.firebase.FirebaseConfig;
import com.tienda.modelo.Proveedor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ProveedorDAO {
    private static final String COLLECTION = "proveedores";
    private Firestore db;

    public ProveedorDAO() {
        this.db = FirebaseConfig.getFirestore();
    }

    // Obtener todos los proveedores
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION)
                    .orderBy("nombreProveedor")
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (DocumentSnapshot document : documents) {
                Proveedor proveedor = documentToProveedor(document);
                if (proveedor != null) {
                    proveedores.add(proveedor);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al obtener proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Agregar nuevo proveedor
    public boolean agregar(Proveedor proveedor) {
        try {
            Map<String, Object> data = proveedorAMap(proveedor);
            data.put("fechaRegistro", FieldValue.serverTimestamp());

            ApiFuture<DocumentReference> future = db.collection(COLLECTION).add(data);
            DocumentReference docRef = future.get();

            // Guardar el ID generado
            proveedor.setIdProveedor(docRef.getId().hashCode());

            System.out.println("✓ Proveedor agregado con ID: " + docRef.getId());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al agregar proveedor: " + e.getMessage());
            return false;
        }
    }

    // Obtener proveedor por ID del documento
    public Proveedor obtenerPorDocId(String docId) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return documentToProveedor(document);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
        }

        return null;
    }

    // Obtener proveedor por ID numérico
    public Proveedor obtenerPorId(int id) {
        List<Proveedor> todos = obtenerTodos();
        for (Proveedor prov : todos) {
            if (prov.getIdProveedor() == id) {
                return prov;
            }
        }
        return null;
    }

    // Actualizar proveedor
    public boolean actualizar(String docId, Proveedor proveedor) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            Map<String, Object> data = proveedorAMap(proveedor);

            ApiFuture<WriteResult> future = docRef.set(data, SetOptions.merge());
            future.get();

            System.out.println("✓ Proveedor actualizado");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            return false;
        }
    }

    // Eliminar proveedor
    public boolean eliminar(String docId) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            ApiFuture<WriteResult> future = docRef.delete();
            future.get();

            System.out.println("✓ Proveedor eliminado");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al eliminar proveedor: " + e.getMessage());
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                System.err.println("No se puede eliminar: hay productos asociados a este proveedor");
            }
            return false;
        }
    }

    // Buscar proveedores por nombre
    public List<Proveedor> buscarPorNombre(String nombre) {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            List<Proveedor> todos = obtenerTodos();
            String nombreLower = nombre.toLowerCase();

            for (Proveedor p : todos) {
                if (p.getNombreProveedor().toLowerCase().contains(nombreLower)) {
                    proveedores.add(p);
                }
            }

        } catch (Exception e) {
            System.err.println("Error al buscar proveedores: " + e.getMessage());
        }

        return proveedores;
    }

    // Contar productos de un proveedor
    public int contarProductos(int idProveedor) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("productos")
                    .whereEqualTo("idProveedor", idProveedor)
                    .whereEqualTo("activo", true)
                    .get();

            return future.get().size();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al contar productos: " + e.getMessage());
            return 0;
        }
    }

    // Convertir Proveedor a Map
    private Map<String, Object> proveedorAMap(Proveedor p) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombreProveedor", p.getNombreProveedor());
        data.put("telefono", p.getTelefono());
        data.put("email", p.getEmail());
        data.put("direccion", p.getDireccion());
        data.put("ciudad", p.getCiudad());
        data.put("pais", p.getPais());
        return data;
    }

    // Convertir DocumentSnapshot a Proveedor
    private Proveedor documentToProveedor(DocumentSnapshot doc) {
        try {
            int id = doc.getId().hashCode();
            String nombreProveedor = doc.getString("nombreProveedor");
            String telefono = doc.getString("telefono");
            String email = doc.getString("email");
            String direccion = doc.getString("direccion");
            String ciudad = doc.getString("ciudad");
            String pais = doc.getString("pais");

            Proveedor proveedor = new Proveedor(
                    id,
                    nombreProveedor,
                    telefono,
                    email,
                    direccion,
                    ciudad,
                    pais
            );

            // Fecha de registro
            com.google.cloud.Timestamp timestamp = doc.getTimestamp("fechaRegistro");
            if (timestamp != null) {
                proveedor.setFechaRegistro(new java.sql.Timestamp(timestamp.toDate().getTime()));
            }

            return proveedor;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento de proveedor: " + e.getMessage());
            System.err.println("   Documento ID: " + doc.getId());
            e.printStackTrace();
            return null;
        }
    }
}