package forge.map.object;

import java.util.List;

import com.google.common.collect.ImmutableList;

import armory.Armory;
import armory.rez.Resource;
import forge.map.ObjectHandle;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

public class MapObject {

  private static int idCounter = 0;

  public int id = idCounter++; // not serialized, used by Undo

  public final Resource rez;
  public Rect location; // autotiles don't use this

  public MapObject(Resource rez, Rect location) {
    this.rez = rez;
    this.location = location;
  }

  public boolean intersects(MapObject o) {
    return getBounds().intersects(o.getBounds());
  }

  public void moveTo(int x, int y) {
    location = location.location(x, y);
  }

  public boolean isHit(int x, int y) {
    return location.contains(x, y);
  }

  public Rect getBounds() {
    return location;
  }

  public void render(Graphics3D g, Rect clip) {
    for (int i = 0; i < location.w; i += rez.getWidth()) {
      for (int j = 0; j < location.h; j += rez.getHeight()) {
        double x = i + location.x;
        double y = j + location.y;
        if (clip.intersects(x, y, rez.getWidth(), rez.getHeight())) {
          rez.render(g, x, y);
        }
      }
    }
  }

  public Json toJson() {
    return Json.object()
        .with("sprite", rez.getId())
        .with("loc", location.serialize());
  }

  public List<ObjectHandle> getHandles(){
    return ImmutableList.of();
  }


  public static MapObject load(Json json, Armory armory) {
    Resource rez = armory.getResource(json.getInt("sprite"));
    return rez.loadObject(json);
  }

}
