package armory;

import jasonlib.Rect;
import jasonlib.swing.component.GButton;
import jasonlib.swing.component.GLabel;
import jasonlib.swing.component.GPanel;
import jasonlib.swing.component.GTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ObjectDetailsPanel extends GPanel {

  private final TilesetPanel parent;
  private final JLabel imageLabel = new JLabel();
  private final BufferedImage bi;
  private final GTextField tagsInput = new GTextField();
  private MapObject selected = null;

  public ObjectDetailsPanel(TilesetPanel parent, BufferedImage bi) {
    this.parent = parent;
    this.bi = bi;
    add(imageLabel, "span, wrap");
    add(new GLabel("Tags:").color(Color.white), "");
    add(tagsInput, "wrap");
    add(new GButton(saveAction), "span");

    tagsInput.onEnter(() -> {
      saveAction.actionPerformed(null);
    });

    setVisible(false);
  }

  public void load(MapObject o) {
    this.selected = o;
    tagsInput.setText(o.tags);
    imageLabel.setIcon(new ImageIcon(selected.subimage));

    setVisible(true);
    tagsInput.requestFocusInWindow();
  }

  public void onSelection(Rect selection) {
    selected = new MapObject(bi, selection);

    imageLabel.setIcon(new ImageIcon(selected.subimage));
    tagsInput.setText("");
    
    setVisible(true);
    tagsInput.requestFocusInWindow();
  }

  private Action saveAction = new AbstractAction("Save") {
    @Override
    public void actionPerformed(ActionEvent e) {
      selected.tags = tagsInput.getText();
      parent.onSave(selected);
      imageLabel.setIcon(null);
      tagsInput.setText("");
      setVisible(false);
    }
  };

}
