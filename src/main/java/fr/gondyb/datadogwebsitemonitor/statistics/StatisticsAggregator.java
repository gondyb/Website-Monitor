package fr.gondyb.datadogwebsitemonitor.statistics;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.AvailabilityCalculator;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadogwebsitemonitor.statistics.event.StatisticsUpdatedEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class constains diverse statistics about a website. It handles {@link WebsiteUpEvent}, {@link WebsiteDownEvent}
 * and {@link AvailabilityCalculatedEvent}. From these events, the class computes statistics and produces
 * {@link StatisticsUpdatedEvent} accordingly.
 */
public class StatisticsAggregator {

    /**
     * The URI of the websited concerned with the statistics.
     */
    private final URI websiteUri;

    /**
     * The aggrgation time in milliseconds.
     */
    private final long savedStatisticsDuration;

    /**
     * The latencies during the last {@code savedStatisticsDuration}.
     */
    private final CircularFifoQueue<Long> latencies;

    /**
     * The response codes during the last {@code savedStatisticsDuration}.
     */
    private final CircularFifoQueue<Integer> responseCodes;

    /**
     * A map containing the number of hits per response code category during the last {@code savedStatisticsDuration}.
     */
    private final Map<Integer, Integer> responseCodesHits;

    /**
     * The global EventBus.
     */
    private final EventBus eventBus;
    /**
     * The period of time in milliseconds at which requests are sent to the website.
     */
    private final long pollingRate;
    /**
     * The minimum latency of the last {@code savedStatisticsDuration}.
     */
    private long minLatency = Long.MAX_VALUE;
    /**
     * The maximum latency of the last {@code savedStatisticsDuration}.
     */
    private long maxLatency = 0;
    /**
     * The average latency of the last {@code savedStatisticsDuration}
     */
    private long avgLatency = -1;
    /**
     * The sum of latencies currently contained in {@code latencies} queue. Used to compute the {@code avgLatency}.
     */
    private long sumLatencies = 0;
    /**
     * The availability percentage for the last {@code savedStatisticsDuration}.
     */
    private double availability = -1;

    /**
     * Class constructor
     *
     * @param websiteUri              The URI of the websited concerned with the statistics
     * @param pollingRate             The period of time in milliseconds at which requests are sent to the website
     * @param savedStatisticsDuration The aggrgation time in milliseconds
     * @param eventPushingRate        The update frequency of statistics (Ex: Statistics are updated every 10s)
     * @param eventBus                The global EventBus
     */
    public StatisticsAggregator(URI websiteUri, long pollingRate, long savedStatisticsDuration, long eventPushingRate, EventBus eventBus) {
        this.pollingRate = pollingRate;
        this.websiteUri = websiteUri;
        this.savedStatisticsDuration = savedStatisticsDuration;
        this.eventBus = eventBus;

        // These queues' size is designed to hold exactly the amount of events during the duration of these statistics
        this.latencies = new CircularFifoQueue<>((int) (savedStatisticsDuration / pollingRate));
        responseCodes = new CircularFifoQueue<>((int) (savedStatisticsDuration / pollingRate));
        responseCodesHits = new HashMap<>();

        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pushStatistics();
            }
        }, 0, eventPushingRate);
    }

    /**
     * Handles {@link WebsiteUpEvent}. It triggers a new calculation of statistics.
     *
     * @param event The event containing the latency, and response code
     */
    protected void handleWebsiteUpEvent(WebsiteUpEvent event) {
        computeTimeStatistics(event.getResponseTime());
        computeResponseCodesStatistics(event.getResponseCode());
    }

    /**
     * Handles the {@link WebsiteDownEvent}. It triggers a new calculation of statistics?
     *
     * @param event The event containing the URI
     */
    protected void handleWebsiteDownEvent(WebsiteDownEvent event) {
        computeTimeStatistics(pollingRate);
    }

    /**
     * Handles the {@link AvailabilityCalculatedEvent}. It updates the store availability.
     *
     * @param event The event containing the availability
     * @see AvailabilityCalculator
     */
    protected void handleAvailabilityCalculatedEvent(AvailabilityCalculatedEvent event) {
        this.availability = event.getAvailability();
    }

    /**
     * This method computes the min, max and average latencies.
     * To do so, it stores everything in a sum of every latency in the queue, then computes the average.
     * For the min and max, we have to check if we remove the min or max from the queue. In that case, we need to find
     * the second min and max before finding a new one.
     *
     * @param newLatency The latency to add to statistics
     */
    private void computeTimeStatistics(long newLatency) {
        if (latencies.isAtFullCapacity()) {
            long removedLatency = latencies.remove();
            if (removedLatency == minLatency) {
                minLatency = latencies.stream().mapToLong(v -> v).min().orElse(-1);
            }
            if (removedLatency == maxLatency) {
                maxLatency = latencies.stream().mapToLong(v -> v).max().orElse(-1);
            }

            sumLatencies -= removedLatency;
        }
        latencies.add(newLatency);

        minLatency = Math.min(minLatency, newLatency);
        maxLatency = Math.max(maxLatency, newLatency);

        sumLatencies += newLatency;
        avgLatency = sumLatencies / latencies.size();
    }

    /**
     * This methods stores the number of hits per response code categories.
     * To do so, it increments the counter of {@code responseCodes}.
     * If the queue is full, it firstly removes the last item from the queue, then decrements the appropriate counters.
     * That way, there are only statistics from the correct duration in the couters.
     *
     * @param responseCode The response code to add
     */
    private void computeResponseCodesStatistics(Integer responseCode) {
        if (responseCodes.isAtFullCapacity()) {
            Integer responseCodeToRemove = responseCodes.remove();

            Integer responseCodeCategory = responseCodeToRemove / 100;

            Integer previousCounter = responseCodesHits.get(responseCodeCategory);
            responseCodesHits.put(responseCodeCategory, previousCounter - 1);
        }

        Integer responseCodeCategory = responseCode / 100;
        Integer previousCounter = responseCodesHits.getOrDefault(responseCodeCategory, 0);
        responseCodesHits.put(responseCodeCategory, previousCounter + 1);
    }

    /**
     * This functions produces a {@link StatisticsUpdatedEvent} to the EventBus.
     */
    private void pushStatistics() {
        StatisticsUpdatedEvent event = new StatisticsUpdatedEvent(
                this.websiteUri,
                this.avgLatency,
                this.maxLatency,
                this.minLatency == Long.MAX_VALUE ? 0 : this.minLatency,
                this.availability,
                new HashMap<>(this.responseCodesHits),  // We need to create a new HashMap,
                // otherwise the pointer is passed,
                // and the window's data is updated continuously.
                this.savedStatisticsDuration
        );

        eventBus.post(event);
    }
}
