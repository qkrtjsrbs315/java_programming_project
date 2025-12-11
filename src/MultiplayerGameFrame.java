// MultiplayerGameFrame.java
import move.ViewPanel;
import javax.swing.*;

public class MultiplayerGameFrame extends JFrame {
    public MultiplayerGameFrame(String title, ViewPanel panel) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}