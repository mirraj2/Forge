package forge;

import jasonlib.Rect;

public class TileGrid {

  private int xOffset = 0;
  private int[] yOffsets = new int[0];
  private boolean[][] grid = new boolean[0][0];

  public Rect bounds = null;

  public void translate(int dx, int dy) {
    bounds = bounds.translate(dx, dy);
    xOffset += dx;
    for (int i = 0; i < yOffsets.length; i++) {
      yOffsets[i] += dy;
    }
  }

  public void add(int i, int j) {
    expandGridTo(i, j);

    grid[i - xOffset][j - yOffsets[i - xOffset]] = true;

    if (bounds == null) {
      bounds = new Rect(i, j, 0, 0);
    } else {
      bounds = bounds.expandToInclude(i, j);
    }
  }

  public boolean get(int i, int j) {
    i -= xOffset;
    if (i < 0 || i >= yOffsets.length) {
      return false;
    }
    j -= yOffsets[i];

    boolean[] t = grid[i];
    if (t == null || j < 0 || j >= t.length) {
      return false;
    }

    return t[j];
  }

  private void expandGridTo(int i, int j) {
    expandXAxisTo(i);

    i -= xOffset;

    boolean[] t = grid[i];
    if (t == null) {
      grid[i] = new boolean[] { true };
      yOffsets[i] = j;
      return;
    }

    j -= yOffsets[i];
    if (j >= 0 && j < t.length) {
      return;
    }

    int oldSize = t.length;
    int newSize;
    int insertOffset = 0;
    if (j >= 0) {
      newSize = j + 1;
    } else {
      newSize = oldSize - j;
      insertOffset = -j;
      yOffsets[i] += j;
    }

    boolean[] newTiles = new boolean[newSize];
    for (int n = 0; n < oldSize; n++) {
      newTiles[n + insertOffset] = t[n];
    }
    grid[i] = newTiles;
  }

  private void expandXAxisTo(int i) {
    i -= xOffset;
    if (i >= 0 && i < yOffsets.length) {
      return;
    }

    int oldSize = yOffsets.length;
    int newSize;
    int insertOffset = 0;

    if (i >= 0) {
      newSize = i + 1;
    } else {
      newSize = oldSize - i;
      insertOffset = -i;
      xOffset += i;
    }

    boolean[][] newGrid = new boolean[newSize][];
    int[] newYOffsets = new int[newSize];

    // copy old data in
    for (int n = 0; n < oldSize; n++) {
      newGrid[n + insertOffset] = grid[n];
      newYOffsets[n + insertOffset] = yOffsets[n];
    }

    grid = newGrid;
    yOffsets = newYOffsets;
  }

}
