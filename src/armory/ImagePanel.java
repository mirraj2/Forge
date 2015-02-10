package armory;

import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import jasonlib.swing.component.GPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import com.google.common.collect.Lists;
import forge.Forge;

public class ImagePanel extends GPanel {

  public static final Color SELECTION_COLOR = new Color(100, 100, 255, 100);
  public static final Color MAPPED_COLOR = new Color(100, 255, 100, 100);

  private final TilesetPanel parent;
  public final BufferedImage bi;
  public Rect selection = null;
  private ObjectDetailsPanel objectPanel;

  public ImagePanel(TilesetPanel parent, BufferedImage bi, ObjectDetailsPanel objectPanel) {
    this.parent = parent;
    this.bi = bi;
    this.objectPanel = objectPanel;

    setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));

    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
  }

  @Override
  protected void paintComponent(Graphics gg) {
    Graphics3D g = Graphics3D.create(gg);

    int size = 8;
    for (int x = 0; x < getWidth(); x += size) {
      for (int y = 0; y < getHeight(); y += size) {
        g.color(((x / size) + (y / size)) % 2 == 0 ? Color.lightGray : Color.white);
        g.fillRect(x, y, size, size);
      }
    }

    g.draw(bi, 0, 0);

    g.color(100, 100, 100, 150);
    for (int x = Forge.TILE_SIZE; x < getWidth(); x += Forge.TILE_SIZE) {
      g.line(x, 0, x, getHeight());
    }
    for (int y = Forge.TILE_SIZE; y < getHeight(); y += Forge.TILE_SIZE) {
      g.line(0, y, getHeight(), y);
    }

    if (selection != null) {
      g.color(SELECTION_COLOR).fill(selection);
      g.color(Color.white).draw(selection);
    }
    for (MapObject o : parent.mapObjects) {
      g.color(MAPPED_COLOR).fill(o.bounds);
      g.color(Color.white).draw(o.bounds);
    }
  }

  private final MouseAdapter mouseListener = new MouseAdapter() {
    int pressX, pressY, mouseX, mouseY;
    MapObject pressedObject;

    @Override
    public void mousePressed(MouseEvent e) {
      mouseX = pressX = e.getX();
      mouseY = pressY = e.getY();

      pressedObject = getObjectAt(pressX, pressY);
      if (pressedObject == null) {
        computeSelection();
      } else {
        selection = pressedObject.bounds;
        objectPanel.load(pressedObject);
      }
    };

    @Override
    public void mouseDragged(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();

      if (pressedObject == null) {
        computeSelection();
      }
    };

    @Override
    public void mouseReleased(MouseEvent e) {
      if (pressedObject == null) {
        parent.onSelection(selection);
      }
    };

    private void computeSelection() {
      Rect r = getSelectionArea(pressX, pressY, mouseX, mouseY);
      selection = r.constrain(0, 0, bi.getWidth(), bi.getHeight());
    }

    private MapObject getObjectAt(int x, int y) {
      for (MapObject o : Lists.reverse(parent.mapObjects)) {
        if (o.bounds.contains(x, y)) {
          return o;
        }
      }
      return null;
    }
  };

  public static Rect getSelectionArea(int pressX, int pressY, int mouseX, int mouseY) {
    int x = snap(Math.min(pressX, mouseX));
    int y = snap(Math.min(pressY, mouseY));
    int x2 = snapForward(Math.max(pressX, mouseX));
    int y2 = snapForward(Math.max(pressY, mouseY));

    return new Rect(x, y, x2 - x, y2 - y);
  }

  private static int snapForward(int n) {
    return snap(n) + Forge.TILE_SIZE;
  }

  private static int snap(int n) {
    int ret = n / Forge.TILE_SIZE * Forge.TILE_SIZE;
    if (n < 0) {
      ret -= Forge.TILE_SIZE;
    }
    return ret;
  }

}
