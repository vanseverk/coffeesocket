package be.reactiveprogramming.coffeesocket.service.rsocket;

import be.reactiveprogramming.coffeesocket.service.dto.CoffeeOrder;
import be.reactiveprogramming.coffeesocket.service.dto.CoffeeServerSubscription;
import be.reactiveprogramming.coffeesocket.service.dto.PaymentInformation;
import be.reactiveprogramming.coffeesocket.service.entity.Coffee;
import be.reactiveprogramming.coffeesocket.service.paymentgateway.PaymentSender;
import be.reactiveprogramming.coffeesocket.service.repository.CoffeeRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import static java.time.temporal.ChronoUnit.SECONDS;

@Controller
public class CoffeeOrderController {

    private final Random r = new Random();

    private final PaymentSender paymentSender;
    private final CoffeeRepository coffeeRepository;

    public CoffeeOrderController(PaymentSender paymentSender, CoffeeRepository coffeeRepository) {
        this.paymentSender = paymentSender;
        this.coffeeRepository = coffeeRepository;
    }

    @PostConstruct
    public void init() {
        List<String> coffeeTypes = List.of("Café Latte", "Cappuccino", "Espresso", "Flat White", "Long Black", "Macchiato", "Mochaccino");

        coffeeRepository
                .deleteAll()
                .thenMany(Flux.fromIterable(coffeeTypes))
                .map(ct -> new Coffee(ct, r.nextInt(5)))
                .flatMap(coffee -> coffeeRepository.save(coffee))
                .subscribe();

    }

    @MessageMapping("coffeeOrders")
    public Flux<CoffeeOrder> coffeeOrdersStream(CoffeeServerSubscription request) {
        return orders()
                .flatMap(
                        coffeeOrder -> coffeeRepository.findOneByCoffeeType(coffeeOrder.getCoffeeType())
                            .flatMap(ct -> paymentSender.sendMessage(new PaymentInformation(coffeeOrder.getTableNumber(), ct.getPrice())))
                            .map(paymentResult -> coffeeOrder)
                );

    }

    public Flux<CoffeeOrder> orders() {
        return Flux.range(1, 1000)
                .flatMap(n -> randomCoffee())
                .log()
                .delayElements(Duration.of(1, SECONDS));
    }

    private Mono<CoffeeOrder> randomCoffee() {
        return coffeeRepository.findAll().collectList()
                .map(coffeesOverview -> coffeesOverview.get(r.nextInt(coffeesOverview.size() - 1)))
                .map(coffeeType -> {
                    CoffeeOrder coffee = new CoffeeOrder();
                    coffee.setCoffeeType(coffeeType.getCoffeeType());
                    coffee.setTableNumber("Table " + r.nextInt(20));
                    coffee.setAmount(r.nextInt(10));
                    return coffee;
                });
    }
}
