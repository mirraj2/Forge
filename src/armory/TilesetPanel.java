package armory;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.component.GPanel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.JScrollPane;
import armory.rez.Sprite;
import com.google.common.collect.Lists;

public class TilesetPanel extends GPanel {

  public final Armory armory;
  public final int id;
  public final String name;
  public final BufferedImage bi;
  public final ObjectDetailsPanel objectPanel;
  public final ImagePanel imagePanel;

  public final List<Sprite> mapObjects = Lists.newArrayList();

  public TilesetPanel(Armory armory, int id, String name, BufferedImage bi) {
    this.armory = armory;
    this.id = id;
    this.name = name;
    this.bi = bi;
    this.objectPanel = new ObjectDetailsPanel(this);
    this.imagePanel = new ImagePanel(this, bi, objectPanel);

    initUI();
  }

  public static TilesetPanel load(Armory armory, File dir, int id) {
    BufferedImage bi = IO.from(new File(dir, id + ".png")).toImage();

    Json json = IO.from(new File(dir, id + ".json")).toJson();
    TilesetPanel ret = new TilesetPanel(armory, json.getInt("id"), json.get("name"), bi);

    for (Json object : json.getJson("objects").asJsonArray()) {
      Sprite o = Sprite.load(object, bi);
      ret.mapObjects.add(o);
      armory.resourcesPanel.add(o);
    }

    return ret;
  }

  public void save(File dir) {
    File imageFile = new File(dir, id + ".png");
    if (!imageFile.exists()) {
      IO.from(bi).to(imageFile);
    }

    Json json = Json.object()
        .with("id", id)
        .with("name", name)
        .with("objects", Json.array(mapObjects, o -> o.toJson()));

    IO.from(json).to(new File(dir, id + ".json"));
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

  public void onSelection(Rect selection) {
    Sprite m = new Sprite(Sprite.idCounter++, bi, selection);
    mapObjects.add(m);
    imagePanel.selection = null;
    objectPanel.load(m);
    armory.resourcesPanel.add(m);
  }

  public void delete(Sprite object) {
    mapObjects.remove(object);
    imagePanel.selection = null;
    armory.resourcesPanel.remove(object);
  }

}
