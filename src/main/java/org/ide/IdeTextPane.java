package org.ide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class IdeTextPane {
    private static String commentText;

    public JTextPane create(@Nullable String fileType) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(true);
        StyledDocument document = textPane.getStyledDocument();
        Map<String, Color> keywords;

        switch (fileType != null ? fileType : "") {
            case "java" -> {
                keywords = Keywords.Java();
                commentText = "//";
            }
            case "py" -> {
                keywords = Keywords.Python();
                commentText = "#";
            }
            default -> {
                keywords = Keywords.defaults();
                commentText = "";
            }
        }
        Map<String, Color> finalKeywords = keywords;
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightKeywords(document, finalKeywords));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightKeywords(document, finalKeywords));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not used in this
            }
        });
        return textPane;
    }

    private static void highlightKeywords(StyledDocument document, Map<String, Color> keywords) {
        document.setCharacterAttributes(0, document.getLength(), document.getStyle(StyleContext.DEFAULT_STYLE), true);
        for (String keyword : keywords.keySet()) {
            highlightAllOccurrences(document, keyword, keywords.get(keyword));
        }
        highlightCommentedLines(document);
    }

    private static AttributeSet getAttributeSetWithColor(Color color) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        return attributeSet;
    }

    private static void highlightAllOccurrences(StyledDocument document, String text, Color color) {
        String documentText;
        try {
            documentText = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            return;
        }
        int index = -1;
        while ((index = documentText.indexOf(text, index + 1)) != -1) {
            boolean validSurrounding = isSurroundedByValidCharacters(documentText, index, text.length());
            boolean commentedOut = isLineCommented(documentText, index);
            if (validSurrounding && !commentedOut) {
                document.setCharacterAttributes(index, text.length(), getAttributeSetWithColor(color), true);
            }
        }
    }

    private static void highlightCommentedLines(StyledDocument document) {
        Element root = document.getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element line = root.getElement(i);
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset() - 1;
            try {
                String lineText = document.getText(lineStart, lineEnd - lineStart);
                if (lineText.trim().startsWith(commentText)) {
                    document.setCharacterAttributes(lineStart, lineEnd - lineStart, getAttributeSetWithColor(Color.ORANGE), true);
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean isSurroundedByValidCharacters(String text, int startIndex, int keywordLength) {
        int beforeIndex = startIndex - 1;
        int afterIndex = startIndex + keywordLength;
        char charBefore = beforeIndex >= 0 ? text.charAt(beforeIndex) : '\0';
        char charAfter = afterIndex < text.length() ? text.charAt(afterIndex) : '\0';
        boolean isValidBefore = Character.isWhitespace(charBefore) || charBefore == '}' || charBefore == ')' || charBefore == '\0';
        boolean isValidAfter = Character.isWhitespace(charAfter) || charAfter == '{' || charAfter == '(' || charAfter == '\0';
        return isValidBefore && isValidAfter;
    }

    private static int getLineStartIndex(String text, int index) {
        int start = index;
        while (start > 0 && text.charAt(start - 1) != '\n') {
            start--;
        }
        return start;
    }

    private static int getLineEndIndex(String text, int index) {
        int end = index;
        while (end < text.length() && text.charAt(end) != '\n') {
            end++;
        }
        return end;
    }

    private static boolean isLineCommented(String text, int keywordIndex) {
        if (commentText.length() == 0) return false;
        int lineStartIndex = getLineStartIndex(text, keywordIndex);
        if (lineStartIndex >= 0 && lineStartIndex < text.length()) {
            for (int i = 0; i < commentText.length(); i++) {
                char commentChar = commentText.charAt(i);
                int commentCharIndex = lineStartIndex + i;
                if (commentCharIndex < text.length() && text.charAt(commentCharIndex) != commentChar) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}