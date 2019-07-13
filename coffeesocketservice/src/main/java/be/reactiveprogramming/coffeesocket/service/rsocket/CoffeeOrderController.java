package be.reactiveprogramming.coffeesocket.service.rsocket;

import be.reactiveprogramming.coffeesocket.service.dto.CoffeeOrder;
import be.reactiveprogramming.coffeesocket.service.dto.CoffeeServerSubscription;
import be.reactiveprogramming.coffeesocket.service.dto.PaymentInformation;
import be.reactiveprogramming.coffeesocket.service.paymentgateway.PaymentSender;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static java.time.temporal.ChronoUnit.SECONDS;

@Controller
public class CoffeeOrderController {

    private final Random r = new Random();

    private final PaymentSender paymentSender;

    public CoffeeOrderController(PaymentSender paymentSender) {
        this.paymentSender = paymentSender;
    }

    @MessageMapping("coffeeOrders")
    public Flux<CoffeeOrder> coffeeOrdersStream(CoffeeServerSubscription request) {
        return orders()
                .flatMap(
                        o -> paymentSender.sendMessage(new PaymentInformation(o.getTableNumber(), o.getAmount()))
                            .map(r -> o)
                );

    }

    public Flux<CoffeeOrder> orders() {
        return Flux.range(1, 1000).map(n -> randomCoffee()).log().delayElements(Duration.of(1, SECONDS));
    }

    private CoffeeOrder randomCoffee() {
        List<String> coffeeTypes = List.of("Café Latte", "Cappuccino", "Espresso", "Flat White", "Long Black", "Macchiato", "Mochaccino");
        String coffeeType = coffeeTypes.get(r.nextInt(coffeeTypes.size() - 1));

        CoffeeOrder coffee = new CoffeeOrder();
        coffee.setCoffeeType(coffeeType);
        coffee.setTableNumber("Table " + r.nextInt(20));
        coffee.setAmount(r.nextInt(10));
        return coffee;
    }
}
