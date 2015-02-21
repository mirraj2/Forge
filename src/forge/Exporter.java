package forge;

import static com.google.common.collect.Iterables.filter;
import static forge.ui.Forge.TILE_SIZE;
import jasonlib.IO;
import jasonlib.Json;
import jasonlib.Log;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import armory.rez.Sprite;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import forge.map.Region;
import forge.map.object.Autotile;
import forge.map.object.MapObject;
import forge.map.object.PortalObject;
import forge.map.object.TagObject;
import forge.ui.Canvas;
import forge.ui.Forge;

public class Exporter {

  private final Forge forge;

  public Exporter(Forge forge) {
    this.forge = forge;
  }

  public void export(File dir) {
    Stopwatch watch = Stopwatch.createStarted();

    Json json = Json.array();
    for (Region region : getTraversableRegions()) {
      json.add(region.id);
      File regionDir = new File(dir, "region" + region.id);
      regionDir.mkdirs();
      export(region, regionDir);
    }
    IO.from(json).to(new File(dir, "regions.json"));

    Log.debug("Exported in " + watch);
  }

  // map.json listing objects
  // object images
  private void export(Region region, File dir) {
    BufferedImage backgroundImage = renderToImage(region, o -> isPartOfBackground(o));
    IO.from(backgroundImage).to(new File(dir, "background.png"));

    Rect r = getBounds(region);

    Json objects = Json.array();
    Json json = Json.object()
        .with("id", region.id)
        .with("name", region.name)
        .with("objects", objects)
        .with("collisions", getCollisions(region, r));

    // BitSet b = new BitSet(json.getJson("collisions").asLongArray());
    // Graphics3D g = Graphics3D.create(backgroundImage.createGraphics());
    // g.color(Color.red);
    // for (int x = 0; x < backgroundImage.getWidth(); x += TILE_SIZE) {
    // for (int y = 0; y < backgroundImage.getHeight(); y += TILE_SIZE) {
    // if (b.get(x / TILE_SIZE + (y / TILE_SIZE) * (backgroundImage.getWidth() / TILE_SIZE))) {
    // g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
    // }
    // }
    // }
    // g.dispose();


    Set<Sprite> spritesSeen = Sets.newHashSet();
    for (MapObject o : filter(region.objects, o -> !isPartOfBackground(o))) {
      if (o.rez instanceof Sprite) {
        Sprite s = (Sprite) o.rez;
        if (spritesSeen.add(s)) {
          IO.from(s.subimage).to(new File(dir, s.id + ".png"));
        }
      }

      Json objectJson = o.toJson();
      objectJson.with("loc", o.location.translate(-r.x, -r.y).serialize());
      objects.add(objectJson);
    }

    IO.from(json).to(new File(dir, "map.json"));
  }

  private Json getCollisions(Region region, Rect bounds) {
    int w = bounds.w() / TILE_SIZE;
    int h = bounds.h() / TILE_SIZE;

    BitSet objectCollisions = new BitSet(w * h);
    BitSet tileLocations = new BitSet(w * h);

    for (MapObject o : region.objects) {
      if (isPartOfBackground(o)) {
        Rect r = o.getBounds();
        for (int x = r.x(); x < r.maxX(); x += TILE_SIZE) {
          for (int y = r.y(); y < r.maxY(); y += TILE_SIZE) {
            if (o.isHit(x, y)) {
              int i = (x - bounds.x()) / TILE_SIZE;
              int j = (y - bounds.y()) / TILE_SIZE;
              tileLocations.set(i + j * w, true);
            }
          }
        }
      } else if (o.rez instanceof Sprite) {
        Sprite s = (Sprite) o.rez;
        Rect r = o.location;
        for (int x = r.x(); x < r.maxX(); x += TILE_SIZE) {
          for (int y = r.y(); y < r.maxY(); y += TILE_SIZE) {
            if (s.isCollision(x - r.x(), y - r.y())) {
              int i = (x - bounds.x()) / TILE_SIZE;
              int j = (y - bounds.y()) / TILE_SIZE;
              objectCollisions.set(i + j * w, true);
            }
          }
        }
      }
    }

    BitSet collisions = objectCollisions.or(tileLocations.invert());

    return Json.array(Longs.asList(collisions.words));
  }

  private boolean isPartOfBackground(MapObject o) {
    if (o instanceof Autotile) {
      return true;
    }

    if (o.rez instanceof Sprite) {
      Sprite sprite = (Sprite) o.rez;
      if (!sprite.hasAnyCollisions()) {
        return true;
      }
    }

    return false;
  }

  private Collection<Region> getTraversableRegions() {
    Set<Region> ret = Sets.newLinkedHashSet();

    Queue<Region> queue = Lists.newLinkedList();
    queue.add(forge.canvas.region);

    while (!queue.isEmpty()) {
      Region current = queue.poll();
      ret.add(current);

      for (PortalObject p : filter(current.objects, PortalObject.class)) {
        Region target = forge.world.getRegion(p.targetRegionId);
        if (!ret.contains(target)) {
          queue.add(target);
        }
      }
    }

    return ret;
  }

  private BufferedImage renderToImage(Region region, Predicate<MapObject> objectFilter) {
    Canvas canvas = new Canvas(forge);
    canvas.setRegion(forge.canvas.region);

    Rect r = getBounds(forge.canvas.region);
    canvas.panX = r.x();
    canvas.panY = r.y();

    BufferedImage bi = new BufferedImage(r.w(), r.h(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    canvas.render(Graphics3D.create(g), r, objectFilter);
    g.dispose();

    return bi;
  }

  public void exportPNG(File file) {
    BufferedImage bi = renderToImage(forge.canvas.region, o -> !(o instanceof TagObject));
    IO.from(bi).to(file);
    try {
      Desktop.getDesktop().open(file);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  private Rect getBounds(Region region) {
    Rect r = null;
    for (MapObject o : region.objects) {
      r = o.getBounds().union(r);
    }
    return r;
  }

}
