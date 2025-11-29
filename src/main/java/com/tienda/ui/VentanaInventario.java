package com.tienda.ui;

import com.tienda.dao.CategoriaDAO;
import com.tienda.dao.ProductoDAO;
import com.tienda.dao.ProveedorDAO;
import com.tienda.database.FirestoreConfig;
import com.tienda.modelo.Categoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
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

    // ComboBox para FK y filtros
    private JComboBox<Categoria> cmbCategoria;
    private JComboBox<Proveedor> cmbProveedor;
    private JComboBox<String> cmbFiltroCategoria; // Nuevo: para búsqueda por categoría

    // Botones
    private JButton btnAgregar, btnActualizar, btnEliminar, btnLimpiar,
            btnBuscar, btnStockBajo, btnFiltrarCategoria;

    // Estado de conexión
    private JLabel lblEstadoConexion;
    private boolean conexionActiva = false;

    public VentanaInventario() {
        productoDAO = new ProductoDAO();
        categoriaDAO = new CategoriaDAO();
        proveedorDAO = new ProveedorDAO();

        verificarConexion();
        inicializarComponentes();

        if (conexionActiva) {
            cargarCategorias();
            cargarProveedores();
            cargarCategoriasFiltro();
            cargarDatos();
        } else {
            mostrarErrorConexion();
        }
    }

    private void verificarConexion() {
        conexionActiva = FirestoreConfig.estaConectado();
        if (conexionActiva) {
            System.out.println("✓ Conexión a Firestore verificada");
        } else {
            System.err.println("✗ Error de conexión a Firestore");
        }
    }

    private void mostrarErrorConexion() {
        JOptionPane.showMessageDialog(this,
                "❌ No se pudo conectar a Firestore.\n\n" +
                        "Verifica que:\n" +
                        "1. El archivo serviceAccountKey.json esté en src/main/resources/\n" +
                        "2. El archivo sea válido (descargado de Firebase Console)\n" +
                        "3. Tengas conexión a Internet\n" +
                        "4. Las credenciales tengan permisos de lectura/escritura\n\n" +
                        "La aplicación se cerrará.",
                "Error de Conexión",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private void inicializarComponentes() {
        setTitle("Sistema de Inventario - Tienda");
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior - Búsqueda
        add(crearPanelBusqueda(), BorderLayout.NORTH);

        // Panel central - Tabla
        add(crearPanelTabla(), BorderLayout.CENTER);

        // Panel derecho - Formulario
        add(crearPanelFormulario(), BorderLayout.EAST);

        // Panel inferior - Estado
        add(crearPanelEstado(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelBusqueda() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(BorderFactory.createTitledBorder("Búsqueda y Filtros"));

        // Panel superior: búsqueda por nombre
        JPanel panelBusquedaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panelBusquedaNombre.add(new JLabel("🔍 Buscar por nombre:"));
        txtBuscar = new JTextField(25);
        txtBuscar.setToolTipText("Ingrese el nombre del producto a buscar");
        // Buscar al presionar Enter
        txtBuscar.addActionListener(e -> buscarProductos());
        panelBusquedaNombre.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(100, 150, 250));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(e -> buscarProductos());
        panelBusquedaNombre.add(btnBuscar);

        JButton btnMostrarTodos = new JButton("📋 Mostrar Todos");
        btnMostrarTodos.setBackground(new Color(80, 180, 100));
        btnMostrarTodos.setForeground(Color.WHITE);
        btnMostrarTodos.setFocusPainted(false);
        btnMostrarTodos.addActionListener(e -> {
            txtBuscar.setText("");
            if (cmbFiltroCategoria != null) {
                cmbFiltroCategoria.setSelectedIndex(0);
            }
            cargarDatos();
        });
        panelBusquedaNombre.add(btnMostrarTodos);

        btnStockBajo = new JButton("⚠️ Stock Bajo");
        btnStockBajo.setBackground(new Color(255, 150, 50));
        btnStockBajo.setForeground(Color.WHITE);
        btnStockBajo.setFocusPainted(false);
        btnStockBajo.addActionListener(e -> mostrarStockBajo());
        panelBusquedaNombre.add(btnStockBajo);

        // Panel inferior: filtro por categoría
        JPanel panelFiltroCategoria = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        panelFiltroCategoria.add(new JLabel("📦 Filtrar por categoría:"));
        cmbFiltroCategoria = new JComboBox<>();
        cmbFiltroCategoria.setPreferredSize(new Dimension(200, 25));
        cmbFiltroCategoria.setToolTipText("Seleccione una categoría para filtrar");
        panelFiltroCategoria.add(cmbFiltroCategoria);

        btnFiltrarCategoria = new JButton("Aplicar Filtro");
        btnFiltrarCategoria.setBackground(new Color(120, 100, 200));
        btnFiltrarCategoria.setForeground(Color.WHITE);
        btnFiltrarCategoria.setFocusPainted(false);
        btnFiltrarCategoria.addActionListener(e -> filtrarPorCategoria());
        panelFiltroCategoria.add(btnFiltrarCategoria);

        // Agregar ambos paneles al panel principal
        panelPrincipal.add(panelBusquedaNombre, BorderLayout.NORTH);
        panelPrincipal.add(panelFiltroCategoria, BorderLayout.SOUTH);

        return panelPrincipal;
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
        tablaProductos.setRowHeight(25);
        tablaProductos.getTableHeader().setReorderingAllowed(false);

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

        // Renderer personalizado para resaltar stock bajo
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                try {
                    int stockActual = Integer.parseInt(table.getValueAt(row, 4).toString());
                    int stockMinimo = Integer.parseInt(table.getValueAt(row, 5).toString());

                    if (!isSelected) {
                        if (stockActual <= stockMinimo) {
                            c.setBackground(new Color(255, 200, 200)); // Rojo claro
                            c.setForeground(Color.BLACK);
                        } else if (stockActual <= stockMinimo * 1.5) {
                            c.setBackground(new Color(255, 255, 200)); // Amarillo claro
                            c.setForeground(Color.BLACK);
                        } else {
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                        }
                    }
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        };

        // Aplicar renderer a todas las columnas
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            tablaProductos.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel con contador de productos
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("Total de productos: 0");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfo.add(lblInfo);
        panel.add(panelInfo, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        panel.setPreferredSize(new Dimension(380, 0));

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
        txtPrecio.setToolTipText("Ingrese solo números (ej: 99.99)");
        panel.add(txtPrecio, gbc);
        fila++;

        // Stock Actual
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Stock Actual:*"), gbc);
        gbc.gridx = 1;
        txtStockActual = new JTextField(20);
        txtStockActual.setToolTipText("Ingrese solo números enteros");
        panel.add(txtStockActual, gbc);
        fila++;

        // Stock Mínimo
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Stock Mínimo:*"), gbc);
        gbc.gridx = 1;
        txtStockMinimo = new JTextField(20);
        txtStockMinimo.setToolTipText("Ingrese solo números enteros");
        panel.add(txtStockMinimo, gbc);
        fila++;

        // Categoría (ComboBox)
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Categoría:*"), gbc);
        gbc.gridx = 1;
        cmbCategoria = new JComboBox<>();
        panel.add(cmbCategoria, gbc);
        fila++;

        // Proveedor (ComboBox)
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Proveedor:*"), gbc);
        gbc.gridx = 1;
        cmbProveedor = new JComboBox<>();
        panel.add(cmbProveedor, gbc);
        fila++;

        // Código de Barras
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel("Código Barras:"), gbc);
        gbc.gridx = 1;
        txtCodigoBarras = new JTextField(20);
        txtCodigoBarras.setToolTipText("Opcional");
        panel.add(txtCodigoBarras, gbc);
        fila++;

        // Nota de campos obligatorios
        gbc.gridx = 0; gbc.gridy = fila;
        gbc.gridwidth = 2;
        JLabel lblNota = new JLabel("* Campos obligatorios");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 10));
        lblNota.setForeground(Color.GRAY);
        panel.add(lblNota, gbc);
        fila++;

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 5, 5));

        btnAgregar = new JButton("➕ Agregar Producto");
        btnAgregar.setBackground(new Color(80, 180, 100));
        btnAgregar.setForeground(Color.WHITE);
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(e -> agregarProducto());
        panelBotones.add(btnAgregar);

        btnActualizar = new JButton("✏️ Actualizar Producto");
        btnActualizar.setBackground(new Color(100, 150, 250));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.addActionListener(e -> actualizarProducto());
        panelBotones.add(btnActualizar);

        btnEliminar = new JButton("🗑️ Eliminar Producto");
        btnEliminar.setBackground(new Color(220, 80, 80));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarProducto());
        panelBotones.add(btnEliminar);

        btnLimpiar = new JButton("🧹 Limpiar Campos");
        btnLimpiar.setBackground(new Color(150, 150, 150));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = fila;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        lblEstadoConexion = new JLabel();
        if (conexionActiva) {
            String projectId = FirestoreConfig.getProjectId();
            lblEstadoConexion.setText("✅ Conectado a: Firestore (" + projectId + ")");
            lblEstadoConexion.setForeground(new Color(0, 150, 0));
        } else {
            lblEstadoConexion.setText("❌ Sin conexión a Firestore");
            lblEstadoConexion.setForeground(new Color(200, 0, 0));
        }
        lblEstadoConexion.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblEstadoConexion);

        // Botón para reconectar
        JButton btnReconectar = new JButton("🔄 Reconectar");
        btnReconectar.setFocusPainted(false);
        btnReconectar.addActionListener(e -> {
            FirestoreConfig.inicializar();
            verificarConexion();
            if (conexionActiva) {
                String projectId = FirestoreConfig.getProjectId();
                lblEstadoConexion.setText("✅ Conectado a: Firestore (" + projectId + ")");
                lblEstadoConexion.setForeground(new Color(0, 150, 0));
                cargarCategorias();
                cargarProveedores();
                cargarCategoriasFiltro();
                cargarDatos();
                JOptionPane.showMessageDialog(this, "✅ Reconexión exitosa");
            } else {
                lblEstadoConexion.setText("❌ Sin conexión a Firestore");
                lblEstadoConexion.setForeground(new Color(200, 0, 0));
                JOptionPane.showMessageDialog(this, "❌ No se pudo reconectar");
            }
        });
        panel.add(btnReconectar);

        return panel;
    }

    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        List<Categoria> categorias = categoriaDAO.obtenerTodas();

        if (categorias.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ No hay categorías registradas.\n" +
                            "Por favor, ejecuta el script SQL de inicialización primero.",
                    "Sin Categorías",
                    JOptionPane.WARNING_MESSAGE);
            btnAgregar.setEnabled(false);
        } else {
            btnAgregar.setEnabled(true);
            for (Categoria cat : categorias) {
                cmbCategoria.addItem(cat);
            }
        }
    }

    private void cargarProveedores() {
        cmbProveedor.removeAllItems();
        List<Proveedor> proveedores = proveedorDAO.obtenerTodos();

        if (proveedores.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ No hay proveedores registrados.\n" +
                            "Por favor, ejecuta el script SQL de inicialización primero.",
                    "Sin Proveedores",
                    JOptionPane.WARNING_MESSAGE);
            btnAgregar.setEnabled(false);
        } else {
            btnAgregar.setEnabled(true);
            for (Proveedor prov : proveedores) {
                cmbProveedor.addItem(prov);
            }
        }
    }

    private void cargarCategoriasFiltro() {
        cmbFiltroCategoria.removeAllItems();
        cmbFiltroCategoria.addItem("-- Todas las categorías --");

        List<Categoria> categorias = categoriaDAO.obtenerTodas();
        for (Categoria cat : categorias) {
            cmbFiltroCategoria.addItem(cat.getNombreCategoria());
        }
    }

    private void cargarDatos() {
        if (!conexionActiva) {
            JOptionPane.showMessageDialog(this,
                    "❌ No hay conexión a la base de datos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.obtenerTodosLosProductos();

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ℹ️ No hay productos registrados.\n" +
                            "Use el botón 'Agregar' para crear nuevos productos.",
                    "Inventario Vacío",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                agregarFilaTabla(p);
            }
        }

        actualizarContador();
    }

    private void agregarFilaTabla(Producto p) {
        Object[] fila = {
                p.getIdProducto(),
                p.getNombreProducto(),
                p.getDescripcion() != null ? p.getDescripcion() : "",
                String.format("$%.2f", p.getPrecioUnitario()),
                p.getStockActual(),
                p.getStockMinimo(),
                p.getNombreCategoria(),
                p.getNombreProveedor(),
                p.getCodigoBarras() != null ? p.getCodigoBarras() : ""
        };
        modeloTabla.addRow(fila);
    }

    private void actualizarContador() {
        int total = modeloTabla.getRowCount();
        // Buscar el panel de info y actualizar
        Component[] components = ((JPanel)((JScrollPane)tablaProductos.getParent().getParent())
                .getParent()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComps = ((JPanel) comp).getComponents();
                for (Component subComp : subComps) {
                    if (subComp instanceof JLabel) {
                        ((JLabel) subComp).setText("Total de productos: " + total);
                    }
                }
            }
        }
    }

    private void buscarProductos() {
        String termino = txtBuscar.getText().trim();
        if (termino.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Por favor ingrese un término de búsqueda",
                    "Campo Vacío",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.buscarPorNombre(termino);

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ℹ️ No se encontraron productos con ese nombre",
                    "Sin Resultados",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                agregarFilaTabla(p);
            }
            JOptionPane.showMessageDialog(this,
                    "✅ Se encontraron " + productos.size() + " producto(s)",
                    "Búsqueda Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        actualizarContador();
    }

    private void filtrarPorCategoria() {
        if (cmbFiltroCategoria.getSelectedIndex() == 0) {
            // "Todas las categorías" seleccionado
            cargarDatos();
            return;
        }

        String nombreCategoria = (String) cmbFiltroCategoria.getSelectedItem();

        // Buscar el ID de la categoría
        List<Categoria> categorias = categoriaDAO.obtenerTodas();
        int idCategoria = -1;
        for (Categoria cat : categorias) {
            if (cat.getNombreCategoria().equals(nombreCategoria)) {
                idCategoria = cat.getIdCategoria();
                break;
            }
        }

        if (idCategoria == -1) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error al obtener la categoría",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.buscarPorCategoria(idCategoria);

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ℹ️ No hay productos en la categoría: " + nombreCategoria,
                    "Sin Resultados",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                agregarFilaTabla(p);
            }
            JOptionPane.showMessageDialog(this,
                    "✅ Se encontraron " + productos.size() + " producto(s) en la categoría: " + nombreCategoria,
                    "Filtro Aplicado",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        actualizarContador();
    }

    private void mostrarStockBajo() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoDAO.obtenerProductosStockBajo();

        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "✅ ¡Excelente! No hay productos con stock bajo",
                    "Stock OK",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Producto p : productos) {
                agregarFilaTabla(p);
            }

            JOptionPane.showMessageDialog(this,
                    "⚠️ ATENCIÓN: Se encontraron " + productos.size() + " producto(s) con stock bajo o crítico.\n" +
                            "Por favor, considere reabastecer estos productos.",
                    "Alerta de Stock",
                    JOptionPane.WARNING_MESSAGE);
        }

        actualizarContador();
    }

    private void agregarProducto() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "❌ El nombre del producto es obligatorio",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);
                txtNombre.requestFocus();
                return;
            }

            if (txtPrecio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "❌ El precio es obligatorio",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);
                txtPrecio.requestFocus();
                return;
            }

            if (txtStockActual.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "❌ El stock actual es obligatorio",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);
                txtStockActual.requestFocus();
                return;
            }

            if (txtStockMinimo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "❌ El stock mínimo es obligatorio",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);
                txtStockMinimo.requestFocus();
                return;
            }

            if (cmbCategoria.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        "❌ Debe seleccionar una categoría",
                        "Campo Requerido",
                        JOptionPane.WARNING_MESSAGE);