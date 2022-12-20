package dev.cvaugh.imagelabeler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import javax.imageio.ImageIO;

public class LabeledImage implements Comparable<LabeledImage> {
    public final BufferedImage image;
    public File file;
    public Label label = Label.NONE;
    public Color averageColor;

    public LabeledImage(File file) throws IOException {
        BufferedImage temp = ImageIO.read(file);
        int w = temp.getWidth() / Main.IMAGE_SCALE;
        int h = temp.getHeight() / Main.IMAGE_SCALE;
        Image scaled = temp.getScaledInstance(w, h, Image.SCALE_FAST);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        this.file = file;
        temp = null;
        scaled = null;
        label = Labels.get(file.getAbsolutePath());
    }

    public void label(Label c) {
        this.label = c;
        Labels.set(file.getAbsolutePath(), c);
    }

    /**
     * Adapted from <a href="https://stackoverflow.com/a/28162725">
     *      https://stackoverflow.com/a/28162725
     * </a>
     */
    public void calculateAverageColor() {
        long r = 0, g = 0, b = 0;
        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                r += (rgb >> 16) & 0x000000FF;
                g += (rgb >> 8) & 0x000000FF;
                b += (rgb) & 0x000000FF;
            }
        }
        int n = image.getWidth() * image.getHeight();
        long ra = r / n;
        long ga = g / n;
        long ba = b / n;
        averageColor = new Color((float) ra / 255.0f, (float) ga / 255.0f, (float) ba / 255.0f);
    }

    private int getAverageRed() {
        return averageColor.getRed();
    }

    private int getAverageGreen() {
        return averageColor.getGreen();
    }

    private int getAverageBlue() {
        return averageColor.getBlue();
    }

    @Override
    public int compareTo(LabeledImage o) {
        return Comparator.comparing(LabeledImage::getAverageRed).thenComparing(LabeledImage::getAverageGreen)
                .thenComparing(LabeledImage::getAverageBlue).compare(this, o);
    }
}
