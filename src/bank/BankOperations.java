package bank;

public interface BankOperations {
    void deposit(double amount);

    void withdraw(double amount);

    double checkBalance();
}
