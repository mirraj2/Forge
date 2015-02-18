package armory.rez;

public interface ListenerResource extends Resource {

  public default void mousePress(int x, int y) {
  }

  public default void mouseDrag(int x, int y) {
  }

  public default void mouseMove(int x, int y) {
  }

  public default void mouseRelease(int x, int y) {
  }

}
