import java.awt.image.BufferedImage;

class State {
    private final BufferedImage baseImage;
    private final int hue;

    public State(BufferedImage baseImage, int hue) {
        this.baseImage = baseImage;
        this.hue = hue;
    }

    public BufferedImage getBaseImage() {
        return baseImage;
    }

    public int getHue() {
        return hue;
    }

}
