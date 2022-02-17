package me.ronkzinho.speedrunpractice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.ronkzinho.speedrunpractice.command.Command;
import me.ronkzinho.speedrunpractice.practice.*;
import me.ronkzinho.speedrunpractice.config.ModConfig;
import me.ronkzinho.speedrunpractice.screens.QuickSettingsScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.util.version.SemanticVersionImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

import static java.lang.Math.ceil;

public class SpeedrunPractice implements ModInitializer {
    public static ModConfig config;
    public static String worldName;
    public static QuickSettingsScreen selectingWorldParent;
    public static PracticeMode practiceMode = PracticeMode.END;
    public static String practiceSeedText;
    public static final Identifier BUTTON_ICON_TEXTURE = new Identifier("textures/item/golden_carrot.png");
    public static Map<StructureFeature<?>, StructureConfig> overworldStructures = Maps.newHashMap(StructuresConfig.DEFAULT_STRUCTURES);
    public static Map<StructureFeature<?>, StructureConfig> netherStructures = Maps.newHashMap(StructuresConfig.DEFAULT_STRUCTURES);
    public static List<StructurePoolFeatureConfig> possibleBastionConfigs=new ArrayList<>();
    public static SpeedrunPracticeRandom random = new SpeedrunPracticeRandom();
    public static boolean welcomeShown = false;
    private static Gson gson = new Gson();
    private static final ModContainer modContainer = FabricLoader.getInstance().getModContainer("speedrun-practice").get();
    private static final String donationLink = "https://ko-fi.com/gregor0410";
    private static final Version version = modContainer.getMetadata().getVersion();
    public static AutoSaveStater autoSaveStater = new AutoSaveStater();
    public static SpeedrunIGTInterface speedrunIGTInterface=null;
    public static boolean isPlaying = false;

    static {
        netherStructures.put(StructureFeature.RUINED_PORTAL, new StructureConfig(25, 10, 34222645));
    }


    @Override
    public void onInitialize() {
        try {
            speedrunIGTInterface = new SpeedrunIGTInterface();
        } catch (NoSuchFieldException | ClassNotFoundException ignored) {}
        config = ModConfig.load();
        update();
        Command.registerCommands();
    }

    public static void sendWelcomeMessage(ServerPlayerEntity player) throws IOException, VersionParsingException {
        player.sendMessage(new LiteralText(String.format("[SpeedrunPractice v%s by Gregor0410]",version)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00ff00))),false);
        player.sendMessage(new LiteralText("[Donation Link]")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,donationLink))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new LiteralText("Click"))))
                .formatted(Formatting.DARK_GREEN),false);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.github.com/repos/ronkzinho/speedrunpractice/releases/latest");
        JsonObject jsonObject = client.execute(request, res -> gson.fromJson(new InputStreamReader(res.getEntity().getContent()), JsonObject.class));
        String latestVersion = jsonObject.get("tag_name").getAsString().substring(1); //get rid of the leading v
        String patchNotes = jsonObject.get("body").getAsString();
        if(version.compareTo(new SemanticVersionImpl(latestVersion,false))<0){
            player.sendMessage(new LiteralText(String.format("There is a new version available: v%s", latestVersion)).formatted(Formatting.RED),false);
            player.sendMessage(new LiteralText(String.format("Patch notes:\n%s ", patchNotes.replace('\r',' ').replace('-','•'))),false);
            player.sendMessage(
                new LiteralText("Click to download latest version")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00ff00))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://github.com/ronkzinho/SpeedrunPractice/releases/latest"))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new LiteralText("Click")))),false);
        }else{
            player.sendMessage(new LiteralText("You are on the latest version."),false);
        }
    }


    public static void update() {
        updateStructures();
    }

    public static void updateStructures(){
        //Update Nether structure spacing and separation
        int defaultNetherSpacing = StructuresConfig.DEFAULT_STRUCTURES.get(StructureFeature.FORTRESS).getSpacing();
        int defaultNetherSeparation = StructuresConfig.DEFAULT_STRUCTURES.get(StructureFeature.FORTRESS).getSeparation();
        int netherSalt = 30084232;
        StructureConfig netherConfig = new StructureConfig((int) ceil(defaultNetherSpacing*config.netherRegionSize), (int) ceil(defaultNetherSeparation*config.netherRegionSize),netherSalt);
        netherStructures.putAll(ImmutableMap.of(StructureFeature.FORTRESS,netherConfig,StructureFeature.BASTION_REMNANT,netherConfig));
        //Update possible bastion types
        possibleBastionConfigs.clear();
        if(config.housing) possibleBastionConfigs.add(new StructurePoolFeatureConfig(new Identifier("bastion/units/base"),60));
        if(config.stables) possibleBastionConfigs.add(new StructurePoolFeatureConfig(new Identifier("bastion/hoglin_stable/origin"),60));
        if(config.treasure) possibleBastionConfigs.add(new StructurePoolFeatureConfig(new Identifier("bastion/treasure/starters"),60));
        if(config.bridge) possibleBastionConfigs.add(new StructurePoolFeatureConfig(new Identifier("bastion/bridge/start"),60));

    }

    public static ButtonWidget quickSettingsButton(int x, int y, ButtonWidget.PressAction onPress){
        return new ButtonWidget(x, y, 20, 20, new LiteralText(""), onPress);
    }

    public static void practice(){
        MinecraftClient client = MinecraftClient.getInstance();
        Objects.requireNonNull(client.getServer()).getPlayerManager().getPlayerList().forEach(player -> player.setGameMode(GameMode.SURVIVAL));
        client.submit(() -> {
            client.method_29970(new SaveLevelScreen(new TranslatableText("speedrun-practice.screens.practiceworld")));
        });
        practiceMode.getPracticeClass().run();
    }

    public enum DragonType{
        FRONT,
        BACK,
        BOTH
    }

    public enum PracticeMode{
        END(0, "speedrun-practice.quicksettings.modes.end", "end", new EndPractice()),
        NETHER(1, "speedrun-practice.quicksettings.modes.nether", "nether", new NetherPractice()),
        OVERWORLD(2, "speedrun-practice.quicksettings.modes.overworld", "overworld", new OverworldPractice()),
        POSTBLIND(3, "speedrun-practice.quicksettings.modes.postblind", "postblind", new PostBlindPractice());

        private static final PracticeMode[] VALUES;
        private final int id;
        private final String translationKey;
        private final Practice practiceClass;
        private String simplifiedName;

        PracticeMode(int id, String translationKey, String simplifiedName, Practice practiceClass) {
            this.id = id;
            this.translationKey = translationKey;
            this.simplifiedName = simplifiedName;
            this.practiceClass = practiceClass;
        }

        public int getId() {
            return this.id;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }

        public PracticeMode next() {
            return PracticeMode.byId(this.getId() + 1);
        }

        public Practice getPracticeClass(){
            return this.practiceClass;
        }

        public String getSimplifiedName(){
            return this.simplifiedName;
        }

        public static PracticeMode byId(int id) {
            return VALUES[MathHelper.floorMod(id, VALUES.length)];
        }

        static {
            VALUES = Arrays.stream(PracticeMode.values()).sorted(Comparator.comparingInt(PracticeMode::getId)).toArray(PracticeMode[]::new);
        }
    }
}