package forge.map;

import java.awt.Point;

public class Autotiles {

  public static Point EMPTY = new Point(-2, -2);
  public static Point CENTER = new Point(32, 64);

  public static Point compute(int i, int j, TileGrid grid, int cliffHeight) {
    Point p = getNormalAutotile(i, j, grid);

    if (p == EMPTY) {
      p = getCliffAutotile(i, j, grid, cliffHeight);
    }

    return p;
  }

  public static Point getCliffAutotile(int i, int j, TileGrid grid, int cliffHeight) {
    if (cliffHeight == 0) {
      return EMPTY;
    }

    Point above = null;
    int k; // how far up we found the cliff ledge
    for (k = 0; k < cliffHeight; k++) {
      above = getNormalAutotile(i, j - k - 1, grid);
      if (above != EMPTY) {
        if (above.y != 80) {
          return EMPTY;
        }
        break;
      }
    }

    if (above == EMPTY) {
      return EMPTY;
    }

    if (k == cliffHeight - 1) {
      // this is at the base
      return new Point(i % 2 == 0 ? 16 : 32, 144);
    } else if (k == 0) {
      // this is right below the ledge
      return new Point(i % 2 == 0 ? 16 : 32, 96);
    } else {
      return new Point(i % 2 == 0 ? 16 : 32, k % 2 == 1 ? 112 : 128);
    }
  }

  public static Point getNormalAutotile(int i, int j, TileGrid grid) {
    if (grid.get(i, j)) {
      return new Point(i % 2 == 0 ? 16 : 32, j % 2 == 0 ? 48 : 64);
      // return CENTER;
    }

    boolean north = grid.get(i, j - 1);
    boolean east = grid.get(i + 1, j);
    boolean south = grid.get(i, j + 1);
    boolean west = grid.get(i - 1, j);
    boolean northwest = grid.get(i - 1, j - 1);
    boolean northeast = grid.get(i + 1, j - 1);
    boolean southeast = grid.get(i + 1, j + 1);
    boolean southwest = grid.get(i - 1, j + 1);

    if (northwest && northeast) {
      north = true;
    }
    if (northeast && southeast) {
      east = true;
    }
    if (southeast && southwest) {
      south = true;
    }
    if (northwest && southwest) {
      west = true;
    }

    if ((north && south) || (west && east)) {
      return new Point(32, 64);
    }

    if (north && east && southwest) {
      return new Point(32, 64);
    }
    if (north && west && southeast) {
      return new Point(32, 64);
    }
    if (south && west && northeast) {
      return new Point(32, 64);
    }
    if (south && east && northwest) {
      return new Point(32, 64);
    }

    int count = 0;
    if (north)
      count++;
    if (east)
      count++;
    if (south)
      count++;
    if (west)
      count++;
    if (northwest)
      count++;
    if (northeast)
      count++;
    if (southeast)
      count++;
    if (southwest)
      count++;

    if (count == 0) {
      return EMPTY;
    }

    if (count == 1) {
      if (southeast) {
        return new Point(0, 0);
      } else if (southwest) {
        return new Point(16, 0);
      } else if (northwest) {
        return new Point(48, 80);
      } else if (northeast) {
        return new Point(0, 80);
      }
    }

    if ((north && (east || southeast)) || (east && (north || northwest))) {
      return new Point(32, 16);
    }
    if ((east && (south || southwest)) || (south && (east || northeast))) {
      return new Point(32, 0);
    }
    if ((west && (south || southeast)) || (south && (west || northwest))) {
      return new Point(48, 0);
    }
    if ((north && (west || southwest)) || (west && (north || northeast))) {
      return new Point(48, 16);
    }

    if ((south || southeast || southwest) && !north && !northwest && !northeast && !west && !east) {
      return new Point(16, 32);
    }
    if ((east || northeast || southeast) && !west && !northwest && !southwest && !north && !south) {
      return new Point(0, 48);
    }
    if ((north || northwest || northeast) && !south && !southeast && !southwest && !west && !east) {
      return new Point(16, 80);
    }
    if ((west || northwest || southwest) && !east && !northeast && !southeast && !north && !south) {
      return new Point(48, 48);
    }

    return CENTER;
  }

}
