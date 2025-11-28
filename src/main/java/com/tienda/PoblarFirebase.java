package com.tienda;

import com.tienda.dao.CategoriaDAO;
import com.tienda.dao.ProductoDAO;
import com.tienda.dao.ProveedorDAO;
import com.tienda.database.ConexionDB;
import com.tienda.modelo.Categoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.Proveedor;

import javax.swing.*;

/**
 * Clase para poblar Firebase Firestore con datos iniciales
 * Ejecuta esta clase UNA VEZ para crear los datos de ejemplo
 */
public class PoblarFirebase {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("POBLANDO FIREBASE CON DATOS INICIALES");
        System.out.println("=".repeat(60));

        // Verificar conexión
        if (!ConexionDB.probarConexion()) {
            System.err.println("\n❌ No se pudo conectar a Firebase.");
            System.err.println("Verifica que el archivo de credenciales esté en src/main/resources/");
            return;
        }

        System.out.println("\n✅ Conexión exitosa a Firebase");
        System.out.println("Proyecto: " + ConexionDB.obtenerProyectoId());

        // Mostrar confirmación
        int respuesta = JOptionPane.showConfirmDialog(
                null,
                "¿Deseas poblar Firebase con datos de ejemplo?\n\n" +
                        "Esto agregará:\n" +
                        "• 8 categorías\n" +
                        "• 5 proveedores\n" +
                        "• 25 productos\n\n" +
                        "ADVERTENCIA: Si ya existen datos, se agregarán más.",
                "Confirmar población de datos",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            System.out.println("\n❌ Operación cancelada por el usuario");
            return;
        }

        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        ProductoDAO productoDAO = new ProductoDAO();

        // Paso 1: Crear Categorías
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PASO 1: Creando Categorías...");
        System.out.println("=".repeat(60));

        Categoria[] categorias = crearCategorias(categoriaDAO);

        // Paso 2: Crear Proveedores
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PASO 2: Creando Proveedores...");
        System.out.println("=".repeat(60));

        Proveedor[] proveedores = crearProveedores(proveedorDAO);

        // Paso 3: Crear Productos
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PASO 3: Creando Productos...");
        System.out.println("=".repeat(60));

        crearProductos(productoDAO, categorias, proveedores);

        // Resumen final
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ PROCESO COMPLETADO EXITOSAMENTE");
        System.out.println("=".repeat(60));
        System.out.println("Total de categorías: " + categorias.length);
        System.out.println("Total de proveedores: " + proveedores.length);
        System.out.println("Total de productos creados: Verificar en consola");
        System.out.println("\n¡Ahora puedes ejecutar la aplicación principal!");

        JOptionPane.showMessageDialog(
                null,
                "✅ Datos cargados exitosamente en Firebase\n\n" +
                        "Ahora puedes cerrar esta ventana y ejecutar Main.java",
                "Proceso Completado",
                JOptionPane.INFORMATION_MESSAGE
        );

