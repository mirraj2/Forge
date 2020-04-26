package forge.map;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import armory.Armory;
import ox.IO;
import ox.Json;
import ox.Log;
import ox.OS;

public class World {

  private static final File file = new File(OS.getAppFolder("forge"), "regions.json");

  public final List<Region> regions = Lists.newArrayList();

  public World(Armory armory) {
    if (!file.exists()) {
      regions.add(new Region());
      return;
    }

    for (int id : IO.from(file).toJson().asIntArray()) {
      try {
        regions.add(new Region(id, armory));
        Region.idCounter = Math.max(Region.idCounter, id + 1);
      } catch (Exception e) {
        Log.error("Problem loading region: " + id);
        e.printStackTrace();
      }
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
