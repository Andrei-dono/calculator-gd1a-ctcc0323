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
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/calculator", "root", "");
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS calculations (id INT AUTO_INCREMENT PRIMARY KEY, num1 DOUBLE, num2 DOUBLE, operator CHAR(1), result DOUBLE)");
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        frame = new JFrame("Simple Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel(new GridLayout(5, 2)); // Increase the row count to accommodate the history button

        JLabel num1Label = new JLabel("First Number:");
        num1Field = new JTextField();
        JLabel num2Label = new JLabel("Second Number:");
        num2Field = new JTextField();
        JLabel operatorLabel = new JLabel("Operator (+, -, *, /):");
        operatorField = new JTextField();
        JButton calculateButton = new JButton("Calculate");
        JButton historyButton = new JButton("History"); // Add a history button
        JButton clearButton = new JButton("Clear History"); // Add a clear history button
        resultLabel = new JLabel("Result:");

        calculateButton.addActionListener(new CalculateButtonClickListener());
        historyButton.addActionListener(new HistoryButtonClickListener()); // Add action listener for the history button
        clearButton.addActionListener(new ClearButtonClickListener()); // Add action listener for the clear history button

        panel.add(num1Label);
        panel.add(num1Field);
        panel.add(num2Label);
        panel.add(num2Field);
        panel.add(operatorLabel);
        panel.add(operatorField);
        panel.add(calculateButton);
        panel.add(historyButton); // Add the history button to the panel
        panel.add(clearButton); // Add the clear history button to the panel
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

    private void displayHistory() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM calculations");

            StringBuilder historyText = new StringBuilder();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double num1 = resultSet.getDouble("num1");
                double num2 = resultSet.getDouble("num2");
                char operator = resultSet.getString("operator").charAt(0);
                double result = resultSet.getDouble("result");

                String calculation = String.format("%.2f %c %.2f = %.2f", num1, operator, num2, result);
                historyText.append(calculation).append("\n");
            }

            JOptionPane.showMessageDialog(frame, historyText.toString(), "Calculation History", JOptionPane.PLAIN_MESSAGE);
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearHistory() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM calculations");
            statement.close();
            JOptionPane.showMessageDialog(frame, "Calculation history cleared.", "Clear History", JOptionPane.INFORMATION_MESSAGE);
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

    private class HistoryButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            displayHistory();
        }
    }

    private class ClearButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            clearHistory();
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
