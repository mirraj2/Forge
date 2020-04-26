package forge.ui;

import static forge.ui.Forge.TILE_SIZE;

import java.awt.Color;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import armory.ImagePanel;
import armory.rez.Resource;
import armory.rez.Sprite;
import forge.map.ObjectHandle;
import forge.map.Region;
import forge.map.object.MapObject;
import forge.ui.ToolPanel.Tool;
import ox.Log;
import ox.Rect;
import swing.Graphics3D;
import swing.component.GCanvas;

public class Canvas extends GCanvas {

  public final Forge forge;
  public Region region;

  public int panX, panY;

  public Rect hoverLoc, selectionBox;
  public MapObject hoverObject;
  public Set<MapObject> selectedObjects = Sets.newHashSet();

  public boolean showGrid = false;

  public Canvas(Forge forge) {
    this.forge = forge;
    setRegion(forge.world.regions.get(0));
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public void select(MapObject o) {
    selectedObjects.clear();
    if (o != null) {
      selectedObjects.add(o);
    }
  }

  @Override
  public void render(Graphics3D g) {
    render(g, new Rect(panX, panY, getWidth(), getHeight()), o -> true);
  }

  public void render(Graphics3D g, Rect clip, Predicate<MapObject> objectFilter) {
    Stopwatch watch = Stopwatch.createStarted();

    if (showGrid) {
      drawGrid(g, Color.DARK_GRAY, TILE_SIZE);
      drawGrid(g, Color.GRAY, TILE_SIZE * 2);
    }

    g.translate(-panX, -panY);

    for (MapObject o : region.objects) {
      if (!objectFilter.test(o)) {
        continue;
      }

      o.render(g, clip);

      if (forge.toolPanel.tool == Tool.CURSOR) {
        if (selectedObjects.contains(o)) {
          Rect r = o.getBounds();
          g.color(Color.white).setStroke(3).draw(r);
          for (ObjectHandle handle : o.getHandles()) {
            g.setStroke(2).color(Color.white).line(r.centerX(), r.maxY(), r.centerX(), handle.location.centerY());
            g.setStroke(1);
            g.color(handle.color).fillOval(handle.location);
            g.color(Color.white).drawOval(handle.location);
          }
        } else if (o == hoverObject) {
          g.color(255, 255, 255, 100).setStroke(3).draw(o.getBounds());
        }
      }
    }

    if (hoverLoc != null) {
      Resource rez = forge.toolPanel.getResource();
      if(rez.isAutotile()){
        g.draw(((Sprite) rez).subimage, hoverLoc.x - 8, hoverLoc.y - 8, 0, 0, 32, 32);
      } else{
        rez.render(g, hoverLoc.x, hoverLoc.y);
      }
    }

    if (selectionBox != null) {
      g.color(ImagePanel.SELECTION_COLOR).fill(selectionBox);
      g.setStroke(2.0).color(Color.white).draw(selectionBox);
    }

    if (watch.elapsed(TimeUnit.MILLISECONDS) > 30) {
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
