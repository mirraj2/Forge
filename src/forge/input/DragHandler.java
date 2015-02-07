package forge.input;

import jasonlib.IO;
import jasonlib.Log;
import jasonlib.swing.DragListener;
import jasonlib.swing.global.DND;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import forge.Forge;

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

      forge.armory.importTileset(name, bi);
    }
  }

}
