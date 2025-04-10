package bank;

public class SavingsAccount extends BankAccount implements BankOperations {
    private double minimumBalance;

    public SavingsAccount(String accountNumber, double balance, double minimumBalance){
        // Call to superclass constructor in BankAccount.java
        super(accountNumber, balance);
        this.minimumBalance = minimumBalance;
    }

    // Override deposit method
    @Override
    public void deposit(double amount){
        balance += amount;
    }

    // Override withdraw method with specific rules for savings account
    @Override
    public void withdraw(double amount){
        if (balance - amount >= minimumBalance){
            balance -= amount;
        } else {
            System.out.println("Insufficient balance");
        }
    }

    @Override
    public double checkBalance(){
        return balance; // Return current balance
    }

    // Getter method for minimum balance
    public double getMinimumBalance() {
        return minimumBalance;
    }
}
