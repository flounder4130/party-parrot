import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class Parrot extends JFrame {

    private int hue = Constants.STARTING_HUE;
    private boolean isRainbowMode = false;
    private int rotationInterval = 1000 / Constants.STARTING_SPEED;
    private int currentParrotIndex = 0;
    private final List<BufferedImage> parrots = new ArrayList<>();
    private final Map<State, BufferedImage> cache = new HashMap<>();

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    private final JLabel parrot = new JLabel();

    public static void main(String[] args) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        Files.list(Paths.get(Constants.PARROTS_PATH)).sorted(Comparator.comparingInt(value -> {
            //noinspection CodeBlock2Expr
            return Integer.parseInt(value.toFile().getName().split("\\.")[0]);
        })).forEach(path -> {
            try {
                images.add(ImageIO.read(path.toFile()));
            } catch (IOException e) {
                images.add(null);
            }
        });
        new Parrot("Hello parrot", images).configureAndShow();
    }

    public Parrot(String title, List<BufferedImage> parrots) throws HeadlessException {
        super(title);
        this.parrots.addAll(parrots);
    }

    private void configureAndShow() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(createPanel());
        setPreferredSize(new Dimension(256, 256));
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdown();
            }
        });

        submitUpdateParrotTask();
        submitUpdateHueTask();
    }

    private void submitUpdateParrotTask() {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                updateParrot();
                executor.schedule(this, getRotationInterval(), TimeUnit.MILLISECONDS);
            }
        }, getRotationInterval(), TimeUnit.MILLISECONDS);
    }

    private void submitUpdateHueTask() {
        executor.scheduleAtFixedRate(() -> {
            if (isRainbowMode()) {
                updateHue();
            }
        }, 0, Constants.HUE_INCREMENT_SPEED_MILLIS, TimeUnit.MILLISECONDS);
    }

    public void updateParrot() {
        currentParrotIndex = (currentParrotIndex + 1) % parrots.size();
        BufferedImage baseImage = parrots.get(currentParrotIndex);
        State state = new State(baseImage, getHue());
        BufferedImage coloredImage = cache.computeIfAbsent(state, (s) -> Recolor.recolor(baseImage, hue));
        parrot.setIcon(new ImageIcon(coloredImage));
    }

    public void updateHue() {
        hue = (hue >= 100) ? 1 : (hue + 1);
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public boolean isRainbowMode() {
        return isRainbowMode;
    }

    public void setRainbowMode(boolean rainbowMode) {
        isRainbowMode = rainbowMode;
    }

    public int getRotationInterval() {
        return rotationInterval;
    }

    public void setRotationInterval(int rotationInterval) {
        this.rotationInterval = rotationInterval;
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, Constants.MIN_SPEED, Constants.MAX_SPEED, Constants.STARTING_SPEED);
        speedSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                setRotationInterval(1000 / source.getValue());
            }
        });

        JSlider hueSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, Constants.STARTING_HUE);
        hueSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                setHue(source.getValue());
            }
        });

        JCheckBox checkBox = new JCheckBox("Rainbow mode");
        checkBox.addItemListener(e -> setRainbowMode(e.getStateChange() == ItemEvent.SELECTED));

        panel.add(speedSlider);
        panel.add(hueSlider);
        panel.add(checkBox);
        panel.add(parrot);

        return panel;
    }
}
