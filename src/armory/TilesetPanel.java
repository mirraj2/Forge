package armory;

import jasonlib.swing.component.GPanel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JScrollPane;
import com.google.common.collect.Sets;

public class TilesetPanel extends GPanel {

  public final Armory armory;
  public final String name;
  public final BufferedImage bi;
  public final ObjectDetailsPanel objectPanel;
  public final ImagePanel imagePanel;

  public final Set<MapObject> mapObjects = Sets.newLinkedHashSet();

  public TilesetPanel(Armory armory, String name, BufferedImage bi) {
    this.armory = armory;
    this.name = name;
    this.bi = bi;
    this.objectPanel = new ObjectDetailsPanel(this, bi);
    this.imagePanel = new ImagePanel(this, bi, objectPanel);

    initUI();
  }

  private void initUI() {
    setBackground(Color.DARK_GRAY);
    setOpaque(true);
    JScrollPane scroll = new JScrollPane(imagePanel);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setBorder(null);
    add(Box.createHorizontalGlue(), "width 100%, span, wrap");
    add(scroll, "width pref+20, height 100%, split, alignx center");
    add(objectPanel, "alignx center, aligny top");
  }

  public void onSave(MapObject object) {
    mapObjects.add(object);
    imagePanel.selection = null;
  }

}
