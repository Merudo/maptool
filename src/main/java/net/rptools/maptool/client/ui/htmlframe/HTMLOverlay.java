/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.model.Token;

/**
 * Represents a JDialog holding an HTML panel. Can hold either an HTML3.2 (Swing) or a HTML5
 * (JavaFX) panel.
 */
@SuppressWarnings("serial")
public class HTMLOverlay implements HTMLPanelContainer {
  /** The static map of the HTMLDialogs. */
  private static final Map<String, HTMLOverlay> overlays = new HashMap<>();

  /** The map of the macro callbacks. */
  private final Map<String, String> macroCallbacks = new HashMap<String, String>();

  /** The temporary status of the dialog. A temporary dialog isn't stored after being closed. */
  private boolean temporary;

  /** The value stored in the frame. */
  private Object value;

  /** Panel for HTML. */
  private HTMLPane pane;

  /** The name of the frame. */
  private final String name;

  /** Can the dialog be resized? */
  private final boolean canResize = true;

  @Override
  public boolean isVisible() {
    return pane.isVisible();
  }

  @Override
  public Map<String, String> macroCallbacks() {
    return macroCallbacks;
  }

  @Override
  public void setVisible(boolean visible) {
    pane.setVisible(visible);
  }

  /**
   * Return whether the frame is visible or not.
   *
   * @param name the name of the frame.
   * @return true if the frame is visible.
   */
  static boolean isVisible(String name) {
    if (overlays.containsKey(name)) {
      return overlays.get(name).isVisible();
    }
    return false;
  }

  /**
   * Request that the frame close.
   *
   * @param name The name of the frame.
   */
  static void close(String name) {
    if (overlays.containsKey(name)) {
      overlays.get(name).closeRequest();
    }
  }

  /**
   * Create a HTMLDialog
   *
   * @param name the name of the dialog
   * @param width the width of the dialog
   * @param height the height of the dialog
   */
  private HTMLOverlay(String name) {
    this.name = name;
    pane = new HTMLPane(true);
    JLayeredPane jLayeredPane = MapTool.getFrame().getZoneRenderLayered();
    jLayeredPane.setLayer(pane, MapToolFrame.LayeredPaneLayout.OVERLAY_LAYER);
  }
  /**
   * Shows the HTML Dialog. This will create a new dialog if the named dialog does not already
   * exist. The width and height fields are ignored if the dialog has already been opened so that it
   * will not override any resizing that the user may have done.
   *
   * @param name the name of the dialog.
   * @param width the width in pixels of the dialog.
   * @param height the height in pixels of the dialog.
   * @param xOffset the x offset.
   * @param yOffset the y offset.
   * @param value a value to be returned by getDialogProperties()
   * @param html the HTML to display in the dialog
   * @return the dialog
   */
  public static HTMLOverlay showOverlay(
      String name,
      int width,
      int height,
      int xOffset,
      int yOffset,
      MapToolFrame.LayeredLayoutHorizontal borderH,
      MapToolFrame.LayeredLayoutVertical borderV,
      Object value,
      String html) {
    HTMLOverlay overlay;
    if (overlays.containsKey(name)) {
      overlay = overlays.get(name);
    } else {
      overlay = new HTMLOverlay(name);
      overlays.put(name, overlay);
    }
    overlay.updateContents(width, height, xOffset, yOffset, borderH, borderV, html, value);

    // dialog.canResize = false;
    if (!overlay.isVisible()) {
      overlay.setVisible(true);
    }
    return overlay;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean getTemporary() {
    return this.temporary;
  }

  @Override
  public void setTemporary(boolean temp) {
    this.temporary = temp;
  }

  /** Run all callback macros for "onChangeSelection". */
  public static void doSelectedChanged() {
    for (HTMLOverlay overlay : overlays.values()) {
      if (overlay.isVisible()) {
        HTMLPanelContainer.selectedChanged(overlay.macroCallbacks());
      }
    }
  }

  /** Run all callback macros for "onChangeImpersonated". */
  public static void doImpersonatedChanged() {
    for (HTMLOverlay overlay : overlays.values()) {
      if (overlay.isVisible()) {
        HTMLPanelContainer.impersonatedChanged(overlay.macroCallbacks());
      }
    }
  }

  /**
   * Run all callback macros for "onChangeToken".
   *
   * @param token the token that changed.
   */
  public static void doTokenChanged(Token token) {
    if (token != null) {
      for (HTMLOverlay overlay : overlays.values()) {
        if (overlay.isVisible()) {
          HTMLPanelContainer.tokenChanged(token, overlay.macroCallbacks());
        }
      }
    }
  }

  /**
   * Updates the contents of the dialog.
   *
   * @param html the html contents of the dialog
   * @param val the value held in the frame
   */
  private void updateContents(
      int width,
      int height,
      int xOffset,
      int yOffset,
      MapToolFrame.LayeredLayoutHorizontal borderH,
      MapToolFrame.LayeredLayoutVertical borderV,
      String html,
      Object val) {

    this.value = val;
    macroCallbacks.clear();
    pane.updateContents(html);

    MapToolFrame.LayeredLayoutLocation location =
        new MapToolFrame.LayeredLayoutLocation(xOffset, yOffset, borderH, borderV);

    JLayeredPane jLayeredPane = MapTool.getFrame().getZoneRenderLayered();
    jLayeredPane.remove(pane);
    jLayeredPane.add(pane, location);

    if (width >= 0 && height >= 0) {
      pane.setPreferredSize(new Dimension(width, height));
    } else if (width >= 0 || height >= 0) {
      if (width < 0) width = 100;
      if (height < 0) height = 100;
      pane.setPreferredSize(new Dimension(width, height));
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e instanceof HTMLActionEvent.FormActionEvent) {
      HTMLActionEvent.FormActionEvent fae = (HTMLActionEvent.FormActionEvent) e;
      MacroLinkFunction.runMacroLink(fae.getAction() + fae.getData());
    }
    if (e instanceof HTMLActionEvent.RegisterMacroActionEvent) {
      HTMLActionEvent.RegisterMacroActionEvent rmae = (HTMLActionEvent.RegisterMacroActionEvent) e;
      macroCallbacks.put(rmae.getType(), rmae.getMacro());
    }

    if (e instanceof HTMLActionEvent.MetaTagActionEvent) {
      String name = ((HTMLActionEvent.MetaTagActionEvent) e).getName();
      String content = ((HTMLActionEvent.MetaTagActionEvent) e).getContent();
      if (name.equalsIgnoreCase("input")) {
      } else if (name.equalsIgnoreCase("onChangeToken")
          || name.equalsIgnoreCase("onChangeSelection")
          || name.equalsIgnoreCase("onChangeImpersonated")) {
        macroCallbacks.put(name, content);
      } else if (name.equalsIgnoreCase("width")) {
        if (canResize) {
          pane.setPreferredSize(
              new Dimension(Integer.parseInt(content), pane.getPreferredSize().height));
        }
      } else if (name.equalsIgnoreCase("height")) {
        if (canResize) {
          pane.setPreferredSize(
              new Dimension(pane.getPreferredSize().width, Integer.parseInt(content)));
        }
      } else if (name.equalsIgnoreCase("value")) {
        setValue(content);
      }
    }
    if (e.getActionCommand().equals("Close")) {
      closeRequest();
    }
  }

  @Override
  public void closeRequest() {
    setVisible(false);
    pane.flush();
    if (temporary) {
      overlays.remove(this.name);
    }
  }

  @Override
  public Component add(Component component) {
    return component;
  }

  @Override
  public void remove(Component component) {}
}
