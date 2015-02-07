package forge.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import forge.Forge;

public class KeyboardHandler {

  private final Forge forge;

  public KeyboardHandler(Forge forge) {
    this.forge = forge;
    
    forge.addKeyListener(keyListener);
  }

  private final KeyAdapter keyListener = new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
      int code = e.getKeyCode();

      if (code >= '1' && code <= '9') {
        forge.toolPanel.setSelectedTool(code - '1');
      }
    }
  };

}
