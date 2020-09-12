package fr.gondyb.datadog.website.monitor.ui;

import com.google.common.eventbus.EventBus;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import fr.gondyb.datadog.website.monitor.alarm.AlarmDetector;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadog.website.monitor.statistics.StatisticsAggregator;
import fr.gondyb.datadog.website.monitor.statistics.event.StatisticsUpdatedEvent;
import fr.gondyb.datadog.website.monitor.ui.event.StartMonitorEvent;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class is the main window. It contains the different UI components. The library used for this UI is Lanterna.
 */
public class MainWindow extends BasicWindow {

    /**
     * This panel contains the last 10 minutes table, updated every minute.
     */
    private final Panel minuteStatsPanel;

    /**
     * This panel contains the last hour table, updated every 10 minutes.
     */
    private final Panel hourlyStatsPanel;

    /**
     * This table contains the last 10 minutes statistics, updated every minute.
     */
    private final Table<String> minuteTable;

    /**
     * This table contains the last hour statistics, updated 10 minutes.
     */
    private final Table<String> hourTable;

    /**
     * This table contains every Alarm Triggered and Alarm Stopped events.
     */
    private final Table<String> alertsTable;

    /**
     * This panel contains the alerts table.
     */
    private final Panel alertsPanel = new Panel();

    /**
     * This map contains the last statistics for the last 10 minutes, sorted by website URI.
     */
    private final Map<URI, StatisticsUpdatedEvent> minuteStats = new HashMap<>();

    /**
     * This map contains the last statistics for the last hour, sorted by website URI.
     */
    private final Map<URI, StatisticsUpdatedEvent> hourStats = new HashMap<>();

    /**
     * The global EventBus.
     */
    private final EventBus eventBus;

