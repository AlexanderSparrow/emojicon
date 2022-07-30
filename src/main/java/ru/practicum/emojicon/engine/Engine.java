package ru.practicum.emojicon.engine;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Engine implements Runnable {

    //время одного кадра при частоте в 60 FPS
    private static final Long FRAME_TIME = 1000L / 60;
    private Terminal terminal;
    private Screen screen;
    private List<Drawable> roots = new ArrayList<>();
    private Camera camera;

    private Instant timestamp;

    public Engine(){
        try {
            this.terminal = new DefaultTerminalFactory(System.out, System.in, StandardCharsets.UTF_8).createTerminal();
            this.screen = new TerminalScreen(terminal);
            this.timestamp = Instant.now();
            Point rb = getTerminalSize();
            this.camera = new Camera(0, 0, rb.getX(), rb.getY());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Point getTerminalSize() {
        try {
            return new Point(terminal.getTerminalSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            screen.startScreen();
            KeyStroke key;
            do {
                key = screen.pollInput();
                screen.clear();
                screen.doResizeIfNecessary();
                Point size = getTerminalSize();
                screen.setCursorPosition(new TerminalPosition(size.getX(), 0));
                camera.setRightBottom(size.dec(1, 1));
                Frame rootFrame = camera.getFrame(screen);
                roots.forEach(root -> root.drawFrame(rootFrame));
                screen.refresh(Screen.RefreshType.DELTA);
                long dt = Instant.now().toEpochMilli() - timestamp.toEpochMilli();
                timestamp = Instant.now();
                Thread.sleep(Math.max(0, FRAME_TIME - dt)); //сколько-то времени ушло на кадр
            } while (key == null || !(key.getKeyType().equals(KeyType.Escape)));
            screen.stopScreen();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void addRoot(Drawable root) {
        this.roots.add(root);
    }
}