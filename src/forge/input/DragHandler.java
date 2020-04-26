package forge.input;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;

import forge.ui.Forge;
import ox.IO;
import ox.Log;
import swing.DragListener;
import swing.global.DND;

public class DragHandler {

  private final Forge forge;

  public DragHandler(Forge forge) {
    this.forge = forge;

    DND.addDragListener(forge, listener);
  }

  private final DragListener listener = new DragListener() {
    @Override
    public void handleDrop(Object data, int x, int y) {
      if (data instanceof Collection) {
        for (Object o : (Collection<?>) data) {
          handle(o);
        }
      } else {
        handle(data);
      }
    }
  };

  private void handle(Object o) {
    if (o instanceof File) {
      File file = (File) o;
      Log.debug("importing file: " + file);

      BufferedImage bi = IO.from(file).toImage();
      String name = file.getName();

      int i = name.lastIndexOf('.');
      if (i >= 0) {
        name = name.substring(0, i);
      }

      forge.armory.importTileset(name, bi);
    } else if (o instanceof String) {
      String s = (String) o;
      BufferedImage bi = IO.fromURL(s).toImage();
      forge.armory.importTileset("untitled", bi);
    } else {
      Log.warn("Unhandled type: " + o.getClass());
    }
  }

}
