import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class GUI {

    public static boolean startLoop = false;
    public static boolean stopScript = false;
    private static JFrame jFrame;

    public GUI() {
        jFrame = new JFrame("Egg Hunter");
        jFrame.setSize(400, 400);
        jFrame.setLocationRelativeTo(null);
        jFrame.setLayout(null);
        JLabel label = new JLabel("Enter target names here, comma separated, blank for none");
        label.setBounds(50, 30, 350, 30);
        JTextField textField = new JTextField();
        textField.setBounds(50, 80, 350, 50);

        JButton button = new JButton();
        button.setText("Start");
        button.setSize(200, 100);
        button.setBounds(
                (jFrame.getWidth() - button.getWidth()) / 2, 200, button.getWidth(), button.getHeight());

        button.addActionListener(
                e -> {
                    ThrowsEggs.targets = getNames(textField.getText());
                    jFrame.dispose();
                    startLoop = true;
                });

        jFrame.add(label);
        jFrame.add(textField);
        jFrame.add(button);
        jFrame.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        startLoop = true;
                        stopScript = true;
                        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    }
                });
        jFrame.setVisible(true);
    }

    public static void close() {
        jFrame.setVisible(false);
        jFrame.dispose();
    }
    public static List<String> getNames(String input) {
        List<String> result = new ArrayList<>();

        if (input.contains(",")) {
            String[] parts = input.split(",");
            for (String part : parts) {
                String processed = part.trim().toLowerCase(Locale.ROOT);
                if (!processed.isEmpty()) {
                    result.add(processed);
                }
            }
        } else {
            String processed = input.trim().toLowerCase(Locale.ROOT);
            if (!processed.isEmpty()) {
                result.add(processed);
            }
        }

        return result;
    }
}