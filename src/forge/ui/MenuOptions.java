package forge.ui;

import jasonlib.IO;
import jasonlib.OS;
import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import forge.map.MapObject;
import forge.map.TagObject;

public class MenuOptions {

  private final Forge forge;

  public MenuOptions(Forge forge) {
    this.forge = forge;

    JMenuBar bar = new JMenuBar();

    JMenu fileMenu = new JMenu("Export As");
    fileMenu.add(new JMenuItem(exportImage));
    bar.add(fileMenu);

    Forge.frame.setJMenuBar(bar);
  }

  private final Action exportImage = new AbstractAction("Image") {
    @Override
    public void actionPerformed(ActionEvent e) {
      Canvas canvas = forge.canvas;

      Rect r = null;
      for (MapObject o : canvas.region.objects) {
        r = o.getBounds().union(r);
      }

      canvas.selectedObjects.clear();
      canvas.hoverLoc = null;
      canvas.hoverObject = null;
      canvas.showGrid = false;
      canvas.panX = r.x();
      canvas.panY = r.y();

      BufferedImage bi = new BufferedImage(r.w(), r.h(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = bi.createGraphics();
      canvas.render(Graphics3D.create(g), r, o -> !(o instanceof TagObject));
      g.dispose();

      File file = new File(OS.getDesktop(), "export.png");
      IO.from(bi).to(file);
      try {
        Desktop.getDesktop().open(file);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  };

}
