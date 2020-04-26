package armory;

import static java.lang.Integer.parseInt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

import armory.rez.Sprite;
import swing.Graphics3D;
import swing.component.GButton;
import swing.component.GCheckbox;
import swing.component.GLabel;
import swing.component.GPanel;
import swing.component.GTextField;
import swing.global.Components;

public class ObjectDetailsPanel extends GPanel {

  private final TilesetPanel parent;
  private final ObjectRenderer objectRenderer;
  private final GTextField tagsInput = new GTextField();
  private final GCheckbox autotile = new GCheckbox("Autotile").color(Color.white);
  private final GTextField rowsInput = new GTextField().columns(3).focusSelects();
  private final GTextField colsInput = new GTextField().columns(3).focusSelects();
  private final GButton deleteButton = new GButton("Delete");
  private Sprite selected = null;

  public ObjectDetailsPanel(TilesetPanel parent) {
    this.parent = parent;

    add(objectRenderer = new ObjectRenderer(), "span, wrap 10");
    add(autotile, "span, wrap");
    add(new GLabel("Animate").color(Color.white), "");
    add(colsInput, "split");
    add(rowsInput, "wrap");
    add(new GLabel("Tags:").color(Color.white), "");
    add(tagsInput, "wrap");
    add(deleteButton, "span");

    listen();

    setVisible(false);
  }

  private int parse(String s) {
    try {
      return parseInt(s);
    } catch (Exception e) {
      return 0;
    }
  }

  public void load(Sprite o) {
    this.selected = o;

    Components.refresh(objectRenderer);
    autotile.setSelected(o.autotile);
    rowsInput.setText(o.animationRows + "");
    colsInput.setText(o.animationCols + "");
    tagsInput.setText(selected.tags);
    setVisible(true);
    tagsInput.requestFocusInWindow();
  }

  private void listen() {
    tagsInput.onChange(() -> {
      selected.tags = tagsInput.getText();
    });

    rowsInput.onChange(() -> {
      selected.animate(parse(rowsInput.getText()), selected.animationCols);
    });

    colsInput.onChange(() -> {
      selected.animate(selected.animationRows, parse(colsInput.getText()));
    });

    autotile.onChange(() -> {
      selected.autotile = autotile.isSelected();
    });

    deleteButton.click(() -> {
      parent.delete(selected);
      setVisible(false);
    });
  }

  private class ObjectRenderer extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
      selected.render(Graphics3D.create(g), 0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
      if (selected == null) {
        return super.getPreferredSize();
      }
      return selected.getRenderSize();
    }
  }

}
