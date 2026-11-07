/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 * Arista dirigida del grafo: un vuelo de origen a destino con dos pesos
 * (duración en minutos y costo en dólares).
 */
public record Ruta(String origen, String destino, double minutos, double costo) {

    @Override
    public String toString() {
        return origen + " -> " + destino
                + " (" + Formato.numero(minutos) + " min, $" + Formato.numero(costo) + ")";
    }
}