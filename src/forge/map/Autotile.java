package forge.map;

import static forge.ui.Forge.TILE_SIZE;
import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.function.BiConsumer;
import armory.Sprite;
import com.google.common.collect.ImmutableList;
import forge.input.MouseHandler;
import forge.ui.Forge;

public class Autotile extends MapObject {

  public TileGrid grid;
  private final boolean isCliff;
  private int cliffHeight = 0;

  public Autotile(Sprite sprite, int startX, int startY) {
    this(sprite, new TileGrid());

    addAutotile(startX, startY);
  }

  private Autotile(Sprite sprite, TileGrid grid) {
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
    Rect r = grid.bounds;
    r = new Rect(r.x * TILE_SIZE, r.y * TILE_SIZE, (r.w + 1) * TILE_SIZE, (r.h + 1) * TILE_SIZE);
    r = r.grow(TILE_SIZE, TILE_SIZE);
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
          g.draw(sprite.getFrame(), x, y, p.x, p.y, 16, 16);
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
      Rect r = getBounds();
      int size = 10;
      ObjectHandle handle = new ObjectHandle().color(Color.green)
          .loc(new Rect(r.centerX() - size / 2, r.maxY() + 20 + cliffHeight * Forge.TILE_SIZE, size, size))
          .onDrag(onDrag);
      return ImmutableList.of(handle);
    }
    return super.getHandles();
  }

  private final BiConsumer<Integer, Integer> onDrag = (x, y) -> {
    Rect r = getBounds();
    cliffHeight = (int) ((y - (r.maxY() + 20)) / Forge.TILE_SIZE);
    cliffHeight = Math.max(cliffHeight, 0);
  };

  @Override
  public Json toJson() {
    Json ret = Json.object()
        .with("sprite", sprite.id)
        .with("grid", grid.serialize());
    if (cliffHeight > 0) {
      ret.with("cliff_height", cliffHeight);
    }
    return ret;
  }

  public static Autotile load(Json json, Forge forge) {
    Sprite sprite = forge.armory.getSprite(json.getInt("sprite"));
    TileGrid grid = TileGrid.parse(json.get("grid"));
    Autotile ret = new Autotile(sprite, grid);
    if (json.has("cliff_height")) {
      ret.cliffHeight = json.getInt("cliff_height");
    }
    return ret;
  }

}
