package armory;

import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.component.GScrollPane;
import jasonlib.swing.global.Components;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ResourcesPanel extends GPanel {

  private final Armory armory;

  public List<Sprite> sprites = Lists.newArrayList();
  private final Map<Sprite, Rect> layout = Maps.newHashMap();

  private Sprite hoverObject;

  public ResourcesPanel(Armory armory) {
    super(new MigLayout("insets 0, gap 0"));

    this.armory = armory;

    GPanel wrapper = new GPanel(new MigLayout("insets 10"));
    wrapper.setBackground(Color.DARK_GRAY);
    wrapper.setOpaque(true);

    JScrollPane scroll = new GScrollPane(view);
    wrapper.add(scroll, "width 100%, height 100%");
    add(wrapper, "width 100%, height 100%");

    listen();
  }

  public Sprite get(int id) {
    for (Sprite o : sprites) {
      if (o.id == id) {
        return o;
      }
    }
    return null;
  }

  public void add(Sprite o) {
    sprites.add(o);
    computeLayout();
  }

  public void remove(Sprite o) {
    sprites.remove(o);
    computeLayout();
  }

  private void computeLayout() {
    layout.clear();

    Rect area = new Rect(0, 0, 1, 1);

    int x = 0;
    int y = 0;
    int rowHeight = 0;
    for (Sprite o : sprites) {
      int w = o.getWidth();
      int h = o.getHeight();

      if (x > 0 && x + w > getWidth() - 20 - 10) { // 20 for miglayout insets, 10 for scrollbar
        // new row
        x = 0;
        y += rowHeight;
        rowHeight = 0;
      }
      Rect r = new Rect(x, y, w, h);
      area = area.union(r);
      layout.put(o, r);
      x += r.w();
      rowHeight = Math.max(r.h(), rowHeight);
    }

    Dimension dim = new Dimension(area.w(), area.h());
    view.setPreferredSize(dim);

    Components.refresh(view);
  }

  private Sprite getObjectAt(int x, int y) {
    for (Entry<Sprite, Rect> e : layout.entrySet()) {
      if (e.getValue().contains(x, y)) {
        return e.getKey();
      }
    }
    return null;
  }

  private void listen() {
    onResize(() -> computeLayout());
    view.addMouseListener(mouseListener);
    view.addMouseMotionListener(mouseListener);
  }

  private final MouseAdapter mouseListener = new MouseAdapter() {

    @Override
    public void mousePressed(MouseEvent e) {
      hoverObject = getObjectAt(e.getX(), e.getY());

      armory.forge.toolPanel.equip(hoverObject);
    };

    @Override
    public void mouseMoved(MouseEvent e) {
      hoverObject = getObjectAt(e.getX(), e.getY());
    };

    @Override
    public void mouseExited(MouseEvent e) {
      hoverObject = null;
    };
  };

  private final JComponent view = new GPanel() {
    @Override
    protected void paintComponent(Graphics gg) {
      Graphics3D g = Graphics3D.create(gg);
      for (Sprite o : sprites) {
        Rect r = layout.get(o);
        g.alpha(hoverObject == null || o == hoverObject ? 1 : .5);
        o.render(g, r.x(), r.y(), hoverObject != null && o != hoverObject);
      }
    };

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    };
  };

}
