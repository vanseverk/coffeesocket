package be.reactiveprogramming.coffeesocket.client.dto;

public class CoffeeServerSubscription {

    private String name;

    public CoffeeServerSubscription() {
    }

    public CoffeeServerSubscription(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
