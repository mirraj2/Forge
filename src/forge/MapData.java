package forge;

import jasonlib.Rect;
import java.util.List;
import armory.Sprite;
import com.google.common.collect.Lists;

public class MapData {

  public final List<MapObject> objects = Lists.newArrayList();

  public void add(Sprite s, Rect r) {
    objects.add(new MapObject(s, r));
  }

  public static class MapObject {
    public final Sprite sprite;
    public final Rect location;

    public MapObject(Sprite sprite, Rect location) {
      this.sprite = sprite;
      this.location = location;
    }
  }

}
