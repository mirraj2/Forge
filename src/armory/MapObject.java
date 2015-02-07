package armory;

import jasonlib.Rect;
import java.awt.image.BufferedImage;

public class MapObject {

  public final BufferedImage bi, subimage;
  public final Rect bounds;
  public String tags = "";

  public MapObject(BufferedImage bi, Rect bounds) {
    this.bi = bi;
    this.subimage = bi.getSubimage(bounds.x(), bounds.y(), bounds.w(), bounds.h());
    this.bounds = bounds;
  }

}
