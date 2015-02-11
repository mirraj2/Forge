package forge.input;

import static forge.Forge.TILE_SIZE;
import jasonlib.Rect;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import armory.Sprite;
import forge.Autotile;
import forge.Canvas;
import forge.Forge;
import forge.MapObject;
import forge.ToolPanel;
import forge.ToolPanel.Tool;

public class MouseHandler {

  private final Forge forge;
  private final ToolPanel toolPanel;
  private final Canvas canvas;

  private int mouseX, mouseY;
  private int offsetX, offsetY;
  private boolean mouseInside;

  private Rect startingLoc;

  public MouseHandler(Forge forge) {
    this.forge = forge;
    this.toolPanel = forge.toolPanel;
    this.canvas = forge.canvas;

    listen();
  }

  private void mousePress() {
    if (toolPanel.tool == Tool.BRUSH) {
      if (canvas.hoverLoc != null) {
        Sprite sprite = toolPanel.getSprite();
        if (sprite.autotile) {
          int x = canvas.hoverLoc.x(), y = canvas.hoverLoc.y();
          canvas.selectedObject = canvas.data.getAutotile(x, y, sprite);
          if (canvas.selectedObject == null) {
            canvas.selectedObject = new Autotile(sprite, x / TILE_SIZE, y / TILE_SIZE);
          }
          canvas.hoverLoc = null;
        } else {
          startingLoc = canvas.hoverLoc;
          canvas.hoverLoc = null;
          canvas.selectedObject = new MapObject(sprite, startingLoc);
        }
        canvas.data.add(canvas.selectedObject);
      }
    } else if (toolPanel.tool == Tool.CURSOR) {
      canvas.selectedObject = canvas.data.getObjectAt(mouseX, mouseY);
      if (canvas.selectedObject != null) {
        Rect bounds = canvas.selectedObject.getBounds();
        offsetX = mouseX - bounds.x();
        offsetY = mouseY - bounds.y();
      }
    }
  }

  private void mouseMove() {
    computeBrushHover();

    if (toolPanel.tool == Tool.CURSOR) {
      canvas.hoverObject = canvas.data.getObjectAt(mouseX, mouseY);
    }
  }

  private void mouseDrag() {
    MapObject selected = canvas.selectedObject;
    if (toolPanel.tool == Tool.BRUSH) {
      if (selected != null) {
        if (selected instanceof Autotile) {
          ((Autotile) selected).addAutotile(mouseX / TILE_SIZE, mouseY / TILE_SIZE);
        } else {
          Rect r = startingLoc;
          double x1 = r.x, y1 = r.y, x2 = r.maxX(), y2 = r.maxY();
          while (mouseX < x1) {
            x1 -= r.w;
          }
          while (mouseY < y1) {
            y1 -= r.h;
          }
          while (mouseX > x2) {
            x2 += r.w;
          }
          while (mouseY > y2) {
            y2 += r.h;
          }
          selected.location = new Rect(x1, y1, x2 - x1, y2 - y1);
        }
      }
    } else if (toolPanel.tool == Tool.CURSOR) {
      if (selected != null) {
        selected.moveTo(round(mouseX - offsetX), round(mouseY - offsetY));
      }
    }
  }

  private void computeBrushHover() {
    canvas.hoverLoc = null;

    if (toolPanel.tool == Tool.BRUSH) {
      Sprite sprite = toolPanel.getSprite();
      if (sprite != null && mouseInside) {
        if (sprite.autotile) {
          canvas.hoverLoc = new Rect(round(mouseX), round(mouseY), 16, 16);
        } else {
          int w = sprite.getWidth(), h = sprite.getHeight();
          canvas.hoverLoc = new Rect(round(mouseX - w / 2), round(mouseY - h / 2), w, h);
        }
      }
    }
  }

  public static int round(int n) {
    int ret = n / TILE_SIZE * TILE_SIZE;
    if (n < 0) {
      ret -= TILE_SIZE;
    }
    return ret;
  }

  private final MouseAdapter mouseListener = new MouseAdapter() {
    boolean inToolPanel = false;

    @Override
    public void mousePressed(MouseEvent e) {
      MouseEvent e2 = SwingUtilities.convertMouseEvent(forge, e, toolPanel);
      toolPanel.mousePressed(e2);
      inToolPanel = e2.isConsumed();
      if (inToolPanel) {
        return;
      }

      mouseX = e.getX() + canvas.panX;
      mouseY = e.getY() + canvas.panY;
      mousePress();
    };

    @Override
    public void mouseDragged(MouseEvent e) {
      if (inToolPanel) {
        e = SwingUtilities.convertMouseEvent(forge, e, toolPanel);
        toolPanel.mouseDragged(e);
        return;
      }

      mouseX = e.getX() + canvas.panX;
      mouseY = e.getY() + canvas.panY;
      mouseDrag();
    };

    @Override
    public void mouseMoved(MouseEvent e) {
      mouseX = e.getX() + canvas.panX;
      mouseY = e.getY() + canvas.panY;

      if (toolPanel.getDrawBounds().contains(mouseX, mouseY - toolPanel.getY())) {
        mouseInside = false;
      } else {
        mouseInside = true;
      }

      mouseMove();
    };

    @Override
    public void mouseReleased(MouseEvent e) {
      // canvas.selectedObject = null;
      startingLoc = null;
    };

    @Override
    public void mouseEntered(MouseEvent e) {
      mouseInside = true;
    };

    @Override
    public void mouseExited(MouseEvent e) {
      mouseInside = false;
      computeBrushHover();
    };
  };

  private void listen() {
    forge.addMouseListener(mouseListener);
    forge.addMouseMotionListener(mouseListener);

    toolPanel.onChange(() -> {
      computeBrushHover();
      canvas.hoverObject = null;
    });
  }

}
