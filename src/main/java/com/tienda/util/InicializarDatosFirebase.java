package com.tienda.util;

import com.tienda.dao.CategoriaDAO;
import com.tienda.dao.ProveedorDAO;
import com.tienda.database.ConexionDB;
import com.tienda.modelo.Categoria;
import com.tienda.modelo.Proveedor;

/**
 * Clase helper para inicializar datos de ejemplo en Firebase
 * Ejecutar una sola vez al inicio
 */
public class InicializarDatosFirebase {

    public static void main(String[] args) {
        System.out.println("=== Inicializando datos en Firebase ===");

        // Inicializar Firebase
        ConexionDB.inicializar();

        if (!ConexionDB.estaConectado()) {
            System.err.println("❌ No se pudo conectar a Firebase");
            return;
        }

        // Inicializar DAOs
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();

        // Agregar categorías de ejemplo
        System.out.println("\n--- Agregando Categorías ---");

        Categoria cat1 = new Categoria("Electrónica", "Dispositivos electrónicos y accesorios");
        Categoria cat2 = new Categoria("Alimentos", "Productos comestibles y bebidas");
        Categoria cat3 = new Categoria("Ropa", "Prendas de vestir");
        Categoria cat4 = new Categoria("Hogar", "Artículos para el hogar");
        Categoria cat5 = new Categoria("Deportes", "Equipo y ropa deportiva");

        if (categoriaDAO.agregar(cat1)) System.out.println("✓ Categoría Electrónica agregada");
        if (categoriaDAO.agregar(cat2)) System.out.println("✓ Categoría Alimentos agregada");
        if (categoriaDAO.agregar(cat3)) System.out.println("✓ Categoría Ropa agregada");
        if (categoriaDAO.agregar(cat4)) System.out.println("✓ Categoría Hogar agregada");
        if (categoriaDAO.agregar(cat5)) System.out.println("✓ Categoría Deportes agregada");

        // Agregar proveedores de ejemplo
        System.out.println("\n--- Agregando Proveedores ---");

        Proveedor prov1 = new Proveedor(
                "TechSupply SA",
                "555-0001",
                "ventas@techsupply.com",
                "Av. Reforma 123",
                "Ciudad de México",
                "México"
        );

        Proveedor prov2 = new Proveedor(
                "Distribuidora Global",
                "555-0002",
                "contacto@disglobal.com",
                "Calle Juárez 456",
                "Guadalajara",
                "México"
        );

        Proveedor prov3 = new Proveedor(
                "Proveedor Local",
                "555-0003",
                "info@provlocal.com",
                "Hidalgo 789",
                "Pachuca",
                "México"
        );

        Proveedor prov4 = new Proveedor(
                "ImportMex",
                "555-0004",
                "ventas@importmex.com",
                "Industrial 321",
                "Monterrey",
                "México"
        );

        if (proveedorDAO.agregar(prov1)) System.out.println("✓ Proveedor TechSupply agregado");
        if (proveedorDAO.agregar(prov2)) System.out.println("✓ Proveedor Distribuidora Global agregado");
        if (proveedorDAO.agregar(prov3)) System.out.println("✓ Proveedor Local agregado");
        if (proveedorDAO.agregar(prov4)) System.out.println("✓ Proveedor ImportMex agregado");

        System.out.println("\n=== Datos inicializados correctamente ===");
        System.out.println("Ahora puedes ejecutar la aplicación principal (Main.java)");
    }
}