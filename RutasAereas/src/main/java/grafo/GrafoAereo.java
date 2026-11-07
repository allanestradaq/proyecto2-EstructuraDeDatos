/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package grafo;

import modelo.Aeropuerto;
import modelo.Ruta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Grafo dirigido y ponderado con lista de adyacencia.
 * Cada vértice (código IATA) tiene su lista de rutas salientes.
 * Las rutas canceladas se guardan aparte para poder restaurarlas.
 */
public class GrafoAereo {

    private final Map<String, Aeropuerto> aeropuertos = new LinkedHashMap<>();
    private final Map<String, List<Ruta>> adyacencia = new LinkedHashMap<>();
    private final List<Ruta> canceladas = new ArrayList<>();

    /** @return false si el código ya existía */
    public boolean agregarAeropuerto(Aeropuerto a) {
        if (aeropuertos.containsKey(a.codigo())) {
            return false;
        }
        aeropuertos.put(a.codigo(), a);
        adyacencia.put(a.codigo(), new ArrayList<>());
        return true;
    }

    /** @return false si el origen o el destino no existen */
    public boolean agregarRuta(Ruta r) {
        if (!existe(r.origen()) || !existe(r.destino())) {
            return false;
        }
        adyacencia.get(r.origen()).add(r);
        return true;
    }

    public boolean existe(String codigo) {
        return aeropuertos.containsKey(codigo);
    }

    public Optional<Aeropuerto> aeropuerto(String codigo) {
        return Optional.ofNullable(aeropuertos.get(codigo));
    }

    public Collection<Aeropuerto> aeropuertos() {
        return Collections.unmodifiableCollection(aeropuertos.values());
    }

    public Set<String> codigos() {
        return Collections.unmodifiableSet(aeropuertos.keySet());
    }

    /** Rutas que salen de un aeropuerto (lista vacía si el código no existe). */
    public List<Ruta> rutasSalientes(String codigo) {
        return Collections.unmodifiableList(adyacencia.getOrDefault(codigo, List.of()));
    }

    public List<Ruta> todasLasRutas() {
        List<Ruta> todas = new ArrayList<>();
        for (List<Ruta> lista : adyacencia.values()) {
            todas.addAll(lista);
        }
        return todas;
    }

    public int cantidadAeropuertos() {
        return aeropuertos.size();
    }

    public int cantidadRutas() {
        int total = 0;
        for (List<Ruta> lista : adyacencia.values()) {
            total += lista.size();
        }
        return total;
    }

    /** Quita la ruta origen->destino del grafo en memoria (el archivo no se toca). */
    public boolean cancelarRuta(String origen, String destino) {
        List<Ruta> salientes = adyacencia.get(origen);
        if (salientes == null) {
            return false;
        }
        boolean cancelada = false;
        Iterator<Ruta> it = salientes.iterator();
        while (it.hasNext()) {
            Ruta r = it.next();
            if (r.destino().equals(destino)) {
                it.remove();
                canceladas.add(r);
                cancelada = true;
            }
        }
        return cancelada;
    }

    /** Devuelve al grafo todas las rutas canceladas. @return cuántas se restauraron */
    public int restaurarRutas() {
        int cantidad = canceladas.size();
        for (Ruta r : canceladas) {
            adyacencia.get(r.origen()).add(r);
        }
        canceladas.clear();
        return cantidad;
    }

    public List<Ruta> rutasCanceladas() {
        return List.copyOf(canceladas);
    }

    /**
     * Grafo con todas las aristas invertidas (transpuesto). Un DFS sobre él desde X
     * responde "¿desde qué aeropuertos se puede llegar a X?" en O(n + m).
     */
    public GrafoAereo invertido() {
        GrafoAereo inv = new GrafoAereo();
        for (Aeropuerto a : aeropuertos.values()) {
            inv.agregarAeropuerto(a);
        }
        for (Ruta r : todasLasRutas()) {
            inv.agregarRuta(new Ruta(r.destino(), r.origen(), r.minutos(), r.costo()));
        }
        return inv;
    }
}