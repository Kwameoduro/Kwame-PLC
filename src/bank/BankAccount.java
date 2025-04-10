package bank;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class BankAccount {
    protected String accountNumber;
    protected double balance;
    private LinkedList<Transaction> transactions = new LinkedList<>();

    public BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public abstract void deposit(double amount);
    public abstract void withdraw(double amount);

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;  // Added this method to retrieve account number
    }

    public void addTransaction(String type, double amount) {
        transactions.add(new Transaction(type, amount, new Date()));
    }

    // Method to retrieve the last N transactions
    public List<Transaction> getLastNTransactions(int n) {
        return transactions.subList(Math.max(transactions.size() - n, 0), transactions.size());
    }
}
