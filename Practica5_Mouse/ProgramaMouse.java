import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ProgramaMouse extends JFrame {

    public ProgramaMouse() {
        setTitle("Programa con Mouse - Victor Enrique Yañez Medina");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Lienzo lienzo = new Lienzo();
        add(lienzo);
    }

    static class Lienzo extends JPanel {
        int x = 200, y = 200;
        BufferedImage imagenPersonalizada;
        final int ANCHO_IMG = 120;
        final int ALTO_IMG = 60;

        public Lienzo() {
            setBackground(new Color(240, 240, 245));
            crearImagenNombre();

            // Sigue el puntero únicamente posicionando/moviendo el mouse encima
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    // Centrar la imagen en la punta del cursor
                    x = e.getX() - (ANCHO_IMG / 2);
                    y = e.getY() - (ALTO_IMG / 2);
                    repaint();
                }
            });
        }

        private void crearImagenNombre() {
            imagenPersonalizada = new BufferedImage(ANCHO_IMG, ALTO_IMG, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = imagenPersonalizada.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo distintivo tipo tarjeta/badge
            g2.setColor(new Color(30, 144, 255));
            g2.fillRoundRect(0, 0, ANCHO_IMG, ALTO_IMG, 20, 20);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(1, 1, ANCHO_IMG - 2, ALTO_IMG - 2, 20, 20);

            // Texto con tu nombre
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("Victor", 40, 24);
            g2.drawString("Yañez M.", 34, 42);
            g2.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenPersonalizada != null) {
                g.drawImage(imagenPersonalizada, x, y, this);
            }
        }
    }

    public static void main(String[] args) {
        ProgramaMouse v = new ProgramaMouse();
        v.setVisible(true);
    }
}