        System.exit(0);
    }

    private static Categoria[] crearCategorias(CategoriaDAO dao) {
        String[][] datosCategoria = {
                {"Electrónica", "Dispositivos electrónicos y accesorios tecnológicos"},
                {"Ropa", "Prendas de vestir y accesorios de moda"},
                {"Alimentos", "Productos alimenticios y bebidas"},
                {"Hogar", "Artículos para el hogar y decoración"},
                {"Deportes", "Equipamiento deportivo y fitness"},
                {"Libros", "Libros y material de lectura"},
                {"Juguetes", "Juguetes y entretenimiento infantil"},
                {"Salud", "Productos de salud y cuidado personal"}
        };

        Categoria[] categorias = new Categoria[datosCategoria.length];

        for (int i = 0; i < datosCategoria.length; i++) {
            Categoria cat = new Categoria(datosCategoria[i][0], datosCategoria[i][1]);
            if (dao.agregar(cat)) {
                categorias[i] = cat;
                System.out.println("  ✓ Categoría creada: " + cat.getNombreCategoria() + " (ID: " + cat.getIdCategoria() + ")");
            } else {
                System.err.println("  ✗ Error al crear categoría: " + datosCategoria[i][0]);
            }
        }

        return categorias;
    }

    private static Proveedor[] crearProveedores(ProveedorDAO dao) {
        String[][] datosProveedor = {
                {"TechSupply SA de CV", "555-1234-5678", "ventas@techsupply.com.mx", "Av. Tecnología 100", "Ciudad de México", "México"},
                {"Distribuidora Nacional", "555-8765-4321", "contacto@disnacional.com", "Calle Comercio 200", "Guadalajara", "México"},
                {"ImportExport Global", "555-9012-3456", "info@impexglobal.com", "Boulevard Industrial 300", "Monterrey", "México"},
                {"Proveedora del Norte", "555-1111-2222", "ventas@provnorte.com", "Av. Frontera 400", "Tijuana", "México"},
                {"Comercial del Bajío", "555-3333-4444", "info@combajio.com", "Calle Principal 500", "León", "México"}
        };

        Proveedor[] proveedores = new Proveedor[datosProveedor.length];

        for (int i = 0; i < datosProveedor.length; i++) {
            Proveedor prov = new Proveedor(
                    datosProveedor[i][0], // nombre
                    datosProveedor[i][1], // telefono
                    datosProveedor[i][2], // email
                    datosProveedor[i][3], // direccion
                    datosProveedor[i][4], // ciudad
                    datosProveedor[i][5]  // pais
            );

            if (dao.agregar(prov)) {
                proveedores[i] = prov;
                System.out.println("  ✓ Proveedor creado: " + prov.getNombreProveedor() + " (ID: " + prov.getIdProveedor() + ")");
            } else {
                System.err.println("  ✗ Error al crear proveedor: " + datosProveedor[i][0]);
            }
        }

        return proveedores;
    }

    private static void crearProductos(ProductoDAO dao, Categoria[] categorias, Proveedor[] proveedores) {
        // Datos de productos: {nombre, descripcion, precio, stock, stockMin, idxCategoria, idxProveedor, codigoBarras}
        Object[][] datosProductos = {
                // Electrónica (categoría 0, proveedor 0)
                {"Laptop HP 15-dy2021la", "Laptop HP 15.6 pulgadas, Intel Core i5, 8GB RAM, 256GB SSD", 12999.99, 15, 5, 0, 0, "7501234567890"},
                {"Mouse Logitech M185", "Mouse inalámbrico ergonómico con receptor USB", 299.99, 50, 10, 0, 0, "7501234567891"},
                {"Teclado Mecánico RGB", "Teclado mecánico retroiluminado para gaming", 899.99, 30, 8, 0, 0, "7501234567892"},
                {"Monitor Samsung 24\"", "Monitor LED Full HD 24 pulgadas", 2499.99, 20, 5, 0, 0, "7501234567893"},
                {"Audífonos Bluetooth", "Audífonos inalámbricos con cancelación de ruido", 1299.99, 40, 10, 0, 2, "7501234567894"},

                // Ropa (categoría 1, proveedor 1)
                {"Playera Básica Negra", "Playera de algodón 100% talla M", 199.99, 100, 20, 1, 1, "7501234567895"},
                {"Jeans Mezclilla Azul", "Jeans corte recto talla 32", 599.99, 75, 15, 1, 1, "7501234567896"},
                {"Chamarra Deportiva", "Chamarra impermeable con capucha", 899.99, 45, 10, 1, 1, "7501234567897"},
                {"Zapatos Casual", "Zapatos casuales de cuero talla 27", 1299.99, 30, 8, 1, 3, "7501234567898"},

                // Alimentos (categoría 2, proveedor 4)
                {"Café Molido Premium 500g", "Café 100% arábica molido", 159.99, 200, 30, 2, 4, "7501234567899"},
                {"Cereal Integral 400g", "Cereal de trigo integral con miel", 89.99, 150, 25, 2, 4, "7501234567900"},
                {"Aceite de Oliva 500ml", "Aceite de oliva extra virgen", 249.99, 80, 15, 2, 4, "7501234567901"},

                // Hogar (categoría 3, proveedor 1 y 2)
                {"Juego de Toallas 3 piezas", "Toallas de algodón suave", 399.99, 60, 12, 3, 1, "7501234567902"},
                {"Lámpara de Mesa LED", "Lámpara LED moderna regulable", 599.99, 35, 8, 3, 2, "7501234567903"},
                {"Cojines Decorativos", "Set de 2 cojines decorativos", 299.99, 70, 15, 3, 3, "7501234567904"},

                // Deportes (categoría 4, proveedor 3)
                {"Balón de Fútbol No. 5", "Balón profesional cosido a mano", 449.99, 25, 5, 4, 3, "7501234567905"},
                {"Pesas Mancuernas 5kg", "Par de mancuernas hexagonales", 599.99, 40, 10, 4, 3, "7501234567906"},
                {"Cuerda para Saltar", "Cuerda de saltar ajustable", 149.99, 80, 15, 4, 3, "7501234567907"},

                // Libros (categoría 5, proveedor 4)
                {"El Principito", "Novela clásica de Antoine de Saint-Exupéry", 199.99, 50, 10, 5, 4, "7501234567908"},
                {"Cien Años de Soledad", "Obra maestra de Gabriel García Márquez", 299.99, 40, 8, 5, 4, "7501234567909"},

                // Juguetes (categoría 6, proveedor 2)
                {"Cubo Rubik Original", "Cubo mágico 3x3 clásico", 249.99, 60, 12, 6, 2, "7501234567910"},
                {"Pelota Sensorial", "Pelota de estimulación para bebés", 179.99, 55, 10, 6, 2, "7501234567911"},

                // Salud (categoría 7, proveedor 4 y 0)
                {"Gel Antibacterial 500ml", "Gel desinfectante para manos", 89.99, 120, 20, 7, 4, "7501234567912"},
                {"Termómetro Digital", "Termómetro digital infrarrojo", 399.99, 45, 10, 7, 0, "7501234567913"},
                {"Cubrebocas KN95 (10 pzas)", "Mascarillas de alta protección", 199.99, 200, 30, 7, 4, "7501234567914"}
        };

        int exitosos = 0;
        int errores = 0;

        for (Object[] datos : datosProductos) {
            try {
                int idxCategoria = (int) datos[5];
                int idxProveedor = (int) datos[6];

                Producto producto = new Producto(
                        (String) datos[0],  // nombre
                        (String) datos[1],  // descripcion
                        (double) datos[2],  // precio
                        (int) datos[3],     // stock actual
                        (int) datos[4],     // stock minimo
                        categorias[idxCategoria].getIdCategoria(),  // id_categoria
                        proveedores[idxProveedor].getIdProveedor(), // id_proveedor
                        (String) datos[7]   // codigo barras
                );

                // Guardar nombres para la visualización
                producto.setNombreCategoria(categorias[idxCategoria].getNombreCategoria());
                producto.setNombreProveedor(proveedores[idxProveedor].getNombreProveedor());

                if (dao.agregarProducto(producto)) {
                    exitosos++;
                    System.out.println("  ✓ Producto creado: " + producto.getNombreProducto());
                } else {
                    errores++;
                    System.err.println("  ✗ Error al crear: " + datos[0]);
                }

                // Pequeña pausa para no saturar Firebase
                Thread.sleep(100);

            } catch (Exception e) {
                errores++;
                System.err.println("  ✗ Error al procesar producto: " + datos[0] + " - " + e.getMessage());
            }
        }

        System.out.println("\n📊 Resumen de productos:");
        System.out.println("  ✓ Exitosos: " + exitosos);
        System.out.println("  ✗ Errores: " + errores);
    }
}