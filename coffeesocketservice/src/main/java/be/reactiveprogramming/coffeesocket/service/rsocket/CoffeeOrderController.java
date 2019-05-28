package be.reactiveprogramming.coffeesocket.service.rsocket;

import be.reactiveprogramming.coffeesocket.service.dto.CoffeeOrder;
import be.reactiveprogramming.coffeesocket.service.dto.CoffeeServerSubscription;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static java.time.temporal.ChronoUnit.SECONDS;

@Controller
public class CoffeeOrderController {

    private final Random r = new Random();

    @MessageMapping("coffeeOrders")
    public Flux<CoffeeOrder> coffeeOrdersStream(CoffeeServerSubscription request) {
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
