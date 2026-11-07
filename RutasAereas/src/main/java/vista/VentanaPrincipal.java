/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import carga.CargadorDatos;
import carga.CargadorDatos.ResultadoCarga;
import grafo.GrafoAereo;
import modelo.Criterio;
import modelo.Formato;
import modelo.Itinerario;
import modelo.Ruta;
import servicio.CodigoInexistenteException;
import servicio.PlanificadorVuelos;
import servicio.PlanificadorVuelos.Alcance;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;

/** Ventana principal: mapa a la izquierda y panel de control y resultados a la derecha. */
public class VentanaPrincipal extends JFrame {

    private static final Path AEROPUERTOS_POR_DEFECTO = Path.of("data", "aeropuertos.txt");
    private static final Path RUTAS_POR_DEFECTO = Path.of("data", "rutas.txt");

    private final PanelMapa mapa = new PanelMapa();
    private final AnimadorVuelo animador = new AnimadorVuelo();

    private final JComboBox<String> cmbOrigen = new JComboBox<>();
    private final JComboBox<String> cmbDestino = new JComboBox<>();
    private final JComboBox<Criterio> cmbCriterio = new JComboBox<>(Criterio.values());
    private final JButton btnPausa = new JButton("Pausar");
    private final JLabel lblVuelo = new JLabel(" ");
    private final JTextArea txtInfo = new JTextArea();

    private PlanificadorVuelos planificador;
    private boolean eligiendoOrigen = true; // alterna los clics del mapa: origen, destino, origen...

    public VentanaPrincipal(Path aeropuertos, Path rutas) {
        super("Planificador de vuelos - EIF207");
        construirInterfaz();
        animador.setOyente(this::alAnimar);
        mapa.setAlClicAeropuerto(this::alClicEnAeropuerto);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        if (Files.exists(aeropuertos) && Files.exists(rutas)) {
            cargar(aeropuertos, rutas);
        }
    }

    public VentanaPrincipal() {
        this(AEROPUERTOS_POR_DEFECTO, RUTAS_POR_DEFECTO);
    }

    // ---------------------------------------------------------------- construcción de la interfaz

