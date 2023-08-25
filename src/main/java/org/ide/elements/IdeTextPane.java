package org.ide.elements;

import org.ide.utils.Pair;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.Map;

import static org.ide.utils.Language.defaults;
import static org.ide.Main.languageList;

public class IdeTextPane {

    private static String commentText;
    private static Pair<String, String> multiLineCommentText;
    private static Map<String, Color> keywords;

    public JTextPane create(String fileType) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(true);
        StyledDocument document = textPane.getStyledDocument();
        keywords = defaults();
        commentText = "";
        multiLineCommentText = new Pair<>("", "");
        languageList.forEach(language -> {
            if (language.getFileTypes().contains(fileType)) {
                System.out.println(fileType);
                System.out.println(language);
                keywords = language.keywords;
                commentText = language.lineComment;
                multiLineCommentText = language.multilineComment;
            }
        });
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    highlightKeywords(document, keywords);
                    suggestKeyword(document, keywords);
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightKeywords(document, keywords));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //s
            }
        });

        return textPane;
    }

    private static void suggestKeyword(StyledDocument document, Map<String, Color> keywords) {
        Element root = document.getDefaultRootElement();

        int numLines = root.getElementCount();
        for (int i = 0; i < numLines; i++) {
            Element line = root.getElement(i);
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset();
            String lineText = null;
            try {
                lineText = document.getText(lineStart, lineEnd - lineStart);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }

            String[] words = lineText.split("\\s+");
            for (String word : words) {
                for (Map.Entry<String, Color> entry : keywords.entrySet()) {
                    if (entry.getKey().length() <= 2) return;
                    if (word.equals(entry.getKey().substring(0,2))) {
                        System.out.println("Keyword predicted: " + entry.getKey());
                    }
                }
            }
        }
    }

    private static void highlightKeywords(StyledDocument document, Map<String, Color> keywords) {
        document.setCharacterAttributes(0, document.getLength(), document.getStyle(StyleContext.DEFAULT_STYLE), true);
        for (String keyword : keywords.keySet()) {
            highlightAllOccurrences(document, keyword, keywords.get(keyword));
        }
        highlightCommentedLines(document);
        highlightMultiCommentedLines(document);
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
        if (commentText.length() == 0) return;
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

    private static void highlightMultiCommentedLines(StyledDocument document) {
        if (multiLineCommentText.first().length() == 0 && multiLineCommentText.second().length() == 0) return;
        Element root = document.getDefaultRootElement();
        int lineCount = root.getElementCount();
        boolean commented = false;
        for (int i = 0; i < lineCount; i++) {
            Element line = root.getElement(i);
            int lineStart = line.getStartOffset();
            int lineEnd = line.getEndOffset() - 1;
            int lineLength = lineEnd - lineStart;
            try {
                String lineText = document.getText(lineStart, lineLength);
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
