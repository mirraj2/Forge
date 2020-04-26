package armory.rez;

import static forge.ui.Forge.TILE_SIZE;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

import armory.ImagePanel;
import forge.BitSet;
import forge.map.object.Autotile;
import forge.map.object.MapObject;
import forge.ui.Forge;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

public class Sprite implements Resource {

  public static int idCounter = 0;

  public final int id;
  public final BufferedImage bi, subimage;
  public final Rect bounds;

  // User options
  public String tags = "";
  public boolean autotile;
  public int animationRows = 1, animationCols = 1;
  public int animationSpeed = 5; // frames per second

  private final List<BufferedImage> animationFrames = Lists.newArrayList();
  private int animationOffset = (int) (Math.random() * 10000); // make it so the animations don't look sync'd

  private final BitSet collisionData;

  // private final long[] collisionData; // each long holds 64 booleans

  public Sprite(int id, BufferedImage bi, Rect bounds) {
    this(id, bi, bounds, null);
  }

  public Sprite(int id, BufferedImage bi, Rect bounds, BitSet collisionData) {
    this.id = id;
    this.bi = bi;
    this.subimage = bi.getSubimage(bounds.x(), bounds.y(), bounds.w(), bounds.h());
    this.bounds = bounds;
    
    if(collisionData == null){
      this.collisionData = new BitSet((bounds.w() / TILE_SIZE) * (bounds.h() / TILE_SIZE));
    } else {
      this.collisionData = collisionData;
    }
    
    if (bounds.h() == 96) {
      if (bounds.w() == 64) {
        autotile = true;
      } else if (bounds.w() == 192) {
        autotile = true;
        animate(1, 3);
      }
    }
  }

  public void animate(int rows, int cols) {
    rows = Math.max(rows, 1);
    cols = Math.max(cols, 1);

    this.animationRows = rows;
    this.animationCols = cols;

    animationFrames.clear();
    if (rows > 1 || cols > 1) {
      int w = bounds.w() / cols;
      int h = bounds.h() / rows;
      for (int j = 0; j < rows; j++) {
        for (int i = 0; i < cols; i++) {
          animationFrames.add(subimage.getSubimage(i * w, j * h, w, h));
        }
      }
    }
  }

  @Override
  public void render(Graphics3D g, double x, double y, boolean freezeAnimation) {
    if (isAnimated()) {
      if (!Forge.enableAnimations) {
        freezeAnimation = true;
      }
      BufferedImage frame = freezeAnimation ? animationFrames.get(0) : getFrame();
      g.draw(frame, x, y);
    } else {
      g.draw(subimage, x, y);
    }

    if (Forge.collisionMode) {
      int w = bounds.w() / TILE_SIZE;
      g.color(ImagePanel.COLLISION_COLOR);
      for (int i = 0; i < collisionData.size(); i++) {
        if (collisionData.get(i)) {
          int xLoc = (i % w) * TILE_SIZE;
          int yLoc = (i / w) * TILE_SIZE;
          g.fillRect(x + xLoc, y + yLoc, TILE_SIZE, TILE_SIZE);
        }
      }
    }
  }

  public Dimension getRenderSize() {
    if (isAnimated()) {
      BufferedImage frame = animationFrames.get(0);
      return new Dimension(frame.getWidth(), frame.getHeight());
    }
    return new Dimension(bounds.w(), bounds.h());
  }

  public BufferedImage getFrame() {
    if (isAnimated()) {
      return animationFrames.get(getFrameIndex());
    } else {
      return subimage;
    }
  }

  private int getFrameIndex() {
    if (!Forge.enableAnimations) {
      return 0;
    }
    long time = (System.currentTimeMillis() & 0xFFFFFF) + animationOffset;
    int n = animationFrames.size();
    int i = (int) ((time / (1000 / animationSpeed)));

    if (n <= 2) {
      return i % n;
    }

    // loop the frames
    i %= n + n - 2;

    if (i >= n) {
      i = i - ((i % n) + 2);
    }

    return i;
  }

  @Override
  public int getWidth() {
    return getRenderSize().width;
  }

  @Override
  public int getHeight() {
    return getRenderSize().height;
  }

  public boolean isAnimated() {
    return !animationFrames.isEmpty();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public boolean isAutotile() {
    return autotile;
  }

  @Override
  public boolean isCollision(double x, double y) {
    int index = getIndex(x, y);
    return collisionData.get(index);
  }

  @Override
  public void setCollision(double x, double y, boolean b) {
    int index = getIndex(x, y);
    collisionData.set(index, b);
  }

  private int getIndex(double x, double y) {
    return ((int) y / TILE_SIZE) * ((int) bounds.w / TILE_SIZE) + ((int) x / TILE_SIZE);
  }

  @Override
  public void renderIcon(Rect r, Graphics3D g) {
    BufferedImage bi = getFrame();
    if (autotile) {
      g.draw(bi, r.x, r.y, r.maxX(), r.maxY(), 0, 32, 64, 96);
    } else {
      double ratio = 1.0 * bi.getWidth() / bi.getHeight();
      if (ratio >= 1) {
        // scale until the width fits
        double offset = (r.h - r.h / ratio) / 2;
        g.draw(bi, r.x, r.y + offset, r.maxX(), r.y + r.h / ratio + offset, 0, 0, bi.getWidth(), bi.getHeight());
      } else {
        // scale until the height fits
        double offset = (r.w - r.w * ratio) / 2;
        g.draw(bi, r.x + offset, r.y, r.x + r.w * ratio + offset, r.maxY(), 0, 0, bi.getWidth(), bi.getHeight());
      }
    }
  }

  @Override
  public MapObject loadObject(Json json) {
    if (isAutotile()) {
      return Autotile.load(json, this);
    }
    return new MapObject(this, Rect.parse(json.get("loc")));
  }

  public boolean hasAnyCollisions() {
    for (long word : collisionData.words) {
      if (word != 0) {
        return true;
      }
    }
    return false;
  }

  public Json toJson() {
    Json ret = Json.object()
        .with("id", id)
        .with("bounds", bounds.serialize())
        .with("tags", tags)
        .with("autotile", autotile);

    if (isAnimated()) {
      ret.with("animation", Json.object()
          .with("rows", animationRows)
          .with("cols", animationCols)
          .with("speed", animationSpeed));
    }

    if (hasAnyCollisions()) {
      ret.with("collisions", Json.array(Longs.asList(collisionData.words)));
    }

    return ret;
  }

  public static Sprite load(Json json, BufferedImage bi) {
    int id = json.getInt("id");
    idCounter = Math.max(idCounter, id + 1);

    BitSet collisionData = null;
    if (json.has("collisions")) {
      collisionData = new BitSet(json.getJson("collisions").asLongArray());
    }

    Sprite ret = new Sprite(id, bi, Rect.parse(json.get("bounds")), collisionData);

    ret.tags = json.get("tags");
    ret.autotile = json.getBoolean("autotile");

    if (json.has("animation")) {
      Json animation = json.getJson("animation");
      ret.animate(animation.getInt("rows"), animation.getInt("cols"));
      ret.animationSpeed = animation.getInt("speed");
    }


    return ret;
  }

}
