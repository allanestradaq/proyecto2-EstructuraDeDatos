/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 * Vértice del grafo: un aeropuerto identificado por su código IATA.
 * Los archivos usan '_' en lugar de espacios, por eso se ofrecen versiones legibles.
 */
public record Aeropuerto(String codigo, double x, double y, String ciudad, String pais) {

    public String ciudadLegible() {
        return ciudad.replace('_', ' ');
    }

    public String paisLegible() {
        return pais.replace('_', ' ');
    }

    @Override
    public String toString() {
        return codigo + " - " + ciudadLegible() + ", " + paisLegible();
    }
}