package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaDAO {
    private static final String COLLECTION_NAME = "categorias";
    private Firestore firestore;

    public CategoriaDAO() {
        this.firestore = FirestoreConfig.getFirestore();
    }

    /**
     * Obtener todas las categorías
     */
    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .orderBy("nombreCategoria")
                    .get();

            QuerySnapshot querySnapshot = future.get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Categoria categoria = documentToCategoria(document);
                if (categoria != null) {
                    categorias.add(categoria);
                }
            }

            System.out.println("✓ Se obtuvieron " + categorias.size() + " categorías");

        } catch (Exception e) {
            System.err.println("✗ Error al obtener categorías: " + e.getMessage());
            e.printStackTrace();
        }

        return categorias;
    }

    /**
     * Agregar nueva categoría
     */
    public boolean agregar(Categoria categoria) {
        try {
            Map<String, Object> data = categoriaToMap(categoria);

            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Guardar el ID generado
            categoria.setIdCategoria(docRef.getId().hashCode());

            System.out.println("✓ Categoría agregada con ID: " + docRef.getId());
            return true;

        } catch (Exception e) {
            System.err.println("✗ Error al agregar categoría: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener categoría por ID
     */
    public Categoria obtenerPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idCategoria", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                return documentToCategoria(querySnapshot.getDocuments().get(0));
            }

        } catch (Exception e) {
            System.err.println("✗ Error al obtener categoría: " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualizar categoría
     */
    public boolean actualizar(Categoria categoria) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idCategoria", categoria.getIdCategoria())
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();
                Map<String, Object> data = categoriaToMap(categoria);

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .set(data);

                writeResult.get();
                System.out.println("✓ Categoría actualizada");
                return true;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al actualizar categoría: " + e.getMessage());
        }

        return false;
    }

    /**
     * Eliminar categoría
     */
    public boolean eliminar(int id) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("idCategoria", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                String docId = querySnapshot.getDocuments().get(0).getId();

                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                        .document(docId)
                        .delete();

                writeResult.get();
                System.out.println("✓ Categoría eliminada");
                return true;
            }

        } catch (Exception e) {
            System.err.println("✗ Error al eliminar categoría: " + e.getMessage());
            if (e.getMessage().contains("FAILED_PRECONDITION")) {
                System.err.println("  Puede que haya productos asociados a esta categoría");
            }
        }

        return false;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, Object> categoriaToMap(Categoria categoria) {
        Map<String, Object> data = new HashMap<>();
        data.put("idCategoria", categoria.getIdCategoria());
        data.put("nombreCategoria", categoria.getNombreCategoria());
        data.put("descripcion", categoria.getDescripcion());
        data.put("fechaCreacion", System.currentTimeMillis());
        return data;
    }

    private Categoria documentToCategoria(DocumentSnapshot document) {
        try {
            Categoria categoria = new Categoria();
            categoria.setIdCategoria(document.getLong("idCategoria") != null ?
                    document.getLong("idCategoria").intValue() : 0);
            categoria.setNombreCategoria(document.getString("nombreCategoria"));
            categoria.setDescripcion(document.getString("descripcion"));
            return categoria;
        } catch (Exception e) {
            System.err.println("✗ Error al convertir documento a categoría: " + e.getMessage());
            return null;
        }
    }
}