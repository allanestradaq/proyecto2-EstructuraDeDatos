/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package algoritmos;

import grafo.GrafoAereo;
import modelo.Ruta;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Búsqueda en profundidad (iterativa, con una pila explícita para no desbordar la pila de
 * llamadas en redes grandes). Devuelve todos los vértices a los que se puede llegar
 * desde el origen, incluido el origen. Complejidad: O(n + m).
 */
public final class Dfs {

    private Dfs() {
    }

    public static Set<String> alcanzables(GrafoAereo grafo, String origen) {
        return alcanzables(grafo, origen, r -> true);
    }

    /** Variante que ignora las rutas que no cumplen el filtro (sirve para simular cancelaciones). */
    public static Set<String> alcanzables(GrafoAereo grafo, String origen, Predicate<Ruta> permitida) {
        Set<String> visitados = new LinkedHashSet<>();
        Deque<String> pila = new ArrayDeque<>();
        pila.push(origen);

        while (!pila.isEmpty()) {
            String actual = pila.pop();
            if (!visitados.add(actual)) {
                continue; // ya se había visitado
            }
            for (Ruta r : grafo.rutasSalientes(actual)) {
                if (permitida.test(r) && !visitados.contains(r.destino())) {
                    pila.push(r.destino());
                }
            }
        }
        return visitados;
    }
}