package org.ide.utils;

import org.ide.utils.Pair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import static java.awt.Color.*;

public class Language {
    private static final Color PURPLE = new Color(128, 0, 128);
    private static final Color DARK_RED = Color.RED.darker().darker();
    private static final Color LIGHT_RED = Color.RED.brighter().brighter();
    private static final Color DARK_MAGENTA = new Color(127, 0, 85);
    private static final Color NICE_BLUE = new Color(0, 0, 255);
    private static final Color DARK_YELLOW = new Color(107, 95, 12);
    public Map<String, Color> keywords;
    private List<String> fileTypes = new ArrayList<>();
    private static final Pattern fileExtensionPattern = Pattern.compile("^(?!.*[<>:\"/\\\\|?*])[A-Za-z0-9]+$");

    public String name;
    public String lineComment;
    public Pair<String, String> multilineComment;

    public Language(String name, String lineComment, Pair<String, String> multilineComment, boolean useDefaults) {
        this.name = name;
        this.lineComment = lineComment;
        this.multilineComment = multilineComment;
        if (useDefaults) {
            keywords = defaults();
        }
    }

    public void addFileType(String extension) {
        if (fileExtensionPattern.matcher(extension).matches()) {
            fileTypes.add(extension);
        }
    }

    public List<String> getFileTypes() {
        return fileTypes;
    }
    
    
    public static Map<String, Color> defaults() {
        Map<String, Color> keywordColorMap = new HashMap<>();
        keywordColorMap.put("assert", DARK_MAGENTA);
        keywordColorMap.put("break", DARK_RED);
        keywordColorMap.put("class", PURPLE);
        keywordColorMap.put("continue", GREEN);
        keywordColorMap.put("finally", DARK_RED);
        keywordColorMap.put("import", LIGHT_RED);
        keywordColorMap.put("return", GREEN);
        keywordColorMap.put("try", LIGHT_RED);
        keywordColorMap.put("if", RED);
        keywordColorMap.put("else", RED);
        keywordColorMap.put("while", ORANGE);
        keywordColorMap.put("for", ORANGE);
        keywordColorMap.put("this", NICE_BLUE);
        keywordColorMap.put("int", NICE_BLUE);
        keywordColorMap.put("float", NICE_BLUE);
        keywordColorMap.put("case", DARK_MAGENTA);
        keywordColorMap.put("super", DARK_MAGENTA);
        keywordColorMap.put("==", DARK_YELLOW);
        keywordColorMap.put("!=", DARK_YELLOW);
        keywordColorMap.put(">=", DARK_YELLOW);
        keywordColorMap.put("<=", DARK_YELLOW);
        keywordColorMap.put(">", DARK_YELLOW);
        keywordColorMap.put("<", DARK_YELLOW);
        keywordColorMap.put("/", DARK_YELLOW);
        keywordColorMap.put("*", DARK_YELLOW);
        keywordColorMap.put("+", DARK_YELLOW);
        return keywordColorMap;
    }
}
