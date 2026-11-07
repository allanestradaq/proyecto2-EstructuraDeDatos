/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Resultado de una búsqueda: la secuencia de vuelos (aristas) de origen a destino.
 * Si origen y destino son el mismo aeropuerto, la lista de tramos está vacía.
 */
public record Itinerario(String origen, String destino, List<Ruta> tramos) {

    public Itinerario {
        tramos = List.copyOf(tramos);
    }

    public int vuelos() {
        return tramos.size();
    }

    public double minutosTotales() {
        double suma = 0;
        for (Ruta r : tramos) {
            suma += r.minutos();
        }
        return suma;
    }

    public double costoTotal() {
        double suma = 0;
        for (Ruta r : tramos) {
            suma += r.costo();
        }
        return suma;
    }

    /** Códigos de los aeropuertos visitados, incluyendo origen y destino. */
    public List<String> codigos() {
        List<String> codigos = new ArrayList<>();
        codigos.add(origen);
        for (Ruta r : tramos) {
            codigos.add(r.destino());
        }
        return codigos;
    }

    public String secuencia() {
        return String.join(" -> ", codigos());
    }

    /**
     * Reconstruye el camino caminando hacia atrás desde el destino con el mapa
     * "previo" (para cada vértice, la arista con la que se llegó a él).
     */
    public static Itinerario reconstruir(String origen, String destino, Map<String, Ruta> previo) {
        LinkedList<Ruta> tramos = new LinkedList<>();
        String actual = destino;
        while (!actual.equals(origen)) {
            Ruta r = previo.get(actual);
            tramos.addFirst(r);
            actual = r.origen();
        }
        return new Itinerario(origen, destino, tramos);
    }
}