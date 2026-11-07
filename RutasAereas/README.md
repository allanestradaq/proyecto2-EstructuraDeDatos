# Rutas aéreas — Proyecto II (EIF207)

Universidad Nacional de Costa Rica · Sede Regional Brunca · II Ciclo 2026

## Integrantes
- Allan Estrada Quesada 604940962

## Descripción
Planificador de vuelos entre aeropuertos de América sobre un grafo dirigido y ponderado.
Calcula la ruta con menos escalas (BFS), la más rápida y la más barata (Dijkstra),
los destinos alcanzables (DFS) y permite cancelar y restaurar rutas, con un mapa y un avión animado.

## Tecnologías
- Lenguaje: Java 17
- Entorno: NetBeans
- Biblioteca gráfica: Swing
- Construcción: Maven

## Cómo compilar y ejecutar
Con Maven: 
clic derecho sobre archivo del proyecto
seleccionar run

## Formato de los archivos
- `aeropuertos.txt`: primera línea con la cantidad; luego `código x y ciudad país`.
- `rutas.txt`: primera línea con la cantidad; luego `origen destino minutos costo`.

## Estructura del proyecto
```text
RutasAereas/
 ├─ pom.xml
 ├─ README.md
 ├─ data/
 │   ├─ aeropuertos.txt
 │   └─ rutas.txt
 └─ src/main/java/
     ├─ app/
     │  └─ Main.java
     ├─ modelo/
     │   ├─ Aeropuerto.java
     │   ├─ Ruta.java
     │   ├─ Itinerario.java
     │   ├─ Criterio.java
     │   └─ Formato.java
     ├─ grafo/
     │   └─ GrafoAereo.java
     ├─ carga/
     │   └─ CargadorDatos.java
     ├─ algoritmos/
     │   ├─ Bfs.java
     │   ├─ Dijkstra.java
     │   └─ Dfs.java
     ├─ servicio/
     │   ├─ PlanificadorVuelos.java
     │   └─ CodigoInexistenteException.java
     └─ vista/
         ├─ VentanaPrincipal.java
         ├─ PanelMapa.java
         └─ AnimadorVuelo.java
```

## Algoritmos
| Consulta | Algoritmo | Clase |
|---|---|---|
| Menos escalas | BFS | `algoritmos/Bfs.java` |
| Más rápida y más barata | Dijkstra (parametrizado por peso) | `algoritmos/Dijkstra.java` |
| Alcanzables | DFS | `algoritmos/Dfs.java` |

## Uso
Se selecciona ruta de origen, luego se selecciona la ruta de destino, luego se selecciona el criterio de recorrido(Bfs, Dijkstra, Dfs).
tras la seleccion, se activa el boton "buscar ruta".
tambien se puede ver todos los aeropuertos alcanzables y la informacion de los aeropuertos.
tambien existe la opcion de pausar y reiniciar la animacion de vuelo

## Atribuciones y herramientas de apoyo
El proyecto se realizo en su mayoria por el estudiante a cargo, los archivos de datos proporcionados por el profesor y paquete vistas modificado con ia
