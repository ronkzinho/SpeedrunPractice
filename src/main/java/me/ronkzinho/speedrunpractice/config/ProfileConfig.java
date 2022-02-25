package me.ronkzinho.speedrunpractice.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ProfileConfig {
    @Nullable
    public Integer selected = 0;
    public List<Profile> profiles = new ArrayList<>();
    public static String fileName = "speedrun-practice-profiles.json";

    public static ProfileConfig load(){
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(path);
            Gson gson = new Gson();
            ProfileConfig config = gson.fromJson(reader, ProfileConfig.class);
            if(config.selected != null && config.selected >= config.profiles.size()){
                config.selected = 0;
                config.save();
            }
            reader.close();
            return config;
        } catch (IOException e) {
            return new ProfileConfig();
        }
    }

    public void addDefaults(){
        for(int i = 0; i < SpeedrunPractice.PracticeMode.values().length; i++) {
            SpeedrunPractice.PracticeMode mode = SpeedrunPractice.PracticeMode.values()[i];
            if (this.profiles.stream().noneMatch(profile -> profile.name.equals(mode.getTranslationKey()))) {
                this.profiles.add(i, new Profile(mode.getTranslationKey(), mode, null, null).setEditable(false));
            }
            else{
                Profile profile = this.profiles.stream().filter(p -> p.name.equals(mode.getTranslationKey())).findFirst().orElseGet(null);
                if(profile != null){
                    profile.setEditable(false);
                    profile.modeName = mode.getSimplifiedName();
                }
            }
        }
    }

    public void save() throws IOException {
        System.out.println("Flushing changes to " + fileName);
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        Files.createDirectories(path.getParent());
        BufferedWriter writer = Files.newBufferedWriter(path);
        gson.toJson(this,writer);
        writer.close();
    }

    public static class Profile {
        public String name;
        public String modeName;
        public String worldName;
        public String seedText;
        public boolean editable = true;
        public int inventorySlot;

        public Profile(String name, SpeedrunPractice.PracticeMode mode, String worldName, String seed){
            this.name = name;
            this.modeName = mode.getSimplifiedName();
            this.worldName = worldName;
            this.seedText = seed;
            this.initializeISlot();
        }

        public void initializeISlot(){
            this.inventorySlot = (this.inventorySlot > 0 && this.inventorySlot < 3) ? this.inventorySlot - 1 : SpeedrunPractice.config.practiceSlots.get(modeName);
        }

        public SpeedrunPractice.PracticeMode getMode(){
            return SpeedrunPractice.PracticeMode.fromSimplifiedName(modeName);
        }

        public Text getNameText(){
            return new TranslatableText(this.name);
        }

        public String getDisplayName(){
            return I18n.hasTranslation(this.name) ? I18n.translate(this.name) : this.name;
        }

        public Profile setEditable(boolean editable){
            this.editable = editable;
            return this;
        }

        public Profile copy(){
            Profile profile = new Profile(this.name, SpeedrunPractice.PracticeMode.fromSimplifiedName(this.modeName), this.worldName, this.seedText);
            profile.inventorySlot = this.inventorySlot;
            profile.editable = this.editable;
            return profile;
        }
    }
}
