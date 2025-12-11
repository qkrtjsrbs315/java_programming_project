// WaitingDialog.java
import javax.swing.*;

public class WaitingDialog {
    public static void showWaiting(String message) {
        JOptionPane.showMessageDialog(null, message, "대기 중", JOptionPane.INFORMATION_MESSAGE);
    }
}
