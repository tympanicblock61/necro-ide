package org.ide.elements;

import org.ide.utils.Pair;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ide.Main.languageList;
import static org.ide.utils.Language.defaults;

public class IdeTextPane {
    private static String commentText;
    private static Pair<String, String> multiLineCommentText;
    private static Map<String, Color> keywords;
    private static StyledDocument document;
    private static JTextPane textPane;
    private static suggestPanel suggestPanel;
    private static int lastY;
    private static int lastX;
    private boolean doSuggestions = false;

    public JTextPane create(String fileType) {
        textPane = new JTextPane();
        textPane.setEditable(true);
        document = textPane.getStyledDocument();
        keywords = defaults();
        commentText = "";
        multiLineCommentText = new Pair<>("", "");
        languageList.forEach(language -> {
            if (language.getFileTypes().contains(fileType)) {
                keywords = language.keywords;
                commentText = language.lineComment;
                multiLineCommentText = language.multilineComment;
            }
        });
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    highlightKeywords();
                    if (doSuggestions) suggestKeyword();
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (suggestPanel.isVisible()) suggestPanel.setVisible(false);
                    highlightKeywords();
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //unused
            }
        });

        textPane.addCaretListener(e -> {
            int caretPosition = e.getDot();
            try {
                Rectangle2D caretRectangle = textPane.modelToView2D(caretPosition);
                lastX = (int)caretRectangle.getX();
                lastY = (int) caretRectangle.getY();
            } catch (BadLocationException ignored) {}
        });

        suggestPanel = new suggestPanel();
        textPane.add(suggestPanel);

        return textPane;
    }

    public void setDoSuggestions(boolean value) {
        doSuggestions = value;
    }

    public boolean isDoSuggestions() {
        return doSuggestions;
    }

    public suggestPanel getSuggestPanel() {
        return suggestPanel;
    }

    private static void suggestKeyword() {
        boolean found = false;
        int start = textPane.getCaretPosition();
        Deque<Character> reversedChars = new StringBuilder(getLineText()).reverse().chars().mapToObj(c -> (char) c).collect(Collectors.toCollection(ArrayDeque::new));
        StringBuilder word = new StringBuilder();
        while (!reversedChars.isEmpty()) {
            char character = reversedChars.pollFirst();
            if (!Character.isWhitespace(character) && character != '{' && character != '}' && character != '(' && character != ')'){
                word.append(character);
            } else break;
        }
        String toComplete = word.reverse().toString();
        if(word.isEmpty()) {
            if(suggestPanel.isVisible()) suggestPanel.setVisible(false);
            return;
        }
        suggestPanel.setLocation(lastX, lastY);
        suggestPanel.removeAll();
        for (Map.Entry<String, Color> entry : keywords.entrySet()) {
            if (entry.getKey().startsWith(toComplete) && !(entry.getKey().equals(toComplete)) && !toComplete.isEmpty()) {
                found = true;
                JButton button = new JButton(entry.getKey());
                button.setSize(new Dimension(100, 50));
                button.setForeground(entry.getValue());
                button.addActionListener(e -> {
                    removeAndInsert(start, toComplete.length(), entry.getKey(), entry.getValue());
                    int newPos = (start-toComplete.length())+entry.getKey().length();
                    textPane.setCaretPosition(newPos);
                    suggestPanel.setVisible(false);
                });
                button.setVisible(true);
                suggestPanel.add(button);
            }
        }
        suggestPanel.setVisible(found);
    }

    private static void highlightKeywords() {
        document.setCharacterAttributes(0, document.getLength(), document.getStyle(StyleContext.DEFAULT_STYLE), true);
        for (Map.Entry<String, Color> entry : keywords.entrySet()) {
            highlightAllOccurrences(entry.getKey(), entry.getValue());
        }
        highlightCommentedLines();
        highlightMultiCommentedLines();
    }

    private static AttributeSet getAttributeSetWithColor(Color color) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        return attributeSet;
    }

    private static void highlightAllOccurrences(String text, Color color) {
        String documentText = getText(document,0, document.getLength());
        int index = -1;
        while ((index = documentText.indexOf(text, index + 1)) != -1) {
            boolean validSurrounding = isSurroundedByValidCharacters(documentText, index, text.length());
            boolean commentedOut = isLineCommented(documentText, index);
            if (validSurrounding && !commentedOut) {
                document.setCharacterAttributes(index, text.length(), getAttributeSetWithColor(color), true);
            }
        }
    }

    private static void highlightCommentedLines() {
        if (commentText.isEmpty()) return;
        Element root = document.getDefaultRootElement();
        int lineCount = root.getElementCount();
        for (int i = 0; i < lineCount; i++) {
            Element line = root.getElement(i);
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset() - 1;
            String lineText = getText(document, lineStart, lineEnd - lineStart);
            if (lineText.trim().startsWith(commentText)) document.setCharacterAttributes(lineStart, lineEnd - lineStart, getAttributeSetWithColor(Color.ORANGE), true);
        }
    }

    private static void highlightMultiCommentedLines() {
        if (multiLineCommentText.first().isEmpty() && multiLineCommentText.second().isEmpty()) return;
        Element root = document.getDefaultRootElement();
        int lineCount = root.getElementCount();
        boolean commented = false;
        for (int i = 0; i < lineCount; i++) {
            Element line = root.getElement(i);
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset() - 1;
            int lineLength = lineEnd - lineStart;
            String lineText = getText(document, lineStart, lineLength);
            int commentStart = lineText.indexOf(multiLineCommentText.first());
            int commentEnd = lineText.indexOf(multiLineCommentText.second());
            if (commented) {
                if (commentEnd >= 0) {
                    int commentEndOffset = lineStart + commentEnd + multiLineCommentText.second().length();
                    int highlightLength = Math.min(commentEndOffset - lineStart, lineLength);
                    document.setCharacterAttributes(lineStart, highlightLength, getAttributeSetWithColor(Color.ORANGE), true);
                    commented = false;
                } else {
                    document.setCharacterAttributes(lineStart, lineLength, getAttributeSetWithColor(Color.ORANGE), true);
                }
            }
            if (commentStart >= 0) {
                int commentStartOffset = lineStart + commentStart;
                if (commentEnd > commentStart) {
                    int commentEndOffset = lineStart + commentEnd + multiLineCommentText.second().length();
                    document.setCharacterAttributes(commentStartOffset, commentEndOffset - commentStartOffset, getAttributeSetWithColor(Color.ORANGE), true);
                } else {
                    commented = true;
                    document.setCharacterAttributes(commentStartOffset, lineLength - commentStart, getAttributeSetWithColor(Color.ORANGE), true);
                }
            }
        }
    }

    private static boolean isSurroundedByValidCharacters(String text, int startIndex, int keywordLength) {
        int beforeIndex = startIndex - 1;
        int afterIndex = startIndex + keywordLength;
        char charBefore = beforeIndex >= 0 ? text.charAt(beforeIndex) : '\0';
        char charAfter = afterIndex < text.length() ? text.charAt(afterIndex) : '\0';
        boolean isValidBefore = Character.isWhitespace(charBefore) || charBefore == '}' || charBefore == ')' || charBefore == '.' ||charBefore == '\0';
        boolean isValidAfter = Character.isWhitespace(charAfter) || charAfter == '{' || charAfter == '(' || charAfter == '.' || charAfter == '\0';
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
        if (commentText.isEmpty()) return false;
        int lineStartIndex = getLineStartIndex(text, keywordIndex);
        if (lineStartIndex >= 0 && lineStartIndex < text.length()) {
            for (int i = 0; i < commentText.length(); i++) {
                char commentChar = commentText.charAt(i);
                int commentCharIndex = lineStartIndex + i;
                if (commentCharIndex < text.length() && text.charAt(commentCharIndex) != commentChar) return false;
            }
            return true;
        }
        return false;
    }

    private static String getLineText() {
        Element line = document.getParagraphElement(textPane.getCaretPosition());
        int lineStart = line.getStartOffset();
        return getText(line.getDocument(), lineStart, textPane.getCaretPosition()-lineStart);
    }

    private static String getText(Document document, int start, int end) {
        try {
            return document.getText(start, end);
        } catch(BadLocationException ignored) {
            return "";
        }
    }

    private static void removeAndInsert(int start, int startOffset, String string,  Color color) {
        try {
            document.remove(start - startOffset, startOffset);
            document.insertString(start - startOffset, string, getAttributeSetWithColor(color));
        } catch (BadLocationException ignored) {}
    }
}
