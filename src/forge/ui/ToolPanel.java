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
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import armory.Armory;
import armory.rez.Resource;
import com.google.common.collect.Lists;

public class ToolPanel extends JComponent {

  private static final File file = new File(OS.getAppFolder("forge"), "tools.json");
  private static final int NULL_ID = -9;

  private final Forge forge;
  private final Armory armory;

  public Tool tool;

  private Resource[] resources = new Resource[9];
  private int resourceIndex = 0;

  private List<Runnable> listeners = Lists.newArrayList();

  public ToolPanel(Forge forge) {
    this.forge = forge;
    this.armory = forge.armory;

    load();

    setTool(Tool.CURSOR);
  }

  public void setSelectedResource(int resourceIndex) {
    this.resourceIndex = resourceIndex;
    setTool(Tool.BRUSH);

    notifyListeners();
  }

  public Resource getResource() {
    return resources[resourceIndex];
  }

  public void equip(Resource o) {
    resources[resourceIndex] = o;
    setTool(Tool.BRUSH);
  }

  public void save() {
    Json json = Json.array(Arrays.asList(resources), tool -> tool == null ? NULL_ID : tool.getId());
    IO.from(json).to(file);
  }

  private void load() {
    if (file.exists()) {
      int index = 0;
      for (int id : IO.from(file).toJson().asIntArray()) {
        if (id != NULL_ID) {
          resources[index] = armory.resourcesPanel.get(id);
        }
        index++;
      }
    }
  }

  public void mousePressed(MouseEvent e) {
    Rect r = getDrawBounds();
    if (r.contains(e.getX(), e.getY())) {
      e.consume();
      setSelectedResource((int) ((e.getX() - r.x) / (r.w / 9)));

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
    resourceIndex = (int) ((e.getX() - r.x) / (r.w / 9));
    resourceIndex = Math.max(resourceIndex, 0);
    resourceIndex = Math.min(resourceIndex, 8);
  }

  public Rect getDrawBounds() {
    double height = Math.min(getHeight(), 72);
    double idealWidth = height * 9 + 26;
    return new Rect((getWidth() - idealWidth) / 2, getHeight() - height, idealWidth, height);
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

    for (int i = 0; i < resources.length; i++) {
      Resource tool = resources[i];
      if (tool == null) {
        continue;
      }
      double size = Math.min(64, widthPerBox - 10);
      Rect toolBounds = new Rect(r.x + i * widthPerBox + 4, r.y + 4, size, size);
      tool.renderIcon(toolBounds, g);
    }

    g.color(Color.lightGray).draw(new Rect(r.x + resourceIndex * widthPerBox, r.y, widthPerBox, r.h).grow(-2, -2));
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
