package forge;

import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class ToolPanel extends JComponent {

  private int toolIndex = 0;

  public void setSelectedTool(int toolIndex) {
    this.toolIndex = toolIndex;
  }

  public void mousePressed(MouseEvent e) {
    Rect r = getDrawBounds();
    if (r.contains(e.getX(), e.getY())) {
      e.consume();
      toolIndex = (int) ((e.getX() - r.x) / (r.w / 9));
    }
  }

  public void mouseDragged(MouseEvent e) {
    Rect r = getDrawBounds();
    toolIndex = (int) ((e.getX() - r.x) / (r.w / 9));
    toolIndex = Math.max(toolIndex, 0);
    toolIndex = Math.min(toolIndex, 8);
  }

  private Rect getDrawBounds() {
    int idealWidth = getHeight() * 9 + 26;
    return new Rect((getWidth() - idealWidth) / 2, 0, idealWidth, getHeight());
  }

  @Override
  protected void paintComponent(Graphics gg) {
    Graphics3D g = Graphics3D.create(gg);

    Rect r = getDrawBounds();

    g.color(0, 0, 0, 100).fill(r);
    g.setStroke(5.0).color(Color.DARK_GRAY).draw(r.grow(-2, -2));

    double widthPerBox = r.w / 9;
    g.setStroke(5.0);

    for (int i = 1; i < 9; i++) {
      double x = r.x + i * widthPerBox;
      g.line(x, 0, x, getHeight());
    }

    g.color(Color.lightGray).draw(new Rect(r.x + toolIndex * widthPerBox, 0, widthPerBox, getHeight()).grow(-2, -2));
  }

}
