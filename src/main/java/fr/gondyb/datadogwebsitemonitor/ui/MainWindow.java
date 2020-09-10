package fr.gondyb.datadogwebsitemonitor.ui;

import com.google.common.eventbus.EventBus;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalResizeListener;

import java.awt.desktop.QuitEvent;
import java.io.IOException;

public class MainWindow {

    private Terminal terminal;

    private EventBus eventBus;

    public MainWindow(EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            terminal = new DefaultTerminalFactory().createTerminal();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mainWindow() throws IOException {
        if (terminal == null) {
            throw new IOException();
        }
        terminal.enterPrivateMode();
        terminal.setCursorVisible(false);

        placeControls(terminal, terminal.getTerminalSize());

        terminal.addResizeListener(new TerminalResizeListener() {
            @Override
            public void onResized(Terminal terminal, TerminalSize newSize) {
                placeControls(terminal, newSize);
                try {
                    terminal.flush();
                }
                catch(IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        KeyStroke keyStroke = terminal.readInput();

        while(keyStroke.getCharacter() != 'q') {
            keyStroke = terminal.readInput();
        }
        eventBus.post(new QuitEvent());
        try {
            terminal.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    private void placeControls(Terminal terminal, TerminalSize size) {
        try {
            terminal.clearScreen();
            final TextGraphics textGraphics = terminal.newTextGraphics();

            textGraphics.putString(0, size.getRows()-1, "Press Q to exit", SGR.BOLD);
            textGraphics.setForegroundColor(TextColor.ANSI.DEFAULT);
            textGraphics.setBackgroundColor(TextColor.ANSI.DEFAULT);
            terminal.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
