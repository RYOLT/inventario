package com.tienda.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * DAO para gestionar Categorías en Firebase Firestore
 * Reemplaza la versión con MySQL/JDBC
 */
public class CategoriaDAO {

    private static final String COLLECTION_NAME = "categorias";
    private Firestore db;

    public CategoriaDAO() {
        this.db = FirestoreConfig.getDatabase();
        if (this.db == null) {
            System.err.println("⚠️ Error: Firestore no está inicializado en CategoriaDAO");
        }
    }

    /**
     * Obtener todas las categorías ordenadas por nombre
     */
    public List<Categoria> obtenerTodas() {
        List<Categoria> categorias = new ArrayList<>();

        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return categorias;
        }

        try {
            // Consulta a Firestore ordenada por nombre_categoria
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .orderBy("nombre_categoria", Query.Direction.ASCENDING)
                    .get();

            // Obtener los documentos
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Convertir cada documento a objeto Categoria
            for (QueryDocumentSnapshot document : documents) {
                Categoria categoria = documentToCategoria(document);
                if (categoria != null) {
                    categorias.add(categoria);
                }
            }

            System.out.println("✓ Se obtuvieron " + categorias.size() + " categorías de Firestore");

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener categorías: " + e.getMessage());
            e.printStackTrace();
        }

        return categorias;
    }

    /**
     * Agregar nueva categoría
     */
    public boolean agregar(Categoria categoria) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Crear mapa con los datos de la categoría
            Map<String, Object> data = new HashMap<>();
            data.put("nombre_categoria", categoria.getNombreCategoria());
            data.put("descripcion", categoria.getDescripcion() != null ? categoria.getDescripcion() : "");
            data.put("fecha_creacion", Timestamp.now());

            // Agregar documento a Firestore (auto-genera ID)
            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();

            // Asignar el ID generado al objeto Categoria
            // En Firestore, los IDs son Strings, pero los convertimos a int con hashCode
            String firestoreId = docRef.getId();
            categoria.setIdCategoria(firestoreId.hashCode()); // Convertir String a int

            // Guardar el ID de Firestore en el documento para referencia futura
            docRef.update("firestore_id", firestoreId);

            System.out.println("✓ Categoría agregada con ID: " + firestoreId);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al agregar categoría: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener categoría por ID
     */
    public Categoria obtenerPorId(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            // Buscar por el campo id_categoria (que es numérico)
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                return documentToCategoria(documents.get(0));
            }

            System.out.println("⚠️ No se encontró categoría con ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener categoría por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtener categoría por su ID de Firestore (String)
     */
    public Categoria obtenerPorFirestoreId(String firestoreId) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return null;
        }

        try {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(firestoreId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                return documentToCategoria(document);
            }

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al obtener categoría: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualizar categoría existente
     */
    public boolean actualizar(Categoria categoria) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Buscar el documento por id_categoria
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", categoria.getIdCategoria())
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró la categoría para actualizar");
                return false;
            }

            // Obtener el ID del documento en Firestore
            String docId = documents.get(0).getId();

            // Preparar datos a actualizar
            Map<String, Object> updates = new HashMap<>();
            updates.put("nombre_categoria", categoria.getNombreCategoria());
            updates.put("descripcion", categoria.getDescripcion() != null ? categoria.getDescripcion() : "");

            // Actualizar el documento
            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .update(updates);

            writeResult.get(); // Esperar a que se complete

            System.out.println("✓ Categoría actualizada: " + categoria.getNombreCategoria());
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al actualizar categoría: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar categoría (elimina físicamente de Firestore)
     */
    public boolean eliminar(int id) {
        if (db == null) {
            System.err.println("❌ Error: No hay conexión a Firestore");
            return false;
        }

        try {
            // Buscar el documento por id_categoria
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("id_categoria", id)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                System.err.println("❌ No se encontró la categoría para eliminar");
                return false;
            }

            // Obtener el ID del documento
            String docId = documents.get(0).getId();

            // VERIFICAR si hay productos con esta categoría antes de eliminar
            ApiFuture<QuerySnapshot> productosConCategoria = db.collection("productos")
                    .whereEqualTo("id_categoria", id)
                    .limit(1)
                    .get();

            if (!productosConCategoria.get().isEmpty()) {
                System.err.println("❌ No se puede eliminar: hay productos asociados a esta categoría");
                return false;
            }

            // Eliminar el documento
            ApiFuture<WriteResult> writeResult = db.collection(COLLECTION_NAME)
                    .document(docId)
                    .delete();

            writeResult.get(); // Esperar a que se complete

            System.out.println("✓ Categoría eliminada con ID: " + id);
            return true;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al eliminar categoría: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Contar cuántas categorías hay en total
     */
    public int contarCategorias() {
        if (db == null) {
            return 0;
        }

        try {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            return future.get().size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error al contar categorías: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Método auxiliar: Convierte un DocumentSnapshot de Firestore a objeto Categoria
     */
    private Categoria documentToCategoria(DocumentSnapshot document) {
        try {
            // Obtener el ID de Firestore (es un String)
            String firestoreId = document.getId();

            // Obtener los datos del documento
            String nombreCategoria = document.getString("nombre_categoria");
            String descripcion = document.getString("descripcion");
            Timestamp fechaCreacion = document.getTimestamp("fecha_creacion");

            // Crear objeto Categoria
            // Usar hashCode del ID de Firestore como ID numérico
            int idCategoria = document.contains("id_categoria")
                    ? document.getLong("id_categoria").intValue()
                    : firestoreId.hashCode();

            Categoria categoria = new Categoria(
                    idCategoria,
                    nombreCategoria != null ? nombreCategoria : "",
                    descripcion != null ? descripcion : ""
            );

            // Convertir Timestamp de Firestore a java.sql.Timestamp
            if (fechaCreacion != null) {
                categoria.setFechaCreacion(
                        new java.sql.Timestamp(fechaCreacion.toDate().getTime())
                );
            }

            return categoria;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir documento a Categoria: " + e.getMessage());
            return null;
        }
    }

    /**
     * Inicializar categorías de ejemplo (ejecutar solo una vez)
     */
    public void inicializarCategoriasEjemplo() {
        if (contarCategorias() > 0) {
            System.out.println("⚠️ Ya existen categorías en Firestore");
            return;
        }

        String[][] categoriasEjemplo = {
                {"Electrónica", "Dispositivos y componentes electrónicos"},
                {"Ropa", "Prendas de vestir y accesorios"},
                {"Alimentos", "Productos alimenticios"},
                {"Bebidas", "Todo tipo de bebidas"},
                {"Hogar", "Artículos para el hogar"},
                {"Deportes", "Equipo y ropa deportiva"},
                {"Juguetes", "Juguetes y entretenimiento"},
                {"Libros", "Literatura y material educativo"}
        };

        int contador = 0;
        for (String[] cat : categoriasEjemplo) {
            Categoria categoria = new Categoria(cat[0], cat[1]);
            if (agregar(categoria)) {
                contador++;
            }
        }

        System.out.println("✓ Se agregaron " + contador + " categorías de ejemplo");
    }
}