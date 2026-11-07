/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servicio;

import algoritmos.Bfs;
import algoritmos.Dfs;
import algoritmos.Dijkstra;
import grafo.GrafoAereo;
import modelo.Aeropuerto;
import modelo.Criterio;
import modelo.Formato;
import modelo.Itinerario;
import modelo.Ruta;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Fachada que usa la interfaz: valida los códigos y elige el algoritmo según el criterio.
 * La vista nunca llama a BFS, DFS o Dijkstra directamente.
 */
public class PlanificadorVuelos {

    /** Resultado de la consulta de alcanzabilidad. El origen no aparece en ninguna de las dos listas. */
    public record Alcance(String origen, Set<String> alcanzables, Set<String> noAlcanzables) {
    }

    private final GrafoAereo grafo;

    public PlanificadorVuelos(GrafoAereo grafo) {
        this.grafo = grafo;
    }

    public GrafoAereo grafo() {
        return grafo;
    }

    /**
     * @return el itinerario, o vacío si no existe ruta. Si origen == destino devuelve un itinerario con 0 vuelos.
     * @throws CodigoInexistenteException si algún código no está en el grafo
     */
    public Optional<Itinerario> buscar(String origen, String destino, Criterio criterio) {
        exigirExistente(origen);
        exigirExistente(destino);
        return switch (criterio) {
            case MENOS_ESCALAS -> Bfs.menosEscalas(grafo, origen, destino);
            case MAS_RAPIDA, MAS_BARATA -> Dijkstra.rutaOptima(grafo, origen, destino, criterio.peso());
        };
    }

    /** Destinos a los que se llega desde el origen (con cualquier cantidad de escalas) y a los que no. */
    public Alcance alcance(String origen) {
        exigirExistente(origen);
        Set<String> visitados = Dfs.alcanzables(grafo, origen);

        Set<String> alcanzables = new LinkedHashSet<>(visitados);
        alcanzables.remove(origen);

        Set<String> noAlcanzables = new LinkedHashSet<>(grafo.codigos());
        noAlcanzables.removeAll(visitados);
        return new Alcance(origen, alcanzables, noAlcanzables);
    }

    /** Aeropuertos desde los que se puede llegar a "destino" (DFS sobre el grafo invertido). */
    public Set<String> quienesLlegan(String destino) {
        exigirExistente(destino);
        Set<String> visitados = Dfs.alcanzables(grafo.invertido(), destino);
        Set<String> resultado = new LinkedHashSet<>(visitados);
        resultado.remove(destino);
        return resultado;
    }

    /** Texto con los datos de un aeropuerto y todas sus rutas salientes (requisito 4.1). */
    public String describir(String codigo) {
        exigirExistente(codigo);
        Aeropuerto a = grafo.aeropuerto(codigo).orElseThrow();
        List<Ruta> salientes = grafo.rutasSalientes(codigo);

        StringBuilder sb = new StringBuilder();
        sb.append(a).append("\nPosición en el mapa: x=").append(Formato.numero(a.x()))
                .append(", y=").append(Formato.numero(a.y())).append('\n');
        sb.append("Rutas salientes (").append(salientes.size()).append("):\n");
        for (Ruta r : salientes) {
            sb.append("  ").append(r.origen()).append(" -> ").append(r.destino())
                    .append("   ").append(Formato.numero(r.minutos())).append(" min   $")
                    .append(Formato.numero(r.costo())).append('\n');
        }
        return sb.toString();
    }

    private void exigirExistente(String codigo) {
        if (!grafo.existe(codigo)) {
            throw new CodigoInexistenteException(codigo);
        }
    }
}