package io.github.kloping.spt.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class IoUtils {
    private IoUtils() { }

    public static byte[] readAll(InputStream input) throws IOException {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1) out.write(buffer, 0, length);
            return out.toByteArray();
        }
    }

    public static String readString(String file) throws IOException {
        return new String(Files.readAllBytes(new File(file).toPath()), StandardCharsets.UTF_8);
    }

    public static String[] readLines(String file) throws IOException {
        List<String> lines = Files.readAllLines(new File(file).toPath(), StandardCharsets.UTF_8);
        return lines.toArray(new String[0]);
    }

    public static void write(File file, byte[] bytes) throws IOException {
        Files.write(file.toPath(), bytes);
    }

    public static byte[] readUrl(URL url) throws IOException {
        return readAll(url.openStream());
    }
}
