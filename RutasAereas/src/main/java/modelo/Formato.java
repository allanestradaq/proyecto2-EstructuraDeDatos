/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.util.Locale;

/** Utilidad para mostrar números sin decimales innecesarios (165 en lugar de 165.0). */
public final class Formato {

    private Formato() {
    }

    public static String numero(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.format(Locale.US, "%.2f", valor);
    }
}