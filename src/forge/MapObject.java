package forge;

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

}
