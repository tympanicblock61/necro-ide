package org.ide;


import com.google.gson.*;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static Path currentPath = Paths.get("").toAbsolutePath();
    public static Gson jsonParser = new Gson();

    public static ArrayList<Language> languageList = new ArrayList<>();
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            loadLanguages();
            System.out.println("finished loading languages");
            JFrame frame = new JFrame("IDE Text Editor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create the IdeTextPane and set it as the content pane of the frame
            IdeTextPane ideTextPane = new IdeTextPane();
            JTextPane textPane = ideTextPane.create("java");
            JScrollPane scrollPane = new JScrollPane(textPane);
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            // Create a JList with a JButton and add it to the frame
            DefaultListModel<String> listModel = new DefaultListModel<>();

            JList<String> jList = new JList<>(listModel);
            JButton button = new JButton("Click Me");
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(jList), BorderLayout.CENTER);
            panel.add(button, BorderLayout.SOUTH);
            frame.getContentPane().add(panel, BorderLayout.EAST);

            textPane.setText("if else text lmao class object assert");

            frame.setSize(800, 600);
            frame.setVisible(true);
        });
    }

    public static void loadLanguages() {
        File langs = new File(currentPath.toFile(), "languages");
        if (langs.exists() && langs.isDirectory()) {
            File[] files = langs.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        try (FileReader fileReader = new FileReader(file);
                             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                            StringBuilder json = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                json.append(line);
                            }
                            JsonObject data = jsonParser.fromJson(json.toString(), JsonObject.class);
                            try {
                                String name = data.get("name").getAsString();
                                String comment = data.get("comment").getAsString();
                                Pair<String, String> multilineComment = new Pair<>(
                                        data.get("multiline").getAsJsonArray().get(0).getAsString(),
                                        data.get("multiline").getAsJsonArray().get(1).getAsString()
                                );
                                boolean defaults = data.get("defaults").getAsBoolean();
                                Language language = new Language(name, comment, multilineComment, defaults);
                                for (JsonElement element : data.get("files").getAsJsonArray()) {
                                    language.addFileType(element.getAsString());
                                }
                                for (JsonElement element : data.get("keywords").getAsJsonArray()) {
                                    JsonObject obj = element.getAsJsonObject();
                                    JsonArray color = obj.get("color").getAsJsonArray();
                                    Color keywordColor = new Color(
                                            color.get(0).getAsInt(),
                                            color.get(1).getAsInt(),
                                            color.get(2).getAsInt(),
                                            color.get(3).getAsInt()
                                    );
                                    language.keywords.put(obj.get("key").getAsString(), keywordColor);
                                }
                                System.out.println("loaded the language "+language.name);
                                languageList.add(language);
                            } catch (JsonIOException ignored) {
                                System.out.println(file.getName() + " is not a valid JSON file");
                            }
                        } catch (IOException | JsonSyntaxException | JsonIOException ignored) {
                            System.out.println(file.getName() + " is not a valid JSON file");
                        }
                    }
                }
            }
        }
    }
}

