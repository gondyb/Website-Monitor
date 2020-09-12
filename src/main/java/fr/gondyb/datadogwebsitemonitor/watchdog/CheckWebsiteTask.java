package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.TimerTask;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CheckWebsiteTask extends TimerTask {

    private final URI url;

    private final long timeout;

    private final HttpClient client;

    private final EventBus eventBus;

    public CheckWebsiteTask(URI url, long timeout, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.timeout = timeout;
        this.url = url;
        this.client = HttpClient.newHttpClient();
    }

    public void run() {
        long startMillis = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .orTimeout(timeout, TimeUnit.MILLISECONDS)
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
