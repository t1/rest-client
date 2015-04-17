package com.github.t1.rest.fallback;

import java.io.*;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import lombok.SneakyThrows;

public class ConverterTools {
    public static boolean isConvertible(Class<?> type) {
        return type != String.class && type != InputStream.class; // TODO more types are not convertible
    }

    public static String readString(InputStream in, MediaType mediaType) {
        return readFromAsString(new InputStreamReader(in, getCharset(mediaType)));
    }

    public static Charset getCharset(MediaType mediaType) {
        String charsetName = (mediaType == null) ? null : mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
        if (charsetName == null)
            charsetName = "UTF-8";
        return Charset.forName(charsetName);
    }

    @SneakyThrows(IOException.class)
    public static String readFromAsString(Reader reader) {
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[8 * 1024];
        while (true) {
            int length = reader.read(buffer);
            if (length < 0)
                break;
            out.append(buffer, 0, length);
        }
        return out.toString();
    }

}
