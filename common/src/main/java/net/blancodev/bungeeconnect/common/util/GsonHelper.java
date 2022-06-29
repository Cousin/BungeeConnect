package net.blancodev.bungeeconnect.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonHelper {

    public static Gson GSON = baseBuilder().create();

    public static Gson PRETTY_GSON = baseBuilder().setPrettyPrinting().create();

    private static GsonBuilder baseBuilder() {
        final GsonBuilder gsonBuilder = new GsonBuilder();

        return gsonBuilder;
    }

}

