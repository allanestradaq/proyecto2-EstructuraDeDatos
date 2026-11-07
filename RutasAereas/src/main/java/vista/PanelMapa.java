/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import grafo.GrafoAereo;
import modelo.Aeropuerto;
import modelo.Itinerario;
import modelo.Ruta;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Dibuja el mapa: aeropuertos como puntos con su código, rutas como líneas, la ruta resultante
 * resaltada, los alcanzables por color, las rutas canceladas punteadas y el avión animado.
 * Solo dibuja lo que otros le entregan; no calcula rutas.
 */
public class PanelMapa extends JPanel {

    private static final double REFERENCIA = 1000.0;   // los archivos usan una ventana de 1000 x 1000
    private static final int MARGEN = 30;
    private static final double RADIO_CLIC = 12;

    private static final Color COLOR_AEROPUERTO = new Color(200, 30, 30);
    private static final Color COLOR_ALCANZABLE = new Color(40, 160, 60);
    private static final Color COLOR_NO_ALCANZABLE = new Color(90, 90, 90);
    private static final Color COLOR_ORIGEN = new Color(0, 100, 255);
    private static final Color COLOR_DESTINO = new Color(255, 140, 0);
    private static final Color COLOR_RUTA = new Color(185, 185, 185);
    private static final Color COLOR_CANCELADA = new Color(220, 40, 40);
    private static final Color TRAMO_PENDIENTE = new Color(30, 100, 220);
    private static final Color TRAMO_ACTUAL = new Color(255, 140, 0);
    private static final Color TRAMO_RECORRIDO = new Color(30, 150, 60);

    private GrafoAereo grafo;
    private Itinerario itinerario;
    private Set<String> alcanzables;
    private Set<String> noAlcanzables;
    private String origen;
    private String destino;
    private AnimadorVuelo.Estado vuelo;
    private Consumer<String> alClicAeropuerto = codigo -> { };

