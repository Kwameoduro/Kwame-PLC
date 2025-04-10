package bank;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class BankAccountManagementSystem extends Application {

    private Map<String, BankAccount> accounts = new HashMap<>();

    private void updateTransactionList(BankAccount account, ListView<String> transactionList) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Transaction transaction : account.getLastNTransactions(10)) {
            items.add(transaction.toString());
        }
        transactionList.setItems(items);
    }

    private void updateAccountList(TextArea accountListArea) {
        StringBuilder sbSavings = new StringBuilder("Savings Accounts:\n");
        StringBuilder sbCurrent = new StringBuilder("Current Accounts:\n");
        StringBuilder sbFixedDeposit = new StringBuilder("Fixed Deposit Accounts:\n");

        for (String accountNumber : accounts.keySet()) {
            BankAccount account = accounts.get(accountNumber);
            if (account instanceof SavingsAccount) {
                sbSavings.append("Account Number: ").append(account.getAccountNumber())
                        .append(", Balance: GHC ").append(account.getBalance()).append("\n");
            } else if (account instanceof CurrentAccount) {
                sbCurrent.append("Account Number: ").append(account.getAccountNumber())
                        .append(", Balance: GHC ").append(account.getBalance()).append("\n");
            } else if (account instanceof FixedDepositAccount) {
                sbFixedDeposit.append("Account Number: ").append(account.getAccountNumber())
                        .append(", Balance: GHC ").append(account.getBalance()).append("\n");
            }
        }

        sbSavings.append("\n");
        sbCurrent.append("\n");
        sbFixedDeposit.append("\n");

        sbSavings.append(sbCurrent.toString()).append(sbFixedDeposit.toString());

        accountListArea.setText(sbSavings.toString());
    }

    @Override
    public void start(Stage primaryStage) {
        // UI Components
        TextField accountNumberField = new TextField();
        accountNumberField.setPromptText("12-digit Account Number");
        accountNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,12}")) {
                accountNumberField.setText(oldValue);
            }
        });

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (in GHC)");

        // Added "GHC" denomination to amount field
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.startsWith("GHC") && !newValue.isEmpty()) {
                amountField.setText("GHC " + newValue.replace("GHC", "").trim());
            }
        });

        TextField minBalanceField = new TextField();
        minBalanceField.setPromptText("Minimum Balance");

        TextField overdraftLimitField = new TextField();
        overdraftLimitField.setPromptText("Overdraft Limit");

        DatePicker maturityDatePicker = new DatePicker();
        maturityDatePicker.setPromptText("Maturity Date");

        // Disable the maturity date picker initially
        maturityDatePicker.setDisable(true);

        // Label to display the calculated interest rate
        Label interestRateLabel = new Label("Interest Rate: ");

        ComboBox<String> accountTypeBox = new ComboBox<>();
        accountTypeBox.setItems(FXCollections.observableArrayList("Savings", "Current", "Fixed Deposit"));
        accountTypeBox.setPromptText("Select Account Type");

        accountTypeBox.setOnAction(e -> {
            // Enable/Disable the maturity date field based on selected account type
            if ("Fixed Deposit".equals(accountTypeBox.getValue())) {
                maturityDatePicker.setDisable(false);
            } else {
                maturityDatePicker.setDisable(true);
            }
        });

        Button createAccountButton = new Button("Create Account");
        Button depositButton = new Button("Deposit");
        Button withdrawButton = new Button("Withdraw");

        ListView<String> transactionList = new ListView<>();

        TextArea accountListArea = new TextArea();
        accountListArea.setEditable(false);

        maturityDatePicker.setOnAction(e -> {
            try {
                // Calculate the number of years between today and the maturity date
                LocalDate currentDate = LocalDate.now();
                LocalDate maturityDate = maturityDatePicker.getValue();
                long yearsBetween = ChronoUnit.YEARS.between(currentDate, maturityDate);

                double interestRate = 0;
                if (yearsBetween == 1) {
                    interestRate = 15.0; // 15% for 1 year
                } else if (yearsBetween == 2) {
                    interestRate = 18.0; // 18% for 2 years
                } else if (yearsBetween == 3) {
                    interestRate = 30.0; // 30% for 3 years
                } else if (yearsBetween > 3) {
                    interestRate = 35.0; // 35% for more than 3 years
                }

                // Display the calculated interest rate
                interestRateLabel.setText("Interest Rate: " + interestRate + "% for " + yearsBetween + " year(s)");

            } catch (Exception ex) {
                showAlert("Error", "Please Enter a correct maturity date");
            }
        });

        createAccountButton.setOnAction(e -> {
            try {
                String accountNumber = accountNumberField.getText();
                if (!accountNumber.matches("\\d{12}"))
                    throw new IllegalArgumentException("Account Number must be exactly 12 digits");

                if (accounts.containsKey(accountNumber)) {
                    showAlert("Error", "Account Number already exists. Please choose a different one.");
                    return;
                }

                double balance = Double.parseDouble(amountField.getText().replace("GHC", "").trim());
                String selectedType = accountTypeBox.getValue();

                if (selectedType == null) {
                    showAlert("Error", "Please select account type");
                    return;
                }

                switch (selectedType) {
                    case "Savings" -> {
                        double minBalance = Double.parseDouble(minBalanceField.getText());
                        accounts.put(accountNumber, new SavingsAccount(accountNumber, balance, minBalance));
                    }
                    case "Current" -> {
                        double overdraftLimit = Double.parseDouble(overdraftLimitField.getText());
                        accounts.put(accountNumber, new CurrentAccount(accountNumber, balance, overdraftLimit));
                    }
                    case "Fixed Deposit" -> {
                        if (maturityDatePicker.getValue() == null) {
                            showAlert("Error", "Please select maturity date for fixed deposit");
                            return;
                        }
                        LocalDate maturityDate = maturityDatePicker.getValue();
                        accounts.put(accountNumber, new FixedDepositAccount(accountNumber, balance, Date.valueOf(maturityDate)));
                    }
                }
                updateAccountList(accountListArea);
                showAlert("Success", selectedType + " Your Account is Created");

            } catch (Exception ex) {
                showAlert("Error", "Wrong input, Try again");
            }
        });

        depositButton.setOnAction(e -> {
            try {
                String accountNumber = accountNumberField.getText();
                double amount = Double.parseDouble(amountField.getText().replace("GHC", "").trim());
                BankAccount account = findAccount(accountNumber);

                if (account != null) {
                    account.deposit(amount);
                    account.addTransaction("Deposit", amount);
                    updateTransactionList(account, transactionList);
                    updateAccountList(accountListArea);
                    showAlert("Success", "Deposit successful");
                } else {
                    showAlert("Error", "Account not found");
                }
            } catch (Exception ex) {
                showAlert("Error", "Wrong deposit amount");
            }
        });

        withdrawButton.setOnAction(e -> {
            try {
                String accountNumber = accountNumberField.getText();
                double amount = Double.parseDouble(amountField.getText().replace("GHC", "").trim());
                BankAccount account = findAccount(accountNumber);

                if (account != null) {
                    if (account instanceof FixedDepositAccount) {
                        // Check if Fixed Deposit Account can withdraw
                        FixedDepositAccount fdAccount = (FixedDepositAccount) account;
                        LocalDate maturityDate = fdAccount.getMaturityDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        LocalDate currentDate = LocalDate.now();
                        if (currentDate.isBefore(maturityDate)) {
                            showAlert("Error", "Please withdrawals are not allowed until maturity date.");
                            return;
                        }
                    }

                    // Check for Savings Account Minimum Balance during withdrawal
                    if (account instanceof SavingsAccount) {
                        SavingsAccount savingsAccount = (SavingsAccount) account;
                        if (account.getBalance() - amount < savingsAccount.getMinimumBalance()) {
                            showAlert("Error", "You cannot withdraw at the moment.");
                            return;
                        }
                    }

                    account.withdraw(amount);
                    account.addTransaction("Withdrawal", amount);
                    updateTransactionList(account, transactionList);
                    updateAccountList(accountListArea);
                    showAlert("Success", "Withdrawal successful");
                } else {
                    showAlert("Try again", "Account not found");
                }
            } catch (Exception ex) {
                showAlert("Try again", "Invalid withdrawal amount");
            }
        });

        // Layout with tabs and styling
        TabPane tabPane = new TabPane();

        // Tab for Account Management
        Tab accountTab = new Tab("Account Management");
        accountTab.setClosable(false);

        GridPane accountLayout = new GridPane();
        accountLayout.setVgap(10);
        accountLayout.setHgap(10);
        accountLayout.setPadding(new Insets(15));

        accountLayout.add(new Label("Account Number"), 0, 0);
        accountLayout.add(accountNumberField, 1, 0);

        accountLayout.add(new Label("Amount (in GHC)"), 0, 1);
        accountLayout.add(amountField, 1, 1);

        accountLayout.add(new Label("Minimum Balance (Savings Only)"), 0, 2);
        accountLayout.add(minBalanceField, 1, 2);

        accountLayout.add(new Label("Overdraft Limit (Current Only)"), 0, 3);
        accountLayout.add(overdraftLimitField, 1, 3);

        accountLayout.add(new Label("Maturity Date (Fixed Deposit Only)"), 0, 4);
        accountLayout.add(maturityDatePicker, 1, 4);

        accountLayout.add(interestRateLabel, 0, 5);
        accountLayout.add(accountTypeBox, 1, 5);

        HBox buttons = new HBox(5, createAccountButton, depositButton, withdrawButton);
        accountLayout.add(buttons, 0, 6, 2, 1);

        accountTab.setContent(accountLayout);

        // Tab for Transaction History
        Tab transactionTab = new Tab("Transaction History");
        transactionTab.setClosable(false);

        VBox transactionHistoryLayout = new VBox(10, new Label("Transaction History"));
        transactionHistoryLayout.getChildren().add(transactionList);
        transactionTab.setContent(transactionHistoryLayout);

        // Add tabs to tab pane
        tabPane.getTabs().addAll(accountTab, transactionTab);

        // Account List View
        VBox accountListLayout = new VBox(10, new Label("Accounts Created"), accountListArea);
        accountListLayout.setPadding(new Insets(15));

        // Final layout with bold heading
        VBox mainLayout = new VBox(10);
        Label heading = new Label("Kwame Bank PLC");
        heading.setFont(Font.font("Arial", 20));  // Bold heading
        mainLayout.getChildren().addAll(heading, tabPane, accountListLayout);
        mainLayout.setPadding(new Insets(15));

        // Scene styling
        Scene scene = new Scene(mainLayout, 600, 800);
        scene.setFill(Color.LIGHTGRAY);  // Background color for the scene
        primaryStage.setTitle("Bank Account Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BankAccount findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
