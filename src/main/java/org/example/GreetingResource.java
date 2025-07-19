package org.example;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reactivestreams.Publisher;
import jakarta.inject.Inject;

@Path("/hello")
public class GreetingResource {

    // Kafka producer to send messages to the 'greetings' topic
    @Inject
    @Channel("greetings-out")
    Emitter<String> greetingsEmitter;

    @GET
    @Path("/send/{msg}")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> sendGreeting(@jakarta.ws.rs.PathParam("msg") String msg) {
        greetingsEmitter.send(msg);
        return Uni.createFrom().item("Message sent to Kafka: " + msg);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> hello() {
        return Uni.createFrom().item("Hello from Quarkus Reactive REST");
    }

    // Example: async greeting with delay
    @GET
    @Path("/delayed")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> delayedHello() {
        return Uni.createFrom().item("Hello after delay!")
            .onItem().delayIt().by(java.time.Duration.ofSeconds(2));
    }

    // Example: streaming multiple greetings (reactive stream)
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public io.smallrye.mutiny.Multi<String> streamGreetings() {
        // Default back pressure is handled by Mutiny/Quarkus for Multi endpoints
        return io.smallrye.mutiny.Multi.createFrom().items("Hello","Bonjour","Hola","Ciao","Hallo");
    }

    // Example: streaming numbers with artificial delay to demonstrate back pressure
    @GET
    @Path("/numbers")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public io.smallrye.mutiny.Multi<Integer> streamNumbers() {
        // Default back pressure is handled by Mutiny/Quarkus for Multi endpoints
        return io.smallrye.mutiny.Multi.createFrom().range(1, 101);
    }

    // Example: async error handling
    @GET
    @Path("/fail")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> failExample() {
        return Uni.<String>createFrom().failure(new RuntimeException("Simulated failure"))
            .onFailure().recoverWithItem(t -> "Recovered from failure!")
            .onItem().castTo(String.class);
    }

    // Stream Kafka messages as SSE
    @Inject
    @Channel("greetings-in")
    Publisher<String> greetingsFromKafka;

    @GET
    @Path("/kafka-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> streamKafkaMessages() {
        // Default back pressure is handled by the reactive engine
        return greetingsFromKafka;
    }
}
