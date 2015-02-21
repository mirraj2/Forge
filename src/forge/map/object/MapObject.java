package forge.map.object;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.util.List;
import armory.Armory;
import armory.rez.Resource;
import com.google.common.collect.ImmutableList;
import forge.map.ObjectHandle;

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
        rez.render(g, i + location.x, j + location.y);
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
