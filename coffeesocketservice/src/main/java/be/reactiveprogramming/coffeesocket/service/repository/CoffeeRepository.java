package be.reactiveprogramming.coffeesocket.service.repository;

import be.reactiveprogramming.coffeesocket.service.entity.Coffee;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CoffeeRepository extends ReactiveMongoRepository<Coffee, String> {
    Mono<Coffee> findOneByCoffeeType(String coffeeType);
}
