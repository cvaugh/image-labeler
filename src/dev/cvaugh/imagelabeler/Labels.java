package dev.cvaugh.imagelabeler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public final class Labels {
    static File file = new File("labels.tsv");
    private static HashMap<String, Label> registry = new HashMap<>();

    public static Label get(String path) {
        return registry.getOrDefault(path, Label.NONE);
    }

    public static void set(String path, Label label) {
        registry.put(path, label);
    }

    public static void load() throws IOException {
        if(!file.exists()) return;
        List<String> lines = Files.readAllLines(file.toPath());
        for(String line : lines) {
            if(line.isBlank()) continue;
            String[] split = line.split("\t");
            set(split[0], Label.valueOf(split[1]));
        }
    }

    public static void save() throws IOException {
        StringBuilder sb = new StringBuilder();
        for(String key : registry.keySet()) {
            sb.append(key);
            sb.append("\t");
            sb.append(get(key));
            sb.append("\n");
        }
        Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
