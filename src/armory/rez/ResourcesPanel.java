package armory.rez;

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
import armory.ImagePanel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import forge.ui.Forge;

public class ResourcesPanel extends GPanel {

  private final Forge forge;

  public List<Resource> resources = Lists.newArrayList();
  private final Map<Resource, Rect> layout = Maps.newHashMap();

  private Resource hoverObject;

  public ResourcesPanel(Forge forge) {
    super(new MigLayout("insets 0, gap 0"));

    this.forge = forge;

    GPanel wrapper = new GPanel(new MigLayout("insets 10"));
    wrapper.setBackground(Color.DARK_GRAY);
    wrapper.setOpaque(true);

    JScrollPane scroll = new GScrollPane(view);
    wrapper.add(scroll, "width 100%, height 100%");
    add(wrapper, "width 100%, height 100%");

    listen();

    add(new TagResource(forge));
    add(new PortalResource(forge));
  }

  public Resource get(int id) {
    for (Resource o : resources) {
      if (o.getId() == id) {
        return o;
      }
    }
    return null;
  }

  public void add(Resource o) {
    resources.add(o);
    computeLayout();
  }

  public void remove(Resource o) {
    resources.remove(o);
    computeLayout();
  }

  private void computeLayout() {
    layout.clear();

    Rect area = new Rect(0, 0, 1, 1);

    int x = 0;
    int y = 0;
    int rowHeight = 0;
    for (Resource o : resources) {
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

  private Resource getObjectAt(int x, int y) {
    for (Entry<Resource, Rect> e : layout.entrySet()) {
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

    private boolean addingCollisions; // whether we're adding or removing collisions with this stroke

    @Override
    public void mousePressed(MouseEvent e) {
      hoverObject = getObjectAt(e.getX(), e.getY());

      if (Forge.collisionMode) {
        if (hoverObject == null) {
          addingCollisions = true;
        } else {
          Rect r = layout.get(hoverObject);
          addingCollisions = !hoverObject.isCollision(e.getX() - r.x, e.getY() - r.y);
        }
        handleCollision(e.getX(), e.getY());
      } else {
        forge.toolPanel.equip(hoverObject);
      }
    };

    @Override
    public void mouseMoved(MouseEvent e) {
      hoverObject = getObjectAt(e.getX(), e.getY());
    };

    @Override
    public void mouseDragged(MouseEvent e) {
      handleCollision(e.getX(), e.getY());
    };

    @Override
    public void mouseExited(MouseEvent e) {
      hoverObject = null;
    };

    private void handleCollision(int x, int y) {
      Resource rez = getObjectAt(x, y);
      if (rez != null) {
        Rect r = layout.get(rez);
        rez.setCollision(x - r.x, y - r.y, addingCollisions);
      }
    }
  };

  private final JComponent view = new GPanel() {
    @Override
    protected void paintComponent(Graphics gg) {
      Graphics3D g = Graphics3D.create(gg);

      for (Resource o : resources) {
        Rect r = layout.get(o);
        o.render(g, r.x(), r.y());

        if (o == hoverObject && !Forge.collisionMode) {
          g.color(Color.white).draw(r);
        }
      }

      if (Forge.collisionMode) {
        g.setStroke(1);
        ImagePanel.renderGrid(g, getWidth(), getHeight(), Color.gray);
      }
    };

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    };
  };

}
