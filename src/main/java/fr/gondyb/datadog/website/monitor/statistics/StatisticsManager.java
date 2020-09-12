package fr.gondyb.datadog.website.monitor.statistics;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadog.website.monitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadog.website.monitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadog.website.monitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class managed the differents statistics aggregators.
 * Its main jobs are to create the aggregator when a website starts to be monitored, and to transmit the events
 * to the appropriate aggregators.
 */
public class StatisticsManager {
    /**
     * The global EventBus.
     */
    private final EventBus eventBus;

    /**
     * The different aggregators for each URI.
     */
    private final Map<URI, List<StatisticsAggregator>> aggregators;

    /**
     * Class Constructor
     *
     * @param eventBus The main EventBus
     */
    public StatisticsManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.aggregators = new HashMap<>();
    }

    /**
     * This subscriber handles the {@link WebsiteUpEvent}. Its job is to transmit this event to the correct aggregators,
     * in order to compute statistics.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void handleWebsiteUpEvent(WebsiteUpEvent event) {
        List<StatisticsAggregator> websiteAggregators = this.aggregators.getOrDefault(
                event.getUri(),
                new ArrayList<>()
        );

        for (StatisticsAggregator aggregator : websiteAggregators) {
            aggregator.handleWebsiteUpEvent(event);
        }
    }

    /**
     * This subscriber handles the {@link WebsiteDownEvent}. Its job is to transmit this event to the correct
     * aggregators, in order to compute statistics.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void handleWebsiteDownEvent(WebsiteDownEvent event) {
        List<StatisticsAggregator> websiteAggregators = this.aggregators.getOrDefault(
                event.getUri(),
                new ArrayList<>()
        );

        for (StatisticsAggregator aggregator : websiteAggregators) {
            aggregator.handleWebsiteDownEvent(event);
        }
    }

    /**
     * This subscriber handles the {@link AvailabilityCalculatedEvent}. Its job is to transmit this event to the correct
     * aggregators, in order to compute statistics.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void availabilityCalculatedEvent(AvailabilityCalculatedEvent event) {
        List<StatisticsAggregator> websiteAggregators = this.aggregators.getOrDefault(
                event.getUri(),
                new ArrayList<>()
        );

        for (StatisticsAggregator aggregator : websiteAggregators) {
            aggregator.handleAvailabilityCalculatedEvent(event);
        }
    }

    /**
     * This subscriber handles the {@link StartMonitorEvent}. Its job is to create the appropriate aggregators, to
     * start monitoring the statistics for a given site, with given updateRate and pushRates.
     *
     * @param event The event containing the website that will be monitored
     */
    @Subscribe
    public void handleStartMonitor(StartMonitorEvent event) {
        StatisticsAggregator tenMinutesAggregator = new StatisticsAggregator(
                event.getUri(),
                event.getDelay(),
                TimeUnit.MINUTES.toMillis(10),
                TimeUnit.SECONDS.toMillis(10),
                eventBus
        );

        StatisticsAggregator oneHourAggregator = new StatisticsAggregator(
                event.getUri(),
                event.getDelay(),
                TimeUnit.HOURS.toMillis(1),
                TimeUnit.MINUTES.toMillis(1),
                eventBus
        );

        List<StatisticsAggregator> websiteAggregators = this.aggregators.getOrDefault(
                event.getUri(),
                new ArrayList<>()
        );

        websiteAggregators.add(tenMinutesAggregator);
        websiteAggregators.add(oneHourAggregator);

        this.aggregators.put(event.getUri(), websiteAggregators);
    }
}
