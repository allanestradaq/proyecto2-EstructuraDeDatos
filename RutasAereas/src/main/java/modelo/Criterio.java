/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.function.ToDoubleFunction;

/** Criterio de búsqueda que el usuario elige en la interfaz. */
public enum Criterio {
    MENOS_ESCALAS("Menos escalas (BFS)"),
    MAS_RAPIDA("Más rápida (Dijkstra por minutos)"),
    MAS_BARATA("Más barata (Dijkstra por dólares)");

    private final String etiqueta;

    Criterio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Peso de cada arista que usa Dijkstra con este criterio. */
    public ToDoubleFunction<Ruta> peso() {
        return switch (this) {
            case MAS_RAPIDA -> Ruta::minutos;
            case MAS_BARATA -> Ruta::costo;
            case MENOS_ESCALAS -> r -> 1.0;
        };
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}