package forge;

import jasonlib.swing.component.GFrame;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.global.GFocus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.miginfocom.swing.MigLayout;
import armory.Armory;
import forge.input.DragHandler;
import forge.input.KeyboardHandler;
import forge.input.MouseHandler;

public class Forge extends GPanel {

  public static final int TILE_SIZE = 16;

  public final Canvas canvas = new Canvas(this);
  public final Armory armory = new Armory(this);
  public final ToolPanel toolPanel = new ToolPanel(this);

  public Forge() {
    super(new MigLayout("insets 0, gap 0"));

    add(toolPanel, "pos 0% 90% 100% 100%");
    add(armory, "pos 5% 0% 95% 90%");
    add(canvas, "width 100%, height 100%");

    setFocusable(true);

    new KeyboardHandler(this);
    new MouseHandler(this);
    new DragHandler(this);

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(repaint, 0, 30, TimeUnit.MILLISECONDS);
  }

  private Runnable repaint = () -> {
    repaint();
  };

  private Runnable saveAndExit = () -> {
    armory.save();
    toolPanel.save();
    System.exit(0);
  };

  public static void main(String[] args) {
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    Forge forge = new Forge();
    GFrame frame = new GFrame("The Forge")
        .content(forge)
        .size(dim.width - 100, dim.height - 100)
        .disposeOnClose()
        .onClose(forge.saveAndExit)
        .start();
    frame.setBackground(Color.black);
    GFocus.focus(forge);
  }

}
