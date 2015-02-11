package forge.input;

import jasonlib.swing.global.GFocus;
import jasonlib.swing.global.GKeyboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.text.JTextComponent;
import forge.Canvas;
import forge.Forge;

public class KeyboardHandler {

  private final Forge forge;
  private final Canvas canvas;

  public KeyboardHandler(Forge forge) {
    this.forge = forge;
    this.canvas = forge.canvas;

    GKeyboard.addKeyListener(forge, keyListener);
    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(scroller, 0, 30, TimeUnit.MILLISECONDS);
  }

  private Runnable scroller = () -> {
    if (GKeyboard.isKeyDown(KeyEvent.VK_W)) {
      canvas.panY -= Forge.TILE_SIZE;
      canvas.hoverLoc = null;
    } else if (GKeyboard.isKeyDown(KeyEvent.VK_A)) {
      canvas.panX -= Forge.TILE_SIZE;
      canvas.hoverLoc = null;
    } else if (GKeyboard.isKeyDown(KeyEvent.VK_S)) {
      canvas.panY += Forge.TILE_SIZE;
      canvas.hoverLoc = null;
    } else if (GKeyboard.isKeyDown(KeyEvent.VK_D)) {
      canvas.panX += Forge.TILE_SIZE;
      canvas.hoverLoc = null;
    }
  };

  private final KeyAdapter keyListener = new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
      int code = e.getKeyCode();

      if (code == KeyEvent.VK_ESCAPE) {
        forge.armory.setVisible(!forge.armory.isVisible());
      }

      if (GFocus.currentFocusOwner instanceof JTextComponent) {
        return;
      }

      if (code >= '1' && code <= '9') {
        forge.toolPanel.setSelectedSprite(code - '1');
      }

      if (!forge.armory.isVisible()) {
        if (code == KeyEvent.VK_TAB) {
          forge.toolPanel.switchTool();
        } else if (code == KeyEvent.VK_G) {
          canvas.showGrid = !canvas.showGrid;
        } else if (code == KeyEvent.VK_BACK_SPACE) {
          if (canvas.selectedObject != null) {
            canvas.data.remove(canvas.selectedObject);
            canvas.selectedObject = null;
          }
        } else if (code == KeyEvent.VK_MINUS) {
          if (canvas.selectedObject != null) {
            canvas.data.moveBack(canvas.selectedObject);
          }
        } else if (code == KeyEvent.VK_EQUALS) {
          if (canvas.selectedObject != null) {
            canvas.data.moveForward(canvas.selectedObject);
          }
        }
      }
    }
  };

}
