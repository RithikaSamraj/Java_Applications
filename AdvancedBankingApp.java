
// AdvancedBankingApp.java

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;

public class AdvancedBankingApp extends JFrame {
    CardLayout cardLayout;
    JPanel mainPanel;

    private double balance = 10000.0;
    private double loanAmount = 0.0;
    private double emiAmount = 0.0;
    private int emiMonths = 0;
    private String currentLoanType = "";
    private String customerName = "Rithika Samraj";
    private String customerID = "0409";

    private JLabel balanceLabel, loanLabel, emiLabel, customerDetails;
    private JTextField depositField, withdrawField, loanAmountField, customEMIField;
    private JComboBox<String> loanTypeDropdown;
    private JButton payEMIButton, showStatementBtn;
    private File logFile = new File("transaction_log.txt");
    private JTextArea statementArea;

    public AdvancedBankingApp() {
        setTitle("Advanced Banking Application");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createLoginPanel(),"Login");
        mainPanel.add(createBankingPanel(), "Banking");
        mainPanel.add(createStatementPanel(), "Statement");
        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("User Login");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        JTextField mobileField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.gridx = 0;
        panel.add(new JLabel("Mobile Number:"), gbc);
        gbc.gridx = 1; panel.add(mobileField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            if (mobileField.getText().equals("1234567890") && new String(passField.getPassword()).equals("123")) {
                cardLayout.show(mainPanel, "Banking");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }
        });
        return panel;
    }

    private JPanel createBankingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(4, 1));
        customerDetails = new JLabel("👤 Customer: " + customerName + " | ID: " + customerID);
        balanceLabel = new JLabel();
        loanLabel = new JLabel();
        emiLabel = new JLabel();
        updateStatus();
        topPanel.add(customerDetails);
        topPanel.add(balanceLabel);
        topPanel.add(loanLabel);
        topPanel.add(emiLabel);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel opPanel = new JPanel(new GridLayout(6, 3, 10, 10));
        depositField = new JTextField(); withdrawField = new JTextField();
        loanAmountField = new JTextField(); customEMIField = new JTextField();
        loanTypeDropdown = new JComboBox<>(new String[]{"Car Loan", "Home Loan", "Personal Loan"});

        opPanel.add(new JLabel("Deposit (₹):"));
        opPanel.add(depositField); JButton depositBtn = new JButton("Deposit");
        opPanel.add(depositBtn);

        opPanel.add(new JLabel("Withdraw (₹):"));
        opPanel.add(withdrawField); JButton withdrawBtn = new JButton("Withdraw");
        opPanel.add(withdrawBtn);

        opPanel.add(new JLabel("Loan Type:"));
        opPanel.add(loanTypeDropdown); opPanel.add(new JLabel(""));

        opPanel.add(new JLabel("Loan Amount (₹):"));
        opPanel.add(loanAmountField); opPanel.add(new JLabel(""));

        opPanel.add(new JLabel("Preferred EMI (₹):"));
        opPanel.add(customEMIField); JButton loanBtn = new JButton("Request Loan");
        opPanel.add(loanBtn);

        opPanel.add(new JLabel("Pay EMI:"));
        opPanel.add(new JLabel(""));
        payEMIButton = new JButton("Pay EMI");
        payEMIButton.setEnabled(false);
        opPanel.add(payEMIButton);

        panel.add(opPanel, BorderLayout.CENTER);

        showStatementBtn = new JButton("View Bank Statement");
        JPanel btnPanel = new JPanel();
        btnPanel.add(showStatementBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        depositBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(depositField.getText());
                balance += amt;
                log("Deposited ₹" + amt);
                updateStatus();
                refreshStatement();
            } catch (Exception ex) {
                showError("Invalid deposit amount.");
            }
        });

        withdrawBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(withdrawField.getText());
                if (amt > balance) showError("Insufficient balance!");
                else {
                    balance -= amt;
                    log("Withdrawn ₹" + amt);
                    updateStatus();
                    refreshStatement();
                }
            } catch (Exception ex) {
                showError("Invalid withdrawal amount.");
            }
        });

        loanBtn.addActionListener(e -> {
            try {
                double amt = Double.parseDouble(loanAmountField.getText());
                double emi = Double.parseDouble(customEMIField.getText());
                if (emi <= 0 || emi >= amt) {
                    showError("Enter valid EMI amount"); return;
                }
                currentLoanType = (String) loanTypeDropdown.getSelectedItem();
                loanAmount = amt;
                emiAmount = emi;
                emiMonths = (int) Math.ceil(amt / emi);
                balance += amt;
                payEMIButton.setEnabled(true);
                log(currentLoanType + " loan of ₹" + amt + " approved. EMI: ₹" + emi + " x " + emiMonths);
                updateStatus();
                refreshStatement();
            } catch (Exception ex) {
                showError("Invalid loan input");
            }
        });

        payEMIButton.addActionListener(e -> {
            new Thread(() -> {
                if (emiMonths > 0 && balance >= emiAmount) {
                    balance -= emiAmount;
                    loanAmount -= emiAmount;
                    emiMonths--;
                    log("Paid EMI ₹" + emiAmount + ", Remaining Months: " + emiMonths);
                    updateStatus();
                    refreshStatement();
                    if (emiMonths == 0) {
                        payEMIButton.setEnabled(false);
                        currentLoanType = "";
                        emiAmount = 0;
                        log("✅ Loan fully repaid.");
                        refreshStatement();
                    }
                } else {
                    showError("Insufficient balance or no EMI left");
                }
            }).start();
        });

        showStatementBtn.addActionListener(e -> {
            refreshStatement();
            cardLayout.show(mainPanel, "Statement");
        });

        return panel;
    }

    private JPanel createStatementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statementArea = new JTextArea();
        statementArea.setEditable(false);
        JButton backButton = new JButton("Back to Dashboard");
        JButton printButton = new JButton("Print Statement");
        JPanel btnPanel = new JPanel();
        btnPanel.add(backButton);
        btnPanel.add(printButton);

        panel.add(new JScrollPane(statementArea), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Banking"));

        printButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().print(logFile);
            } catch (IOException ex) {
                showError("Error printing statement.");
            }
        });

        return panel;
    }

    private void refreshStatement() {
        if (statementArea == null) return;
        statementArea.setText("");
        try (Scanner sc = new Scanner(logFile)) {
            while (sc.hasNextLine()) {
                statementArea.append(sc.nextLine() + "\n");
            }
        } catch (IOException e) {
            statementArea.setText("No transactions found.");
        }
    }

    private void updateStatus() {
        balanceLabel.setText("💰 Balance: ₹" + String.format("%.2f", balance));
        loanLabel.setText("🏦 Loan Outstanding: ₹" + String.format("%.2f", loanAmount));
        emiLabel.setText("📆 EMI: ₹" + String.format("%.2f", emiAmount) + " | Months Left: " + emiMonths);
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fullMsg = "[" + timestamp + "] " + message;
        try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
            out.println(fullMsg);
        } catch (IOException e) {
            showError("Could not write to log file.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdvancedBankingApp().setVisible(true));
    }
}
