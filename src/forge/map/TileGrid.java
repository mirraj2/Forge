package forge.map;

import static com.google.common.base.Preconditions.checkState;

import java.nio.ByteBuffer;
import java.util.Base64;

import ox.Rect;

public class TileGrid {

  private int xOffset;
  private int[] yOffsets;
  private boolean[][] grid;

  public Rect bounds = null;

  public TileGrid() {
    this(0, new int[0], new boolean[0][0]);
  }

  private TileGrid(int xOffset, int[] yOffsets, boolean[][] grid) {
    this.xOffset = xOffset;
    this.yOffsets = yOffsets;
    this.grid = grid;

    for (int i = 0; i < grid.length; i++) {
      boolean[] col = grid[i];
      if (col != null) {
        for (int j = 0; j < col.length; j++) {
          expandBounds(i + xOffset, j + yOffsets[i]);
        }
      }
    }
  }

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

    expandBounds(i, j);
  }

  public void remove(int i, int j) {
    int x = i - xOffset;
    if (x < 0 || x >= grid.length) {
      return;
    }
    int y = j - yOffsets[x];
    boolean[] col = grid[x];
    if (y < 0 || y >= col.length) {
      return;
    }
    col[y] = false;
  }

  private void expandBounds(int i, int j) {
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

  public String serialize() {
    int bytesNeeded = 2 + 2 + yOffsets.length * 2; // offsets
    for (int i = 0; i < grid.length; i++) {
      boolean[] col = grid[i];
      bytesNeeded += 2; // column height
      bytesNeeded += col == null ? 0 : (grid[i].length + 7) / 8; // pack 8 booleans into a byte
    }

    // Log.debug("allocating buffer with %d bytes", bytesNeeded);

    ByteBuffer b = ByteBuffer.allocate(bytesNeeded);

    b.putShort((short) xOffset);
    b.putShort((short) yOffsets.length);
    for (int i = 0; i < grid.length; i++) {
      b.putShort((short) yOffsets[i]);
      boolean[] col = grid[i];
      if (col == null) {
        b.putShort((short) 0);
        continue;
      }
      b.putShort((short) col.length);

      for (int j = 0; j < col.length; j += 8) {
        byte bits = 0;
        for (int k = 0; k < 8 && j + k < col.length; k++) {
          if (col[j + k]) {
            bits |= 1 << k;
          }
        }
        b.put(bits);
      }
    }

    checkState(b.position() == bytesNeeded, "Expected buffer to have %d, but it had %d", bytesNeeded, b.position());

    return Base64.getEncoder().encodeToString(b.array());
  }

  public static TileGrid parse(String s) {
    ByteBuffer b = ByteBuffer.wrap(Base64.getDecoder().decode(s));

    int xOffset = b.getShort();
    int[] yOffsets = new int[b.getShort()];
    boolean[][] grid = new boolean[yOffsets.length][];
    for (int i = 0; i < yOffsets.length; i++) {
      yOffsets[i] = b.getShort();
      boolean[] col = new boolean[b.getShort()];
      for (int j = 0; j < col.length; j += 8) {
        byte bits = b.get();
        for (int k = 0; k < 8 && j + k < col.length; k++) {
          if ((bits & (1 << k)) > 0) {
            col[j + k] = true;
          }
        }
      }
      grid[i] = col;
    }

    return new TileGrid(xOffset, yOffsets, grid);
  }

}
