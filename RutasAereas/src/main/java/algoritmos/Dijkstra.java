/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algoritmos;

import grafo.GrafoAereo;
import modelo.Itinerario;
import modelo.Ruta;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;

/**
 * Dijkstra con cola de prioridad. Es UN solo algoritmo: lo único que cambia entre
 * "más rápida" y "más barata" es la función que da el peso de cada arista.
 * Requiere pesos no negativos. Complejidad: O((n + m) log n).
 */
public final class Dijkstra {

    /** Elemento de la cola: un vértice con la distancia con la que se encoló. */
    private record Entrada(String codigo, double distancia) {
    }

    private Dijkstra() {
    }

    public static Optional<Itinerario> rutaOptima(GrafoAereo grafo, String origen, String destino,
                                                  ToDoubleFunction<Ruta> peso) {
        Map<String, Double> distancia = new HashMap<>();
        Map<String, Ruta> previo = new HashMap<>();
        PriorityQueue<Entrada> cola = new PriorityQueue<>(Comparator.comparingDouble(Entrada::distancia));

        distancia.put(origen, 0.0);
        cola.add(new Entrada(origen, 0.0));

        while (!cola.isEmpty()) {
            Entrada actual = cola.poll();

            // Entrada obsoleta: después de encolarla se encontró un camino mejor.
            if (actual.distancia() > distancia.get(actual.codigo())) {
                continue;
            }
            // Al sacar el destino su distancia ya es la mínima: no hace falta seguir.
            if (actual.codigo().equals(destino)) {
                break;
            }

            for (Ruta r : grafo.rutasSalientes(actual.codigo())) {
                double w = peso.applyAsDouble(r);
                if (w < 0) {
                    throw new IllegalArgumentException("Dijkstra no admite pesos negativos (ruta " + r + ")");
                }
                double nueva = actual.distancia() + w;
                if (nueva < distancia.getOrDefault(r.destino(), Double.POSITIVE_INFINITY)) { // relajación
                    distancia.put(r.destino(), nueva);
                    previo.put(r.destino(), r);
                    cola.add(new Entrada(r.destino(), nueva));
                }
            }
        }

        if (!distancia.containsKey(destino)) {
            return Optional.empty();
        }
        return Optional.of(Itinerario.reconstruir(origen, destino, previo));
    }
}