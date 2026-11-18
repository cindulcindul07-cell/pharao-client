package dev.pharao.client.hud;

import dev.pharao.client.config.ClientConfig;
import dev.pharao.client.detection.DetectionService;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class PharaoHud {
    private PharaoHud() {}

    public static void register() {
        HudElementRegistry.addLast(Identifier.of("pharao", "findings"), PharaoHud::render);
    }

private static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;
        if (!ClientConfig.actionEnabled[0] && !ClientConfig.actionEnabled[2] &&
            !ClientConfig.entities && !ClientConfig.actionEnabled[3]) return;

        var findings = DetectionService.getFindings();
        if (findings.isEmpty()) return;

        int x = 8;
        int y = 8;
        int max = Math.min(findings.size(), 8);
        int width = 220;
        int height = 18 + max * 13;
        int opacity = ClientConfig.opacity;
        int bg = (opacity << 24) | 0x00111116;
        context.fill(x, y, x + width, y + height, bg);
        context.fill(x, y, x + 2, y + height, ClientConfig.accent);
        context.drawText(client.textRenderer, "PHARAO  /  NEARBY", x + 9, y + 5, 0xFFE8E7ED, false);

        for (int i = 0; i < max; i++) {
            DetectionService.Finding f = findings.get(i);
            String line = DetectionService.directionTo(client, f.pos()) + " " +
                    f.label() + "  " + Math.round(f.distance()) + "m";
            context.drawText(client.textRenderer, line, x + 9, y + 19 + i * 13,
                    f.color(), false);
        }
    }
}