    public PanelMapa() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String codigo = aeropuertoCerca(e.getX(), e.getY());
                if (codigo != null) {
                    alClicAeropuerto.accept(codigo);
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String codigo = aeropuertoCerca(e.getX(), e.getY());
                setToolTipText(codigo == null ? null : grafo.aeropuerto(codigo).orElseThrow().toString());
            }
        });
    }

    // ---------------------------------------------------------------- datos que recibe

    public void setGrafo(GrafoAereo grafo) {
        this.grafo = grafo;
        limpiarResultados();
    }

    public void setItinerario(Itinerario itinerario) {
        this.itinerario = itinerario;
        this.vuelo = null;
        repaint();
    }

    public void setAlcance(Set<String> alcanzables, Set<String> noAlcanzables) {
        this.alcanzables = alcanzables;
        this.noAlcanzables = noAlcanzables;
        repaint();
    }

    public void setSeleccion(String origen, String destino) {
        this.origen = origen;
        this.destino = destino;
        repaint();
    }

    public void setVuelo(AnimadorVuelo.Estado vuelo) {
        this.vuelo = vuelo;
        repaint();
    }

    public void setAlClicAeropuerto(Consumer<String> alClicAeropuerto) {
        this.alClicAeropuerto = alClicAeropuerto;
    }

    public void limpiarResultados() {
        itinerario = null;
        alcanzables = null;
        noAlcanzables = null;
        vuelo = null;
        repaint();
    }

    // ---------------------------------------------------------------- coordenadas

    /** Escala para que la ventana de 1000 x 1000 de los archivos quepa en el panel. */
    private double escala() {
        return (Math.min(getWidth(), getHeight()) - 2.0 * MARGEN) / REFERENCIA;
    }

    private Point2D.Double aPantalla(Aeropuerto a) {
        double e = escala();
        double desdeX = (getWidth() - REFERENCIA * e) / 2;
        double desdeY = (getHeight() - REFERENCIA * e) / 2;
        return new Point2D.Double(desdeX + a.x() * e, desdeY + a.y() * e);
    }

    private Point2D.Double punto(String codigo) {
        return aPantalla(grafo.aeropuerto(codigo).orElseThrow());
    }

    private String aeropuertoCerca(int px, int py) {
        if (grafo == null) {
            return null;
        }
        String mejor = null;
        double mejorDistancia = RADIO_CLIC;
        for (Aeropuerto a : grafo.aeropuertos()) {
            double d = aPantalla(a).distance(px, py);
            if (d <= mejorDistancia) {
                mejorDistancia = d;
                mejor = a.codigo();
            }
        }
        return mejor;
    }

    // ---------------------------------------------------------------- dibujo

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (grafo == null) {
            g2.setColor(Color.GRAY);
            g2.drawString("Cargue los archivos de aeropuertos y rutas.", 20, 30);
            g2.dispose();
            return;
        }

        dibujarMarco(g2);
        dibujarRutasNormales(g2);
        dibujarRutasCanceladas(g2);
        dibujarItinerario(g2);
        dibujarAeropuertos(g2);
        if (vuelo != null) {
            dibujarAvion(g2);
        }
        g2.dispose();
    }

    private void dibujarMarco(Graphics2D g2) {
        double e = escala();
        double x = (getWidth() - REFERENCIA * e) / 2;
        double y = (getHeight() - REFERENCIA * e) / 2;
        g2.setColor(new Color(225, 225, 225));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new java.awt.geom.Rectangle2D.Double(x, y, REFERENCIA * e, REFERENCIA * e));
    }

    private void dibujarRutasNormales(Graphics2D g2) {
        g2.setColor(COLOR_RUTA);
        g2.setStroke(new BasicStroke(1f));
        for (Ruta r : grafo.todasLasRutas()) {
            Point2D.Double a = punto(r.origen());
            Point2D.Double b = punto(r.destino());
            g2.draw(new Line2D.Double(a, b));
        }
    }

    private void dibujarRutasCanceladas(Graphics2D g2) {
        g2.setColor(COLOR_CANCELADA);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{5f, 5f}, 0f)); // línea punteada
        for (Ruta r : grafo.rutasCanceladas()) {
            g2.draw(new Line2D.Double(punto(r.origen()), punto(r.destino())));
        }
    }

    private void dibujarItinerario(Graphics2D g2) {
        if (itinerario == null) {
            return;
        }
        List<Ruta> tramos = itinerario.tramos();
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < tramos.size(); i++) {
            g2.setColor(colorDelTramo(i));
            Point2D.Double a = punto(tramos.get(i).origen());
            Point2D.Double b = punto(tramos.get(i).destino());
            g2.draw(new Line2D.Double(a, b));
            dibujarFlecha(g2, a, b);
        }
    }

    /** Pendiente: azul. En vuelo: naranja. Ya recorrido: verde. */
    private Color colorDelTramo(int indice) {
        if (vuelo == null) {
            return TRAMO_PENDIENTE;
        }
        if (vuelo.terminado() || indice < vuelo.tramo()) {
            return TRAMO_RECORRIDO;
        }
        return indice == vuelo.tramo() ? TRAMO_ACTUAL : TRAMO_PENDIENTE;
    }

    private void dibujarFlecha(Graphics2D g2, Point2D.Double a, Point2D.Double b) {
        double angulo = Math.atan2(b.y - a.y, b.x - a.x);
        double bx = b.x - 9 * Math.cos(angulo);   // la punta queda un poco antes del aeropuerto
        double by = b.y - 9 * Math.sin(angulo);
        Path2D flecha = new Path2D.Double();
        flecha.moveTo(bx, by);
        flecha.lineTo(bx - 9 * Math.cos(angulo - 0.45), by - 9 * Math.sin(angulo - 0.45));
        flecha.lineTo(bx - 9 * Math.cos(angulo + 0.45), by - 9 * Math.sin(angulo + 0.45));
        flecha.closePath();
        g2.fill(flecha);
    }

    private void dibujarAeropuertos(Graphics2D g2) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        for (Aeropuerto a : grafo.aeropuertos()) {
            Point2D.Double p = aPantalla(a);
            double radio = 5;
            Color color = colorDelAeropuerto(a.codigo());
            if (a.codigo().equals(origen) || a.codigo().equals(destino)) {
                radio = 8;
            }
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(p.x - radio, p.y - radio, 2 * radio, 2 * radio));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(a.codigo(), (float) (p.x + radio + 1), (float) (p.y - radio));
        }
    }

    private Color colorDelAeropuerto(String codigo) {
        if (codigo.equals(origen)) {
            return COLOR_ORIGEN;
        }
        if (codigo.equals(destino)) {
            return COLOR_DESTINO;
        }
        if (alcanzables != null && alcanzables.contains(codigo)) {
            return COLOR_ALCANZABLE;
        }
        if (noAlcanzables != null && noAlcanzables.contains(codigo)) {
            return COLOR_NO_ALCANZABLE;
        }
        return COLOR_AEROPUERTO;
    }

    /** El avión se coloca interpolando linealmente entre los dos aeropuertos del tramo actual. */
    private void dibujarAvion(Graphics2D g2) {
        Ruta r = vuelo.rutaActual();
        Point2D.Double a = punto(r.origen());
        Point2D.Double b = punto(r.destino());
        double x = a.x + (b.x - a.x) * vuelo.progreso();
        double y = a.y + (b.y - a.y) * vuelo.progreso();
        double angulo = Math.atan2(b.y - a.y, b.x - a.x);

        AffineTransform original = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angulo);   // el avión apunta hacia el destino del tramo

        Path2D alas = new Path2D.Double();
        alas.moveTo(3, 0);
        alas.lineTo(-5, -11);
        alas.lineTo(-8, -11);
        alas.lineTo(-4, 0);
        alas.lineTo(-8, 11);
        alas.lineTo(-5, 11);
        alas.closePath();
        Path2D cola = new Path2D.Double();
        cola.moveTo(-8, 0);
        cola.lineTo(-13, -5);
        cola.lineTo(-14, -5);
        cola.lineTo(-12, 0);
        cola.lineTo(-14, 5);
        cola.lineTo(-13, 5);
        cola.closePath();
        Ellipse2D fuselaje = new Ellipse2D.Double(-11, -2.5, 25, 5);

        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(20, 40, 120));
        g2.fill(fuselaje);
        g2.fill(alas);
        g2.fill(cola);
        g2.setColor(Color.WHITE);
        g2.draw(fuselaje);
        g2.draw(alas);
        g2.setTransform(original);
    }
}