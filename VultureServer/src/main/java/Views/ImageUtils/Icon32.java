package Views.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Icon32 extends ImageIcon {
    public Icon32(URL f) {
        super(f);

        BufferedImage i= new BufferedImage(getImage().getWidth(null), getImage().getHeight(null),
                BufferedImage.TYPE_INT_RGB);


        Graphics2D g2d = (Graphics2D) i.getGraphics();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(getImage(), 0, 0, getImage().getWidth(null), getImage().getHeight(null), null);
        setImage(i);
    }

    public int getIconHeight() {
        return getImage().getHeight(null);
    }

    public int getIconWidth() {
        return getImage().getWidth(null);
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(getImage(), x, y, c);
    }
}
