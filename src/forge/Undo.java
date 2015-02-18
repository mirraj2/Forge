package forge;

import static jasonlib.util.Functions.map;
import jasonlib.Json;
import jasonlib.Log;
import jasonlib.util.Functions;
import java.util.Collection;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import forge.map.MapObject;
import forge.ui.Forge;

public class Undo {

  private final Forge forge;
  private final List<Event> events = Lists.newArrayList();
  private int eventIndex = -1; // points to the last event represented in the current state

  public Undo(Forge forge) {
    this.forge = forge;
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
    add(new ModifyEvent(ImmutableList.of(o)));
  }

  public void onModify(Collection<MapObject> objects) {
    add(new ModifyEvent(objects));
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
      forge.canvas.region.remove(id);
    }
  }

  private class ModifyEvent extends Event {

    private final List<Json> json;
    private final List<Integer> ids;

    public ModifyEvent(Collection<MapObject> objects) {
      json = map(objects, o -> o.toJson());
      ids = map(objects, o -> o.id);
    }

    @Override
    public void undo() {
      Functions.splice(json, ids, (json, id) -> {
        MapObject old = MapObject.load(json, forge.armory);
        old.id = id;
        forge.canvas.region.replace(id, old);
      });
    }
  }

}
