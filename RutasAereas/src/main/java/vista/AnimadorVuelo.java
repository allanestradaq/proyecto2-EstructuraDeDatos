/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

import modelo.Itinerario;
import modelo.Ruta;

import javax.swing.Timer;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controla el tiempo de la animación. No dibuja nada: solo calcula en qué tramo está el avión,
 * qué fracción del tramo lleva recorrida y cuántos minutos y dólares van acumulados.
 * Cada tramo dura MS_VUELO y en cada escala el avión se detiene MS_ESCALA.
 */
public class AnimadorVuelo {

    public static final int MS_VUELO = 1600;
    public static final int MS_ESCALA = 800;
    private static final int TICK_MS = 16; // ~60 cuadros por segundo

    /** Foto del estado de la animación en un instante. */
    public record Estado(int tramo, int totalTramos, Ruta rutaActual, double progreso,
                         boolean enEscala, boolean terminado, double minutos, double costo) {
    }

    private final Timer timer = new Timer(TICK_MS, e -> avanzar());
    private Consumer<Estado> oyente = estado -> { };

    private Itinerario itinerario;
    private int tramo;
    private boolean enEscala;
    private boolean terminado;
    private boolean pausado;
    private long msEnFase;      // tiempo transcurrido en la fase actual (vuelo o escala)
    private long ultimoTick;

    public void setOyente(Consumer<Estado> oyente) {
        this.oyente = oyente;
    }

    /** Arranca la animación desde el primer tramo. */
    public void iniciar(Itinerario nuevo) {
        timer.stop();
        itinerario = nuevo;
        if (itinerario == null || itinerario.vuelos() == 0) {
            itinerario = null;
            return;
        }
        tramo = 0;
        enEscala = false;
        terminado = false;
        pausado = false;
        msEnFase = 0;
        ultimoTick = System.currentTimeMillis();
        timer.start();
        oyente.accept(estadoActual());
    }

    public void reiniciar() {
        iniciar(itinerario);
    }

    /** Alterna entre pausa y reanudación. @return true si quedó en pausa */
    public boolean alternarPausa() {
        if (itinerario == null || terminado) {
            return false;
        }
        pausado = !pausado;
        ultimoTick = System.currentTimeMillis(); // evita un salto al reanudar
        return pausado;
    }

    public void detener() {
        timer.stop();
        itinerario = null;
    }

    public boolean hayVuelo() {
        return itinerario != null;
    }

    private void avanzar() {
        long ahora = System.currentTimeMillis();
        long dt = ahora - ultimoTick;
        ultimoTick = ahora;
        if (pausado || itinerario == null) {
            return;
        }

        msEnFase += dt;
        if (!enEscala) {
            if (msEnFase >= MS_VUELO) {
                if (tramo == itinerario.vuelos() - 1) { // llegó al destino final
                    terminado = true;
                    timer.stop();
                } else {                                // llegó a una escala: se detiene un momento
                    enEscala = true;
                    msEnFase = 0;
                }
            }
        } else if (msEnFase >= MS_ESCALA) {             // termina la escala: sigue el próximo tramo
            tramo++;
            enEscala = false;
            msEnFase = 0;
        }
        oyente.accept(estadoActual());
    }

    private Estado estadoActual() {
        List<Ruta> tramos = itinerario.tramos();
        double progreso = (enEscala || terminado) ? 1.0 : Math.min(1.0, msEnFase / (double) MS_VUELO);

        double minutos = 0;
        double costo = 0;
        for (int i = 0; i < tramo; i++) {
            minutos += tramos.get(i).minutos();
            costo += tramos.get(i).costo();
        }
        Ruta actual = tramos.get(tramo);
        minutos += actual.minutos() * progreso; // acumulado proporcional a lo volado del tramo
        costo += actual.costo() * progreso;

        return new Estado(tramo, tramos.size(), actual, progreso, enEscala, terminado, minutos, costo);
    }
}