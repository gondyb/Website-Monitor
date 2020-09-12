package fr.gondyb.datadogwebsitemonitor.alarm;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AvailabilityCalculatorTest {

    @Test
    public void it_should_publish_availability_after_receiving_a_website_up_event() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AvailabilityCalculator calculator = new AvailabilityCalculator(TimeUnit.MINUTES.toMillis(2), eventBus);

        URI uri = URI.create("http://test/");

        StartMonitorEvent startMonitorEvent = new StartMonitorEvent(
                uri,
                1000
        );

        calculator.handleStartMonitoring(startMonitorEvent);

        WebsiteUpEvent event = new WebsiteUpEvent(
                300,
                200,
               uri
        );

        // Act
        calculator.handlerNewUpLog(event);

        // Assert
        verify(eventBus).post(isA(AvailabilityCalculatedEvent.class));
    }


    @Test
    public void it_should_publish_availability_after_receiving_a_website_down_event() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AvailabilityCalculator calculator = new AvailabilityCalculator(TimeUnit.MINUTES.toMillis(2), eventBus);

        URI uri = URI.create("http://test/");

        StartMonitorEvent startMonitorEvent = new StartMonitorEvent(
                uri,
                1000
        );

        calculator.handleStartMonitoring(startMonitorEvent);

        WebsiteDownEvent event = new WebsiteDownEvent(
                uri
        );

        // Act
        calculator.handlerNewDownLog(event);

        // Assert
        verify(eventBus).post(isA(AvailabilityCalculatedEvent.class));
    }

    @Test
    public void it_should_return_75_availability() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AvailabilityCalculator calculator = new AvailabilityCalculator(TimeUnit.MINUTES.toMillis(2), eventBus);

        URI uri = URI.create("http://test/");

        StartMonitorEvent startMonitorEvent = new StartMonitorEvent(
                uri,
                1000
        );

        calculator.handleStartMonitoring(startMonitorEvent);

        // Act
        for (int i = 0 ; i < 90 ; i++) {
            WebsiteUpEvent event = new WebsiteUpEvent(
                    384,
                    200,
                    uri
            );

            // Act
            calculator.handlerNewUpLog(event);
        }

        for (int i = 0 ; i < 30 ; i++) {
            WebsiteDownEvent event = new WebsiteDownEvent(
                    uri
            );

            // Act
            calculator.handlerNewDownLog(event);
        }


        // Assert
        ArgumentCaptor<AvailabilityCalculatedEvent> argument = ArgumentCaptor.forClass(AvailabilityCalculatedEvent.class);
        verify(eventBus, VerificationModeFactory.times(120)).post(argument.capture());
        AvailabilityCalculatedEvent event = argument.getAllValues().get(argument.getAllValues().size() - 1);
        assertEquals(event.getAvailability(), 75, 0.1);
    }

    @Test
    public void it_should_return_under_75_availability() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AvailabilityCalculator calculator = new AvailabilityCalculator(TimeUnit.MINUTES.toMillis(2), eventBus);

        URI uri = URI.create("http://test/");

        StartMonitorEvent startMonitorEvent = new StartMonitorEvent(
                uri,
                2000
        );

        calculator.handleStartMonitoring(startMonitorEvent);

        // Act
        for (int i = 0 ; i < 90 ; i++) {
            WebsiteUpEvent event = new WebsiteUpEvent(
                    384,
                    200,
                    uri
            );

            // Act
            calculator.handlerNewUpLog(event);
        }

        for (int i = 0 ; i < 30 ; i++) {
            WebsiteDownEvent event = new WebsiteDownEvent(
                    uri
            );

            // Act
            calculator.handlerNewDownLog(event);
        }


        // Assert
        ArgumentCaptor<AvailabilityCalculatedEvent> argument = ArgumentCaptor.forClass(AvailabilityCalculatedEvent.class);
        verify(eventBus, VerificationModeFactory.times(120)).post(argument.capture());
        AvailabilityCalculatedEvent event = argument.getAllValues().get(argument.getAllValues().size() - 1);
        assertTrue(event.getAvailability() < 75);
    }

    @Test
    public void it_should_return_over_75_availability() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AvailabilityCalculator calculator = new AvailabilityCalculator(TimeUnit.MINUTES.toMillis(2), eventBus);

        URI uri = URI.create("http://test/");

        StartMonitorEvent startMonitorEvent = new StartMonitorEvent(
                uri,
                1000
        );

        calculator.handleStartMonitoring(startMonitorEvent);

        // Act
        for (int i = 0 ; i < 90 ; i++) {
            WebsiteDownEvent event = new WebsiteDownEvent(
                    uri
            );

            calculator.handlerNewDownLog(event);
        }

        for (int i = 0 ; i < 110 ; i++) {
            WebsiteUpEvent event = new WebsiteUpEvent(
                    384,
                    200,
                    uri
            );

            calculator.handlerNewUpLog(event);
        }

        // Assert
        ArgumentCaptor<AvailabilityCalculatedEvent> argument = ArgumentCaptor.forClass(AvailabilityCalculatedEvent.class);
        verify(eventBus, VerificationModeFactory.times(200)).post(argument.capture());
        AvailabilityCalculatedEvent event = argument.getAllValues().get(argument.getAllValues().size() - 1);
        assertTrue(event.getAvailability() > 75);
    }

}