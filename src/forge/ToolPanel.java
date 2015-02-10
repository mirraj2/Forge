package forge;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.OS;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.swing.JComponent;
import armory.Armory;
import armory.MapObject;

public class ToolPanel extends JComponent {

  private static final File file = new File(OS.getAppFolder("forge"), "tools.json");

  private final Armory armory;

  private MapObject[] tools = new MapObject[9];
  private int toolIndex = 0;

  public ToolPanel(Armory armory) {
    this.armory = armory;

    load();
  }

  public void setSelectedTool(int toolIndex) {
    this.toolIndex = toolIndex;
  }

  public void equip(MapObject o) {
    tools[toolIndex] = o;
  }

  public void save() {
    Json json = Json.array(Arrays.asList(tools), tool -> tool == null ? -1 : tool.id);
    IO.from(json).to(file);
  }

  private void load() {
    if (file.exists()) {
      int index = 0;
      for (int id : IO.from(file).toJson().asIntArray()) {
        if (id >= 0) {
          tools[index] = armory.resourcesPanel.get(id);
        }
        index++;
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    Rect r = getDrawBounds();
    if (r.contains(e.getX(), e.getY())) {
      e.consume();
      toolIndex = (int) ((e.getX() - r.x) / (r.w / 9));

      if (e.getClickCount() == 2) {
        armory.tabs.setSelectedIndex(0);
        armory.setVisible(true);
      }
    }
  }

  public void mouseDragged(MouseEvent e) {
    Rect r = getDrawBounds();
    toolIndex = (int) ((e.getX() - r.x) / (r.w / 9));
    toolIndex = Math.max(toolIndex, 0);
    toolIndex = Math.min(toolIndex, 8);
  }

  private Rect getDrawBounds() {
    double height = Math.min(getHeight(), 68);
    double idealWidth = height * 9 + 26;
    return new Rect((getWidth() - idealWidth) / 2, getHeight() - height, idealWidth, height);
  }

  private void renderIcon(MapObject tool, Rect r, Graphics3D g) {
    BufferedImage bi = tool.subimage;
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

    for (int i = 0; i < tools.length; i++) {
      MapObject tool = tools[i];
      if (tool == null) {
        continue;
      }
      double size = Math.min(64, widthPerBox - 6);
      Rect toolBounds = new Rect(r.x + i * widthPerBox + 4, r.y + 4, size, size);
      renderIcon(tool, toolBounds, g);
    }

    g.color(Color.lightGray).draw(new Rect(r.x + toolIndex * widthPerBox, r.y, widthPerBox, r.h).grow(-2, -2));
  }

}
