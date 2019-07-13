package be.reactiveprogramming.coffeesocket.client.rsocket;

import be.reactiveprogramming.coffeesocket.client.dto.CoffeeOrder;
import be.reactiveprogramming.coffeesocket.client.dto.CoffeeServerSubscription;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CoffeeServiceClient {

    private final RSocketRequester requester;

    CoffeeServiceClient(RSocketRequester requester) {
        this.requester = requester;
    }

    public Flux<CoffeeOrder> receiveCoffeeOrders() {
        return this.requester
                .route("coffeeOrders")
                .data(new CoffeeServerSubscription("WaiterName"))
                .retrieveFlux(CoffeeOrder.class)
                .log();
    }
}
