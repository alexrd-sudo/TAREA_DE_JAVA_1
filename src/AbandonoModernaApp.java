import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AbandonoModernaApp {

    static class EjemploCliente {
        double antiguedadNorm;
        double reclamosNorm;
        double bajoUso;
        int abandono;

        EjemploCliente(int mesesAntiguedad, int reclamosRecientes, boolean caidaUso, int abandono) {
            this.antiguedadNorm = mesesAntiguedad / 36.0;
            this.reclamosNorm = reclamosRecientes / 5.0;
            this.bajoUso = caidaUso ? 1.0 : 0.0;
            this.abandono = abandono;
        }
    }

    static class ModeloAbandono {
        private double wAntiguedad = 0.0;
        private double wReclamos = 0.0;
        private double wBajoUso = 0.0;
        private double bias = 0.0;

        private double sigmoide(double z) {
            return 1.0 / (1.0 + Math.exp(-z));
        }

        public double predecirProbabilidad(EjemploCliente c) {
            double z = bias
                    + (wAntiguedad * c.antiguedadNorm)
                    + (wReclamos * c.reclamosNorm)
                    + (wBajoUso * c.bajoUso);
            return sigmoide(z);
        }

        public void entrenar(List<EjemploCliente> datos, double tasaAprendizaje, int epocas) {
            for (int epoca = 0; epoca < epocas; epoca++) {
                for (EjemploCliente c : datos) {
                    double pred = predecirProbabilidad(c);
                    double error = c.abandono - pred;

                    bias += tasaAprendizaje * error;
                    wAntiguedad += tasaAprendizaje * error * c.antiguedadNorm;
                    wReclamos += tasaAprendizaje * error * c.reclamosNorm;
                    wBajoUso += tasaAprendizaje * error * c.bajoUso;
                }
            }
        }
    }

    public static List<EjemploCliente> cargarDesdeCSV(String rutaArchivo) throws IOException {
        List<EjemploCliente> lista = new ArrayList<>();

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

                int mesesAntiguedad = Integer.parseInt(partes[0].trim());
                int reclamosRecientes = Integer.parseInt(partes[1].trim());
                boolean caidaUso = Boolean.parseBoolean(partes[2].trim());
                int abandono = Integer.parseInt(partes[3].trim());

                lista.add(new EjemploCliente(mesesAntiguedad, reclamosRecientes, caidaUso, abandono));
            }
        }

        return lista;
    }

    private static void estilizarCampo(JTextField campo, Color fondo, Color texto, Color borde) {
        campo.setBackground(fondo);
        campo.setForeground(texto);
        campo.setCaretColor(texto);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private static void estilizarCombo(JComboBox<String> combo, Color fondo, Color texto, Color borde) {
        combo.setBackground(fondo);
        combo.setForeground(texto);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(borde, 1, true));
    }

    public static void main(String[] args) {
        String rutaCSV = "data/clientes.csv";

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            List<EjemploCliente> datos;
            try {
                datos = cargarDesdeCSV(rutaCSV);
                if (datos.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El archivo CSV está vacío.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "No se pudo leer el archivo: " + rutaCSV, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ModeloAbandono modelo = new ModeloAbandono();
            modelo.entrenar(datos, 0.5, 1000);

            Color bgVentana = new Color(15, 23, 42);
            Color bgTarjeta = new Color(30, 41, 59);
            Color bgCampo = new Color(15, 23, 42);
            Color bordeSuave = new Color(71, 85, 105);
            Color textoClaro = new Color(248, 250, 252);
            Color textoSuave = new Color(148, 163, 184);
            Color acentoAzul = new Color(59, 130, 246);
            Color verde = new Color(34, 197, 94);
            Color ambar = new Color(245, 158, 11);
            Color rojo = new Color(239, 68, 68);

            JFrame frame = new JFrame("Predicción de Abandono · Caso 15");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(460, 520);
            frame.setMinimumSize(new Dimension(430, 490));
            frame.setLocationRelativeTo(null);

            JPanel root = new JPanel(new BorderLayout(0, 16));
            root.setBackground(bgVentana);
            root.setBorder(new EmptyBorder(22, 22, 22, 22));

            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setBackground(bgVentana);

            JLabel titulo = new JLabel("Riesgo de Abandono de Clientes");
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titulo.setForeground(textoClaro);

            JLabel subtitulo = new JLabel("Modelo entrenado con " + datos.size() + " registros · Caso 15");
            subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subtitulo.setForeground(textoSuave);

            header.add(titulo);
            header.add(Box.createVerticalStrut(4));
            header.add(subtitulo);

            JPanel cardForm = new JPanel(new GridLayout(3, 2, 10, 12));
            cardForm.setBackground(bgTarjeta);
            cardForm.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bordeSuave, 1, true),
                    new EmptyBorder(16, 16, 16, 16)
            ));

            JLabel lblMeses = new JLabel("Meses de antigüedad");
            lblMeses.setForeground(textoClaro);
            lblMeses.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JTextField txtMeses = new JTextField("6");
            estilizarCampo(txtMeses, bgCampo, textoClaro, bordeSuave);

            JLabel lblReclamos = new JLabel("Reclamos recientes (0-5)");
            lblReclamos.setForeground(textoClaro);
            lblReclamos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JTextField txtReclamos = new JTextField("2");
            estilizarCampo(txtReclamos, bgCampo, textoClaro, bordeSuave);

            JLabel lblUso = new JLabel("Caída de uso");
            lblUso.setForeground(textoClaro);
            lblUso.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JComboBox<String> cmbUso = new JComboBox<>(new String[]{"No", "Sí"});
            estilizarCombo(cmbUso, bgCampo, textoClaro, bordeSuave);

            cardForm.add(lblMeses);
            cardForm.add(txtMeses);
            cardForm.add(lblReclamos);
            cardForm.add(txtReclamos);
            cardForm.add(lblUso);
            cardForm.add(cmbUso);

            JPanel cardResultado = new JPanel();
            cardResultado.setLayout(new BoxLayout(cardResultado, BoxLayout.Y_AXIS));
            cardResultado.setBackground(bgTarjeta);
            cardResultado.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bordeSuave, 1, true),
                    new EmptyBorder(16, 16, 16, 16)
            ));

            JLabel lblPorcentaje = new JLabel("— %", SwingConstants.LEFT);
            lblPorcentaje.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblPorcentaje.setForeground(textoClaro);
            lblPorcentaje.setAlignmentX(JLabel.LEFT_ALIGNMENT);

            JLabel lblEstado = new JLabel("Completa los datos y pulsa Calcular");
            lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblEstado.setForeground(textoSuave);
            lblEstado.setAlignmentX(JLabel.LEFT_ALIGNMENT);

            JProgressBar barra = new JProgressBar(0, 100);
            barra.setValue(0);
            barra.setStringPainted(true);
            barra.setPreferredSize(new Dimension(100, 22));
            barra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            barra.setAlignmentX(JProgressBar.LEFT_ALIGNMENT);
            barra.setBackground(bgCampo);
            barra.setForeground(acentoAzul);

            cardResultado.add(lblPorcentaje);
            cardResultado.add(Box.createVerticalStrut(4));
            cardResultado.add(lblEstado);
            cardResultado.add(Box.createVerticalStrut(12));
            cardResultado.add(barra);

            JButton btnCalcular = new JButton("Calcular riesgo");
            btnCalcular.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnCalcular.setBackground(acentoAzul);
            btnCalcular.setForeground(Color.WHITE);
            btnCalcular.setFocusPainted(false);
            btnCalcular.setBorderPainted(false);
            btnCalcular.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCalcular.setPreferredSize(new Dimension(100, 42));

            btnCalcular.addActionListener(e -> {
                try {
                    int meses = Integer.parseInt(txtMeses.getText().trim());
                    int reclamos = Integer.parseInt(txtReclamos.getText().trim());
                    boolean caidaUso = "Sí".equals(cmbUso.getSelectedItem());

                    EjemploCliente consulta = new EjemploCliente(meses, reclamos, caidaUso, 0);
                    double pct = modelo.predecirProbabilidad(consulta) * 100.0;
                    int pctClamp = (int) Math.round(Math.max(0.0, Math.min(100.0, pct)));

                    Color colorRiesgo = pct >= 70.0 ? rojo : (pct >= 40.0 ? ambar : verde);
                    String nivel = pct >= 70.0 ? "Riesgo alto" : (pct >= 40.0 ? "Riesgo medio" : "Riesgo bajo");

                    lblPorcentaje.setText(String.format("%.1f%%", pct));
                    lblPorcentaje.setForeground(colorRiesgo);
                    lblEstado.setText(nivel);
                    lblEstado.setForeground(colorRiesgo);
                    barra.setValue(pctClamp);
                    barra.setForeground(colorRiesgo);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Ingresa números enteros válidos en antigüedad y reclamos.",
                            "Dato inválido",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            });

            JPanel centro = new JPanel();
            centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
            centro.setBackground(bgVentana);
            centro.add(cardForm);
            centro.add(Box.createVerticalStrut(14));
            centro.add(cardResultado);

            root.add(header, BorderLayout.NORTH);
            root.add(centro, BorderLayout.CENTER);
            root.add(btnCalcular, BorderLayout.SOUTH);

            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }
}