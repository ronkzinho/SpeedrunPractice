package me.ronkzinho.speedrunpractice.practice;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.SpeedrunPracticeRandom;
import me.ronkzinho.speedrunpractice.mixin.ServerPlayerEntityAccess;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.Tag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Practice {
    public int run(){
        OptionalLong seed;
        OptionalLong optionalLong2;
        String string = SpeedrunPractice.practiceSeedText;
        seed = StringUtils.isEmpty(string) ? OptionalLong.empty() : ((optionalLong2 = tryParseLong(string)).isPresent() && optionalLong2.getAsLong() != 0L ? optionalLong2 : OptionalLong.of(string.hashCode()));
        return this.run(seed.isPresent() ? seed.getAsLong() : SpeedrunPractice.random.nextLong());
    }

    private static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        }
        catch (NumberFormatException numberFormatException) {
            return OptionalLong.empty();
        }
    }


    abstract public int run(long seed);

    public static int setSlot(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String result = setSlot(IntegerArgumentType.getInteger(ctx,"slot"), ctx.getNodes().get(2).getNode().getName(), ctx.getSource().getPlayer(), 1);
        return result != null ? 1 : 0;
    }

    public static String setSlot(int slot, String key) {
        return setSlot(slot, key, Objects.requireNonNull(MinecraftClient.getInstance().getServer()).getPlayerManager().getPlayerList().get(0), 2);
    }

    public static String setSlot(int slot, String key, ServerPlayerEntity player, int mode){
        String text = String.format(Language.getInstance().get("speedrun-practice.inventorymanagement.options.slot.set"), key,slot);
        if(mode == 1) player.sendMessage(new LiteralText(text),false);
        SpeedrunPractice.config.practiceSlots.put(key,slot-1);
        try {
            SpeedrunPractice.config.save();
        } catch (IOException e) {
            return null;
        }
        return text;
    }

    public static int saveSlot(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String result = saveSlot(IntegerArgumentType.getInteger(ctx,"slot"), ctx.getNodes().get(2).getNode().getName(), ctx.getSource().getPlayer(), 1);
        return result != null ? 1 : 0;
    }

    public static String saveSlot(int slot, String key){
        return saveSlot(slot, key, Objects.requireNonNull(MinecraftClient.getInstance().getServer()).getPlayerManager().getPlayerList().get(0), 2);
    }

    public static String saveSlot(int slot, String key, ServerPlayerEntity player, int mode){
        String text = String.format(Language.getInstance().get("speedrun-practice.inventorymanagement.options.slot.save"),key, slot);
        if(mode == 1) player.sendMessage(new LiteralText(text), false);
        PlayerInventory inventory = player.inventory;
        ListTag listTag = new ListTag();
        inventory.serialize(listTag);
        SpeedrunPractice.config.practiceInventories.get(key).set(slot-1,listTag.stream().map(Tag::toString).collect(Collectors.toList()));
        try {
            SpeedrunPractice.config.save();
        } catch (IOException e) {
            return null;
        }
        return text;
    }

    public static void startSpeedrunIGTTimer(){
        try {
            Class<?> timerClass = Class.forName("com.redlimerl.speedrunigt.timer.InGameTimer");
            Method startMethod = timerClass.getMethod("start");
            startMethod.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored){}
    }

    public static void resetScreen(){
        MinecraftClient.getInstance().openScreen(null);
        MinecraftClient.getInstance().mouse.lockCursor();
    }

    public static void getInventory(ServerPlayerEntity player, String key) {
        player.inventory.clear();
        player.playerScreenHandler.sendContentUpdates();
        List<String> inventoryStringList = SpeedrunPractice.config.practiceInventories.get(key).get(SpeedrunPractice.config.practiceSlots.get(key));
        if(inventoryStringList!=null) {
            List<CompoundTag> inventoryTagList = inventoryStringList.stream().map(tag -> {
                try {
                    return StringNbtReader.parse(tag);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            ListTag listTag = new ListTag();
            listTag.addAll(inventoryTagList);
            player.inventory.deserialize(listTag);
            player.playerScreenHandler.sendContentUpdates();
        }
    }

    public static void populatePostBlindInventory(ServerPlayerEntity player,long seed) {
        LootTable table = Objects.requireNonNull(player.getServer()).getLootManager().getTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
        LootContext lootContext = new LootContext.Builder(player.getServerWorld()).random(seed).parameter(LootContextParameters.THIS_ENTITY,player).build(LootContextTypes.BARTER);
        while(player.inventory.main.stream().anyMatch(ItemStack::isEmpty)){
            table.generateLoot(lootContext, player.inventory::insertStack);
        }
    }

    static void resetPlayer(ServerPlayerEntity player) {
        player.setHealth(20f);
        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevelClient(5f);
        player.clearStatusEffects();
        player.setVelocity(Vec3d.ZERO);
        ((ServerPlayerEntityAccess)player).setSeenCredits(false);
        SpeedrunPractice.autoSaveStater.deleteAllStates();
    }

    public static int seed(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String seed = SpeedrunPractice.practiceSeedText != null ? SpeedrunPractice.practiceSeedText : String.valueOf(SpeedrunPractice.random.getSeed());
        Text text = Texts.bracketed((new LiteralText(seed)).styled((style) -> {
            return style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(seed))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click"))).withInsertion(String.valueOf(seed));
        }));
        ctx.getSource().getPlayer().sendMessage(text,false);
        return 1;
    }

    public static int setSeed(CommandContext<ServerCommandSource> ctx) {
        SpeedrunPractice.random.seed.set(ctx.getArgument("seed",long.class));
        return 1;
    }

    static void createPortals(Map<RegistryKey<DimensionType>, PracticeWorld> linkedPracticeWorld, ServerPlayerEntity player, ServerWorld overworld, BlockPos overworldPos) {
        BlockPos prevPos = player.getBlockPos();
        BlockPos netherPos = new BlockPos(overworldPos.getX() / 8D, overworldPos.getY(), overworldPos.getZ() / 8D);
        player.refreshPositionAndAngles(netherPos,90,0);
        player.setInNetherPortal(overworldPos);
        PracticeWorld nether = linkedPracticeWorld.get(DimensionType.THE_NETHER_REGISTRY_KEY);
        nether.getPortalForcer().createPortal(player);
        nether.getChunkManager().addTicket(ChunkTicketType.field_19280, new ChunkPos(netherPos), 3, netherPos);
        player.refreshPositionAndAngles(overworldPos,90,0);
        overworld.getPortalForcer().createPortal(player);
        overworld.getChunkManager().addTicket(ChunkTicketType.field_19280, new ChunkPos(overworldPos), 3, overworldPos);
        player.refreshPositionAndAngles(prevPos,90,0);
        player.netherPortalCooldown = player.getDefaultNetherPortalCooldown();
    }

    public static void setSpawnPos(PracticeWorld overworld, ServerPlayerEntity player) {
        ServerPlayerEntityAccess playerAccess = (ServerPlayerEntityAccess) player;
        playerAccess.setSpawnPointDimension(overworld.getRegistryKey());
        playerAccess.setSpawnPointPosition(null);
        playerAccess.setSpawnPointSet(false);
    }
}
