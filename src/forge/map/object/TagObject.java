package forge.map.object;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import armory.rez.Resource;
import armory.rez.TagResource;

public class TagObject extends MapObject {

  public static final Color SELECTION_COLOR = new Color(255, 100, 30, 100);

  public String text;

  public TagObject(Resource rez, Rect location, String text) {
    super(rez, location);

    this.text = text;
  }

  @Override
  public void render(Graphics3D g, Rect clip) {
    render(g, location, text);
  }

  public static void render(Graphics3D g, Rect location, String text) {
    g.color(SELECTION_COLOR).fill(location);
    g.setStroke(2.0).color(Color.white).draw(location);
    g.font(TagResource.font).text(text, location);
  }

  public static TagObject load(Resource rez, Json json) {
    return new TagObject(rez, Rect.parse(json.get("loc")), json.get("text"));
  }

  @Override
  public Json toJson() {
    return super.toJson().with("text", text);
  }

}
