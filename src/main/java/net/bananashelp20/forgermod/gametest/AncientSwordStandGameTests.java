package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.block.entity.custom.AncientSwordStandBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AncientSwordStandGameTests {
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void oneSwordAndOpening(GameTestHelper test) {
        test.setBlock(POS, ModBlocks.ANCIENT_SWORD_STAND.get());
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        test.useBlock(POS, player);
        test.useBlock(POS, player);
        test.assertTrue(player.getInventory().countItem(ModItems.RUSTY_CLAYMORE.get()) == 1, "Repeated clicks awarded extra claymores");
        test.assertFalse(test.getBlockState(POS).getValue(AncientSwordStandBlock.FULL), "Claimed stand remained full");
        test.assertTrue(test.getBlockState(POS).getValue(AncientSwordStandBlock.OPENING), "Opening never started");
        test.runAfterDelay(40, () -> {
            test.assertFalse(test.getBlockState(POS).getValue(AncientSwordStandBlock.OPENING), "Opening did not finish");
            test.useBlock(POS, player);
            test.assertTrue(player.getInventory().countItem(ModItems.RUSTY_CLAYMORE.get()) == 1, "Empty stand awarded a claymore");
            test.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void fullInventoryDropsOneSword(GameTestHelper test) {
        test.setBlock(POS, ModBlocks.ANCIENT_SWORD_STAND.get());
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            player.getInventory().items.set(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        player.setPos(Vec3.atCenterOf(test.absolutePos(POS)));
        test.useBlock(POS, player);
        test.useBlock(POS, player);
        var drops = test.getLevel().getEntitiesOfClass(ItemEntity.class, test.getBounds(),
                entity -> entity.getItem().is(ModItems.RUSTY_CLAYMORE.get()));
        test.assertTrue(drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() == 1, "Full inventory lost or duplicated sword");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void emptyDropStaysEmptyWhenPlaced(GameTestHelper test) {
        var empty = ModBlocks.ANCIENT_SWORD_STAND.get().defaultBlockState().setValue(AncientSwordStandBlock.FULL, false);
        test.setBlock(POS, empty);
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        var drops = Block.getDrops(empty, test.getLevel(), test.absolutePos(POS), test.getBlockEntity(POS));
        test.assertTrue(drops.size() == 1 && drops.getFirst().is(ModBlocks.ANCIENT_SWORD_STAND.get().asItem()), "Empty stand did not drop itself");
        ItemStack item = drops.getFirst();
        test.assertTrue(item.get(DataComponents.BLOCK_STATE) != null, "Dropped stand lost its full property");
        test.setBlock(POS, Blocks.AIR);
        player.setPos(Vec3.atCenterOf(test.absolutePos(POS)).add(0, 0, 3));
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        var hit = new BlockHitResult(Vec3.atCenterOf(test.absolutePos(POS)), Direction.UP, test.absolutePos(POS), false);
        var placed = ((BlockItem)item.getItem()).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, item, hit));
        test.assertTrue(placed.consumesAction(), "Could not place dropped empty stand");
        test.assertFalse(test.getBlockState(POS).getValue(AncientSwordStandBlock.FULL), "Replaced empty stand refilled itself");
        test.useBlock(POS, player);
        test.assertTrue(player.getInventory().countItem(ModItems.RUSTY_CLAYMORE.get()) == 0, "Replaced empty stand awarded sword");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void shapesAndSaveLoad(GameTestHelper test) {
        test.setBlock(POS, ModBlocks.ANCIENT_SWORD_STAND.get());
        AncientSwordStandBlockEntity stand = test.getBlockEntity(POS);
        long now = test.getLevel().getGameTime();
        stand.beginOpening(now);
        var saved = stand.saveWithoutMetadata(test.getLevel().registryAccess());
        var restored = new AncientSwordStandBlockEntity(test.absolutePos(POS), test.getBlockState(POS));
        restored.loadWithComponents(saved, test.getLevel().registryAccess());
        test.assertTrue(restored.remainingTicks(now + 10) == 25, "Saved opening progress was lost");
        test.assertTrue(restored.animationFrame(now + 12, 0) == 6, "Restored animation frame was incorrect");
        test.assertTrue(restored.remainingTicks(now + 100) == 0, "Unloaded opening never finishes");
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var full = test.getBlockState(POS).setValue(AncientSwordStandBlock.FACING, facing);
            var shape = full.getShape(test.getLevel(), test.absolutePos(POS), CollisionContext.empty());
            var empty = full.setValue(AncientSwordStandBlock.FULL, false)
                    .getShape(test.getLevel(), test.absolutePos(POS), CollisionContext.empty());
            test.assertTrue(Math.abs(shape.max(Direction.Axis.Y) - 31.25/16) < 1e-6, "Full stand sword height changed with facing");
            test.assertTrue(Math.abs(empty.max(Direction.Axis.Y) - 29.0/16) < 1e-6, "Empty stand spine collision is missing");
            test.assertTrue(shape.bounds().getCenter().distanceToSqr(new Vec3(.5,31.25/32,.5)) < .03, "Shape rotated outside its block");
            // Broad reference points on front ribs from the supplied detailed shape export.
            for (Vec3 northPoint : new Vec3[]{new Vec3(.125, .25, .3), new Vec3(.25, 1.1, .25)}) {
                Vec3 point = switch (facing) {
                    case EAST -> new Vec3(1 - northPoint.z, northPoint.y, northPoint.x);
                    case SOUTH -> new Vec3(1 - northPoint.x, northPoint.y, 1 - northPoint.z);
                    case WEST -> new Vec3(northPoint.z, northPoint.y, 1 - northPoint.x);
                    default -> northPoint;
                };
                test.assertTrue(empty.toAabbs().stream().anyMatch(box -> box.contains(point)), "Front ribs missing from simplified shape: " + facing);
            }
            test.assertTrue(shape == full.getShape(test.getLevel(), test.absolutePos(POS), CollisionContext.empty()),
                    "Collision queries rebuilt the cached shape");
        }
        test.succeed();
    }
}
