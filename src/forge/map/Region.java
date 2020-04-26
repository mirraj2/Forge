package forge.map;

import static java.util.stream.Collectors.toList;
import static ox.util.Functions.map;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import armory.Armory;
import armory.rez.Resource;
import forge.map.object.MapObject;
import ox.IO;
import ox.Json;
import ox.Log;
import ox.OS;
import ox.Rect;

public class Region {

  public static int idCounter = 0;

  public int id;
  public String name;
  public final List<MapObject> objects = Lists.newArrayList();

  public Region() {
    this.id = idCounter++;
    this.name = "Untitled Region";
  }

  public Region(int id, Armory armory) {
    this.id = id;
    load(armory);
  }

  public void add(MapObject o) {
    objects.add(o);
  }

  public void remove(MapObject o) {
    objects.remove(o);
  }

  public void remove(int objectId) {
    objects.remove(getIndex(objectId));
  }

  public void replace(int objectId, MapObject replacement) {
    int index = getIndex(objectId);
    if (index == -1) {
      Log.debug(map(objects, o -> o.id));
      throw new RuntimeException("Could not find object of id: " + objectId);
    }
    objects.set(index, replacement);
  }

  private int getIndex(int objectId) {
    for (int i = 0; i < objects.size(); i++) {
      if (objects.get(i).id == objectId) {
        return i;
      }
    }
    Log.debug("Could not find object " + objectId);
    return -1;
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

    if (index == -1) {
      throw new RuntimeException("Could not find object.");
    }

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

  public List<MapObject> getObjects(Rect region) {
    return objects.stream()
        .filter(o -> o.getBounds().intersects(region))
        .collect(toList());
  }

  public MapObject getObjectAt(int x, int y) {
    return getObjectAt(x, y, o -> true);
  }

  public MapObject getObjectAt(int x, int y, Predicate<MapObject> filter) {
    for (MapObject o : Lists.reverse(objects)) {
      if (filter.test(o) && o.isHit(x, y)) {
        return o;
      }
    }
    return null;
  }

  /**
   * Gets a matching autotile at this location.
   */
  public MapObject getAutotile(int x, int y, Resource rez) {
    return getObjectAt(x, y, o -> o.rez == rez);
  }

  public void save() {
    Json json = Json.object()
        .with("id", id)
        .with("name", name)
        .with("objects", Json.array(objects, o -> o.toJson()));

    IO.from(json).to(getFile());
  }

  private void load(Armory armory) {
    Json json = IO.from(getFile()).toJson();
    this.name = json.get("name");
    for (Json o : json.getJson("objects").asJsonArray()) {
      objects.add(MapObject.load(o, armory));
    }
  }

  private File getFile() {
    return new File(OS.getAppFolder("forge"), "region" + id + ".json");
  }

}
