package forge.input;

import jasonlib.Rect;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import armory.Sprite;
import forge.Canvas;
import forge.Forge;
import forge.ToolPanel;

public class MouseHandler {

  private final Forge forge;
  private final ToolPanel toolPanel;
  private final Canvas canvas;

  private int mouseX, mouseY;
  private boolean mouseInside = false;

  public MouseHandler(Forge forge) {
    this.forge = forge;
    this.toolPanel = forge.toolPanel;
    this.canvas = forge.canvas;

    forge.addMouseListener(mouseListener);
    forge.addMouseMotionListener(mouseListener);

    toolPanel.onChange(() -> {
      computeHoverObject();
    });
  }

  private void mousePress() {
    if (canvas.hoverLoc != null) {
      canvas.data.add(toolPanel.getTool(), canvas.hoverLoc);
    }
  }

  private void mouseMove() {
    computeHoverObject();
  }

  private void computeHoverObject() {
    Sprite tool = toolPanel.getTool();
    if (tool == null || !mouseInside) {
      canvas.hoverLoc = null;
    } else {
      int w = tool.getWidth(), h = tool.getHeight();
      canvas.hoverLoc = new Rect(round(mouseX - w / 2), round(mouseY - h / 2), w, h);
    }
  }

  private int round(int n) {
    return n / Forge.TILE_SIZE * Forge.TILE_SIZE;
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

      mouseX = e.getX();
      mouseY = e.getY();
      mousePress();
    };

    @Override
    public void mouseDragged(MouseEvent e) {
      if (inToolPanel) {
        e = SwingUtilities.convertMouseEvent(forge, e, toolPanel);
        toolPanel.mouseDragged(e);
        return;
      }
    };

    @Override
    public void mouseMoved(MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();

      if (toolPanel.getDrawBounds().contains(mouseX, mouseY - toolPanel.getY())) {
        mouseInside = false;
      } else {
        mouseInside = true;
      }

      mouseMove();
    };

    @Override
    public void mouseEntered(MouseEvent e) {
      mouseInside = true;
    };

    @Override
    public void mouseExited(MouseEvent e) {
      mouseInside = false;
      computeHoverObject();
    };
  };

}
