import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Recolor {

    private static final Predicate<Color> mask = c -> c.getAlpha() > 200 && c.getGreen() > 150;

    static final List<Color> rainbowColors = new ArrayList<>() {{
        for (int i = 0; i < Constants.HUE_COARSENESS; i++) {
            float hue = (float) 1 / Constants.HUE_COARSENESS * i;
            int rgb = Color.HSBtoRGB(hue ,1.0f, 1.0f);
            add(new Color(rgb));
        }
    }};


    public static BufferedImage recolor(BufferedImage in, int newColor) {
        int xSize = in.getWidth();
        int ySize = in.getHeight();
        BufferedImage out = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Color originalColor = new Color(in.getRGB(x, y), true);
                if (mask.test(originalColor)) {
                    out.setRGB(x, y, getRgbForHue(newColor));
                } else {
                    out.setRGB(x, y, originalColor.getRGB());
                }
            }
        }
        return out;
    }

    private static int getRgbForHue(int hue) {
        return rainbowColors.get(hue - 1).getRGB();
    }

}
























