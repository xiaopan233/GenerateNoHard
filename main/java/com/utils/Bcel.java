package com.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Bcel {
    private static final int   FREE_CHARS  = 48;
    private static final char  ESCAPE_CHAR = '$';
    private static       int[] CHAR_MAP    = new int[FREE_CHARS];
    private static       int[] MAP_CHAR    = new int[256]; // Reverse map

    static {
        int j = 0, k = 0;
        for(int i='A'; i <= 'Z'; i++) {
            CHAR_MAP[j] = i;
            MAP_CHAR[i] = j;
            j++;
        }

        for(int i='g'; i <= 'z'; i++) {
            CHAR_MAP[j] = i;
            MAP_CHAR[i] = j;
            j++;
        }

        CHAR_MAP[j]   = '$';
        MAP_CHAR['$'] = j;
        j++;

        CHAR_MAP[j]   = '_';
        MAP_CHAR['_'] = j;
    }

    public static boolean isJavaIdentifierPart(char ch) {
        return ((ch >= 'a') && (ch <= 'z')) ||
                ((ch >= 'A') && (ch <= 'Z')) ||
                ((ch >= '0') && (ch <= '9')) ||
                (ch == '_');
    }

    private static class JavaWriter extends FilterWriter {
        public JavaWriter(Writer out) {
            super(out);
        }

        public void write(int b) throws IOException {
            if (isJavaIdentifierPart((char) b) && (b != ESCAPE_CHAR)) {
                out.write(b);
            } else {
                out.write(ESCAPE_CHAR); // Escape character

                // Special escape
                if (b >= 0 && b < FREE_CHARS) {
                    out.write(CHAR_MAP[b]);
                } else { // Normal escape
                    char[] tmp = Integer.toHexString(b).toCharArray();

                    if (tmp.length == 1) {
                        out.write('0');
                        out.write(tmp[0]);
                    } else {
                        out.write(tmp[0]);
                        out.write(tmp[1]);
                    }
                }
            }
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++)
                write(cbuf[off + i]);
        }

        public void write(String str, int off, int len) throws IOException {
            write(str.toCharArray(), off, len);
        }
    }

    private static class JavaReader extends FilterReader {
        public JavaReader(Reader in) {
            super(in);
        }

        public int read() throws IOException {
            int b = in.read();

            if(b != ESCAPE_CHAR) {
                return b;
            } else {
                int i = in.read();

                if(i < 0)
                    return -1;

                if(((i >= '0') && (i <= '9')) || ((i >= 'a') && (i <= 'f'))) { // Normal escape
                    int j = in.read();

                    if(j < 0)
                        return -1;

                    char[] tmp = { (char)i, (char)j };
                    int    s   = Integer.parseInt(new String(tmp), 16);

                    return s;
                } else { // Special escape
                    return MAP_CHAR[i];
                }
            }
        }

        public int read(char[] cbuf, int off, int len) throws IOException {
            for(int i=0; i < len; i++)
                cbuf[off + i] = (char)read();

            return len;
        }
    }

    public static String encode(byte[] bytes, boolean compress) throws IOException {
        if(compress) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos  = new GZIPOutputStream(baos);

            gos.write(bytes, 0, bytes.length);
            gos.close();
            baos.close();

            bytes = baos.toByteArray();
        }

        CharArrayWriter caw = new CharArrayWriter();
        JavaWriter jw  = new JavaWriter(caw);

        for(int i=0; i < bytes.length; i++) {
            int in = bytes[i] & 0x000000ff; // Normalize to unsigned
            jw.write(in);
        }

        return caw.toString();
    }

    public static byte[] decode(String s, boolean uncompress) throws IOException {
        char[] chars = s.toCharArray();

        CharArrayReader car = new CharArrayReader(chars);
        JavaReader jr  = new JavaReader(car);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int ch;

        while((ch = jr.read()) >= 0) {
            bos.write(ch);
        }

        bos.close();
        car.close();
        jr.close();

        byte[] bytes = bos.toByteArray();

        if(uncompress) {
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));

            byte[] tmp   = new byte[bytes.length * 3]; // Rough estimate
            int    count = 0;
            int    b;

            while((b = gis.read()) >= 0)
                tmp[count++] = (byte)b;

            bytes = new byte[count];
            System.arraycopy(tmp, 0, bytes, 0, count);
        }

        return bytes;
    }
}
