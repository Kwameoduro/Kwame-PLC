package bank;

import java.util.Date;

public class FixedDepositAccount extends BankAccount implements BankOperations {
    private Date maturityDate;

    public FixedDepositAccount(String accountNumber, double balance, Date maturityDate) {
        super(accountNumber, balance);
        this.maturityDate = maturityDate;
    }

    public Date getMaturityDate() {
        return maturityDate;
    }

    @Override
    public void deposit(double amount) {
        System.out.println("Please you cannot deposit into a fixed deposit account");
    }

    @Override
    public void withdraw(double amount) {
        Date currentDate = new Date();
        if (currentDate.after(maturityDate)) {
            balance -= amount;
        } else {
            System.out.println("Please you cannot withdraw before maturity date");
        }
    }

    @Override
    public double checkBalance() {
        return balance;
    }
}
