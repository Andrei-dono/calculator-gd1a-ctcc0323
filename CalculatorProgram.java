import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CalculatorProgram {
    private JFrame frame;
    private JTextField num1Field;
    private JTextField num2Field;
    private JTextField operatorField;
    private JLabel resultLabel;
    private Connection connection;

    public CalculatorProgram() {
        initializeDatabase();
        createUI();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/calculator", "root", "");
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS calculations (id INTEGER PRIMARY KEY AUTOINCREMENT, num1 DOUBLE, num2 DOUBLE, operator CHAR(1), result DOUBLE)");
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        frame = new JFrame("A Calculator i guess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel num1Label = new JLabel("First Number:");
        num1Field = new JTextField();
        JLabel num2Label = new JLabel("Second Number:");
        num2Field = new JTextField();
        JLabel operatorLabel = new JLabel("Operator (+, -, *, /):");
        operatorField = new JTextField();
        JButton calculateButton = new JButton("Calculate");
        resultLabel = new JLabel("Result:");

        calculateButton.addActionListener(new CalculateButtonClickListener());

        panel.add(num1Label);
        panel.add(num1Field);
        panel.add(num2Label);
        panel.add(num2Field);
        panel.add(operatorLabel);
        panel.add(operatorField);
        panel.add(calculateButton);
        panel.add(resultLabel);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void insertCalculation(double num1, double num2, char operator, double result) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO calculations (num1, num2, operator, result) VALUES (?, ?, ?, ?)");
            preparedStatement.setDouble(1, num1);
            preparedStatement.setDouble(2, num2);
            preparedStatement.setString(3, String.valueOf(operator));
            preparedStatement.setDouble(4, result);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class CalculateButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double num1 = Double.parseDouble(num1Field.getText());
            double num2 = Double.parseDouble(num2Field.getText());
            char operator = operatorField.getText().charAt(0);
            double result = 0.0;

            if (operator == '+') {
                result = num1 + num2;
                resultLabel.setText("Result: " + formatResult(result));
            } else if (operator == '-') {
                result = num1 - num2;
                resultLabel.setText("Result: " + formatResult(result));
            } else if (operator == '*') {
                result = num1 * num2;
                resultLabel.setText("Result: " + formatResult(result));
            } else if (operator == '/') {
                if (num2 != 0) {
                    result = num1 / num2;
                    resultLabel.setText("Result: " + formatResult(result));
                } else {
                    resultLabel.setText("Error: Division by zero is not allowed.");
                }
            } else {
                resultLabel.setText("Error: Invalid operator.");
            }

            insertCalculation(num1, num2, operator, result);
        }
    }

    private String formatResult(double result) {
        if (result == (int) result) {
            return String.format("%.0f", result); // Remove decimal places if it's a whole number
        } else {
            return String.valueOf(result);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CalculatorProgram();
            }
        });
    }
}
