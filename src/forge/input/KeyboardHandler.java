package forge.input;

import jasonlib.swing.global.GFocus;
import jasonlib.swing.global.GKeyboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.text.JTextComponent;
import forge.map.object.MapObject;
import forge.ui.Canvas;
import forge.ui.Forge;

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
    if (GFocus.currentFocusOwner instanceof JTextComponent) {
      return;
    }

    if (forge.armory.isVisible()) {
      return;
    }

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
      } else if (code == KeyEvent.VK_F1) {
        Forge.enableAnimations = !Forge.enableAnimations;
      }

      if (GFocus.currentFocusOwner instanceof JTextComponent) {
        return;
      }

      if (code == KeyEvent.VK_E) {
        forge.armory.setVisible(!forge.armory.isVisible());
      } else if (code == KeyEvent.VK_C) {
        Forge.collisionMode = !Forge.collisionMode;
      } else if (code >= '1' && code <= '9') {
        forge.toolPanel.setSelectedResource(code - '1');
      }

      if (!forge.armory.isVisible()) {
        if (code == KeyEvent.VK_TAB) {
          forge.toolPanel.switchTool();
        } else if (code == KeyEvent.VK_G) {
          canvas.showGrid = !canvas.showGrid;
        } else if (code == KeyEvent.VK_BACK_SPACE) {
          for (MapObject o : canvas.selectedObjects) {
            canvas.region.remove(o);
          }
          canvas.selectedObjects.clear();
        } else if (code == KeyEvent.VK_MINUS) {
          for (MapObject o : canvas.selectedObjects) {
            canvas.region.moveBack(o);
          }
        } else if (code == KeyEvent.VK_EQUALS) {
          for (MapObject o : canvas.selectedObjects) {
            canvas.region.moveForward(o);
          }
        } else {
          if (GKeyboard.isSystemShortcut(e.getModifiers())) {
            if (code == KeyEvent.VK_Z) {
              forge.undo.undo();
            }
          }
        }
      }
    }
  };

}
