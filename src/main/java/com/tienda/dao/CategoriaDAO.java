package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.firebase.FirebaseConfig;
import com.tienda.modelo.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CategoriaDAO {
    private static final String COLLECTION = "categorias";
    private Firestore db;

    public CategoriaDAO() {
        this.db = FirebaseConfig.getFirestore();
    }

    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION)
                    .orderBy("nombreCategoria")
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (DocumentSnapshot document : documents) {
                Categoria categoria = documentToCategoria(document);
                if (categoria != null) {
                    categorias.add(categoria);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }

        return categorias;
    }

    // Agregar nueva categoría
    public boolean agregar(Categoria categoria) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("nombreCategoria", categoria.getNombreCategoria());
            data.put("descripcion", categoria.getDescripcion());
            data.put("fechaCreacion", FieldValue.serverTimestamp());

            ApiFuture<DocumentReference> future = db.collection(COLLECTION).add(data);
            DocumentReference docRef = future.get();

            // Guardar el ID generado
            categoria.setIdCategoria(docRef.getId().hashCode());

            System.out.println("✓ Categoría agregada con ID: " + docRef.getId());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al agregar categoría: " + e.getMessage());
            return false;
        }
    }

    // Obtener categoría por ID del documento
    public Categoria obtenerPorDocId(String docId) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return documentToCategoria(document);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al obtener categoría: " + e.getMessage());
        }

        return null;
    }

    // Obtener categoría por ID numérico
    public Categoria obtenerPorId(int id) {
        // En Firebase, buscaremos por el hashCode o usaremos el docId
        // Esta es una función de compatibilidad
        List<Categoria> todas = obtenerTodas();
        for (Categoria cat : todas) {
            if (cat.getIdCategoria() == id) {
                return cat;
            }
        }
        return null;
    }

    // Actualizar categoría
    public boolean actualizar(String docId, Categoria categoria) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            Map<String, Object> data = new HashMap<>();
            data.put("nombreCategoria", categoria.getNombreCategoria());
            data.put("descripcion", categoria.getDescripcion());

            ApiFuture<WriteResult> future = docRef.set(data, SetOptions.merge());
            future.get();

            System.out.println("✓ Categoría actualizada");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    // Eliminar categoría
    public boolean eliminar(String docId) {
        try {
            DocumentReference docRef = db.collection(COLLECTION).document(docId);
            ApiFuture<WriteResult> future = docRef.delete();
            future.get();

            System.out.println("✓ Categoría eliminada");
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            if (e.getMessage().contains("foreign key") || e.getMessage().contains("constraint")) {
                System.err.println("No se puede eliminar: hay productos asociados a esta categoría");
            }
            return false;
        }
    }

    // Convertir DocumentSnapshot a Categoria
    private Categoria documentToCategoria(DocumentSnapshot doc) {
        try {
            String nombreCategoria = doc.getString("nombreCategoria");
            String descripcion = doc.getString("descripcion");

            // Usar hashCode del ID del documento como ID numérico
            int id = doc.getId().hashCode();

            Categoria categoria = new Categoria(id, nombreCategoria, descripcion);

            // La fecha de creación en Firestore
            com.google.cloud.Timestamp timestamp = doc.getTimestamp("fechaCreacion");
            if (timestamp != null) {
                categoria.setFechaCreacion(new java.sql.Timestamp(timestamp.toDate().getTime()));
            }

            return categoria;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento de categoría: " + e.getMessage());
            System.err.println("   Documento ID: " + doc.getId());
            e.printStackTrace();
            return null;
        }
    }
}