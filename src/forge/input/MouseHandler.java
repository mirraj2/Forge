package forge.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import forge.Forge;

public class MouseHandler {

  private final Forge forge;

  public MouseHandler(Forge forge) {
    this.forge = forge;

    forge.addMouseListener(mouseListener);
    forge.addMouseMotionListener(mouseListener);
  }

  private final MouseAdapter mouseListener = new MouseAdapter() {
    boolean inToolPanel = false;

    @Override
    public void mousePressed(MouseEvent e) {
      MouseEvent e2 = SwingUtilities.convertMouseEvent(forge, e, forge.toolPanel);
      forge.toolPanel.mousePressed(e2);

      inToolPanel = e2.isConsumed();

      if (inToolPanel) {
        return;
      }
    };

    @Override
    public void mouseDragged(MouseEvent e) {
      if (inToolPanel) {
        e = SwingUtilities.convertMouseEvent(forge, e, forge.toolPanel);
        forge.toolPanel.mouseDragged(e);
        return;
      }
    };
  };

}
