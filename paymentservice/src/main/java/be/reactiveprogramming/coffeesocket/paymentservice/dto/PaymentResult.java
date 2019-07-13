package be.reactiveprogramming.coffeesocket.paymentservice.dto;

public class PaymentResult {

    private boolean successful;

    public PaymentResult() {
    }

    public PaymentResult(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
