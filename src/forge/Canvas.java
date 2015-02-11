package forge;

import static forge.Forge.TILE_SIZE;
import jasonlib.Log;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import jasonlib.swing.component.GCanvas;
import java.awt.Color;
import java.util.concurrent.TimeUnit;
import armory.Sprite;
import com.google.common.base.Stopwatch;
import forge.ToolPanel.Tool;

public class Canvas extends GCanvas {

  public final Forge forge;
  public final MapData data;

  public int panX, panY;

  public Rect hoverLoc;
  public MapObject hoverObject, selectedObject;

  public boolean showGrid = false;

  public Canvas(Forge forge) {
    this.forge = forge;
    this.data = new MapData(forge);
  }

  @Override
  protected void render(Graphics3D g) {
    Stopwatch watch = Stopwatch.createStarted();

    if (showGrid) {
      drawGrid(g, Color.DARK_GRAY, TILE_SIZE);
      drawGrid(g, Color.GRAY, TILE_SIZE * 2);
    }

    Rect clip = new Rect(panX, panY, getWidth(), getHeight());

    g.translate(-panX, -panY);

    for (MapObject o : data.objects) {
      if (hoverObject instanceof Autotile) {
        g.alpha(o == hoverObject ? 1 : .6);
      }
      o.render(g, clip);

      if (forge.toolPanel.tool == Tool.CURSOR) {
        if (o == selectedObject) {
          g.color(Color.white).setStroke(3).draw(o.getBounds());
        } else if (o == hoverObject) {
          g.color(255, 255, 255, 100).setStroke(3).draw(o.getBounds());
        }
      }
    }

    if (hoverLoc != null) {
      Sprite s = forge.toolPanel.getSprite();
      if (s.autotile) {
        g.draw(s.subimage, hoverLoc.x - 8, hoverLoc.y - 8, 0, 0, 32, 32);
      } else {
        s.render(g, hoverLoc.x, hoverLoc.y);
      }
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
