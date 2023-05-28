package org.ide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;

public class IdeTextPane {

    private static JTextPane create() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(true);

        StyledDocument document = textPane.getStyledDocument();

        // Style for keyword highlighting
        Style keywordStyle = document.addStyle("Keyword", null);
        StyleConstants.setForeground(keywordStyle, Color.BLUE);

        // Keywords to highlight
        String[] keywords = {"if", "else", "while", "for", "int", "double", "String"};

        // Add document listener for keyword highlighting
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    highlightKeywords(document, keywordStyle, keywords);
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    highlightKeywords(document, keywordStyle, keywords);
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not used in this example
            }
        });

        return textPane;
    }

    private static void highlightKeywords(StyledDocument document, Style keywordStyle, String[] keywords) {
        // Reset keyword highlighting
        document.setCharacterAttributes(0, document.getLength(), document.getStyle(StyleContext.DEFAULT_STYLE), true);

        // Highlight keywords
        for (String keyword : keywords) {
            highlightAllOccurrences(document, keyword, keywordStyle);
        }
    }

    private static void highlightAllOccurrences(StyledDocument document, String text, Style style) {
        String documentText;
        try {
            documentText = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            return;
        }

        int index = -1;
        while ((index = documentText.indexOf(text, index + 1)) != -1) {
            document.setCharacterAttributes(index, text.length(), style, true);
        }
    }
}
