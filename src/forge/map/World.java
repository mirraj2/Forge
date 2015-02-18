package forge.map;

import jasonlib.IO;
import jasonlib.Json;
import jasonlib.OS;
import java.io.File;
import java.util.List;
import armory.Armory;
import com.google.common.collect.Lists;

public class World {

  private static final File file = new File(OS.getAppFolder("forge"), "regions.json");

  public final List<Region> regions = Lists.newArrayList();

  public World(Armory armory) {
    if (!file.exists()) {
      regions.add(new Region());
      return;
    }

    for (int id : IO.from(file).toJson().asIntArray()) {
      regions.add(new Region(id, armory));
      Region.idCounter = Math.max(Region.idCounter, id + 1);
    }
  }

  public Region getRegion(int id) {
    for (Region r : regions) {
      if (r.id == id) {
        return r;
      }
    }
    return null;
  }

  public void save() {
    IO.from(Json.array(regions, r -> r.id)).to(file);
    for (Region region : regions) {
      region.save();
    }
  }

}
