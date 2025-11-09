package com.tienda.modelo;

import java.sql.Timestamp;

public class Categoria {
    private int idCategoria;
    private String nombreCategoria;
    private String descripcion;
    private Timestamp fechaCreacion;

    // Constructor vacío
    public Categoria() {
    }

    // Constructor completo
    public Categoria(int idCategoria, String nombreCategoria, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
    }

    // Constructor sin ID
    public Categoria(String nombreCategoria, String descripcion) {
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombreCategoria; // Para JComboBox
    }
}