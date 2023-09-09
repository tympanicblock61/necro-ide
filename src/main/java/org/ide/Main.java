package org.ide;


import com.google.gson.*;
import org.apache.commons.io.FilenameUtils;
import org.ide.elements.*;
import org.ide.utils.ColorUtils;
import org.ide.utils.FileUtils;
import org.ide.utils.Language;
import org.ide.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static Path currentPath = Paths.get("").toAbsolutePath();
    public static File langs = new File(currentPath.toFile(), "languages");
    public static File config = new File(currentPath.toFile(), "config.json");
    public static Gson gson = new Gson();
    public static ArrayList<Language> languageList = new ArrayList<>();
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            loadLanguages();
            System.out.println("finished loading languages");
            JFrame frame = new JFrame("IDE Text Editor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon64.png")));
            JMenuBar bar = new JMenuBar();
            frame.setJMenuBar(bar);
            JMenu fileMenu = new JMenu("File");
            JMenu helpMenu = new JMenu("Help");
            JMenu settingMenu = new JMenu("Settings");
            bar.add(fileMenu);
            bar.add(helpMenu);
            bar.add(settingMenu);
            JMenuItem openMenuItem = new JMenuItem("Open");
            JMenuItem saveMenuItem = new JMenuItem("Save As");
            JMenuItem GithubMenuItem = new JMenuItem("Github");
            JMenuItem bgMenuItem = new JMenuItem("Change Background Color");
            JMenuItem txtMenuItem = new JMenuItem("Change Text Color");
            JMenuItem fontMenuItem = new JMenuItem("Change Font");
            JMenuItem suggestionsMenuItem = new JMenuItem("Do Suggestions");
            helpMenu.add(GithubMenuItem);
            fileMenu.add(openMenuItem);
            fileMenu.add(saveMenuItem);
            settingMenu.add(bgMenuItem);
            settingMenu.add(txtMenuItem);
            settingMenu.add(fontMenuItem);
            settingMenu.add(suggestionsMenuItem);
            

            // Create the IdeTextPane and set it as the content pane of the frame
            IdeTextPane ideTextPane = new IdeTextPane();
            final JTextPane[] textPane = {ideTextPane.create("")};
            JScrollPane scrollPane = new JScrollPane(textPane[0]);
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            // Create a JList with a JButton and add it to the frame
            DefaultListModel<String> listModel = new DefaultListModel<>();

            JList<String> jList = new JList<>(listModel);
            JButton button = new JButton("Click Me");
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(jList), BorderLayout.CENTER);
            panel.add(button, BorderLayout.SOUTH);
            frame.getContentPane().add(panel, BorderLayout.EAST);

            openMenuItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    Color bg = textPane[0].getBackground();
                    Color fg = textPane[0].getForeground();
                    scrollPane.remove(textPane[0]);
                    textPane[0] = ideTextPane.create(FilenameUtils.getExtension(selectedFile.getAbsolutePath()));
                    textPane[0].setText(FileUtils.read(selectedFile));
                    textPane[0].setBackground(bg);
                    textPane[0].setForeground(fg);
                    textPane[0].setCaretColor(fg);
                    scrollPane.setViewportView(textPane[0]);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    System.out.println(FilenameUtils.getExtension(selectedFile.getAbsolutePath()));
                }
            });

            saveMenuItem.addActionListener(e -> {
                FileDialog f = new FileDialog(new Frame(), "Save As", FileDialog.SAVE);
                f.setMultipleMode(false);
                f.setVisible(true);
                FileUtils.save(new File(f.getDirectory(), f.getName()), textPane[0].getText());

            });

            GithubMenuItem.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/tympanicblock61/necro-ide"));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            });

            bgMenuItem.addActionListener(e -> {
                ColorPicker picker = new ColorPicker(textPane[0].getBackground(), "Change Background Color");
                picker.setColor(textPane[0].getBackground());
                picker.addEvent(Events.Save, color -> {
                    textPane[0].setBackground(color);
                    return null;
                });
                picker.setVisible(true);
            });

            txtMenuItem.addActionListener(e -> {
                ColorPicker picker = new ColorPicker(textPane[0].getForeground(), "Change Text Color");
                picker.setColor(textPane[0].getForeground());
                picker.addEvent(Events.Save, color -> {
                    textPane[0].setForeground(color);
                    return null;
                });
                picker.setVisible(true);
            });

            fontMenuItem.addActionListener(e -> {
                FontPicker pickers = new FontPicker(new Font(Font.DIALOG, Font.PLAIN, 12), "Change Font");
                pickers.setFont(textPane[0].getFont());
                pickers.addEvent(Events.Save, font -> {
                    textPane[0].setFont(font);
                    return null;
                });
                pickers.setVisible(true);
            });

            suggestionsMenuItem.addActionListener(e -> ideTextPane.setDoSuggestions(!ideTextPane.isDoSuggestions()));

            frame.setSize(800, 600);

            if (config.exists()){
                String file = FileUtils.read(config);
                JsonObject data = gson.fromJson(file, JsonObject.class);
                int fore = data.get("foreground").getAsInt();
                int back = data.get("background").getAsInt();
                JsonObject font = data.getAsJsonObject("font");
                String name = font.get("name").getAsString();
                int size = font.get("size").getAsInt();
                boolean doSuggestions = data.get("suggestions").getAsBoolean();
                textPane[0].setForeground(new Color(fore, true));
                textPane[0].setBackground(new Color(back, true));
                textPane[0].setFont(new Font(name, Font.PLAIN, size));
                ideTextPane.setDoSuggestions(doSuggestions);
            }

            frame.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Color BackgroundColor = textPane[0].getBackground();
                Color ForegroundColor = textPane[0].getForeground();
                Font TextFont = textPane[0].getFont();
                JsonObject ConfigJson = new JsonObject();
                ConfigJson.addProperty("foreground", ColorUtils.colorToHex(ForegroundColor));
                ConfigJson.addProperty("background", ColorUtils.colorToHex(BackgroundColor));
                ConfigJson.addProperty("suggestions", ideTextPane.isDoSuggestions());
                JsonObject Font = new JsonObject();
                Font.addProperty("name", TextFont.getName());
                Font.addProperty("size", TextFont.getSize());
                ConfigJson.add("font", Font);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                FileUtils.save(config, gson.toJson(ConfigJson));
            }));
        });
    }
    public static void loadLanguages() {
        if (langs.exists() && langs.isDirectory()) {
            File[] files = langs.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".json")) {
                        String json = FileUtils.read(file);
                        JsonObject data = gson.fromJson(json, JsonObject.class);
                        try {
                            String name = data.get("name").getAsString();
                            String comment = data.get("comment").getAsString();
                            Pair<String, String> multilineComment = new Pair<>(data.get("multiline").getAsJsonArray().get(0).getAsString(), data.get("multiline").getAsJsonArray().get(1).getAsString());
                            boolean defaults = data.get("defaults").getAsBoolean();
                            Language language = new Language(name, comment, multilineComment, defaults);
                            for (JsonElement element : data.get("files").getAsJsonArray()) {
                                language.addFileType(element.getAsString());
                            }
                            for (JsonElement element : data.get("keywords").getAsJsonArray()) {
                                JsonObject obj = element.getAsJsonObject();
                                JsonArray color = obj.get("color").getAsJsonArray();
                                Color keywordColor = new Color(color.get(0).getAsInt(), color.get(1).getAsInt(), color.get(2).getAsInt(), color.get(3).getAsInt());
                                language.keywords.put(obj.get("key").getAsString(), keywordColor);
                            }
                            System.out.println("loaded the language "+language.name);
                            languageList.add(language);
                        } catch (JsonIOException ignored) {
                            System.out.println(file.getName() + " is not a valid JSON file");
                        }
                    }
                }
            }
        }
    }
}
