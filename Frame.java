import javax.swing.*;

public class Frame extends JFrame {
    // constructor
    Frame() {
        // frame-config
        this.add(new Panel());
        this.setResizable(false);
        this.setTitle("SNAKE");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
