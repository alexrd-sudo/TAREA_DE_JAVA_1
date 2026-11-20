import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ReservasGuiApp {

    static class EjemploReserva {
        double anticipacionNorm;
        double esAgencia;
        double esWeb;
        double sinDeposito;
        int cancelo;

        EjemploReserva(int diasAnticipacion, String canalVenta, boolean tieneDeposito, int cancelo) {
            this.anticipacionNorm = diasAnticipacion / 120.0;
            this.esAgencia = "AGENCIA".equalsIgnoreCase(canalVenta) ? 1.0 : 0.0;
            this.esWeb = "WEB".equalsIgnoreCase(canalVenta) ? 1.0 : 0.0;
            this.sinDeposito = tieneDeposito ? 0.0 : 1.0;
            this.cancelo = cancelo;
        }
    }

    static class ModeloRegresionLogistica {
        private double wAnticipacion = 0.0;
        private double wAgencia = 0.0;
        private double wWeb = 0.0;
        private double wSinDeposito = 0.0;
        private double bias = 0.0;

        private double sigmoide(double z) {
            return 1.0 / (1.0 + Math.exp(-z));
        }

        public double predecirProbabilidad(EjemploReserva r) {
            double z = bias
                    + (wAnticipacion * r.anticipacionNorm)
                    + (wAgencia * r.esAgencia)
                    + (wWeb * r.esWeb)
                    + (wSinDeposito * r.sinDeposito);
            return sigmoide(z);
        }

        public void entrenar(List<EjemploReserva> datos, double tasaAprendizaje, int epocas) {
            for (int epoca = 0; epoca < epocas; epoca++) {
                for (EjemploReserva r : datos) {
                    double pred = predecirProbabilidad(r);
                    double error = r.cancelo - pred;

                    bias += tasaAprendizaje * error;
                    wAnticipacion += tasaAprendizaje * error * r.anticipacionNorm;
                    wAgencia += tasaAprendizaje * error * r.esAgencia;
                    wWeb += tasaAprendizaje * error * r.esWeb;
                    wSinDeposito += tasaAprendizaje * error * r.sinDeposito;
                }
            }
        }
    }

    public static List<EjemploReserva> cargarDesdeCSV(String rutaArchivo) throws IOException {
        List<EjemploReserva> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            boolean esPrimeraLinea = true;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (esPrimeraLinea) {
                    esPrimeraLinea = false;
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 4) continue;

                int diasAnticipacion = Integer.parseInt(partes[0].trim());
                String canalVenta = partes[1].trim();
                boolean tieneDeposito = Boolean.parseBoolean(partes[2].trim());
                int cancelo = Integer.parseInt(partes[3].trim());

                lista.add(new EjemploReserva(diasAnticipacion, canalVenta, tieneDeposito, cancelo));
            }
        }

        return lista;
    }

    private static void mostrarResultadoVisual(int dias, String canal, boolean tieneDeposito, double porcentaje) {
        int pctEntero = (int) Math.round(Math.max(0.0, Math.min(100.0, porcentaje)));

        String nivelRiesgo = porcentaje >= 70.0
                ? "ALTO RIESGO DE CANCELACIÓN"
                : (porcentaje >= 40.0 ? "RIESGO MEDIO" : "BAJO RIESGO");

        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        panel.add(new JLabel("Días de anticipación: " + dias));
        panel.add(new JLabel("Canal de venta: " + canal));
        panel.add(new JLabel("Tiene depósito: " + (tieneDeposito ? "Sí" : "No")));
        panel.add(new JLabel(" "));

        JLabel lblPorcentaje = new JLabel(String.format("Probabilidad de cancelación: %.1f%% (%s)", porcentaje, nivelRiesgo));
        lblPorcentaje.setFont(lblPorcentaje.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        panel.add(lblPorcentaje);
        panel.add(new JLabel(" "));

        JProgressBar barra = new JProgressBar(0, 100);
        barra.setValue(pctEntero);
        barra.setStringPainted(true);
        panel.add(barra);

        JOptionPane.showMessageDialog(
                null,
                panel,
                "Resultado — Predicción de Cancelación (Caso 10)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        String rutaCSV = "data/reservas.csv"; // Cambia a "data/reservas_1000.csv" si quieres usar el de 1000

        SwingUtilities.invokeLater(() -> {
            try {
                List<EjemploReserva> datos = cargarDesdeCSV(rutaCSV);
                if (datos.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El archivo CSV está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                ModeloRegresionLogistica modelo = new ModeloRegresionLogistica();
                modelo.entrenar(datos, 0.5, 1000);

                while (true) {
                    JTextField txtDias = new JTextField("30");
                    JComboBox<String> cmbCanal = new JComboBox<>(new String[]{"AGENCIA", "WEB", "DIRECTO"});
                    JComboBox<String> cmbDeposito = new JComboBox<>(new String[]{"No", "Sí"});

                    JPanel formulario = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
                    formulario.add(new JLabel("Días de anticipación:"));
                    formulario.add(txtDias);
                    formulario.add(new JLabel("Canal de venta:"));
                    formulario.add(cmbCanal);
                    formulario.add(new JLabel("¿Tiene depósito?:"));
                    formulario.add(cmbDeposito);

                    int opcion = JOptionPane.showConfirmDialog(
                            null,
                            formulario,
                            "Formulario de Reserva — Caso 10 (" + datos.size() + " registros)",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                    );

                    if (opcion != JOptionPane.OK_OPTION) {
                        break;
                    }

                    int dias = Integer.parseInt(txtDias.getText().trim());
                    String canal = (String) cmbCanal.getSelectedItem();
                    boolean tieneDeposito = "Sí".equals(cmbDeposito.getSelectedItem());

                    EjemploReserva consulta = new EjemploReserva(dias, canal, tieneDeposito, 0);
                    double porcentaje = modelo.predecirProbabilidad(consulta) * 100.0;

                    mostrarResultadoVisual(dias, canal, tieneDeposito, porcentaje);

                    int repetir = JOptionPane.showConfirmDialog(
                            null,
                            "¿Quieres consultar otra reserva?",
                            "Continuar",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (repetir != JOptionPane.YES_OPTION) {
                        break;
                    }
                }

            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo leer el archivo: " + rutaCSV,
                        "Error de archivo",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ingresa un número entero válido en días de anticipación.",
                        "Dato inválido",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}