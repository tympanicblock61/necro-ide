package org.ide.elements;

import org.ide.utils.Pair;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Optional;
import java.util.function.Function;

public class FontPicker extends JFrame{

    private Font currentFont;
    public static Font[] fonts = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()).map(font -> font.deriveFont(12f)).toArray(Font[]::new);
    private final ArrayList<Pair<Events, Function<Font, ?>>> events = new ArrayList<>();
    private JLabel label;
    private JLabel fontName;
    public static boolean containsFont(Font[] fonts, Font targetFont) {
        for (Font font : fonts) {
            if (font.equals(targetFont)) {
                return true;
            }
        }
        return false;
    }

    public FontPicker(Font font, String name) {
        setTitle(name);
        setSize(500, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        JList<String> list = new JList<>(Arrays.stream(fonts).map(Font::getName).toArray(String[]::new));
        list.setCellRenderer(new FontCellRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItem = list.getSelectedValue();
                if (selectedItem != null) {
                    Optional<Font> findFont = Arrays.stream(fonts).filter(fontt -> fontt.getFontName().equals(selectedItem)).findFirst();
                    if (findFont.isPresent()) {
                        currentFont = findFont.get();
                        label.setFont(currentFont);
                        fontName.setText(currentFont.getFontName());
                        events.forEach(event -> {
                            if (event.first() == Events.Change) {
                                event.second().apply(currentFont);
                            }
                        });
                    }
                }
            }
        });

        final int FONT_MIN = 8;
        final int FONT_MAX = 56;
        final int FONT_INIT = 12;
        label = new JLabel("<html><body style='width: 200px'>Preview of current font</body></html>");
        fontName = new JLabel("");
        JButton button = new JButton("Save");
        button.setPreferredSize(new Dimension(50, 50));
        JPanel leftPanel = new JPanel(new BorderLayout());
        JSlider fontSize = new JSlider(JSlider.HORIZONTAL, FONT_MIN, FONT_MAX, FONT_INIT);
        leftPanel.add(label, BorderLayout.NORTH);
        leftPanel.add(fontName, BorderLayout.CENTER);
        leftPanel.add(button, BorderLayout.SOUTH);
        leftPanel.add(fontSize, BorderLayout.AFTER_LINE_ENDS);
        fontSize.setMajorTickSpacing(20);
        fontSize.setMinorTickSpacing(2);
        fontSize.setPaintTicks(true);
        Hashtable labelTable = new Hashtable();
        labelTable.put(FONT_MIN , new JLabel(String.valueOf(FONT_MIN)));
        labelTable.put(FONT_MAX, new JLabel(String.valueOf(FONT_MAX)));
        fontSize.setLabelTable( labelTable );
        fontSize.setPaintLabels(true);

        fontSize.addChangeListener(e -> {
            int selectedFontSize = fontSize.getValue();
            Font[] updatedFonts = Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                    .map(fnt -> fnt.deriveFont((float) selectedFontSize))
                    .toArray(Font[]::new);
            fonts = updatedFonts;
            if (currentFont != null) {
                Optional<Font> findFont = Arrays.stream(fonts).filter(fnt -> fnt.getFontName().equals(currentFont.getFontName())).findFirst();
                if (findFont.isPresent()) {
                    currentFont = findFont.get();
                    label.setFont(currentFont);
                    fontName.setText(currentFont.getFontName());
                }
            }
        });

        button.addActionListener(e -> {
            if (currentFont != null) {
                events.forEach(event -> {
                    if (event.first() == Events.Save) {
                        event.second().apply(currentFont);
                    }
                });
            }
            dispose();
        });
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        add(mainPanel);
        if (containsFont(fonts, font)) {
            currentFont = font;
        } else {
            currentFont = new Font(Font.DIALOG, Font.PLAIN, fontSize.getValue());
        }
        label.setFont(currentFont);
        fontName.setText(currentFont.getFontName());
    }

    public void addEvent(Events event, Function<Font, ?> action) {
        events.add(new Pair<>(event, action));
    }

    public void setFont(Font font) {
        try {
            currentFont = font;
            label.setFont(currentFont);
            fontName.setText(currentFont.getFontName());
            events.forEach(event -> {
                if (event.first() == Events.Set) {
                    event.second().apply(currentFont);
                }
            });
        } catch(Exception ignored) {}
    }

    public Font getFont() {
        if (currentFont == null) {
            events.forEach(event -> {
                if (event.first() == Events.Get) {
                    event.second().apply(new Font(Font.DIALOG, Font.PLAIN, currentFont.getSize()));
                }
            });
            return new Font(Font.DIALOG, Font.PLAIN, currentFont.getSize());
        } else {
            events.forEach(event -> {
                if (event.first() == Events.Get) {
                    event.second().apply(currentFont);
                }
            });
            return currentFont;
        }
    }

    static class FontCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String item = (String) value;
            for (Font font : fonts) {
                if (font.getFontName().equals(item)) {
                    renderer.setFont(font);
                }
            }
            ((JLabel) renderer).setBorder(new EmptyBorder(2, 5, 2, 5));
            return renderer;
        }
    }
}
