package armory.rez;

import static com.google.common.collect.Iterables.filter;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import forge.map.Region;
import forge.map.object.MapObject;
import forge.map.object.PortalObject;
import forge.map.object.TagObject;
import forge.ui.Forge;
import ox.Json;
import ox.Rect;
import swing.Graphics3D;

public class PortalResource extends TagResource {

  private PortalObject toPortal; // not persisted

  public PortalResource(Forge forge) {
    super(forge);
  }

  @Override
  public void renderIcon(Rect r, Graphics3D g) {
    PortalObject.render(g, r, "Portal", true);
  }

  @Override
  public void render(Graphics3D g, double x, double y, boolean freezeAnimation) {
    renderIcon(new Rect(x, y, getWidth(), getHeight()), g);
  }

  @Override
  public MapObject loadObject(Json json) {
    return PortalObject.load(this, json);
  }

  @Override
  protected TagObject createTag() {
    Region toRegion = new Region();
    forge.world.regions.add(toRegion);

    String token = nextToken();

    toPortal = new PortalObject(this, null, forge.canvas.region.id, false, token);
    toRegion.objects.add(toPortal);

    return new PortalObject(this, null, toRegion.id, true, token);
  }

  @Override
  public void mouseRelease(int x, int y) {
    toPortal.location = tag.location;
    toPortal = null;
    tag = null;
  }

  @Override
  public int getId() {
    return -2;
  }

  private String nextToken() {
    Set<String> usedTokens = Sets.newHashSet();
    for (Region region : forge.world.regions) {
      for (PortalObject portal : filter(region.objects, PortalObject.class)) {
        usedTokens.add(portal.getToken());
      }
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      String token = c + "";
      if (!usedTokens.contains(token)) {
        return token;
      }
    }
    for (int i = 1; i < 999; i++) {
      String token = i + "";
      if (!usedTokens.contains(token)) {
        return token;
      }
    }
    return UUID.randomUUID().toString();
  }

}
