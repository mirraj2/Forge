package forge;

import java.util.List;
import java.util.function.Predicate;
import armory.Sprite;
import com.google.common.collect.Lists;

public class MapData {

  public final List<MapObject> objects = Lists.newArrayList();

  public void add(MapObject o) {
    if (objects.contains(o)) {
      return;
    }
    objects.add(o);
  }

  public void remove(MapObject o) {
    objects.remove(o);
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

}
