package forge;

import jasonlib.Log;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import jasonlib.swing.component.GCanvas;
import java.awt.Color;
import java.util.concurrent.TimeUnit;
import armory.Sprite;
import com.google.common.base.Stopwatch;
import forge.MapData.MapObject;

public class Canvas extends GCanvas {

  public final Forge forge;
  public final MapData data = new MapData();

  public Rect hoverLoc;

  public Canvas(Forge forge) {
    this.forge = forge;
  }

  @Override
  protected void render(Graphics3D g) {
    Stopwatch watch = Stopwatch.createStarted();

    drawGrid(g, Color.DARK_GRAY, Forge.TILE_SIZE);
    drawGrid(g, Color.GRAY, Forge.TILE_SIZE * 2);

    for (MapObject o : data.objects) {
      o.sprite.render(g, o.location.x, o.location.y);
    }

    if (hoverLoc != null) {
      Sprite o = forge.toolPanel.getTool();
      o.render(g, hoverLoc.x, hoverLoc.y);
    }

    if (watch.elapsed(TimeUnit.MILLISECONDS) > 10) {
      Log.debug("rendered in " + watch);
    }
  }

  private void drawGrid(Graphics3D g, Color c, int size) {
    g.color(c);
    int w = getWidth(), h = getHeight();
    for (int x = size; x < w; x += size) {
      g.line(x, 0, x, h);
    }
    for (int y = size; y < h; y += size) {
      g.line(0, y, w, y);
    }
  }

}
