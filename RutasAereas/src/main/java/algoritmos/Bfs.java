/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algoritmos;

import grafo.GrafoAereo;
import modelo.Itinerario;
import modelo.Ruta;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Búsqueda en anchura. Visita los vértices por niveles (primero los que están a 1 vuelo,
 * luego a 2, ...), por eso la primera vez que se descubre el destino se llegó con
 * la menor cantidad de aristas. Los pesos (minutos, costo) no se consultan.
 * Complejidad: O(n + m).
 */
public final class Bfs {

    private Bfs() {
    }

    public static Optional<Itinerario> menosEscalas(GrafoAereo grafo, String origen, String destino) {
        Map<String, Ruta> previo = new HashMap<>(); // arista con la que se descubrió cada vértice
        Set<String> descubiertos = new HashSet<>();
        Queue<String> cola = new ArrayDeque<>();

        descubiertos.add(origen);
        cola.add(origen);

        while (!cola.isEmpty() && !descubiertos.contains(destino)) {
            String actual = cola.poll();
            for (Ruta r : grafo.rutasSalientes(actual)) {
                if (descubiertos.add(r.destino())) { // add devuelve false si ya estaba
                    previo.put(r.destino(), r);
                    cola.add(r.destino());
                }
            }
        }

        if (!descubiertos.contains(destino)) {
            return Optional.empty();
        }
        return Optional.of(Itinerario.reconstruir(origen, destino, previo));
    }
}