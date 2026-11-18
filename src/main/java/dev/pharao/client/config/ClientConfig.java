package dev.pharao.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pharao-client.json");

    public static final int ACTION_COUNT = 4;
    public static final String[] ACTION_NAMES = {"Detection overlay", "Freecam", "Ore inspector", "Entity markers"};
    public static int[] actionKeys = {0, 0, 0, 0};
    public static boolean[] actionEnabled = {true, false, false, true};

    public static boolean chests = true;
    public static boolean amethyst = true;
    public static boolean lighting = true;
    public static boolean entities = true;
    public static int accent = 0xFF9270E8;
    public static int opacity = 238;
    public static int backgroundPreset = 0;
    public static boolean blur = true;

    private ClientConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d == null) return;
            if (d.actionKeys != null && d.actionKeys.length == ACTION_COUNT) actionKeys = d.actionKeys;
            if (d.actionEnabled != null && d.actionEnabled.length == ACTION_COUNT) actionEnabled = d.actionEnabled;
            chests = d.chests;
            amethyst = d.amethyst;
            lighting = d.lighting;
            entities = d.entities;
            accent = d.accent;
            opacity = Math.max(90, Math.min(255, d.opacity));
            backgroundPreset = Math.max(0, Math.min(3, d.backgroundPreset));
            blur = d.blur;
        } catch (Exception ignored) { }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Data d = new Data();
            d.actionKeys = actionKeys;
            d.actionEnabled = actionEnabled;
            d.chests = chests;
            d.amethyst = amethyst;
            d.lighting = lighting;
            d.entities = entities;
            d.accent = accent;
            d.opacity = opacity;
            d.backgroundPreset = backgroundPreset;
            d.blur = blur;
            Files.writeString(FILE, GSON.toJson(d));
        } catch (Exception ignored) { }
    }

    public static void toggleAction(int index) {
        if (index >= 0 && index < ACTION_COUNT) {
            actionEnabled[index] = !actionEnabled[index];
            save();
        }
    }

    private static final class Data {
        int[] actionKeys = {0, 0, 0, 0};
        boolean[] actionEnabled = {true, false, false, true};
        boolean chests = true, amethyst = true, lighting = true, entities = true;
        int accent = 0xFF9270E8, opacity = 238, backgroundPreset = 0;
        boolean blur = true;
    }
}