    private void construirInterfaz() {
        mapa.setPreferredSize(new Dimension(820, 820));

        cmbOrigen.setEditable(true);   // se puede elegir de la lista, escribir el código o hacer clic en el mapa
        cmbDestino.setEditable(true);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Origen:"));
        formulario.add(cmbOrigen);
        formulario.add(new JLabel("Destino:"));
        formulario.add(cmbDestino);
        formulario.add(new JLabel("Criterio:"));
        formulario.add(cmbCriterio);

        JButton btnBuscar = new JButton("Buscar ruta");
        btnBuscar.addActionListener(e -> buscarRuta());
        JButton btnAlcanzables = new JButton("Alcanzables");
        btnAlcanzables.addActionListener(e -> consultarAlcanzables());
        JButton btnDatos = new JButton("Datos del aeropuerto");
        btnDatos.addActionListener(e -> mostrarDatosAeropuerto());
        JButton btnCancelar = new JButton("Cancelar ruta");
        btnCancelar.addActionListener(e -> cancelarRuta());
        JButton btnRestaurar = new JButton("Restaurar rutas");
        btnRestaurar.addActionListener(e -> restaurarRutas());
        JButton btnArchivos = new JButton("Cargar archivos...");
        btnArchivos.addActionListener(e -> seleccionarArchivos());

        JPanel consultas = new JPanel(new GridLayout(0, 2, 6, 6));
        consultas.add(btnBuscar);
        consultas.add(btnAlcanzables);
        consultas.add(btnDatos);
        consultas.add(btnArchivos);
        consultas.add(btnCancelar);
        consultas.add(btnRestaurar);

        btnPausa.addActionListener(e -> alternarPausa());
        JButton btnReiniciar = new JButton("Reiniciar");
        btnReiniciar.addActionListener(e -> reiniciarVuelo());
        JPanel botonesVuelo = new JPanel(new GridLayout(1, 2, 6, 6));
        botonesVuelo.add(btnPausa);
        botonesVuelo.add(btnReiniciar);

        lblVuelo.setVerticalAlignment(SwingConstants.TOP);
        lblVuelo.setPreferredSize(new Dimension(340, 56)); // alto fijo: el texto cambia en cada cuadro
        JPanel animacion = new JPanel(new BorderLayout(0, 4));
        animacion.setBorder(BorderFactory.createTitledBorder("Animación del vuelo"));
        animacion.add(botonesVuelo, BorderLayout.NORTH);
        animacion.add(lblVuelo, BorderLayout.CENTER);

        txtInfo.setEditable(false);
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);
        txtInfo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtInfo);
        scroll.setPreferredSize(new Dimension(360, 300));

        // Arriba van los controles (altura natural); abajo, el área de resultados ocupa el resto.
        JPanel controles = new JPanel();
        controles.setLayout(new BoxLayout(controles, BoxLayout.Y_AXIS));
        for (JPanel bloque : new JPanel[]{formulario, consultas, animacion}) {
            bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
            controles.add(bloque);
            controles.add(Box.createVerticalStrut(8));
        }

        JPanel lateral = new JPanel(new BorderLayout(0, 4));
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lateral.add(controles, BorderLayout.NORTH);
        lateral.add(scroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(mapa, BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);
    }

    // ---------------------------------------------------------------- carga de datos

    private void seleccionarArchivos() {
        File a = elegirArchivo("Seleccione el archivo de aeropuertos");
        if (a == null) {
            return;
        }
        File r = elegirArchivo("Seleccione el archivo de rutas");
        if (r == null) {
            return;
        }
        cargar(a.toPath(), r.toPath());
    }

    private File elegirArchivo(String titulo) {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle(titulo);
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private void cargar(Path aeropuertos, Path rutas) {
        try {
            ResultadoCarga resultado = CargadorDatos.cargar(aeropuertos, rutas);
            animador.detener();
            planificador = new PlanificadorVuelos(resultado.grafo());
            mapa.setGrafo(resultado.grafo());
            mapa.setSeleccion(null, null);
            llenarCombos(resultado.grafo());
            lblVuelo.setText(" ");

            StringBuilder sb = new StringBuilder();
            sb.append("Datos cargados: ").append(resultado.grafo().cantidadAeropuertos())
                    .append(" aeropuertos y ").append(resultado.grafo().cantidadRutas()).append(" rutas.\n");
            for (String aviso : resultado.advertencias()) {
                sb.append("AVISO: ").append(aviso).append('\n');
            }
            mostrar(sb.toString());
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudieron cargar los datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void llenarCombos(GrafoAereo grafo) {
        String[] codigos = new TreeSet<>(grafo.codigos()).toArray(new String[0]);
        cmbOrigen.setModel(new DefaultComboBoxModel<>(codigos));
        cmbDestino.setModel(new DefaultComboBoxModel<>(codigos));
        if (codigos.length > 1) {
            cmbOrigen.setSelectedIndex(0);
            cmbDestino.setSelectedIndex(1);
        }
    }

    // ---------------------------------------------------------------- acciones

    private void buscarRuta() {
        if (!hayDatos()) {
            return;
        }
        String origen = leerCodigo(cmbOrigen);
        String destino = leerCodigo(cmbDestino);
        Criterio criterio = (Criterio) cmbCriterio.getSelectedItem();
        limpiarResultados();
        try {
            Optional<Itinerario> resultado = planificador.buscar(origen, destino, criterio);
            mapa.setSeleccion(origen, destino);
            if (resultado.isEmpty()) {
                mostrar("No existe ruta de " + origen + " a " + destino + ".");
                return;
            }
            Itinerario itinerario = resultado.get();
            if (itinerario.vuelos() == 0) {
                mostrar("El origen y el destino son el mismo aeropuerto (" + origen + "): 0 vuelos.");
                return;
            }
            mostrar(textoItinerario(itinerario, criterio));
            mapa.setItinerario(itinerario);
            btnPausa.setText("Pausar");
            animador.iniciar(itinerario);
        } catch (CodigoInexistenteException | IllegalArgumentException ex) {
            mostrar(ex.getMessage());
        }
    }

    private void consultarAlcanzables() {
        if (!hayDatos()) {
            return;
        }
        String origen = leerCodigo(cmbOrigen);
        limpiarResultados();
        try {
            Alcance alcance = planificador.alcance(origen);
            mapa.setSeleccion(origen, null);
            mapa.setAlcance(alcance.alcanzables(), alcance.noAlcanzables());
            mostrar("Desde " + origen + " se puede llegar a " + alcance.alcanzables().size()
                    + " aeropuertos (verde):\n" + String.join(", ", alcance.alcanzables())
                    + "\n\nNo se puede llegar a " + alcance.noAlcanzables().size()
                    + " aeropuertos (gris):\n" + String.join(", ", alcance.noAlcanzables()));
        } catch (CodigoInexistenteException ex) {
            mostrar(ex.getMessage());
        }
    }

    private void mostrarDatosAeropuerto() {
        if (!hayDatos()) {
            return;
        }
        try {
            mostrar(planificador.describir(leerCodigo(cmbOrigen)));
        } catch (CodigoInexistenteException ex) {
            mostrar(ex.getMessage());
        }
    }

    /** Cancela la ruta Origen -> Destino que estén escritas o seleccionadas en los campos. */
    private void cancelarRuta() {
        if (!hayDatos()) {
            return;
        }
        String origen = leerCodigo(cmbOrigen);
        String destino = leerCodigo(cmbDestino);
        limpiarResultados(); // los resultados anteriores ya no son válidos con el grafo modificado
        if (planificador.grafo().cancelarRuta(origen, destino)) {
            mostrar("Ruta " + origen + " -> " + destino + " cancelada.\n"
                    + "Las nuevas consultas usan el grafo sin ella.\n\n" + textoCanceladas());
        } else {
            mostrar("No existe una ruta activa de " + origen + " a " + destino + ".");
        }
        mapa.repaint();
    }

    private void restaurarRutas() {
        if (!hayDatos()) {
            return;
        }
        limpiarResultados();
        int cantidad = planificador.grafo().restaurarRutas();
        mostrar("Rutas restauradas: " + cantidad + ".");
        mapa.repaint();
    }

    private void alternarPausa() {
        if (animador.hayVuelo()) {
            btnPausa.setText(animador.alternarPausa() ? "Reanudar" : "Pausar");
        }
    }

    private void reiniciarVuelo() {
        if (animador.hayVuelo()) {
            btnPausa.setText("Pausar");
            animador.reiniciar();
        }
    }

    /** Clics en el mapa: el primero fija el origen, el segundo el destino, el tercero vuelve al origen. */
    private void alClicEnAeropuerto(String codigo) {
        if (eligiendoOrigen) {
            cmbOrigen.setSelectedItem(codigo);
        } else {
            cmbDestino.setSelectedItem(codigo);
        }
        eligiendoOrigen = !eligiendoOrigen;
        mapa.setSeleccion(leerCodigo(cmbOrigen), leerCodigo(cmbDestino));
    }

    /** Se invoca en cada cuadro de la animación. */
    private void alAnimar(AnimadorVuelo.Estado e) {
        mapa.setVuelo(e);
        if (e.terminado()) {
            lblVuelo.setText("<html>Vuelo terminado.<br>Total: " + redondear(e.minutos()) + " min, $"
                    + redondear(e.costo()) + "</html>");
            return;
        }
        Ruta r = e.rutaActual();
        String estado = e.enEscala() ? "escala en " + r.destino() : "en vuelo";
        lblVuelo.setText("<html>Tramo " + (e.tramo() + 1) + " de " + e.totalTramos() + ": "
                + r.origen() + " &rarr; " + r.destino() + " (" + estado + ")<br>"
                + "Minutos acumulados: " + redondear(e.minutos()) + "<br>"
                + "Costo acumulado: $" + redondear(e.costo()) + "</html>");
    }

    // ---------------------------------------------------------------- utilidades

    private boolean hayDatos() {
        if (planificador == null) {
            mostrar("Primero cargue los archivos de aeropuertos y rutas.");
            return false;
        }
        return true;
    }

    private void limpiarResultados() {
        animador.detener();
        mapa.limpiarResultados();
        lblVuelo.setText(" ");
    }

    /** Lee el código escrito en el combo (aunque no se haya confirmado con Enter). */
    private String leerCodigo(JComboBox<String> combo) {
        Object valor = combo.getEditor().getItem();
        return valor == null ? "" : valor.toString().trim().toUpperCase(Locale.ROOT);
    }

    private void mostrar(String texto) {
        txtInfo.setText(texto);
        txtInfo.setCaretPosition(0);
    }

    private String textoItinerario(Itinerario it, Criterio criterio) {
        StringBuilder sb = new StringBuilder();
        sb.append("Criterio: ").append(criterio).append("\n\n");
        sb.append("Ruta: ").append(it.secuencia()).append('\n');
        sb.append("Vuelos: ").append(it.vuelos()).append('\n');
        sb.append("Minutos totales: ").append(Formato.numero(it.minutosTotales())).append('\n');
        sb.append("Costo total: $").append(Formato.numero(it.costoTotal())).append("\n\nTramos:\n");
        int i = 1;
        for (Ruta r : it.tramos()) {
            sb.append(i++).append(". ").append(r.origen()).append(" -> ").append(r.destino())
                    .append("  ").append(Formato.numero(r.minutos())).append(" min  $")
                    .append(Formato.numero(r.costo())).append('\n');
        }
        return sb.toString();
    }

    private String textoCanceladas() {
        StringBuilder sb = new StringBuilder("Rutas canceladas actualmente:\n");
        for (Ruta r : planificador.grafo().rutasCanceladas()) {
            sb.append("  ").append(r.origen()).append(" -> ").append(r.destino()).append('\n');
        }
        return sb.toString();
    }

    private String redondear(double valor) {
        return Formato.numero(Math.round(valor * 100) / 100.0);
    }
}