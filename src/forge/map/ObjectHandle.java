package forge.map;

import java.awt.Color;
import java.util.function.BiConsumer;

import ox.Rect;

/**
 * A handle, attached to some object, that the user can drag.
 */
public class ObjectHandle {

  public Color color = Color.white;
  public Rect location;
  public BiConsumer<Integer, Integer> onDrag = (x, y) -> {
  };

  public ObjectHandle color(Color color) {
    this.color = color;
    return this;
  }

  public ObjectHandle loc(Rect location) {
    this.location = location;
    return this;
  }

  public ObjectHandle onDrag(BiConsumer<Integer, Integer> onDrag) {
    this.onDrag = onDrag;
    return this;
  }

  public void drag(int mouseX, int mouseY) {
    onDrag.accept(mouseX, mouseY);
  }

}
