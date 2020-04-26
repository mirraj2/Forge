package forge.input;

import static forge.ui.Forge.TILE_SIZE;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import armory.rez.ListenerResource;
import armory.rez.Resource;
import armory.rez.Sprite;
import forge.map.ObjectHandle;
import forge.map.object.Autotile;
import forge.map.object.MapObject;
import forge.map.object.PortalObject;
import forge.ui.Canvas;
import forge.ui.Forge;
import forge.ui.ToolPanel;
import forge.ui.ToolPanel.Tool;
import ox.Rect;
import swing.global.GKeyboard;

public class MouseHandler {

  private final Forge forge;
  private final ToolPanel toolPanel;
  private final Canvas canvas;

  private int clickCount;
  private int pressX, pressY, mouseX, mouseY;
  private int offsetX, offsetY;
  private boolean mouseInside, dragTriggered;

  private Rect startingLoc;

  private ObjectHandle clickedHandle;

  public MouseHandler(Forge forge) {
    this.forge = forge;
    this.toolPanel = forge.toolPanel;
    this.canvas = forge.canvas;

    listen();
  }

  private void mousePress() {
    if (toolPanel.tool == Tool.BRUSH) {
      brushPress();
    } else if (toolPanel.tool == Tool.CURSOR) {
      cursorPress();
    }
  }
  
  private void brushPress(){
    Resource rez = toolPanel.getResource();
    if (rez instanceof ListenerResource) {
      ((ListenerResource) rez).mousePress(mouseX, mouseY);
      return;
    }
    if (canvas.hoverLoc == null) {
      return;
    }
    MapObject selected;
    if (rez.isAutotile()) {
      int x = canvas.hoverLoc.x(), y = canvas.hoverLoc.y();
      selected = canvas.region.getAutotile(x, y, rez);
      if (selected == null) {
        selected = new Autotile((Sprite) rez, x / TILE_SIZE, y / TILE_SIZE);
      }
      canvas.hoverLoc = null;
    } else {
      startingLoc = canvas.hoverLoc;
      canvas.hoverLoc = null;
      selected = new MapObject(rez, startingLoc);
    }
    if (canvas.region.objects.contains(selected)) {
      forge.undo.onModify(selected);
    } else {
      canvas.region.add(selected);
      forge.undo.onCreate(selected);
    }
    canvas.select(selected);
  }

  private void cursorPress(){
    clickedHandle = getHandleAt(mouseX, mouseY);
    if (clickedHandle != null) {
      return;
    }
    MapObject selected = canvas.region.getObjectAt(mouseX, mouseY);
    canvas.select(selected);
    if (selected != null) {
      Rect bounds = selected.getBounds();
      offsetX = mouseX - bounds.x();
      offsetY = mouseY - bounds.y();

      if (clickCount % 2 == 0) {
        if (selected instanceof PortalObject) {
          // enter the portal!
          PortalObject portal = (PortalObject) selected;
          forge.canvas.setRegion(forge.world.getRegion(portal.targetRegionId));
        }
      }
    }
  }
  
  private void mouseMove() {
    if (toolPanel.tool == Tool.BRUSH) {
      if (toolPanel.getResource() instanceof ListenerResource) {
        ((ListenerResource) toolPanel.getResource()).mouseMove(mouseX, mouseY);
      } else {
        computeBrushHover();
      }
    } else if (toolPanel.tool == Tool.CURSOR) {
      canvas.hoverObject = canvas.region.getObjectAt(mouseX, mouseY);
    }
  }

  private void mouseDrag() {
    if (toolPanel.tool == Tool.BRUSH) {
      if (toolPanel.getResource() instanceof ListenerResource) {
        ((ListenerResource) toolPanel.getResource()).mouseDrag(mouseX, mouseY);
        return;
      }
      for (MapObject o : canvas.selectedObjects) {
        if (o instanceof Autotile) {
          int radius = 0;
          if (GKeyboard.isKeyDown(KeyEvent.VK_SHIFT)) { // big brush size
            radius = 3;
          }
          for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
              ((Autotile) o).addAutotile(mouseX / TILE_SIZE + i, mouseY / TILE_SIZE + j);
            }
          }
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
          o.location = new Rect(x1, y1, x2 - x1, y2 - y1);
        }
      }
    } else if (toolPanel.tool == Tool.CURSOR) {
      if (canvas.selectedObjects.isEmpty() || canvas.selectionBox != null) {
        canvas.selectionBox = Rect.create(pressX, pressY, mouseX, mouseY);
        canvas.selectedObjects.clear();
        canvas.selectedObjects.addAll(canvas.region.getObjects(canvas.selectionBox));
      } else {
        if (clickedHandle == null) {
          if (!dragTriggered) {
            if (Math.abs(mouseX - pressX) >= 5 || Math.abs(mouseY - pressY) >= 5) {
              dragTriggered = true;
              forge.undo.onModify(canvas.selectedObjects);
            }
          }
          if (dragTriggered) {
            for (MapObject o : canvas.selectedObjects) {
              o.moveTo(round(mouseX - offsetX), round(mouseY - offsetY));
            }
          }
        } else {
          clickedHandle.drag(mouseX, mouseY);
        }
      }
    }
  }

  private void computeBrushHover() {
    canvas.hoverLoc = null;

    if (toolPanel.tool == Tool.BRUSH) {
      Resource rez = toolPanel.getResource();
      if (rez != null && !(rez instanceof ListenerResource) && mouseInside) {
        if (rez.isAutotile()) {
          canvas.hoverLoc = new Rect(round(mouseX), round(mouseY), 16, 16);
        } else {
          int w = rez.getWidth(), h = rez.getHeight();
          canvas.hoverLoc = new Rect(round(mouseX - w / 2), round(mouseY - h / 2), w, h);
        }
      }
    }
  }

  private ObjectHandle getHandleAt(int x, int y) {
    for (MapObject o : canvas.selectedObjects) {
      for (ObjectHandle h : o.getHandles()) {
        if (h.location.contains(x, y)) {
          return h;
        }
      }
    }
    return null;
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

      mouseX = pressX = e.getX() + canvas.panX;
      mouseY = pressY = e.getY() + canvas.panY;
      clickCount = e.getClickCount();
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
      startingLoc = null;
      canvas.selectionBox = null;
      dragTriggered = false;

      if (inToolPanel) {
        return;
      }

      if (toolPanel.getResource() instanceof ListenerResource) {
        ((ListenerResource) toolPanel.getResource()).mouseRelease(mouseX, mouseY);
      }
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
      canvas.selectionBox = null;
    });
  }

}
