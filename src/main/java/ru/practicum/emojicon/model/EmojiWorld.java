package ru.practicum.emojicon.model;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import ru.practicum.emojicon.engine.*;

import java.util.*;

public class EmojiWorld extends EmojiObject implements EntityResolver, EmojiObjectHolder, Controller {

    private List<EmojiWorldObject> objects = new ArrayList<>();
    private UUID selection = null;

    public EmojiWorld(){
        this.setWidth(2048);
        this.setHeight(2048);
    }

    @Override
    public void drawFrame(Frame frame) {
        drawEarth(frame);
        drawObjects(frame);
    }

    private void drawObjects(Frame frame) {
        //отсекаем лишние объекты, которые точно не отобразятся
        objects.stream()
                .filter(obj -> frame.getLeft() <= obj.getLeft() && frame.getRight() >= obj.getRight() && frame.getTop() <= obj.getTop() && frame.getBottom() >= obj.getBottom())
                .forEach(obj -> {
                    TranslatedFrame objFrame = new TranslatedFrame(frame, new Point(obj.getX(), obj.getY()));
                    objFrame.setTransparentColor(TextColor.ANSI.BLACK_BRIGHT);
                    obj.drawFrame(objFrame);
                });
    }

    private void drawEarth(Frame frame) {
        for(int x = Math.max(0, frame.getLeft()); x <= Math.min(getWidth(), frame.getRight()); x++){
            for (int y = Math.max(0, frame.getTop()); y <= Math.min(getHeight(), frame.getBottom()); y++){
                frame.setPosition(x, y);
                frame.setFillColor(TextColor.ANSI.BLACK_BRIGHT);
                frame.paint();
            }
        }
    }

    @Override
    public UUID addObject(EmojiObject obj, Point position) {
        EmojiWorldObject wobj = new EmojiWorldObject(this, obj, position);
        addWorldObject(wobj);
        return wobj.getId();
    }

    @Override
    public boolean isFreeArea(int left, int top, int right, int bottom) {
        return left >= 0 && top >=0 && right <= getWidth() && bottom <= getHeight(); // пока всё поле пустое
    }

    private void addWorldObject(EmojiWorldObject obj) {
        objects.add(obj);
    }

    @Override
    public void handleKey(KeyStroke key) {
        objects.stream().filter(obj -> obj.getId().equals(selection)).filter(obj -> obj instanceof Controllable).map(obj -> (Controllable) obj).forEach(obj -> {
            switch (key.getKeyType()){
                case ArrowDown, ArrowLeft, ArrowUp, ArrowRight -> {
                    Point pt = switch (key.getKeyType()){
                        case ArrowDown -> new Point(0, 1);
                        case ArrowLeft -> new Point(-1, 0);
                        case ArrowRight -> new Point(1, 0);
                        case ArrowUp -> new Point(0, -1);
                        default -> throw new IllegalArgumentException();
                    };
                    obj.move(pt);
                }
            }
        });
    }

    @Override
    public void setSelection(UUID... objectId) {
        this.selection = objectId[0];
    }

    @Override
    public List<UUID> getSelection() {
        return selection != null ? Arrays.asList(selection) : Collections.emptyList();
    }

    @Override
    public Optional<? extends Entity> findEntity(UUID uuid) {
        return objects.stream().filter(obj -> obj.getId().equals(uuid)).findFirst(); //TODO заменить на Map и поиск по ключу
    }
}
