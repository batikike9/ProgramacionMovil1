import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Boton extends JFrame {

    public Boton() {
        setSize(200, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Programa 01");

        JButton btnSaludar = new JButton("Saluda");

        btnSaludar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Victor Enrique Yañez Medina");
            }
        });

        add(btnSaludar);
        setVisible(true);
    }
}
