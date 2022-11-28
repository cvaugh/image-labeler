package dev.cvaugh.imagelabeler;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LabeledImage {
    public final BufferedImage image;
    public File file;
    public Label label;

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
}
