package fr.gondyb.datadog.website.monitor.alarm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadog.website.monitor.alarm.event.AvailabilityCalculatedEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This class triggers and untrigger alarms when it receives a {@link AvailabilityCalculatedEvent}.
 * Its logic is checking whether the availability is higher or lower than the given threshold, and checking if the
 * website's alarm is already triggered.
 * It produces {@link AlarmTriggeredEvent} and {@link AlarmStoppedEvent}.
 */
public class AlarmDetector {
    /**
     * The thershold availability percentage after which an alarm should be triggered.
     */
    private final int availabilityThreshold;

    /**
     * The global EventBus.
     */
    private final EventBus eventBus;

    /**
     * A map storing which alarms are triggered or not. The key is a website URL and the value is a boolean that is trus if the alarm is triggere.
     */
    Map<URI, Boolean> triggeredAlarms;

    /**
     * Class constructor
     *
     * @param availabilityThreshold The thershold availability percentage after which an alarm should be triggered
     * @param eventBus              The global EventBus
     */
    public AlarmDetector(int availabilityThreshold, EventBus eventBus) {
        this.availabilityThreshold = availabilityThreshold;
        this.eventBus = eventBus;
        this.triggeredAlarms = new HashMap<>();
    }

    /**
     * This subsciber checks whether an alarm event needs to be triggered or not.
     * It produces {@link AlarmTriggeredEvent} and {@link AlarmStoppedEvent}.
     *
     * @param event The {@link AvailabilityCalculatedEvent} that should be checked.
     */
    @Subscribe
    public void handleAvailabilityCalculatedEvent(AvailabilityCalculatedEvent event) {
        URI uri = event.getUri();

        double percentage = event.getAvailability();

        boolean alreadyTriggered = triggeredAlarms.getOrDefault(uri, false);

        if (percentage < availabilityThreshold && !alreadyTriggered) {
            eventBus.post(new AlarmTriggeredEvent(uri, percentage));
            triggeredAlarms.put(uri, true);
            return;
        }

        if (percentage >= availabilityThreshold && alreadyTriggered) {
            eventBus.post(new AlarmStoppedEvent(uri, percentage));
            triggeredAlarms.put(uri, false);
        }

    }
}