    /**
     * Class constructor
     *
     * @param eventBus The global EventBus
     */
    public MainWindow(EventBus eventBus) {
        this.eventBus = eventBus;

        Panel mainPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        minuteStatsPanel = new Panel();
        String[] tableColumns = new String[]{
                "URL",
                "Min (ms)",
                "Max (ms)",
                "Avg (ms)",
                "Availability (%)",
                "2xx Hits",
                "3xx Hits",
                "4xx Hits",
                "5xx Hits"
        };

        minuteTable = new Table<>(
                tableColumns
        );
        minuteStatsPanel.addComponent(minuteTable);

        hourlyStatsPanel = new Panel();
        hourTable = new Table<>(
                tableColumns
        );
        hourlyStatsPanel.addComponent(hourTable);

        mainPanel.addComponent(minuteStatsPanel.withBorder(Borders.singleLine("Last 10 minutes stats - Updated every 10s")));
        mainPanel.addComponent(hourlyStatsPanel.withBorder(Borders.singleLine("Last hour stats - Updated every minute")));

        alertsPanel.setLayoutManager(new LinearLayout());
        alertsTable = new Table<>("");
        alertsPanel.addComponent(alertsTable);

        mainPanel.addComponent(alertsPanel.withBorder(Borders.singleLine("Down alerts")));

        Panel tipsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        tipsPanel.addComponent(new Label("Press q to exit"));
        tipsPanel.addComponent(new EmptySpace());
        tipsPanel.addComponent(new Label("Press a to add a new website"));

        Panel rootPanel = new Panel();
        rootPanel.addComponent(mainPanel);
        rootPanel.addComponent(tipsPanel);

        setComponent(rootPanel);
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));

        addWindowListener(new MainWindowListener());
    }

    /**
     * This function displays the new website form, allowing a user to start a new monitor.
     * It then produces a {@link StartMonitorEvent} to inform other components of the new monitor.
     *
     * @see MainWindowListener
     */
    public void displayNewWebsiteForm() {
        String websiteUriString = TextInputDialog.showDialog(
                getTextGUI(),
                "New website (1/2)",
                "Please enter the URL for the website to check",
                "http://localhost:"
        );

        if (websiteUriString == null || websiteUriString.isEmpty()) {
            return;
        }
        URI websiteUri;
        try {
            URL url = new URL(websiteUriString);
            websiteUri = url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            MessageDialog.showMessageDialog(
                    getTextGUI(),
                    "Error",
                    "The provided URL is not valid. Please try again. WARNING: The URL has to start with http:// or https://",
                    MessageDialogButton.OK
            );

            displayNewWebsiteForm();
            return;
        }

        String websitePeriodString = TextInputDialog.showDialog(
                getTextGUI(),
                "New website (2/2)",
                "Please enter the check period in milliseconds for the website to check",
                "1000"
        );

        if (websitePeriodString == null || websitePeriodString.isEmpty()) {
            return;
        }

        long period = Long.parseLong(websitePeriodString);

        eventBus.post(new StartMonitorEvent(
                websiteUri,
                period
        ));
    }

    /**
     * This function update panel sizes when the terminal is resized.
     *
     * @param terminalSize The updated terminal size
     */
    void onTerminalResize(TerminalSize terminalSize) {
        TerminalSize third = new TerminalSize(terminalSize.getColumns(), terminalSize.getRows() / 3 - 1);
        minuteStatsPanel.setPreferredSize(third);
        hourlyStatsPanel.setPreferredSize(third);
        alertsPanel.setPreferredSize(third);
    }

    /**
     * This function adds an {@link AlarmTriggeredEvent} to the alarms table.
     *
     * @param event The event to be added to the table
     * @see AlarmDetector
     */
    void onAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        Date currentDate = new Date();
        NumberFormat formatter = new DecimalFormat("#0.0");
        alertsTable.getTableModel().insertRow(
                0,
                Collections.singletonList("Website " + event.getUri() + " is down.\t\tavailability: " + formatter.format(event.getAvailabilityPercentage()) + "%\t\ttime: " + currentDate)
        );
    }

    /**
     * This function adds an {@link AlarmStoppedEvent} to the alarm table.
     *
     * @param event The event to be added to the table
     * @see AlarmDetector
     */
    void onAlarmStoppedEvent(AlarmStoppedEvent event) {
        Date currentDate = new Date();
        NumberFormat formatter = new DecimalFormat("#0.0");
        alertsTable.getTableModel().insertRow(
                0,
                Collections.singletonList("Website " + event.getUri() + " is up.\t\tavailability: " + formatter.format(event.getAvailabilityPercentage()) + "%\t\ttime: " + currentDate)
        );
    }

    /**
     * This function updates the table statistics with new statistics.
     *
     * @param event The new statistics to be displayed
     * @see StatisticsAggregator
     */
    void onStatisticsUpdatedEvent(StatisticsUpdatedEvent event) {
        if (event.getSavedStatisticsDuration() == TimeUnit.MINUTES.toMillis(10)) {
            minuteStats.put(event.getWebsiteUri(), event);
        } else if (event.getSavedStatisticsDuration() == TimeUnit.HOURS.toMillis(1)) {
            hourStats.put(event.getWebsiteUri(), event);
        }

        minuteTable.getTableModel().clear();
        NumberFormat formatter = new DecimalFormat("#0.0");

        for (StatisticsUpdatedEvent siteEvent : minuteStats.values()) {
            minuteTable.getTableModel().addRow(
                    siteEvent.getWebsiteUri().toString(),
                    String.valueOf(siteEvent.getMinLatency()),
                    String.valueOf(siteEvent.getMaxLatency()),
                    String.valueOf(siteEvent.getAverageLatency()),
                    formatter.format(siteEvent.getAvailability()),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(2, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(3, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(4, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(5, 0))
            );
        }

        hourTable.getTableModel().clear();

        for (StatisticsUpdatedEvent siteEvent : hourStats.values()) {
            hourTable.getTableModel().addRow(
                    siteEvent.getWebsiteUri().toString(),
                    String.valueOf(siteEvent.getMinLatency()),
                    String.valueOf(siteEvent.getMaxLatency()),
                    String.valueOf(siteEvent.getAverageLatency()),
                    formatter.format(siteEvent.getAvailability()),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(2, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(3, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(4, 0)),
                    String.valueOf(siteEvent.getResponseCodeHits().getOrDefault(5, 0))
            );
        }
    }


}
