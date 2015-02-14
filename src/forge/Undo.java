package forge;

import jasonlib.Json;
import jasonlib.Log;
import java.util.List;
import com.google.common.collect.Lists;
import forge.map.MapData;
import forge.map.MapObject;
import forge.ui.Forge;

public class Undo {

  private final Forge forge;
  private final MapData data;
  private final List<Event> events = Lists.newArrayList();
  private int eventIndex = -1; // points to the last event represented in the current state

  public Undo(Forge forge) {
    this.forge = forge;
    this.data = forge.canvas.data;
  }

  public void undo() {
    if (eventIndex >= 0) {
      events.get(eventIndex--).undo();
      Log.debug("undo index is now: " + eventIndex);
    }
  }

  public void redo() {
    throw new RuntimeException("Not implemented.");
  }

  public void onModify(MapObject o) {
    add(new ModifyEvent(o));
  }

  public void onCreate(MapObject o) {
    add(new CreateEvent(o));
  }

  private void add(Event e) {
    while (events.size() > eventIndex + 1) {
      events.remove(events.size() - 1);
    }
    events.add(e);
    eventIndex = events.size() - 1;
  }

  private abstract class Event {
    public abstract void undo();
  }

  private class CreateEvent extends Event {
    private final int id;

    public CreateEvent(MapObject o) {
      this.id = o.id;
    }

    @Override
    public void undo() {
      data.remove(id);
    }
  }

  private class ModifyEvent extends Event {
    private final int id;
    private final Json json;

    public ModifyEvent(MapObject o) {
      this.id = o.id;
      json = o.toJson();
    }

    @Override
    public void undo() {
      MapObject old = MapObject.load(json, forge);
      old.id = id;
      data.replace(id, old);
    }
  }

}
