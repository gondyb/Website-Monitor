package fr.gondyb.datadogwebsitemonitor.alarm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AvailabilityCalculator {

    private final int historyDuration;

    Map<URI, CircularFifoQueue<Long>> latencies;

    Map<URI, Integer> upCounters;

    Map<URI, Integer> downCounters;

    private final EventBus eventBus;

    public AvailabilityCalculator(int historyDuration, EventBus eventBus) {
        this.historyDuration = historyDuration;
        this.eventBus = eventBus;

        this.latencies = new HashMap<>();
        this.upCounters = new HashMap<>();
        this.downCounters = new HashMap<>();
    }

    @Subscribe
    public void handleStartMonitoring(StartMonitorEvent event) {
        this.latencies.put(event.getUri(), new CircularFifoQueue<>((int) (historyDuration * 1000 / event.getDelay())));
    }

    @Subscribe
    public void handlerNewUpLog(WebsiteUpEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
            removeLastCounter(event.getUri());
        }

        Integer upCounter = this.upCounters.getOrDefault(event.getUri(), 0);
        upCounter++;
        this.upCounters.put(event.getUri(), upCounter);

        siteLatencies.add(event.getResponseTime());
        latencies.put(event.getUri(), siteLatencies);
        publishAvailability(event.getUri());
    }

    @Subscribe
    public void handlerNewDownLog(WebsiteDownEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
            removeLastCounter(event.getUri());
        }

        Integer downCounter = this.downCounters.getOrDefault(event.getUri(), 0);
        downCounter++;
        this.downCounters.put(event.getUri(), downCounter);

        siteLatencies.add((long) -1);
        latencies.put(event.getUri(), siteLatencies);

        publishAvailability(event.getUri());

    }

    private void removeLastCounter(URI uri) {
        CircularFifoQueue<Long> siteLatencies = latencies.getOrDefault(
                uri,
                new CircularFifoQueue<>()
        );
        Long removedLatency = siteLatencies.remove();
        if (removedLatency == -1) {
            Integer counter = downCounters.get(uri);
            counter -= 1;
            downCounters.put(uri, counter);
        } else {
            Integer counter = upCounters.get(uri);
            counter -= 1;
            upCounters.put(uri, counter);
        }
    }

    private void publishAvailability(URI uri) {
        Integer upCounter = upCounters.getOrDefault(uri, 0);
        Integer downCounter = downCounters.getOrDefault(uri, 0);

        double percentage = (double) upCounter * 100 / (upCounter + downCounter);

        AvailabilityCalculatedEvent event = new AvailabilityCalculatedEvent(
                uri,
                percentage
        );
        eventBus.post(event);
    }
}
