package com.proyectoApi.api.service;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto compartidas. */
final class Text {

    private Text() {
    }

    /** minúsculas, sin tildes y con espacios simples: para comparar nombres/estados de la BD. */
    static String key(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return decomposed.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /** float de la BD (columna real) -> double sin ruido binario: 9000.1f -> 9000.1 */
    static double money(float value) {
        return Double.parseDouble(Float.toString(value));
    }
}
