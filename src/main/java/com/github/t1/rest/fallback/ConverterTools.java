package com.github.t1.rest.fallback;

import lombok.SneakyThrows;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.charset.Charset;

public class ConverterTools {
    private static final int K = 1024;

    public static boolean isConvertible(Class<?> type) {
        return type != String.class && !Closeable.class.isAssignableFrom(type);
    }

    public static String readString(InputStream in, MediaType mediaType) {
        return readStringFrom(new InputStreamReader(in, getCharset(mediaType)));
    }

    public static Charset getCharset(MediaType mediaType) {
        String charsetName = (mediaType == null) ? null : mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
        if (charsetName == null)
            charsetName = "UTF-8";
        return Charset.forName(charsetName);
    }

    @SneakyThrows(IOException.class)
    public static String readStringFrom(Reader reader) {
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[8 * K];
        while (true) {
            int length = reader.read(buffer);
            if (length < 0)
                break;
            out.append(buffer, 0, length);
        }
        return out.toString();
    }

    public static boolean isApplicationType(MediaType mediaType, String subType) {
        return "application".equals(mediaType.getType())
                && (subType.equals(mediaType.getSubtype()) || mediaType.getSubtype().endsWith("+" + subType));
    }
}
