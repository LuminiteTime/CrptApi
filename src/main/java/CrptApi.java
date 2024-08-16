import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Semaphore semaphore;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        semaphore = new Semaphore(requestLimit);
        new Thread(() -> {
            while (true) {
                try {
                    timeUnit.sleep(1);
                    semaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public class Document {
    }

    public void createDocument(Document document, String signature) throws Exception {
        semaphore.acquire();

        String json = objectMapper.writeValueAsString(document);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        semaphore.release();
    }
}