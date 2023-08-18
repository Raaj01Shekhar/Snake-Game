import java.awt.EventQueue;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main extends JFrame {
    public Main() {
        initUI();
    }

    private void initUI() {
        add(new Game());
        setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("head.png")));
        setTitle("Snake 1.1 by Raaj");
        setSize(810, 610);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Main ex = new Main();
            ex.setVisible(true);
        });
    }
}
