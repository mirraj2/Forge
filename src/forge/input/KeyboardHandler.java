package forge.input;

import jasonlib.swing.global.GFocus;
import jasonlib.swing.global.GKeyboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import forge.Forge;

public class KeyboardHandler {

  private final Forge forge;

  public KeyboardHandler(Forge forge) {
    this.forge = forge;

    GKeyboard.addKeyListener(forge, keyListener);
  }

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
        forge.toolPanel.setSelectedTool(code - '1');
      }
    }
  };

}
