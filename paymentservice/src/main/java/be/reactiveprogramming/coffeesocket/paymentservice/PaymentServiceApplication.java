package be.reactiveprogramming.coffeesocket.paymentservice;

import be.reactiveprogramming.coffeesocket.paymentservice.paymentreceiver.PaymentReceiver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        new PaymentReceiver().run();
    }

}
