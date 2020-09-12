package fr.gondyb.datadog.website.monitor.alarm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadog.website.monitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadog.website.monitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteUpEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class computes the availability of a website, when a {@link WebsiteUpEvent} or a {@link WebsiteDownEvent} is
 * received, for a given period {@code historyDration}. It produces {@link AvailabilityCalculatedEvent}.
 * <p>
 * The computation stores the number of {@link WebsiteUpEvent} and {@link WebsiteDownEvent} in counters for the given
 * period, then computes the availability percentage from these counters. A circular queue is used to know which
 * latencies are expiring when adding a new one. When the {@code latencies} queue is full, the class removes the last
 * element from the counters, so that the availability percentage only represents the {@code historyDuration}.
 */
public class AvailabilityCalculator {

    /**
     * The duration in milliseconds for which the availability is stored.
     */
    private final long historyDuration;

    /**
     * A circular queue for each website, keeping track of latencies during {@code historyDuration}.
     */
    private final Map<URI, CircularFifoQueue<Long>> latencies;

    /**
     * A counter for each website, keeping track of {@link WebsiteUpEvent}.
     */
    private final Map<URI, Integer> upCounters;

    /**
     * A counter for each website, keeping track of {@link WebsiteDownEvent}.
     */
    private final Map<URI, Integer> downCounters;

    /**
     * The global EventBus.
     */
    private final EventBus eventBus;

    /**
     * Class constructor
     *
     * @param historyDuration   The duration in milliseconds for which the availability is stored
     * @param eventBus          The global EventBus
     */
    public AvailabilityCalculator(long historyDuration, EventBus eventBus) {
        this.historyDuration = historyDuration;
        this.eventBus = eventBus;

        this.latencies = new HashMap<>();
        this.upCounters = new HashMap<>();
        this.downCounters = new HashMap<>();
    }

    /**
     * A subscriber of the {@link StartMonitorEvent}. This handler creates a queue for the given website and stores it.
     *
     * @param event The event containing the website's URI
     */
    @Subscribe
    public void handleStartMonitoring(StartMonitorEvent event) {
        this.latencies.put(event.getUri(), new CircularFifoQueue<>((int) (historyDuration / event.getDelay())));
    }

    /**
     * A subscriber of the {@link WebsiteUpEvent}. This handler stores the new latency, and asks to compute the new
     * availability.
     *
     * @param event The event containing the latency and the website's URI
     */
    @Subscribe
    public void handleWebsiteUp(WebsiteUpEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
            removeLastCounter(event.getUri());
        }

        Integer upCounter = this.upCounters.getOrDefault(event.getUri(), 0);
        this.upCounters.put(event.getUri(), upCounter + 1);

        siteLatencies.add(event.getResponseTime());
        latencies.put(event.getUri(), siteLatencies);

        publishAvailability(event.getUri());
    }

    /**
     * A subscriber of the {@link WebsiteDownEvent}. This handler stores a down latency (-1), and asks to compute the
     * new availability.
     *
     * @param event The event containing the website's URI
     */
    @Subscribe
    public void handlerNewDownLog(WebsiteDownEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
            removeLastCounter(event.getUri());
        }

        Integer downCounter = this.downCounters.getOrDefault(event.getUri(), 0);
        this.downCounters.put(event.getUri(), downCounter + 1);

        siteLatencies.add((long) -1);
        latencies.put(event.getUri(), siteLatencies);

        publishAvailability(event.getUri());

    }

    /**
     * This function removes the last latency from the queue. It also removes it from the appropriate counter, so that
     * the counters represent the amount of UP and DOWN events from the queue.
     *
     * @param uri   The website's URI
     */
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

    /**
     * This function produces {@link AvailabilityCalculatedEvent} with the availability percentage from the last
     * {@code historyDuration}.
     *
     * @param uri   The website's URI.
     */
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
