package armory;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import com.google.common.collect.Lists;
import forge.ui.Forge;

public class Sprite {

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

  private int offset = (int) (Math.random() * 10000); // make it so the animations don't look sync'd

  public Sprite(int id, BufferedImage bi, Rect bounds) {
    this.id = id;
    this.bi = bi;
    this.subimage = bi.getSubimage(bounds.x(), bounds.y(), bounds.w(), bounds.h());
    this.bounds = bounds;

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

  public void render(Graphics3D g, double x, double y) {
    render(g, x, y, false);
  }

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
      long time = System.currentTimeMillis() + offset;
      int frameIndex = (int) ((time / (1000 / animationSpeed)) % animationFrames.size());
      if (!Forge.enableAnimations) {
        frameIndex = 0;
      }
      return animationFrames.get(frameIndex);
    } else {
      return subimage;
    }
  }

  public int getWidth() {
    return getRenderSize().width;
  }

  public int getHeight() {
    return getRenderSize().height;
  }

  public boolean isAnimated() {
    return !animationFrames.isEmpty();
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

    return ret;
  }

  public static Sprite load(Json json, BufferedImage bi) {
    int id = json.getInt("id");
    idCounter = Math.max(idCounter, id + 1);
    Sprite ret = new Sprite(id, bi, Rect.parse(json.get("bounds")));

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
