package org.ide.utils;

import java.io.*;

public class FileUtils {
    public static String read(File f){
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            return "";
        }
        return stringBuilder.toString();
    }
    
    public static void save(File f, String v){
        FileWriter write;
        try {
            write = new FileWriter(f);
            write.write(v);
            write.close();
        } catch (IOException e) {
            System.out.println("could not save file "+f.getName());
        }

    }
}
