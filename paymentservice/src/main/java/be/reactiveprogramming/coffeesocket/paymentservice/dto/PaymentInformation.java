package be.reactiveprogramming.coffeesocket.paymentservice.dto;

public class PaymentInformation {

    private String tableNumber;
    private int amount;

    public PaymentInformation() {
    }

    public PaymentInformation(String tableNumber, int amount) {
        this.tableNumber = tableNumber;
        this.amount = amount;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
