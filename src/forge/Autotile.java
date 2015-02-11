package forge;

import static forge.Forge.TILE_SIZE;
import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Point;
import armory.Sprite;
import forge.input.MouseHandler;

public class Autotile extends MapObject {

  public TileGrid grid;

  public Autotile(Sprite sprite, int startX, int startY) {
    this(sprite, new TileGrid());

    addAutotile(startX, startY);
  }

  private Autotile(Sprite sprite, TileGrid grid) {
    super(sprite, null);

    this.grid = grid;
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
        Point p = Autotiles.getAutotileDetails(i, j, grid);
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
  public Json toJson() {
    return Json.object()
        .with("sprite", sprite.id)
        .with("grid", grid.serialize());
  }

  public static Autotile load(Json json, Forge forge) {
    Sprite sprite = forge.armory.getSprite(json.getInt("sprite"));
    TileGrid grid = TileGrid.parse(json.get("grid"));
    return new Autotile(sprite, grid);
  }

}
