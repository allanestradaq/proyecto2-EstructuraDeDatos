/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

/** Se lanza cuando una consulta usa un código de aeropuerto que no está en el grafo. */
public class CodigoInexistenteException extends RuntimeException {

    public CodigoInexistenteException(String codigo) {
        super("El aeropuerto '" + codigo + "' no existe");
    }
}