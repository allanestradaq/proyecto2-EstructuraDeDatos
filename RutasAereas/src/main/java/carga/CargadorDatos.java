/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package carga;

import grafo.GrafoAereo;
import modelo.Aeropuerto;
import modelo.Ruta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Lee aeropuertos.txt y rutas.txt y construye el grafo.
 * Formato de ambos archivos: primero la cantidad de líneas que siguen y luego
 * un registro por línea, con campos separados por espacios.
 * No hay ningún dato escrito en el código: todo viene de los archivos.
 */
public final class CargadorDatos {

    /** El grafo construido y los avisos de líneas que se omitieron. */
    public record ResultadoCarga(GrafoAereo grafo, List<String> advertencias) {
    }

    private CargadorDatos() {
    }

    public static ResultadoCarga cargar(Path archivoAeropuertos, Path archivoRutas) throws IOException {
        GrafoAereo grafo = new GrafoAereo();
        List<String> advertencias = new ArrayList<>();
        leerAeropuertos(archivoAeropuertos, grafo, advertencias);
        leerRutas(archivoRutas, grafo, advertencias);
        return new ResultadoCarga(grafo, advertencias);
    }

    private static void leerAeropuertos(Path archivo, GrafoAereo grafo, List<String> advertencias)
            throws IOException {
        try (Scanner sc = abrir(archivo)) {
            int cantidad = leerEntero(sc);
            for (int i = 1; i <= cantidad; i++) {
                String codigo = sc.next().toUpperCase(Locale.ROOT);
                double x = sc.nextDouble();
                double y = sc.nextDouble();
                String ciudad = sc.next();
                String pais = sc.next();
                if (!grafo.agregarAeropuerto(new Aeropuerto(codigo, x, y, ciudad, pais))) {
                    advertencias.add("aeropuertos: el código " + codigo + " está repetido (registro " + i + ")");
                }
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Formato inválido en " + archivo.getFileName()
                    + ": faltan datos o hay un valor que no es número", e);
        }
    }

    private static void leerRutas(Path archivo, GrafoAereo grafo, List<String> advertencias)
            throws IOException {
        try (Scanner sc = abrir(archivo)) {
            int cantidad = leerEntero(sc);
            for (int i = 1; i <= cantidad; i++) {
                String origen = sc.next().toUpperCase(Locale.ROOT);
                String destino = sc.next().toUpperCase(Locale.ROOT);
                double minutos = sc.nextDouble();
                double costo = sc.nextDouble();
                if (!grafo.agregarRuta(new Ruta(origen, destino, minutos, costo))) {
                    advertencias.add("rutas: el registro " + i + " (" + origen + " -> " + destino
                            + ") usa un aeropuerto que no existe; se omite");
                }
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Formato inválido en " + archivo.getFileName()
                    + ": faltan datos o hay un valor que no es número", e);
        }
    }

    private static Scanner abrir(Path archivo) throws IOException {
        Scanner sc = new Scanner(Files.newInputStream(archivo), StandardCharsets.UTF_8);
        sc.useLocale(Locale.ROOT); // el punto es el separador decimal
        return sc;
    }

    /** Lee el primer número del archivo; tolera la marca BOM que agregan algunos editores. */
    private static int leerEntero(Scanner sc) {
        return Integer.parseInt(sc.next().replace("\uFEFF", ""));
    }
}