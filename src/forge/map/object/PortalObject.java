package forge.map.object;

import jasonlib.Json;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import armory.rez.Resource;

public class PortalObject extends TagObject {

  private static final Font portalFont = new Font("Courier", Font.BOLD, 18);

  public final boolean isSource;
  public final int targetRegionId;

  public PortalObject(Resource rez, Rect location, int targetRegionId, boolean isSource, String token) {
    super(rez, location, token);
    this.targetRegionId = targetRegionId;

    this.isSource = isSource;
  }

  public String getToken() {
    return text;
  }

  @Override
  public void render(Graphics3D g, Rect clip) {
    render(g, location, getToken(), isSource);
  }

  public static void render(Graphics3D g, Rect r, String text, boolean isSource) {
    Color edgeColor = isSource ? new Color(0, 0, 150) : new Color(255, 200, 0);
    Ellipse2D.Double e = new Ellipse2D.Double(r.x, r.y, r.w, r.h);
    g.color(0, 0, 0, 100).fill(e);
    g.setStroke(2).color(edgeColor).draw(e);
    g.color(Color.white).font(portalFont).text(text, r);
  }

  public static PortalObject load(Resource rez, Json json) {
    return new PortalObject(rez, Rect.parse(json.get("loc")), json.getInt("target_region"),
        json.getBoolean("is_source"), json.get("token"));
  }

  @Override
  public Json toJson() {
    return super.toJson().remove("text").with("target_region", targetRegionId).with("is_source", isSource)
        .with("token", getToken());
  }

}
