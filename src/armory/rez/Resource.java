package armory.rez;

import forge.map.object.MapObject;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

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
