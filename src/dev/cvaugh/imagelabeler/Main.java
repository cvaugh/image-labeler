package dev.cvaugh.imagelabeler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class Main {
    public static final int IMAGE_SCALE = 6;
    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK, 1);

    private static JFrame frame = new JFrame();
    private static JLabel currentLabel = new JLabel();
    private static JPanel rightPanel = new JPanel();
    private static JScrollPane rightScroll = new JScrollPane(rightPanel);

    private static List<LabeledImage> currentImages = new ArrayList<>();
    private static LabeledImage current = null;
    private static int currentIndex = 0;
    private static int labelSize = 600;
    private static String currentPath = "";
    private static Label showOnly = Label.NONE;
    private static File currentRoot = null;

    public static void main(String[] args) {
        File startDirectory = new File(".");
        if(args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for(String arg : args) {
                sb.append(arg);
                sb.append(" ");
            }
            startDirectory = new File(sb.toString().trim());
        }
        if(!startDirectory.exists()) {
            System.err.println("Dataset root does not exist: " + startDirectory.getAbsolutePath());
            System.exit(1);
        }
        if(!startDirectory.isDirectory()) {
            System.err.println("Dataset root must be a directory: " + startDirectory.getAbsolutePath());
            System.exit(1);
        }
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FILE_CHOOSER.setCurrentDirectory(startDirectory);
        try {
            Labels.load();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        frame.getContentPane().setLayout(new GridLayout(1, 2));
        initMenuBar();
        initComponents();
        frame.setTitle("Image Labeler");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    Labels.save();
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
                System.exit(0);
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_O) {
                    openDir();
                } else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                    try {
                        Labels.save();
                    } catch(IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Failed to save labels", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                } else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_R) {
                    if(currentRoot != null) {
                        openDir(currentRoot);
                    }
                } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                    setCurrent(currentIndex - 1);
                } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    setCurrent(currentIndex + 1);
                } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                    setCurrent(currentIndex - 10);
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                    setCurrent(currentIndex + 10);
                } else if(current != null) {
                    if(e.getKeyCode() == KeyEvent.VK_1 || e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
                        current.label(Label.FACE);
                        nextFace();
                    } else if(e.getKeyCode() == KeyEvent.VK_2 || e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
                        current.label(Label.NOT_FACE);
                        nextFace();
                    } else if(e.getKeyCode() == KeyEvent.VK_3 || e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
                        current.label(Label.AMBIGUOUS);
                        nextFace();
                    } else if(e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_NUMPAD0) {
                        current.label(Label.NONE);
                        nextFace();
                    }
                }
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem = new JMenuItem("Open");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.addActionListener(e -> {
            openDir();
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem("Save");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(e -> {
            try {
                Labels.save();
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(frame, "Failed to save labels", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                return;
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(frame, "Save before exiting?", "Confirm Exit",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if(r == JOptionPane.CANCEL_OPTION) {
                return;
            } else if(r == JOptionPane.YES_OPTION) {
                try {
                    Labels.save();
                } catch(IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to save labels", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                    return;
                }
            }
            System.exit(0);
        });
        fileMenu.add(menuItem);
        menuBar.add(fileMenu);
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        menuItem = new JMenuItem("Skip to first unlabeled");
        menuItem.setMnemonic(KeyEvent.VK_U);
        menuItem.addActionListener(e -> {
            for(int i = 0; i < currentImages.size(); i++) {
                if(currentImages.get(i).label == Label.NONE) {
                    setCurrent(i);
                    return;
                }
            }
            JOptionPane.showMessageDialog(frame, "No unlabeled images found");
        });
        toolsMenu.add(menuItem);
        JMenu showOnlyMenu = new JMenu("Show only...");
        showOnlyMenu.setMnemonic(KeyEvent.VK_O);
        toolsMenu.add(showOnlyMenu);
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem showOnlyFaces = new JRadioButtonMenuItem("Faces");
        showOnlyFaces.setMnemonic(KeyEvent.VK_F);
        showOnlyFaces.addActionListener(e -> {
            showOnly = Label.FACE;
        });
        group.add(showOnlyFaces);
        showOnlyMenu.add(showOnlyFaces);
        JRadioButtonMenuItem showOnlyNonFaces = new JRadioButtonMenuItem("Non-Faces");
        showOnlyNonFaces.setMnemonic(KeyEvent.VK_N);
        showOnlyNonFaces.addActionListener(e -> {
            showOnly = Label.NOT_FACE;
        });
        group.add(showOnlyNonFaces);
        showOnlyMenu.add(showOnlyNonFaces);
        JRadioButtonMenuItem showOnlyAmbiguous = new JRadioButtonMenuItem("Ambiguous");
        showOnlyAmbiguous.setMnemonic(KeyEvent.VK_A);
        showOnlyAmbiguous.addActionListener(e -> {
            showOnly = Label.AMBIGUOUS;
        });
        group.add(showOnlyAmbiguous);
        showOnlyMenu.add(showOnlyAmbiguous);
        JRadioButtonMenuItem showOnlyUnlabeled = new JRadioButtonMenuItem("Unlabeled");
        showOnlyUnlabeled.setMnemonic(KeyEvent.VK_U);
        showOnlyUnlabeled.addActionListener(e -> {
            showOnly = Label.NONE;
        });
        showOnlyUnlabeled.setSelected(true);
        group.add(showOnlyUnlabeled);
        showOnlyMenu.add(showOnlyUnlabeled);
        JRadioButtonMenuItem showEverything = new JRadioButtonMenuItem("Everything");
        showEverything.setMnemonic(KeyEvent.VK_E);
        showEverything.addActionListener(e -> {
            showOnly = null;
        });
        group.add(showEverything);
        showOnlyMenu.add(showEverything);
        menuItem = new JMenuItem("Reload");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.addActionListener(e -> {
            if(currentRoot != null) {
                openDir(currentRoot);
            }
        });
        toolsMenu.add(menuItem);
        menuItem = new JMenuItem("Statistics");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(e -> {
            int faceCount = 0;
            int nonFaceCount = 0;
            int ambiguousCount = 0;
            int unlabeledCount = 0;
            for(String key : Labels.registry.keySet()) {
                switch(Labels.registry.get(key)) {
                case FACE:
                    faceCount++;
                    break;
                case NOT_FACE:
                    nonFaceCount++;
                    break;
                case AMBIGUOUS:
                    ambiguousCount++;
                    break;
                default:
                    unlabeledCount++;
                    break;
                }
            }
            JOptionPane.showMessageDialog(frame,
                    String.format("Total images: %d\nFaces: %d\nNon-faces: %d\nAmbiguous: %d\nUnlabeled: %d",
                            Labels.registry.size(), faceCount, nonFaceCount, ambiguousCount, unlabeledCount),
                    "Statistics", JOptionPane.PLAIN_MESSAGE);
        });
        toolsMenu.add(menuItem);
        menuItem = new JMenuItem("Statistics (loaded images only)");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(e -> {
            int faceCount = 0;
            int nonFaceCount = 0;
            int ambiguousCount = 0;
            int unlabeledCount = 0;
            for(LabeledImage image : currentImages) {
                switch(image.label) {
                case FACE:
                    faceCount++;
                    break;
                case NOT_FACE:
                    nonFaceCount++;
                    break;
                case AMBIGUOUS:
                    ambiguousCount++;
                    break;
                default:
                    unlabeledCount++;
                    break;
                }
            }
            JOptionPane.showMessageDialog(frame,
                    String.format("Total images: %d\nFaces: %d\nNon-faces: %d\nAmbiguous: %d\nUnlabeled: %d",
                            currentImages.size(), faceCount, nonFaceCount, ambiguousCount, unlabeledCount),
                    "Statistics (loaded images only)", JOptionPane.PLAIN_MESSAGE);
        });
        toolsMenu.add(menuItem);
        menuBar.add(toolsMenu);
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        // TODO settings menu
        menuBar.add(settingsMenu);
        frame.setJMenuBar(menuBar);
    }

    private static void initComponents() {
        currentLabel.setPreferredSize(new Dimension(labelSize, labelSize));
        currentLabel.setBorder(BorderFactory.createEtchedBorder());
        frame.add(currentLabel);
        rightPanel.setLayout(new GridLayout(0, IMAGE_SCALE));
        rightPanel.setMaximumSize(new Dimension(labelSize, Integer.MAX_VALUE));
        rightScroll.setPreferredSize(currentLabel.getPreferredSize());
        rightScroll.setViewportView(rightPanel);
        rightScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        rightScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScroll.setBorder(BorderFactory.createEtchedBorder());
        rightScroll.getVerticalScrollBar().setUnitIncrement(10);
        frame.add(rightScroll);
    }

    private static void openDir() {
        int r = FILE_CHOOSER.showOpenDialog(frame);
        if(r == JFileChooser.APPROVE_OPTION) {
            File dir = FILE_CHOOSER.getSelectedFile();
            openDir(dir);
        }
    }

    private static void openDir(File dir) {
        currentRoot = dir;
        frame.setTitle("Image Labeler: Searching for images...");
        List<File> files = traverse(dir);
        frame.setTitle("Image Labeler: Loading images...");
        updateImages(files);
        currentPath = dir.getAbsolutePath();
        updateTitle();
    }

    private static List<File> traverse(File dir) {
        List<File> files = new ArrayList<>();
        for(File file : dir.listFiles()) {
            if(file.isDirectory()) {
                files.addAll(traverse(file));
            } else if(file.getName().endsWith(".jpg")) {
                files.add(file);
            }
        }
        return files;
    }

    public static void updateImages(List<File> files) {
        currentImages.clear();
        int i = 0;
        float count = (float) files.size();
        for(File file : files) {
            float percent = ((float) i / (float) count) * 100.0f;
            frame.setTitle(String.format("Image Labeler: Loading images: %.2f%%", percent));
            LabeledImage image = new LabeledImage(file);
            try {
                if(showOnly == null || image.label == showOnly) {
                    image.load();
                    currentImages.add(image);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            i++;
        }
        if(currentImages.size() == 0) {
            JOptionPane.showMessageDialog(frame, "No images were found in the selected directory", "No images found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        i = 0;
        for(LabeledImage img : currentImages) {
            float percent = ((float) i / (float) count) * 100.0f;
            frame.setTitle(String.format("Image Labeler: Averaging colors: %.2f%%", percent));
            img.calculateAverageColor();
            i++;
        }
        frame.setTitle("Image Labeler: Sorting images...");
        Collections.sort(currentImages);
        rightPanel.removeAll();
        i = 0;
        for(LabeledImage c : currentImages) {
            frame.setTitle("Image Labeler: Rendering images (" + i + " of " + currentImages.size() + ")");
            JLabel label = new JLabel();
            label.setIcon(new ImageIcon(c.image));
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            label.setBackground(c.label.color);
            label.setBorder(BORDER);
            final int j = i;
            label.addMouseListener(new MouseAdapter() {
                private final int index = j;

                @Override
                public void mousePressed(MouseEvent e) {
                    if(e.getButton() == 4 || e.getButton() == 5) {
                        setCurrent(index + 1);
                    } else if(currentIndex <= index) {
                        for(int i = currentIndex; i <= index; i++) {
                            if(e.getButton() == 1) {
                                currentImages.get(i).label(Label.FACE);
                            } else if(e.getButton() == 2) {
                                currentImages.get(i).label(Label.AMBIGUOUS);
                            } else if(e.getButton() == 3) {
                                currentImages.get(i).label(Label.NOT_FACE);
                            }
                        }
                        setCurrent(index + 1);
                    }
                }
            });
            rightPanel.add(label);
            i++;
        }
        setCurrent(0);
        frame.revalidate();
    }

    public static void setCurrent(int index) {
        currentIndex = index;
        for(int i = 0; i < currentImages.size(); i++) {
            JLabel label = (JLabel) rightPanel.getComponent(i);
            label.setBackground(currentImages.get(i).label.color);
            label.setEnabled(i >= currentIndex);
        }
        if(index < 0) {
            currentIndex = 0;
            updateTitle();
            return;
        }
        if(index >= currentImages.size()) {
            currentIndex = currentImages.size();
            JOptionPane.showMessageDialog(frame, "End of current image set reached. Press Ctrl+O to open a new set.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            updateTitle();
            return;
        }
        current = currentImages.get(index);
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(current.file);
        } catch(IOException e) {
            int w = (int) (((float) labelSize / (float) current.image.getHeight()) * (float) current.image.getWidth());
            Image scaled = current.image.getScaledInstance(w, labelSize, Image.SCALE_FAST);
            temp = new BufferedImage(w, labelSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = temp.createGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();
        }
        currentLabel.setIcon(new ImageIcon(temp));
        updateTitle();
    }

    public static void nextFace() {
        setCurrent(currentIndex + 1);
    }

    public static void updateTitle() {
        frame.setTitle(String.format("Image Labeler: %s (%d of %d)", currentPath, currentIndex, currentImages.size()));
    }
}
