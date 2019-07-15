package be.reactiveprogramming.coffeesocket.paymentservice.dto;

public class PaymentInformation {

    private String tableNumber;
    private int totalCost;

    public PaymentInformation() {
    }

    public PaymentInformation(String tableNumber, int totalCost) {
        this.tableNumber = tableNumber;
        this.totalCost = totalCost;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }
}
