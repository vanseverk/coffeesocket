package be.reactiveprogramming.coffeesocket.service.paymentgateway;

import be.reactiveprogramming.coffeesocket.service.dto.PaymentInformation;
import be.reactiveprogramming.coffeesocket.service.dto.PaymentResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.RpcClient;
import reactor.rabbitmq.Sender;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class PaymentSender {

    private RpcClient rpcClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void onInit() {
        String queue = "rpc.server.queue";
        Sender sender = RabbitFlux.createSender();
        rpcClient = sender.rpcClient("", queue);
    }

    @PreDestroy
    public void preDestroy() {
        rpcClient.close();
    }

    public Mono<PaymentResult> sendMessage(PaymentInformation paymentInformation) {
        return rpcClient.rpc(Mono.just(
                new RpcClient.RpcRequest(toBinary(paymentInformation))
        )).map(delivery -> fromBinary(delivery.getBody(), PaymentResult.class));
    }

    private <T> T fromBinary(byte[] object, Class<T> resultType) {
        try {
            return objectMapper.readValue(object, resultType);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] toBinary(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
