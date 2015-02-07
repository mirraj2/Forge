package armory;

import jasonlib.swing.component.GPanel;
import java.awt.image.BufferedImage;
import javax.swing.JTabbedPane;
import forge.Forge;

/**
 * Resource management
 */
public class Armory extends GPanel {

  public final Forge forge;

  private final JTabbedPane tabs = new JTabbedPane();

  public Armory(Forge forge) {
    this.forge = forge;

    initUI();

    setVisible(false);
  }

  private void initUI() {
    add(tabs, "width 100%, height 100%");
  }

  public void importTileset(String name, BufferedImage bi) {
    tabs.addTab(name, new TilesetPanel(this, name, bi));
    tabs.setSelectedIndex(tabs.getTabCount() - 1);
    setVisible(true);
  }

}
