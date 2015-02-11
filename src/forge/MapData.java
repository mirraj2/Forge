package forge;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.OS;
import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import armory.Sprite;
import com.google.common.collect.Lists;

public class MapData {

  private static final File file = new File(OS.getAppFolder("forge"), "map.json");

  public final List<MapObject> objects = Lists.newArrayList();

  public MapData(Forge forge) {
    load(forge);
  }

  public void add(MapObject o) {
    if (objects.contains(o)) {
      return;
    }
    objects.add(o);
  }

  public void remove(MapObject o) {
    objects.remove(o);
  }

  public void moveBack(MapObject o) {
    int index = objects.indexOf(o);
    if (index == 0) {
      return; // already in the back
    }

    // look for the first object that intersects this one
    for (int i = index - 1; i >= 0; i--) {
      if (o.intersects(objects.get(i))) {
        objects.remove(index);
        objects.add(i, o);
        return;
      }
    }

    // there is nothing behind this object, so we'll just move the z-index back by one
    objects.remove(index);
    objects.add(index - 1, o);
  }

  public void moveForward(MapObject o) {
    int index = objects.indexOf(o);
    if (index == objects.size() - 1) {
      return; // already in the front
    }

    // look for the first object that intersects this one
    for (int i = index + 1; i < objects.size(); i++) {
      if (o.intersects(objects.get(i))) {
        objects.remove(index);
        objects.add(i, o);
        return;
      }
    }

    // there is nothing in front of this object, so we'll just move the z-index up by one
    objects.remove(index);
    objects.add(index + 1, o);
  }

  public MapObject getObjectAt(int x, int y) {
    return getObjectAt(x, y, o -> true);
  }

  public MapObject getObjectAt(int x, int y, Predicate<MapObject> filter) {
    for (MapObject o : Lists.reverse(objects)) {
      if (o.isHit(x, y)) {
        return o;
      }
    }
    return null;
  }

  /**
   * Gets a matching autotile at this location.
   */
  public MapObject getAutotile(int x, int y, Sprite sprite) {
    return getObjectAt(x, y, o -> o.sprite == sprite);
  }

  public void save() {
    Json json = Json.object()
        .with("objects", Json.array(objects, o -> o.toJson()));

    IO.from(json).to(file);
  }

  private void load(Forge forge) {
    if (file.exists()) {
      Json json = IO.from(file).toJson();
      for (Json o : json.getJson("objects").asJsonArray()) {
        objects.add(MapObject.load(o, forge));
      }
    }
  }

}
