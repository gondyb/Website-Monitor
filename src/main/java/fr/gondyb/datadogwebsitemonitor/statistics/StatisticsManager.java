package fr.gondyb.datadogwebsitemonitor.statistics;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatisticsManager {

    private final EventBus eventBus;

    private final Map<URI, List<StatisticsAggregator>> aggregators;

    public StatisticsManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.aggregators = new HashMap<>();
    }

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
