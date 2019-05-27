package be.reactiveprogramming.coffeesocket.client.dto;

public class CoffeeOrder {

    private String tableNumber;
    private String coffeeType;
    private int amount;

    public CoffeeOrder() {
    }

    public CoffeeOrder(String tableNumber, String coffeeType, int amount) {
        this.tableNumber = tableNumber;
        this.coffeeType = coffeeType;
        this.amount = amount;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getCoffeeType() {
        return coffeeType;
    }

    public void setCoffeeType(String coffeeType) {
        this.coffeeType = coffeeType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CoffeeOrder{" +
                "tableNumber='" + tableNumber + '\'' +
                ", coffeeType='" + coffeeType + '\'' +
                ", amount=" + amount +
                '}';
    }
}
