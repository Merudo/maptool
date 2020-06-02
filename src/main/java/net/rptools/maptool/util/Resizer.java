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
package net.rptools.maptool.util;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Resizer {
  private static final double pRatio = 0.5;

  private static BufferedImage scaleImage(BufferedImage source, int width, int height) {
    BufferedImage img = new BufferedImage(width, height, source.getType());
    Graphics2D g = img.createGraphics();
    try {
      g.drawImage(source, 0, 0, width, height, null);
    } finally {
      g.dispose();
    }
    return img;
  }

  private static BufferedImage applyTransform(BufferedImage img, AffineTransform xform) {
    AffineTransformOp opxForm = new AffineTransformOp(xform, AffineTransformOp.TYPE_BILINEAR);
    return opxForm.filter(img, null);
  }

  public static boolean drawImageProgressive(
      Graphics2D g,
      BufferedImage img,
      AffineTransform xform,
      double scaleX,
      double scaleY,
      java.awt.image.ImageObserver obs) {
    double newScaleX = Math.max(pRatio, scaleX);
    double newScaleY = Math.max(pRatio, scaleY);

    xform.scale(newScaleX, newScaleY);
    while (newScaleX > scaleX || newScaleY > scaleY) {
      img = applyTransform(img, xform);

      double preScaleX = newScaleX;
      double preScaleY = newScaleY;
      newScaleX = Math.max(preScaleX * pRatio, scaleX);
      newScaleY = Math.max(preScaleY * pRatio, scaleY);

      xform = new AffineTransform();
      xform.scale(newScaleX / preScaleX, newScaleY / preScaleY);
    }
    return g.drawImage(img, xform, obs);
  }

  public static boolean drawImageProgressive(
      Graphics2D g,
      BufferedImage img,
      int x,
      int y,
      int width,
      int height,
      java.awt.image.ImageObserver obs) {
    int w = Math.max((int) (img.getWidth() * pRatio), width);
    int h = Math.max((int) (img.getHeight() * pRatio), height);

    while (w > width || h > height) {
      img = scaleImage(img, w, h);

      w = Math.max((int) (w * pRatio), width);
      h = Math.max((int) (h * pRatio), height);
    }
    return g.drawImage(img, x, y, width, height, obs);
  }
}
