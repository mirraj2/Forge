package forge.ui;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.OS;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import armory.Armory;
import armory.Sprite;
import com.google.common.collect.Lists;

public class ToolPanel extends JComponent {

  private static final File file = new File(OS.getAppFolder("forge"), "tools.json");

  private final Forge forge;
  private final Armory armory;

  public Tool tool;

  private Sprite[] sprites = new Sprite[9];
  private int spriteIndex = 0;

  private List<Runnable> listeners = Lists.newArrayList();

  public ToolPanel(Forge forge) {
    this.forge = forge;
    this.armory = forge.armory;

    load();

    setTool(Tool.CURSOR);
  }

  public void setSelectedSprite(int spriteIndex) {
    this.spriteIndex = spriteIndex;
    setTool(Tool.BRUSH);

    notifyListeners();
  }

  public Sprite getSprite() {
    return sprites[spriteIndex];
  }

  public void equip(Sprite o) {
    sprites[spriteIndex] = o;
  }

  public void save() {
    Json json = Json.array(Arrays.asList(sprites), tool -> tool == null ? -1 : tool.id);
    IO.from(json).to(file);
  }

  private void load() {
    if (file.exists()) {
      int index = 0;
      for (int id : IO.from(file).toJson().asIntArray()) {
        if (id >= 0) {
          sprites[index] = armory.resourcesPanel.get(id);
        }
        index++;
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    Rect r = getDrawBounds();
    if (r.contains(e.getX(), e.getY())) {
      e.consume();
      setSelectedSprite((int) ((e.getX() - r.x) / (r.w / 9)));

      if (e.getButton() == MouseEvent.BUTTON1) {
        if (e.getClickCount() == 2) {
          armory.tabs.setSelectedIndex(0);
          armory.setVisible(true);
        }
      } else if (e.getButton() == MouseEvent.BUTTON3) {
        equip(null);
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
    Rect r = getDrawBounds();
    spriteIndex = (int) ((e.getX() - r.x) / (r.w / 9));
    spriteIndex = Math.max(spriteIndex, 0);
    spriteIndex = Math.min(spriteIndex, 8);
  }

  public Rect getDrawBounds() {
    double height = Math.min(getHeight(), 72);
    double idealWidth = height * 9 + 26;
    return new Rect((getWidth() - idealWidth) / 2, getHeight() - height, idealWidth, height);
  }

  private void renderIcon(Sprite tool, Rect r, Graphics3D g) {
    BufferedImage bi = tool.getFrame();
    if (tool.autotile) {
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
  protected void paintComponent(Graphics gg) {
    Graphics3D g = Graphics3D.create(gg);

    Rect r = getDrawBounds();

    g.color(0, 0, 0, 100).fill(r);
    g.setStroke(5.0).color(Color.DARK_GRAY).draw(r.grow(-2, -2));

    double widthPerBox = r.w / 9;
    g.setStroke(5.0);

    for (int i = 1; i < 9; i++) {
      double x = r.x + i * widthPerBox;
      g.line(x, r.y + 4, x, getHeight());
    }

    for (int i = 0; i < sprites.length; i++) {
      Sprite tool = sprites[i];
      if (tool == null) {
        continue;
      }
      double size = Math.min(64, widthPerBox - 10);
      Rect toolBounds = new Rect(r.x + i * widthPerBox + 4, r.y + 4, size, size);
      renderIcon(tool, toolBounds, g);
    }

    g.color(Color.lightGray).draw(new Rect(r.x + spriteIndex * widthPerBox, r.y, widthPerBox, r.h).grow(-2, -2));
  }

  public void onChange(Runnable callback) {
    listeners.add(callback);
  }

  public void switchTool() {
    Tool[] values = Tool.values();
    setTool(values[(tool.ordinal() + 1) % values.length]);
  }

  public void setTool(Tool tool) {
    this.tool = tool;
    forge.canvas.setCursor(Cursor.getPredefinedCursor(tool.cursor));
    notifyListeners();
  }

  private void notifyListeners() {
    for (Runnable r : listeners) {
      r.run();
    }
  }

  public static enum Tool {
    CURSOR(Cursor.DEFAULT_CURSOR), BRUSH(Cursor.HAND_CURSOR);

    public final int cursor;

    private Tool(int cursor) {
      this.cursor = cursor;
    }
  }

}
