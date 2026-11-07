/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package app;

import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

/**
 * Punto de entrada. Sin argumentos busca data/aeropuertos.txt y data/rutas.txt;
 * si no existen, el usuario elige los archivos con el botón "Cargar archivos...".
 * Con argumentos: java app.Main <aeropuertos.txt> <rutas.txt>
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // si falla se usa el aspecto por defecto de Swing
            }
            VentanaPrincipal ventana = args.length >= 2
                    ? new VentanaPrincipal(Path.of(args[0]), Path.of(args[1]))
                    : new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}