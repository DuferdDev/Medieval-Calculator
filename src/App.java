import java.io.IOException;

public class App {

    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                new CalculatorWindow().show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
