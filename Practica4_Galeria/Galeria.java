import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class Galeria extends JFrame {
    JButton btnAtras, btnAdelante, btnPlay;
    JPanel panelBotones, panelImagen, panelSuperior;
    ImageIcon[] imagenes;
    String[] titulos;
    int imagenActual = 0;
    JLabel etiquetaImagen, etiquetaTitulo;
    Timer timer;
    boolean reproduciendo = false;

    public Galeria() {
        setTitle("Galería de Imágenes");
        setSize(460, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        titulos = new String[]{
            "1. Paisaje Montañoso",
            "2. Atardecer en el Valle",
            "3. Bosque Nublado",
            "4. Horizonte Marino",
            "5. Senderos de Otoño",
            "6. Arquitectura Moderna",
            "7. Palmeras Tropicales",
            "8. Luces de la Ciudad",
            "9. Lago Cristalino",
            "10. Desierto Dorado"
        };

        imagenes = new ImageIcon[10];
        for (int i = 0; i < 10; i++) {
            java.net.URL url = getClass().getResource("/imagenes/img" + (i + 1) + ".jpg");
            if (url != null) {
                imagenes[i] = new ImageIcon(url);
            } else {
                imagenes[i] = new ImageIcon("imagenes/img" + (i + 1) + ".jpg");
            }
        }

        panelSuperior = new JPanel();
        etiquetaTitulo = new JLabel("", SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelSuperior.add(etiquetaTitulo);
        add(panelSuperior, BorderLayout.NORTH);

        panelImagen = new JPanel();
        etiquetaImagen = new JLabel();
        panelImagen.add(etiquetaImagen);
        add(panelImagen, BorderLayout.CENTER);

        panelBotones = new JPanel();
        btnAtras = new JButton("Atras");
        btnAdelante = new JButton("Adelante");
        btnPlay = new JButton("Reproducir");

        panelBotones.add(btnAtras);
        panelBotones.add(btnPlay);
        panelBotones.add(btnAdelante);
        add(panelBotones, BorderLayout.SOUTH);

        mostrarImagen();

        btnAtras.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imagenActual == 0) {
                    imagenActual = imagenes.length - 1;
                } else {
                    imagenActual--;
                }
                mostrarImagen();
            }
        });

        btnAdelante.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avanzarImagen();
            }
        });

        timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                avanzarImagen();
            }
        });

        btnPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!reproduciendo) {
                    btnPlay.setText("Detener");
                    reproduciendo = true;
                    timer.start();
                } else {
                    btnPlay.setText("Reproducir");
                    reproduciendo = false;
                    timer.stop();
                }
            }
        });
    }

    private void avanzarImagen() {
        if (imagenActual == imagenes.length - 1) {
            imagenActual = 0;
        } else {
            imagenActual++;
        }
        mostrarImagen();
    }

    public void mostrarImagen() {
        etiquetaTitulo.setText(titulos[imagenActual]);
        ImageIcon icono = imagenes[imagenActual];
        if (icono != null && icono.getImage() != null) {
            Image imgEscalada = icono.getImage().getScaledInstance(380, 380, Image.SCALE_SMOOTH);
            etiquetaImagen.setIcon(new ImageIcon(imgEscalada));
        }
    }

    public static void main(String[] args) {
        Galeria g = new Galeria();
        g.setVisible(true);
    }
}
