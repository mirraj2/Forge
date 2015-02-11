package forge;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import armory.Sprite;

public class MapObject {

  public final Sprite sprite;

  public Rect location; // for regular map objects

  public MapObject(Sprite sprite, Rect location) {
    this.sprite = sprite;
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
    for (int i = 0; i < location.w; i += sprite.getWidth()) {
      for (int j = 0; j < location.h; j += sprite.getHeight()) {
        sprite.render(g, i + location.x, j + location.y);
      }
    }
  }

  public Json toJson() {
    return Json.object()
        .with("sprite", sprite.id)
        .with("loc", location.serialize());
  }

  public static MapObject load(Json json, Forge forge) {
    if (json.has("grid")) {
      return Autotile.load(json, forge);
    }
    Sprite sprite = forge.armory.getSprite(json.getInt("sprite"));
    return new MapObject(sprite, Rect.parse(json.get("loc")));
  }

}
