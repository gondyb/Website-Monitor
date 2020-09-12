package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.TimerTask;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CheckWebsiteTask extends TimerTask {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);

    private URI url;

    private HttpClient client;

    private EventBus eventBus;

    public CheckWebsiteTask(URI url, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.url = url;
        this.client = HttpClient.newHttpClient();
    }

    public void run() {
        long startMillis = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .orTimeout(DEFAULT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        if (error instanceof TimeoutException || error instanceof CompletionException) {
                            eventBus.post(new WebsiteDownEvent(url));
                            return;
                        }
                        error.printStackTrace();
                        return;
                    }

                    long responseTime = System.currentTimeMillis() - startMillis;
                    eventBus.post(new WebsiteUpEvent(
                            responseTime,
                            response.statusCode(),
                            url
                    ));
                });

    }
}
