package com.andretissot.java.dao;

import javax.swing.text.MaskFormatter;

/**
 * @author André Augusto Tissot
 */
public class Formatter {
    public static String formatAsCPF(String texto) throws Exception {
        MaskFormatter mf = new MaskFormatter("###.###.###-##");
        mf.setValueContainsLiteralCharacters(false);
        return mf.valueToString(texto);
    }

    public static String forceLength(String texto, int length) {
        int addLength = length - texto.length();
        if (addLength > 0) {
            StringBuilder sb = new StringBuilder(texto);
            for (int i = 0; i < addLength; i++)
                sb.append(' ');
            texto = sb.toString();
        }
        texto = texto.substring(0, length);
        return texto;
    }
}
