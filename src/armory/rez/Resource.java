package armory.rez;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import forge.map.object.MapObject;

public interface Resource {

  public int getId();

  public int getWidth();

  public int getHeight();

  public void renderIcon(Rect r, Graphics3D g);

  public default void render(Graphics3D g, double x, double y) {
    render(g, x, y, false);
  }

  public void render(Graphics3D g, double x, double y, boolean freezeAnimation);

  public default boolean isAutotile() {
    return false;
  }

  public MapObject loadObject(Json json);
  
  public default boolean isCollision(double x, double y) {
    return false;
  }
  
  public default void setCollision(double x, double y, boolean b) {
  }

}
