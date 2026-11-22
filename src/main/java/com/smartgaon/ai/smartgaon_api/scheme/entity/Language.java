package com.smartgaon.ai.smartgaon_api.scheme.entity;

import java.util.Arrays;

public enum Language {
    EN("en"),
    HI("hi"),
    MR("mr");

    public static final Language DEFAULT = EN;

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /** Accepts "HI", "hi", "hi-IN". Blank or unknown → English. */
    public static Language from(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        String v = value.trim();
        int dash = v.indexOf('-');
        if (dash > 0) {
            v = v.substring(0, dash);
        }
        final String key = v;
        return Arrays.stream(values())
                .filter(l -> l.name().equalsIgnoreCase(key) || l.code.equalsIgnoreCase(key))
                .findFirst()
                .orElse(DEFAULT);
    }
}
