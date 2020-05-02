package forge.map.object;

import static forge.ui.Forge.TILE_SIZE;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;

import armory.ImagePanel;
import armory.rez.ImageResource;
import armory.rez.Resource;
import forge.input.MouseHandler;
import forge.map.Autotiles;
import forge.map.ObjectHandle;
import forge.map.TileGrid;
import forge.ui.Forge;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

public class Autotile extends MapObject {

  public TileGrid grid;
  private final boolean isCliff;
  private int cliffHeight = 0;

  public Autotile(ImageResource sprite, int startX, int startY) {
    this(sprite, new TileGrid());

    addAutotile(startX, startY);
  }

  private Autotile(ImageResource sprite, TileGrid grid) {
    super(sprite, null);

    this.grid = grid;

    isCliff = sprite.getHeight() > 96;
  }

  @Override
  public void moveTo(int x, int y) {
    int dx = x / TILE_SIZE - grid.bounds.x();
    int dy = y / TILE_SIZE - grid.bounds.y();
    grid.translate(dx, dy);
  }

  @Override
  public Rect getBounds() {
    return getBounds(true);
  }

  public Rect getBounds(boolean includeCliff) {
    Rect r = grid.bounds;
    r = new Rect(r.x * TILE_SIZE, r.y * TILE_SIZE, (r.w + 1) * TILE_SIZE, (r.h + 1) * TILE_SIZE);
    r = r.grow(TILE_SIZE, TILE_SIZE);
    if (includeCliff) {
      r = r.changeSize(0, cliffHeight * TILE_SIZE);
    }
    return r;
  }

  @Override
  public boolean isHit(int x, int y) {
    x = MouseHandler.round(x) / TILE_SIZE;
    y = MouseHandler.round(y) / TILE_SIZE;
    // // check the tiles surrounding the target tile because an autotile is rendered
    // // with an extra 1-tile border
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        if (grid.get(i + x, j + y)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public void render(Graphics3D g, Rect clip) {
    for (double x = clip.x; x < clip.maxX(); x += TILE_SIZE) {
      for (double y = clip.y; y < clip.maxY(); y += TILE_SIZE) {
        int i = (int) x / TILE_SIZE;
        int j = (int) y / TILE_SIZE;
        Point p = Autotiles.compute(i, j, grid, cliffHeight);
        if (p != Autotiles.EMPTY) {
          ImageResource sprite = (ImageResource) rez;
          g.draw(sprite.getFrame(), x, y, p.x, p.y, TILE_SIZE, TILE_SIZE);
          if (Forge.collisionMode && sprite.isCollision(p.x, p.y)) {
            g.color(ImagePanel.COLLISION_COLOR).fillRect(x, y, TILE_SIZE, TILE_SIZE);
          }
        }
      }
    }
  }

  public void addAutotile(int i, int j) {
    grid.add(i, j);
  }

  @Override
  public List<ObjectHandle> getHandles() {
    if (isCliff) {
      Rect r = getBounds(false);
      int size = 20;
      ObjectHandle handle = new ObjectHandle().color(Color.green)
          .loc(new Rect(r.centerX() - size / 2, r.maxY() + 20 + cliffHeight * Forge.TILE_SIZE, size, size))
          .onDrag(onDrag);
      return ImmutableList.of(handle);
    }
    return super.getHandles();
  }

  private final BiConsumer<Integer, Integer> onDrag = (x, y) -> {
    Rect r = getBounds(false);
    cliffHeight = (int) ((y - (r.maxY() + 20)) / Forge.TILE_SIZE);
    cliffHeight = Math.max(cliffHeight, 0);
  };

  @Override
  public Json toJson() {
    Json ret = Json.object()
        .with("imageId", rez.getId())
        .with("grid", grid.serialize());
    if (cliffHeight > 0) {
      ret.with("cliff_height", cliffHeight);
    }
    return ret;
  }

  public static Autotile load(Json json, Resource rez) {
    TileGrid grid = TileGrid.parse(json.get("grid"));
    Autotile ret = new Autotile((ImageResource) rez, grid);
    if (json.has("cliff_height")) {
      ret.cliffHeight = json.getInt("cliff_height");
    }
    return ret;
  }

}
