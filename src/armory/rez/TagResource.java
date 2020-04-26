package armory.rez;

import static forge.ui.Forge.TILE_SIZE;

import java.awt.Font;

import javax.swing.JOptionPane;

import forge.input.MouseHandler;
import forge.map.object.MapObject;
import forge.map.object.TagObject;
import forge.ui.Forge;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

public class TagResource implements ListenerResource {

  public static final Font font = new Font("Arial", Font.BOLD, 16);

  protected final Forge forge;

  private int pressI, pressJ, mouseI, mouseJ;
  protected TagObject tag;

  public TagResource(Forge forge) {
    this.forge = forge;
  }

  @Override
  public void mousePress(int x, int y) {
    forge.canvas.selectionBox = null;

    tag = createTag();
    forge.canvas.region.add(tag);
    forge.undo.onCreate(tag);
    pressI = round(x);
    pressJ = round(y);
    mouseDrag(x, y);
  }

  protected TagObject createTag() {
    return new TagObject(this, null, "");
  }

  @Override
  public void mouseDrag(int x, int y) {
    mouseI = round(x);
    mouseJ = round(y);
    tag.location = Rect.create(mouseI, mouseJ, pressI, pressJ).changeSize(1, 1).scale(TILE_SIZE);
  }

  @Override
  public void mouseMove(int x, int y) {
    forge.canvas.selectionBox = new Rect(round(x), round(y), 1, 1).scale(TILE_SIZE);
  }

  @Override
  public void mouseRelease(int x, int y) {
    if (tag == null) {
      return;
    }

    String text = JOptionPane.showInputDialog(null, "Enter tag:");
    if (text == null) {
      forge.undo.undo();
      tag = null;
      return;
    }
    tag.text = text;
    tag = null;
  }

  private int round(int n) {
    return MouseHandler.round(n) / TILE_SIZE;
  }

  @Override
  public int getId() {
    return -1;
  }

  @Override
  public int getWidth() {
    return 64;
  }

  @Override
  public int getHeight() {
    return 64;
  }

  @Override
  public void renderIcon(Rect r, Graphics3D g) {
    TagObject.render(g, r, "Tag");
  }

  @Override
  public void render(Graphics3D g, double x, double y, boolean freezeAnimation) {
    renderIcon(new Rect(x, y, getWidth(), getHeight()), g);
  }

  @Override
  public MapObject loadObject(Json json) {
    return TagObject.load(this, json);
  }

}
