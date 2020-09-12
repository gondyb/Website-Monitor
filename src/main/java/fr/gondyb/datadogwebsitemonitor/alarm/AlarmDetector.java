package fr.gondyb.datadogwebsitemonitor.alarm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AlarmDetector {
    private static final int AVAILABILITY_THRESHOLD = 80;

    private final EventBus eventBus;

    Map<URI, Boolean> triggeredAlarm;

    public AlarmDetector(EventBus eventBus) {
        this.eventBus = eventBus;
        this.triggeredAlarm = new HashMap<>();
    }

    @Subscribe
    public void availabilityCalculatedHandler(AvailabilityCalculatedEvent event) {
        URI uri = event.getUri();

        double percentage = event.getAvailability();

        boolean alreadyTriggered = triggeredAlarm.getOrDefault(uri, false);

        if (percentage < AVAILABILITY_THRESHOLD && !alreadyTriggered) {
            eventBus.post(new AlarmTriggeredEvent(uri, percentage));
            triggeredAlarm.put(uri, true);
            return;
        }

        if (percentage >= AVAILABILITY_THRESHOLD && alreadyTriggered) {
            eventBus.post(new AlarmStoppedEvent(uri, percentage));
            triggeredAlarm.put(uri, false);
        }

    }
}
