package com.tienda.ui;

import com.tienda.dao.CategoriaDAO;
import com.tienda.dao.ProductoDAO;
import com.tienda.dao.ProveedorDAO;
import com.tienda.modelo.Categoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaInventario extends JFrame {
    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private ProveedorDAO proveedorDAO;

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    // Campos de texto
    private JTextField txtNombre, txtDescripcion, txtPrecio, txtStockActual,
            txtStockMinimo, txtCodigoBarras, txtBuscar;

    // ComboBox para FK
    private JComboBox<Categoria> cmbCategoria;
    private JComboBox<Proveedor> cmbProveedor;

    // Botones
    private JButton btnAgregar, btnActualizar, btnEliminar, btnLimpiar,
            btnBuscar, btnStockBajo;

    public VentanaInventario() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        proveedorDAO = new ProveedorDAO();

        inicializarComponentes();
        cargarDatos();
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Inventario");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Primero crear los ComboBox vacíos
        cmbCategoria = new JComboBox<>();
        cmbProveedor = new JComboBox<>();

        // Luego cargar los datos en ellos
        cargarCategorias();
        cargarProveedores();

        // Ahora crear la interfaz con los ComboBox ya cargados
        // Panel superior - Búsqueda (ahora con categorías cargadas)
        add(crearPanelBusqueda(), BorderLayout.NORTH);

        // Panel central - Tabla
        add(crearPanelTabla(), BorderLayout.CENTER);

        // Panel derecho - Formulario
        add(crearPanelFormulario(), BorderLayout.EAST);

        // Panel inferior - Estado
        /*add(crearPanelEstado(), BorderLayout.SOUTH);*/
    }

    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Búsqueda y Filtros"));

        // Panel superior con búsqueda
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        panelSuperior.add(new JLabel("Buscar producto:"));
        txtBuscar = new JTextField(20);
        panelSuperior.add(txtBuscar);

        btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.addActionListener(e -> buscarProductos());
        panelSuperior.add(btnBuscar);

        JButton btnMostrarTodos = new JButton("Mostrar Todos");
        btnMostrarTodos.addActionListener(e -> cargarDatos());
        panelSuperior.add(btnMostrarTodos);

        btnStockBajo = new JButton("Stock Bajo");
        btnStockBajo.addActionListener(e -> mostrarStockBajo());
        btnStockBajo.setBackground(new Color(255, 200, 100));
        panelSuperior.add(btnStockBajo);

        // Panel inferior con filtro deslizante de categorías
        JPanel panelCategorias = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelCategorias.add(new JLabel("Filtrar por categoría:"));

        JPanel panelBotonesCat = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelBotonesCat.setBorder(BorderFactory.createEtchedBorder());

        // Botón "Todas"
        JButton btnTodasCat = new JButton("Todas");
        btnTodasCat.setBackground(new Color(70, 130, 180));
        btnTodasCat.setForeground(Color.BLACK);
        btnTodasCat.setFocusPainted(false);
        btnTodasCat.addActionListener(e -> cargarDatos());
        panelBotonesCat.add(btnTodasCat);

        // Crear botón para cada categoría
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            Categoria cat = cmbCategoria.getItemAt(i);
            JButton btnCat = new JButton(cat.getNombreCategoria());
            btnCat.setBackground(new Color(100, 149, 237));
            btnCat.setForeground(Color.BLACK);
            btnCat.setFocusPainted(false);
            btnCat.addActionListener(e -> filtrarPorCategoria(cat.getIdCategoria(), cat.getNombreCategoria()));
            panelBotonesCat.add(btnCat);
        }

        // Envolver en un JScrollPane para hacerlo deslizable
        JScrollPane scrollCategorias = new JScrollPane(panelBotonesCat);
        scrollCategorias.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollCategorias.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollCategorias.setPreferredSize(new Dimension(950, 50));
        scrollCategorias.setBorder(null);

        panelCategorias.add(scrollCategorias);

        // Agregar ambos paneles al panel principal
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelCategorias, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Productos en Inventario"));

        String[] columnas = {"ID", "Nombre", "Descripción", "Precio", "Stock",
                "Stock Mín.", "Categoría", "Proveedor", "Código Barras"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProductoSeleccionado();
            }
        });

        // Ajustar anchos de columnas
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(80);  // Precio
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(60);  // Stock
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(80);  // Stock Min
        tablaProductos.getColumnModel().getColumn(6).setPreferredWidth(100); // Categoría
        tablaProductos.getColumnModel().getColumn(7).setPreferredWidth(120); // Proveedor

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        panel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int fila = 0;

        // Nombre
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Nombre:*"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panel.add(txtNombre, gbc);
        fila++;

        // Descripción
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        txtDescripcion = new JTextField(20);
        panel.add(txtDescripcion, gbc);
        fila++;

        // Precio
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Precio:*"), gbc);
        gbc.gridx = 1;
        txtPrecio = new JTextField(20);
        panel.add(txtPrecio, gbc);
        fila++;

        // Stock Actual
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Stock Actual:*"), gbc);
        gbc.gridx = 1;
        txtStockActual = new JTextField(20);
        panel.add(txtStockActual, gbc);
        fila++;

        // Stock Mínimo
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Stock Mínimo:*"), gbc);
        gbc.gridx = 1;
        txtStockMinimo = new JTextField(20);
        panel.add(txtStockMinimo, gbc);
        fila++;

        // Categoría (ComboBox)
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Categoría:*"), gbc);
        gbc.gridx = 1;
        // Ya no creamos el ComboBox aquí, solo lo agregamos
        panel.add(cmbCategoria, gbc);
        fila++;

        // Proveedor (ComboBox)
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Proveedor:*"), gbc);
        gbc.gridx = 1;
        // Ya no creamos el ComboBox aquí, solo lo agregamos
        panel.add(cmbProveedor, gbc);
        fila++;

        // Código de Barras
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Código Barras:"), gbc);
        gbc.gridx = 1;
        txtCodigoBarras = new JTextField(20);
        panel.add(txtCodigoBarras, gbc);
        fila++;

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 5));

        btnAgregar = new JButton("➕ Agregar");
        btnAgregar.setBackground(new Color(100, 200, 100));
        btnAgregar.addActionListener(e -> agregarProducto());
        panelBotones.add(btnAgregar);

        btnActualizar = new JButton("✏️ Actualizar");
        btnActualizar.setBackground(new Color(100, 150, 250));
        btnActualizar.addActionListener(e -> actualizarProducto());
        panelBotones.add(btnActualizar);

        btnEliminar = new JButton("🗑️ Eliminar");
        btnEliminar.setBackground(new Color(250, 100, 100));
        btnEliminar.addActionListener(e -> eliminarProducto());
        panelBotones.add(btnEliminar);

        btnLimpiar = new JButton("🧹 Limpiar");
        btnLimpiar.addActionListener(e -> limpiarCampos());
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(panelBotones, gbc);

        return panel;
    }



    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        List<Categoria> categorias = categoriaDAO.obtenerTodas();

        if (categorias.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay categorías. Ejecuta el script SQL primero.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }

        for (Categoria cat : categorias) {
            cmbCategoria.addItem(cat);
        }
    }

    private void cargarProveedores() {
        cmbProveedor.removeAllItems();
        List<Proveedor> proveedores = proveedorDAO.obtenerTodos();

        if (proveedores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay proveedores. Ejecuta el script SQL primero.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }

        for (Proveedor prov : proveedores) {
            cmbProveedor.addItem(prov);
        }
    }

    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.obtenerTodosLosProductos();

        for (Producto p : productos) {
            Object[] fila = {
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getDescripcion(),
                    String.format("$%.2f", p.getPrecioUnitario()),
                    p.getStockActual(),
                    p.getStockMinimo(),
                    p.getNombreCategoria(),
                    p.getNombreProveedor(),
                    p.getCodigoBarras()
            };

            // Resaltar productos con stock bajo
            modeloTabla.addRow(fila);
            if (p.isBajoStock()) {
                int ultimaFila = modeloTabla.getRowCount() - 1;
                // Podríamos cambiar el color de fondo aquí
            }
        }
    }

    private void buscarProductos() {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un término de búsqueda");
            return;
        }

        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.buscarPorNombre(termino);

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron productos");
        } else {
            for (Producto p : productos) {
                Object[] fila = {
                        p.getIdProducto(),
                        p.getNombreProducto(),
                        p.getDescripcion(),
                        String.format("$%.2f", p.getPrecioUnitario()),
                        p.getStockActual(),
                        p.getStockMinimo(),
                        p.getNombreCategoria(),
                        p.getNombreProveedor(),
                        p.getCodigoBarras()
                };
                modeloTabla.addRow(fila);
            }
        }
    }

    private void mostrarStockBajo() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.obtenerProductosStockBajo();

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "✅ No hay productos con stock bajo",
                    "Stock OK",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                Object[] fila = {
                        p.getIdProducto(),
                        p.getNombreProducto(),
                        p.getDescripcion(),
                        String.format("$%.2f", p.getPrecioUnitario()),
                        p.getStockActual(),
                        p.getStockMinimo(),
                        p.getNombreCategoria(),
                        p.getNombreProveedor(),
                        p.getCodigoBarras()
                };
                modeloTabla.addRow(fila);
            }

            JOptionPane.showMessageDialog(this,
                    "⚠️ Se encontraron " + productos.size() + " productos con stock bajo",
                    "Alerta de Stock",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void filtrarPorCategoria(int idCategoria, String nombreCategoria) {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.buscarPorCategoria(idCategoria);

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay productos en la categoría: " + nombreCategoria,
                    "Sin resultados",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                Object[] fila = {
                        p.getIdProducto(),
                        p.getNombreProducto(),
                        p.getDescripcion(),
                        String.format("$%.2f", p.getPrecioUnitario()),
                        p.getStockActual(),
                        p.getStockMinimo(),
                        p.getNombreCategoria(),
                        p.getNombreProveedor(),
                        p.getCodigoBarras()
                };
                modeloTabla.addRow(fila);
            }

            System.out.println("📂 Filtrado por categoría: " + nombreCategoria +
                    " (" + productos.size() + " productos)");
        }
    }

    private void agregarProducto() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty() ||
                    txtPrecio.getText().trim().isEmpty() ||
                    txtStockActual.getText().trim().isEmpty() ||
                    txtStockMinimo.getText().trim().isEmpty() ||
                    cmbCategoria.getSelectedItem() == null ||
                    cmbProveedor.getSelectedItem() == null) {

                JOptionPane.showMessageDialog(this,
                        "Complete todos los campos obligatorios (*)",
                        "Campos Requeridos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Categoria categoriaSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            Proveedor proveedorSeleccionado = (Proveedor) cmbProveedor.getSelectedItem();

            Producto producto = new Producto(
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    Integer.parseInt(txtStockActual.getText().trim()),
                    Integer.parseInt(txtStockMinimo.getText().trim()),
                    categoriaSeleccionada.getIdCategoria(),
                    proveedorSeleccionado.getIdProveedor(),
                    txtCodigoBarras.getText().trim()
            );

            if (productoDAO.agregarProducto(producto)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Producto agregado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al agregar producto. Verifica las FK.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Formato incorrecto en precio o cantidades",
                    "Error de Formato",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un producto de la tabla");
            return;
        }

        try {
            int id = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

            Categoria categoriaSeleccionada = (Categoria) cmbCategoria.getSelectedItem();
            Proveedor proveedorSeleccionado = (Proveedor) cmbProveedor.getSelectedItem();

            Producto producto = new Producto(
                    id,
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    Double.parseDouble(txtPrecio.getText().trim()),
                    Integer.parseInt(txtStockActual.getText().trim()),
                    Integer.parseInt(txtStockMinimo.getText().trim()),
                    categoriaSeleccionada.getIdCategoria(),
                    proveedorSeleccionado.getIdProveedor(),
                    txtCodigoBarras.getText().trim(),
                    true
            );

            if (productoDAO.actualizarProducto(producto)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Producto actualizado exitosamente");
                limpiarCampos();
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al actualizar producto");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Formato incorrecto en precio o cantidades");
        }
    }

    private void eliminarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un producto de la tabla");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar este producto?\n(Se marcará como inactivo)",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(filaSeleccionada, 0);

            if (productoDAO.eliminarProducto(id)) {
                JOptionPane.showMessageDialog(this,
                        "✅ Producto eliminado exitosamente");
                limpiarCampos();
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al eliminar producto");
            }
        }
    }

    private void cargarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            int id = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
            Producto producto = productoDAO.obtenerProductoPorId(id);

            if (producto != null) {
                txtNombre.setText(producto.getNombreProducto());
                txtDescripcion.setText(producto.getDescripcion());
                txtPrecio.setText(String.valueOf(producto.getPrecioUnitario()));
                txtStockActual.setText(String.valueOf(producto.getStockActual()));
                txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
                txtCodigoBarras.setText(producto.getCodigoBarras());

                // Seleccionar categoría y proveedor en los ComboBox
                for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                    if (cmbCategoria.getItemAt(i).getIdCategoria() == producto.getIdCategoria()) {
                        cmbCategoria.setSelectedIndex(i);
                        break;
                    }
                }

                for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                    if (cmbProveedor.getItemAt(i).getIdProveedor() == producto.getIdProveedor()) {
                        cmbProveedor.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStockActual.setText("");
        txtStockMinimo.setText("");
        txtCodigoBarras.setText("");
        txtBuscar.setText("");

        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        if (cmbProveedor.getItemCount() > 0) {
            cmbProveedor.setSelectedIndex(0);
        }

        tablaProductos.clearSelection();
    }
}