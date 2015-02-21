package forge.ui;

import jasonlib.Config;
import jasonlib.OS;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import forge.Exporter;

public class MenuOptions {

  private final Forge forge;

  public MenuOptions(Forge forge) {
    this.forge = forge;

    JMenuBar bar = new JMenuBar();

    JMenu fileMenu = new JMenu("Export As");
    fileMenu.add(new JMenuItem(codeFormat));
    fileMenu.add(new JMenuItem(exportImage));
    bar.add(fileMenu);

    Forge.frame.setJMenuBar(bar);
  }

  private final Action codeFormat = new AbstractAction("Game-Ready Format") {
    @Override
    public void actionPerformed(ActionEvent e) {
      File dir = getExportDir();
      if (dir == null) {
        return;
      }
      new Exporter(forge).export(dir);
    }
  };

  private File getExportDir() {
    Config config = Config.load("forge");
    String s = config.get("export_dir");
    if (s != null) {
      return new File(s);
    }
    JFileChooser jfc = new JFileChooser(OS.getDesktop());
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setMultiSelectionEnabled(false);
    int i = jfc.showSaveDialog(null);
    if (i != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    File file = jfc.getSelectedFile();
    if (file.getName().equals(file.getParentFile().getName())) {
      file = file.getParentFile();
    }

    file.mkdirs();

    config.put("export_dir", file.getPath());

    return file;
  }

  private final Action exportImage = new AbstractAction("PNG Image") {
    @Override
    public void actionPerformed(ActionEvent e) {
      new Exporter(forge).exportPNG(new File(OS.getDesktop(), "export.png"));
    }
  };

}
