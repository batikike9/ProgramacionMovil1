import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Dibujo extends JFrame {
    JComboBox<String> comboColores;
    JPanel panelDibujo, panelSuperior;
    JButton btnLimpiar;

    BufferedImage imagen;
    BufferedImage imagenOriginal;
    Graphics2D g2;

    int xAnterior, yAnterior;

    public Dibujo() {
        setTitle("Paint");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        comboColores = new JComboBox<>();
        comboColores.addItem("Rojo");
        comboColores.addItem("Verde");
        comboColores.addItem("Azul");

        btnLimpiar = new JButton("Limpiar");

        panelSuperior = new JPanel();
        panelSuperior.add(comboColores);
        panelSuperior.add(btnLimpiar);

        add(panelSuperior, BorderLayout.NORTH);

        // Inicializar el lienzo de dibujo
        imagen = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        g2 = imagen.createGraphics();

        // Cargar imagen de fondo (soporta ejecución desde dentro del JAR o como archivo suelto)
        cargarImagenFondo();

        // Color inicial sincronizado con el combo (Rojo)
        g2.setColor(Color.red);

        panelDibujo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imagen, 40, 10, null);
            }
        };
        panelDibujo.setBackground(Color.yellow);

        // Acción de cambio de color
        comboColores.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int colorSeleccionado = comboColores.getSelectedIndex();
                switch (colorSeleccionado) {
                    case 0: g2.setColor(Color.red); break;
                    case 1: g2.setColor(Color.green); break;
                    case 2: g2.setColor(Color.blue); break;
                }
            }
        });

        // Acción del botón Limpiar: restaura el fondo original
        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restaurarFondo();
                panelDibujo.repaint();
            }
        });

        // Mouse presionado
        panelDibujo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                xAnterior = e.getX() - 40;
                yAnterior = e.getY() - 10;
            }
        });

        // Movimiento de mouse arrastrado
        panelDibujo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX() - 40;
                int y = e.getY() - 10;

                g2.drawLine(xAnterior, yAnterior, x, y);

                xAnterior = x;
                yAnterior = y;

                panelDibujo.repaint();
            }
        });

        add(panelDibujo, BorderLayout.CENTER);
    }

    private void cargarImagenFondo() {
        try {
            // Intenta leer desde el JAR / classpath
            InputStream is = getClass().getResourceAsStream("/fondo.jpg");
            if (is != null) {
                imagenOriginal = ImageIO.read(is);
            } else {
                // Si no está en el JAR, intenta leer desde la carpeta local
                imagenOriginal = ImageIO.read(new File("fondo.jpg"));
            }
        } catch (Exception e) {
            imagenOriginal = null;
        }
        restaurarFondo();
    }

    private void restaurarFondo() {
        if (imagenOriginal != null) {
            g2.drawImage(imagenOriginal, 0, 0, 300, 300, null);
        } else {
            // Fondo blanco de respaldo si no encuentra la imagen
            Color actual = g2.getColor();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, 300, 300);
            g2.setColor(actual);
        }
        // Conservar el color actual del combo tras limpiar
        int colorSeleccionado = comboColores.getSelectedIndex();
        switch (colorSeleccionado) {
            case 0: g2.setColor(Color.red); break;
            case 1: g2.setColor(Color.green); break;
            case 2: g2.setColor(Color.blue); break;
        }
    }

    public static void main(String[] args) {
        Dibujo d = new Dibujo();
        d.setVisible(true);
    }
}
