package dev.pharao.client.detection;

import dev.pharao.client.config.ClientConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DetectionService {
    public record Finding(String label, BlockPos pos, double distance, int color) {}

    private static final List<Finding> FINDINGS = new ArrayList<>();
    private static long lastScanTick = 0;
    private static final int RADIUS = 12;
    private static final int VERTICAL_RANGE = 12;

    private DetectionService() {}

    public static List<Finding> getFindings() {
        return List.copyOf(FINDINGS);
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            FINDINGS.clear();
            return;
        }
        if (client.world.getTime() - lastScanTick < 20) return;
        lastScanTick = client.world.getTime();
        scan(client);
    }

    private static void scan(MinecraftClient client) {
        World world = client.world;
        Vec3d origin = client.player.getPos();
        BlockPos center = client.player.getBlockPos();
        List<Finding> found = new ArrayList<>();

        // Read only already-loaded client chunks. This deliberately never requests or loads chunks.
        if (ClientConfig.actionEnabled[0] || ClientConfig.actionEnabled[2]) {
            for (int x = center.getX() - RADIUS; x <= center.getX() + RADIUS; x++) {
                for (int z = center.getZ() - RADIUS; z <= center.getZ() + RADIUS; z++) {
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                    for (int y = Math.max(world.getBottomY(), center.getY() - VERTICAL_RANGE);
                         y <= Math.min(world.getTopYInclusive(), center.getY() + VERTICAL_RANGE); y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = world.getBlockState(pos);
                        Block block = state.getBlock();
                        double distance = Math.sqrt(pos.getSquaredDistance(center));

                        if (ClientConfig.chests && isStorage(block)) {
                            found.add(new Finding(storageName(block), pos.toImmutable(), distance, 0xFFFFB45C));
                        }

                        if (ClientConfig.amethyst && isAmethyst(block)) {
                            found.add(new Finding(amethystName(block), pos.toImmutable(), distance, 0xFFB78BFF));
                        }

                        if (ClientConfig.lighting && state.getLuminance() > 0 && y < client.player.getBlockY() - 2) {
                            found.add(new Finding("Underground light source", pos.toImmutable(), distance, 0xFFFFD66B));
                        }

                        if (ClientConfig.actionEnabled[2] && isOre(block)) {
                            found.add(new Finding("Visible ore: " + block.getName().getString(),
                                    pos.toImmutable(), distance, 0xFF72C9E8));
                        }
                    }
                }
            }
        }

        if (ClientConfig.entities && ClientConfig.actionEnabled[3]) {
            Box box = client.player.getBoundingBox().expand(32.0);
            for (Entity entity : world.getEntitiesByClass(Entity.class, box, e -> true)) {
                if (entity == client.player) continue;
                String label = entityLabel(entity);
                if (label == null) continue;
                BlockPos pos = entity.getBlockPos();
                found.add(new Finding(label, pos.toImmutable(), entity.distanceTo(client.player),
                        entity instanceof PlayerEntity ? 0xFF78B7FF : 0xFFB38BFF));
            }
        }

        found.sort(Comparator.comparingDouble(Finding::distance));
        FINDINGS.clear();
        for (Finding finding : found) {
            if (FINDINGS.size() >= 24) break;
            FINDINGS.add(finding);
        }
    }

    private static boolean isStorage(Block b) {
        return b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST ||
               b == Blocks.ENDER_CHEST || b == Blocks.BARREL;
    }

    private static String storageName(Block b) {
        if (b == Blocks.ENDER_CHEST) return "Ender chest";
        if (b == Blocks.BARREL) return "Barrel";
        if (b == Blocks.TRAPPED_CHEST) return "Trapped chest";
        return "Chest";
    }

    private static boolean isAmethyst(Block b) {
        return b == Blocks.BUDDING_AMETHYST || b == Blocks.AMETHYST_CLUSTER ||
               b == Blocks.SMALL_AMETHYST_BUD || b == Blocks.MEDIUM_AMETHYST_BUD ||
               b == Blocks.LARGE_AMETHYST_BUD;
    }

    private static String amethystName(Block b) {
        if (b == Blocks.BUDDING_AMETHYST) return "Budding amethyst";
        if (b == Blocks.AMETHYST_CLUSTER) return "Mature amethyst cluster";
        if (b == Blocks.LARGE_AMETHYST_BUD) return "Large amethyst bud";
        if (b == Blocks.MEDIUM_AMETHYST_BUD) return "Medium amethyst bud";
        return "Small amethyst bud";
    }

    private static boolean isOre(Block b) {
        return b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE ||
               b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE ||
               b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE ||
               b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE ||
               b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE ||
               b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE ||
               b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE ||
               b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE ||
               b == Blocks.NETHER_GOLD_ORE || b == Blocks.NETHER_QUARTZ_ORE ||
               b == Blocks.ANCIENT_DEBRIS;
    }

    private static String entityLabel(Entity entity) {
        if (entity instanceof PlayerEntity) return "Player: " + entity.getName().getString();
        if (entity instanceof GlowItemFrameEntity) return "Glow item frame";
        if (entity instanceof ItemFrameEntity) return "Item frame";
        if (entity instanceof ArmorStandEntity) return "Armor stand";
        if (entity instanceof PaintingEntity) return "Painting";
        if (entity instanceof ItemEntity item) return "Dropped item: " + item.getStack().getName().getString();
        if (entity instanceof ArrowEntity) return "Arrow";
        return null;
    }

    public static String directionTo(MinecraftClient client, BlockPos pos) {
        if (client.player == null) return "•";
        double dx = pos.getX() + 0.5 - client.player.getX();
        double dz = pos.getZ() + 0.5 - client.player.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = (targetYaw - client.player.getYaw() + 360.0) % 360.0;
        if (relative < 22.5 || relative >= 337.5) return "↑";
        if (relative < 67.5) return "↗";
        if (relative < 112.5) return "→";
        if (relative < 157.5) return "↘";
        if (relative < 202.5) return "↓";
        if (relative < 247.5) return "↙";
        if (relative < 292.5) return "←";
        return "↖";
    }
}
