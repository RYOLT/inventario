package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tienda.database.ConexionDB;
import com.tienda.modelo.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CategoriaDAO {
    private static final String COLLECTION_NAME = "categorias";
    private Firestore firestore;

    public CategoriaDAO() {
        this.firestore = ConexionDB.getFirestore();
    }

    // Obtener todas las categorías
    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .orderBy("nombre_categoria")
                    .get();

            QuerySnapshot querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Categoria categoria = documentToCategoria(document);
                if (categoria != null) {
                    categorias.add(categoria);
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener categorías: " + e.getMessage());
        }

        return categorias;
    }

    // Agregar nueva categoría
    public boolean agregar(Categoria categoria) {
        try {
            Map<String, Object> categoriaMap = new HashMap<>();

            // Generar ID automático
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            int id = docRef.getId().hashCode();

            categoriaMap.put("id_categoria", id);
            categoriaMap.put("nombre_categoria", categoria.getNombreCategoria());
            categoriaMap.put("descripcion", categoria.getDescripcion());
            categoriaMap.put("timestamp", FieldValue.serverTimestamp());
            categoriaMap.put("docId", docRef.getId());

            ApiFuture<WriteResult> result = docRef.set(categoriaMap);
            result.get();

            categoria.setIdCategoria(id);
            System.out.println("✅ Categoría agregada: " + docRef.getId());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar categoría: " + e.getMessage());
            return false;
        }
    }

    // Obtener categoría por ID
    public Categoria obtenerPorId(int id) {
        try {
            ApiFuture<QuerySnapshot> query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", id)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = query.get();

            if (!querySnapshot.isEmpty()) {
                return documentToCategoria(querySnapshot.getDocuments().get(0));
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener categoría: " + e.getMessage());
        }

        return null;
    }

    // Convertir DocumentSnapshot a Categoria
    private Categoria documentToCategoria(DocumentSnapshot document) {
        try {
            Categoria categoria = new Categoria();
            categoria.setIdCategoria((int) document.getLong("id_categoria").longValue());
            categoria.setNombreCategoria(document.getString("nombre_categoria"));
            categoria.setDescripcion(document.getString("descripcion"));
            return categoria;
        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento de categoría: " + e.getMessage());
            return null;
        }
    }
}