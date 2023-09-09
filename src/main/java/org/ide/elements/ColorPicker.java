package org.ide.elements;

import org.ide.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;


public class ColorPicker extends JFrame {
    private final JPanel colorPanel;
    private Color selectedColor;
    private final ArrayList<Pair<Events, Function<Color, ?>>> events = new ArrayList<>();

    public ColorPicker(Color color, String name) {
        setTitle(name);
        setSize(300, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton colorButton = new JButton("Pick a Color");
        JButton saveButton = new JButton("Save");
        colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(100, 100));

        colorButton.addActionListener(e -> {
            selectedColor = JColorChooser.showDialog(ColorPicker.this, "Choose a Color", color);
            if (selectedColor != null) {
                colorPanel.setBackground(selectedColor);
                events.forEach(event -> {
                    if (event.first() == Events.Change) event.second().apply(selectedColor);
                });
            }
        });

        saveButton.addActionListener(e -> events.forEach(event -> {
            if (selectedColor != null && event.first() == Events.Save) event.second().apply(selectedColor);
            dispose();
        }));

        setLayout(new FlowLayout());
        add(colorButton);
        add(saveButton);
        add(colorPanel);
    }

    public void addEvent(Events event, Function<Color, ?> action) {
        events.add(new Pair<>(event, action));
    }

    public void setColor(Color color) {
        selectedColor = color;
        colorPanel.setBackground(color);
        events.forEach(event -> {
            if (event.first() == Events.Set) event.second().apply(selectedColor);
        });
    }

    public Color getColor() {
        if (selectedColor == null) {
            events.forEach(event -> {
                if (event.first() == Events.Get) event.second().apply(Color.WHITE);
            });
            return Color.WHITE;
        } else {
            events.forEach(event -> {
                if (event.first() == Events.Get) event.second().apply(selectedColor);
            });
            return selectedColor;
        }
    }
}
