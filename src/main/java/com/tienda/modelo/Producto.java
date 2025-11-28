package com.tienda.modelo;

import java.sql.Timestamp;

public class Producto {
    private long idProducto;
    private String nombreProducto;
    private String descripcion;
    private double precioUnitario;
    private int stockActual;
    private int stockMinimo;
    private long idCategoria;
    private long idProveedor;
    private String codigoBarras;
    private Timestamp fechaRegistro;
    private Timestamp ultimaActualizacion;
    private boolean activo;

    // Para mostrar en la interfaz (no están en la BD)
    private String nombreCategoria;
    private String nombreProveedor;

    // Constructor vacío
    public Producto() {
        this.activo = true;
    }

    // Constructor completo
    public Producto(long idProducto, String nombreProducto, String descripcion,
                    double precioUnitario, int stockActual, int stockMinimo,
                    long idCategoria, long idProveedor, String codigoBarras, boolean activo) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.idCategoria = idCategoria;
        this.idProveedor = idProveedor;
        this.codigoBarras = codigoBarras;
        this.activo = activo;
    }

    // Constructor para crear nuevo producto
    public Producto(String nombreProducto, String descripcion, double precioUnitario,
                    int stockActual, int stockMinimo, long idCategoria,
                    long idProveedor, String codigoBarras) {
        this.nombreProducto = nombreProducto;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.idCategoria = idCategoria;
        this.idProveedor = idProveedor;
        this.codigoBarras = codigoBarras;
        this.activo = true;
    }

    // Getters y Setters
    public long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Timestamp getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(Timestamp ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    // Método para verificar stock bajo
    public boolean isBajoStock() {
        return stockActual <= stockMinimo;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", precioUnitario=" + precioUnitario +
                ", stockActual=" + stockActual +
                ", stockMinimo=" + stockMinimo +
                ", categoria='" + nombreCategoria + '\'' +
                ", proveedor='" + nombreProveedor + '\'' +
                ", activo=" + activo +
                '}';
    }
}