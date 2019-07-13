package be.reactiveprogramming.coffeesocket.paymentservice.paymentreceiver;

import be.reactiveprogramming.coffeesocket.paymentservice.dto.PaymentInformation;
import be.reactiveprogramming.coffeesocket.paymentservice.dto.PaymentResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;

public class PaymentReceiver {

    private ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResult handlePayment(PaymentInformation paymentInformation) {
        System.out.println("Handling payment information..." + paymentInformation);
        return new PaymentResult(true);
    }

    public void run() {
        try {
            ConnectionFactory connFactory = new ConnectionFactory();
            Connection conn = connFactory.newConnection();
            final Channel ch = conn.createChannel();

            RpcServer rpcServer = new RpcServer(ch, "rpc.server.queue") {
                @Override
                public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
                    return toBinary(handlePayment(fromBinary(requestBody, PaymentInformation.class)));
                }
            };

            rpcServer.mainloop();
        } catch (Exception ex) {
            System.err.println("Main thread caught exception: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
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
