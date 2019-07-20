# CoffeeSocket Reactive Architecture demo

## Goal
In this demo project a number of technologies are combined to display the use of [Reactive Streams](https://www.reactive-streams.org/) throughout an entire architecture.

## Requirements
- A running RabbitMQ server (preferably on localhost)
- A running MongoDB database server (preferably on localhost)

## Usage
Simply start up the PaymentService, CoffeeSocketService and CoffeeSocketClient applications.

In case you don't have your RabbitMQ server and MongoDB database server running on localhost, you will need to apply some additional configuration in the `application.properties` files of the different applications.

## What happens?
There is one single flow that moves through the different applications. This flow forms one big Reactive Stream, applying back pressure all the way.

On a functional level, the flow starts with the **CoffeeSocketClient**, requesting paid CoffeeOrders from the **CoffeeSocketService**. These start with "Mock orders", CoffeeOrders we pretend entering the system. For these orders we retrieve the price by getting the Coffee information from a MongoDB database. After we retrieved the price, we create some payment information, that we send to the **PaymentService**. After the payment has been taken care of in the **PaymentService**, we finally notify the **CoffeeSocketService** about the CoffeeOrder.

![Architectural drawing](https://raw.githubusercontent.com/vanseverk/coffeesocket/master/architecture/coffeesocket_architecture.png)

On a technical level, the flow starts at the **CoffeeSocketClient** application. This application runs on [Spring Boot](https://spring.io/projects/spring-boot) along with [Project Reactor](https://projectreactor.io/) to facilitate its internal Reactive Streams. It also uses the Spring [RSocket](http://rsocket.io/) integration to enable Reactive Streams using TCP (or other technologies like Websocket, Aeron, etc) as the transport layer.

After starting up the **CoffeeSocketClient** application, it will do a call to the **CoffeeSocketService** using an **RSocketRequester**. This call will set up a Reactive Stream between the **CoffeeSocketClient** and **CoffeeSocketService**. The **CoffeeSocketClient** will request a number of messages (e.g. "256") to the **CoffeeSocketService**, which will make the **CoffeeSocketService** send up to that number of results to the **CoffeeSocketClient**. Neither application has to block threads at any point, but simply react to new information flowing in. 

```Java
private final RSocketRequester requester;
...
public Flux<CoffeeOrder> receiveCoffeeOrders() {
	return this.requester
	    .route("coffeeOrders")
		.data(new CoffeeServerSubscription("WaiterName"))
		.retrieveFlux(CoffeeOrder.class)
		.log();
}
```

The **CoffeeSocketService** also runs [Spring Boot](https://spring.io/projects/spring-boot) along with [Project Reactor](https://projectreactor.io/) and the Spring [RSocket](http://rsocket.io/) integration.
It offers an RSocket message handler to receive the coffeeOrders request from the **CoffeeSocketClient**.

```Java
@MessageMapping("coffeeOrders")
public Flux<CoffeeOrder> coffeeOrdersStream(CoffeeServerSubscription request) {
    return orders()
        .flatMap(
            coffeeOrder -> coffeeRepository.findOneByCoffeeType(coffeeOrder.getCoffeeType())
                .flatMap(ct -> paymentSender.sendMessage(new PaymentInformation(coffeeOrder.getTableNumber(), ct.getPrice())))
                .map(paymentResult -> coffeeOrder)
        );
}
```

When the `coffeeOrdersStream` method gets called through the message listener, a [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) with `coffeeOrder` objects will be created, forming the start of our Reactive Pipeline.

These `coffeeOrder` objects are randomly generated by the **CoffeeSocketService** through the `orders` method but we can pretend they come from somewhere else, like through REST calls, a Kafka Topic, or an AMQP queue.

Each of these orders will go through a sequence of different steps, increasing the length of our Reactive Pipeline.

The first step is a call to a [MongoDB database](https://www.mongodb.com/) using the `spring-boot-starter-data-mongodb-reactive` library, which enables Reactive Streams from MongoDB. We retrieve a `Coffee` from the database, containing some details on the price of the coffee.
We get a `Mono<Coffee>` from the database, which will make the pipeline continue after the value is retrieved from the database. This means we don't need to block a Thread waiting for the result of the database.

After we receive the `Coffee` object, our next goal is to send the cost of the order to the **PaymentService** so it can be processed further there. The processing will simply be printing the cost to the command line, but we can pretend it does some more interesting things. After the message has been printed, we want to continue our stream by sending the `CoffeeOrder` to the client.

The **PaymentService** is some kind of stateless worker application. This means it's an ideal use case for [AMQP](https://www.amqp.org/) using a [RabbitMQ](https://www.rabbitmq.com/) server. By using AMQP queues, we can easily scale our worker applications horizontally. When we require more processing, we spin up more workers and vice versa.
 
Because we want to make this a part of our Reactive Streams we use the [Reactor RabbitMQ](https://projectreactor.io/docs/rabbitmq/milestone/reference/) library. In it we find an `RpcClient` that we can use to do an asynchronous [request/reply](https://projectreactor.io/docs/rabbitmq/milestone/reference/#_request_reply) to the **PaymentService** and back again.  

```Java
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
```

The **PaymentService** itself has an `RPCServer` running to handle the processing of the incoming message. After the processing is done, the reply will be placed on the reply queue so the processing can continue in the **CoffeeSocketService**

```Java
RpcServer rpcServer = new RpcServer(ch, "rpc.server.queue") {
    @Override
    public byte[] handleCall(byte[] requestBody, AMQP.BasicProperties replyProperties) {
        return toBinary(handlePayment(fromBinary(requestBody, PaymentInformation.class)));
    }
};

rpcServer.mainloop();
```

After we receive the reply of the **PaymentService** in the **CoffeeSocketService** we finally pass the `CoffeeOrder` object along the Reactive Stream again, this time sending it over our original RSocket call as a result. When the **CoffeeSocketClient** receives it, its Reactive Stream continues and prints it to the command line, forming the end of our stream.