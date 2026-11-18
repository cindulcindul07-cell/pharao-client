package dev.pharao.client.screen;

import dev.pharao.client.config.ClientConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PharaoScreen extends Screen {
    private enum Page { TOOLS, APPEARANCE, KEYBINDS, SETTINGS }
    private Page page = Page.TOOLS;
    private int waitingForKey = -1;

    private static final int PANEL = 0xEE111116;
    private static final int CARD = 0xFF1B1B23;
    private static final int BORDER = 0xFF30303A;
    private static final int TEXT = 0xFFE8E7ED;
    private static final int MUTED = 0xFF9A98A6;

    public PharaoScreen() {
        super(Text.literal("Pharao Client"));
    }

    @Override
    protected void init() {
        clearChildren();
        int panelW = Math.min(760, width - 32);
        int panelH = Math.min(440, height - 32);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int sideW = 150;

        addNav(left + 16, top + 74, "Tools", Page.TOOLS);
        addNav(left + 16, top + 112, "Appearance", Page.APPEARANCE);
        addNav(left + 16, top + 150, "Keybinds", Page.KEYBINDS);
        addNav(left + 16, top + 188, "Settings", Page.SETTINGS);

        addDrawableChild(ButtonWidget.builder(Text.literal("×"), b -> close())
                .dimensions(left + panelW - 42, top + 12, 28, 24).build());

        int contentX = left + sideW + 28;
        int contentW = panelW - sideW - 48;
        int y = top + 66;

        switch (page) {
            case TOOLS -> buildTools(contentX, y, contentW);
            case APPEARANCE -> buildAppearance(contentX, y, contentW);
            case KEYBINDS -> buildKeybinds(contentX, y, contentW);
            case SETTINGS -> buildSettings(contentX, y, contentW);
        }
    }

    private void addNav(int x, int y, String label, Page target) {
        boolean active = page == target;
        addDrawableChild(ButtonWidget.builder(Text.literal((active ? "•  " : "   ") + label), b -> {
            page = target;
            waitingForKey = -1;
            init();
        }).dimensions(x, y, 118, 28).build());
    }

    private void buildTools(int x, int y, int w) {
        heading("Tools", x, y - 32);
        int gap = 12;
        int cardW = (w - gap) / 2;
        for (int i = 0; i < ClientConfig.ACTION_COUNT; i++) {
            final int index = i;
            int col = i % 2;
            int row = i / 2;
            int cx = x + col * (cardW + gap);
            int cy = y + row * 72;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(ClientConfig.ACTION_NAMES[i] + "   " + (ClientConfig.actionEnabled[i] ? "ON" : "OFF")),
                    b -> {
                        ClientConfig.toggleAction(index);
                        init();
                    }).dimensions(cx, cy, cardW, 52).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Chests  ·  " + (ClientConfig.chests ? "ON" : "OFF")), b -> {
            ClientConfig.chests = !ClientConfig.chests; ClientConfig.save(); init();
        }).dimensions(x, y + 154, cardW, 28).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Amethyst  ·  " + (ClientConfig.amethyst ? "ON" : "OFF")), b -> {
            ClientConfig.amethyst = !ClientConfig.amethyst; ClientConfig.save(); init();
        }).dimensions(x + cardW + gap, y + 154, cardW, 28).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Lighting  ·  " + (ClientConfig.lighting ? "ON" : "OFF")), b -> {
            ClientConfig.lighting = !ClientConfig.lighting; ClientConfig.save(); init();
        }).dimensions(x, y + 188, cardW, 28).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Entities  ·  " + (ClientConfig.entities ? "ON" : "OFF")), b -> {
            ClientConfig.entities = !ClientConfig.entities; ClientConfig.save(); init();
        }).dimensions(x + cardW + gap, y + 188, cardW, 28).build());
        drawHint = "Nearby client-visible findings appear in the HUD.";
    }

    private void buildAppearance(int x, int y, int w) {
        heading("Appearance", x, y - 32);
        addDrawableChild(ButtonWidget.builder(Text.literal("Accent colour  ·  " + colourName()), b -> {
            int[] colours = {0xFF9270E8, 0xFFB7B5C2, 0xFF6F8CCF, 0xFF65A98C, 0xFFC47B91};
            int current = 0;
            for (int i = 0; i < colours.length; i++) if (colours[i] == ClientConfig.accent) current = i;
            ClientConfig.accent = colours[(current + 1) % colours.length];
            ClientConfig.save(); init();
        }).dimensions(x, y, w, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Panel transparency  ·  " + Math.round(ClientConfig.opacity / 255.0 * 100) + "%"), b -> {
            ClientConfig.opacity = ClientConfig.opacity <= 110 ? 255 : ClientConfig.opacity - 35;
            ClientConfig.save(); init();
        }).dimensions(x, y + 42, w, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Background preset  ·  " + (ClientConfig.backgroundPreset + 1)), b -> {
            ClientConfig.backgroundPreset = (ClientConfig.backgroundPreset + 1) % 4;
            ClientConfig.save(); init();
        }).dimensions(x, y + 84, w, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Blur  ·  " + (ClientConfig.blur ? "ON" : "OFF")), b -> {
            ClientConfig.blur = !ClientConfig.blur;
            ClientConfig.save(); init();
        }).dimensions(x, y + 126, w, 30).build());
        drawHint = "Changes are saved automatically.";
    }

    private void buildKeybinds(int x, int y, int w) {
        heading("Keybinds", x, y - 32);
        addDrawableChild(ButtonWidget.builder(Text.literal("Open menu  ·  RIGHT SHIFT"), b -> {
            waitingForKey = -2;
        }).dimensions(x, y, w, 30).build());
        for (int i = 0; i < ClientConfig.ACTION_COUNT; i++) {
            final int index = i;
            String key = waitingForKey == i ? "Press a key…" : keyName(ClientConfig.actionKeys[i]);
            addDrawableChild(ButtonWidget.builder(Text.literal(ClientConfig.ACTION_NAMES[i] + "  ·  " + key), b -> {
                waitingForKey = index;
                init();
            }).dimensions(x, y + 40 + i * 38, w, 30).build());
        }
        drawHint = waitingForKey >= 0 ? "Press a key. Escape cancels." : "Select an action to record a key.";
    }

    private void buildSettings(int x, int y, int w) {
        heading("Settings", x, y - 32);
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset appearance"), b -> {
            ClientConfig.accent = 0xFF9270E8;
            ClientConfig.opacity = 238;
            ClientConfig.backgroundPreset = 0;
            ClientConfig.blur = true;
            ClientConfig.save(); init();
        }).dimensions(x, y, w, 30).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset keybinds"), b -> {
            ClientConfig.actionKeys = new int[]{0, 0, 0, 0};
            ClientConfig.save(); init();
        }).dimensions(x, y + 42, w, 30).build());
        drawHint = "Configuration is stored locally.";
    }

    private String drawHint = "";

    private void heading(String label, int x, int y) {
        // Drawn in render using the active page heading.
    }

    private String colourName() {
        return switch (ClientConfig.accent) {
            case 0xFFB7B5C2 -> "Silver";
            case 0xFF6F8CCF -> "Blue";
            case 0xFF65A98C -> "Sage";
            case 0xFFC47B91 -> "Rose";
            default -> "Violet";
        };
    }

    private static String keyName(int key) {
        if (key == 0) return "NONE";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RIGHT SHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LEFT SHIFT";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            default -> "KEY " + key;
        };
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.getKeycode();
        if (waitingForKey == -2) {
            if (key != GLFW.GLFW_KEY_ESCAPE) {
                // Menu shortcut is intentionally fixed to Right Shift.
                waitingForKey = -1;
            } else {
                waitingForKey = -1;
            }
            init();
            return true;
        }
        if (waitingForKey >= 0) {
            if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_RIGHT_SHIFT) {
                ClientConfig.actionKeys[waitingForKey] = key;
                ClientConfig.save();
            }
            waitingForKey = -1;
            init();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int pw = Math.min(760, width - 32);
        int ph = Math.min(440, height - 32);
        int x = (width - pw) / 2;
        int y = (height - ph) / 2;
        int panel = (ClientConfig.opacity << 24) | 0x00111116;

        context.fill(0, 0, width, height, 0x9907070B);
        context.fill(x, y, x + pw, y + ph, panel);
        context.fill(x, y, x + 2, y + ph, ClientConfig.accent);
        context.fill(x + 150, y + 52, x + 151, y + ph - 18, BORDER);
        context.fill(x + 18, y + 52, x + pw - 18, y + 53, BORDER);

        // Minimal monogram mark.
        int lx = x + 24, ly = y + 18;
        context.fill(lx, ly, lx + 4, ly + 26, ClientConfig.accent);
        context.fill(lx, ly, lx + 18, ly + 4, ClientConfig.accent);
        context.fill(lx + 14, ly + 4, lx + 18, ly + 14, ClientConfig.accent);
        context.fill(lx + 4, ly + 12, lx + 17, ly + 16, ClientConfig.accent);

        context.drawText(textRenderer, "PHARAO", x + 52, y + 20, TEXT, false);
        context.drawText(textRenderer, "CLIENT", x + 52, y + 32, MUTED, false);

        String pageTitle = switch (page) {
            case TOOLS -> "Tools";
            case APPEARANCE -> "Appearance";
            case KEYBINDS -> "Keybinds";
            case SETTINGS -> "Settings";
        };
        context.drawText(textRenderer, pageTitle, x + 178, y + 22, TEXT, false);
        context.drawText(textRenderer, drawHint, x + 178, y + ph - 25, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
