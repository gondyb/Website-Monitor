package fr.gondyb.datadog.website.monitor.watchdog;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.TimerTask;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This {@link TimerTask} checks the availability of a website and produces events accordingly.
 */
public class CheckWebsiteTask extends TimerTask {

    /**
     * The URI of the website to check.
     */
    private final URI url;

    /**
     * The timeout after which send a {@link WebsiteDownEvent}.
     */
    private final long timeout;

    /**
     * The client used to make requests.
     */
    private final HttpClient client;

    /**
     * THe global EventBus
     */
    private final EventBus eventBus;

    /**
     * Class Constructor
     *
     * @param url      The URI of the website to check
     * @param timeout  The timeout after which send a {@link WebsiteDownEvent}
     * @param eventBus The global EventBus
     */
    public CheckWebsiteTask(URI url, long timeout, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        this.timeout = timeout;
        this.url = url;
        this.client = HttpClient.newHttpClient();
    }

    /**
     * This functions checks the website availability, and produces either a {@link WebsiteUpEvent} or
     * {@link WebsiteDownEvent}, according to the response or error.
     */
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
