import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class ReactiveStreamClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/hello/numbers"))
                .header("Accept", "text/event-stream")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    System.out.println("Status: " + response.statusCode());
                    response.body().forEach(line -> {
                        // Simulate slow processing
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                        System.out.println("Received: " + line);
                    });
                })
                .join();
    }
}
