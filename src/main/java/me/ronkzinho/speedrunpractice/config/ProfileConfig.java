package me.ronkzinho.speedrunpractice.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProfileConfig {
    @Nullable
    public Integer selected = 0;
    public List<Profile> profiles = new ArrayList<>();
    public static String fileName = "speedrun-practice-profiles.json";

    public static ProfileConfig load(){
        Path path = FabricLoader.getInstance().getConfigDir().resolve(fileName);
        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(path);
            Gson gson = new Gson();
            ProfileConfig config = gson.fromJson(reader, ProfileConfig.class);
            config.checkSelected();
            reader.close();
            return config;
        } catch (IOException e) {
            return new ProfileConfig();
        }
    }

    public int checkSelected() {
        if(this.selected != null && this.selected >= this.profiles.size()){
            this.selected = 0;
            try {
                this.save();
                return 1;
            } catch (IOException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private void removeDuplicates() {
        ArrayList<Profile> newList = new ArrayList<>();

        for (Profile element : this.profiles) {
            if (newList.stream().noneMatch(profile -> profile.getDisplayName().trim().equals(element.getDisplayName().trim()))) {
                newList.add(element.copy());
            }
        }

        if(newList.size() < this.profiles.size()){
            this.profiles.clear();
            this.profiles.addAll(newList);
            try{
                if(this.checkSelected() == 0) this.save();
            } catch(Exception ignored){}
        }
    }

    public void addDefaults(){
        List<Profile> currentProfiles = new ArrayList<>(profiles);
        for(int i = 0; i < SpeedrunPractice.PracticeMode.values().length; i++) {
            Profile currentProfile = this.profiles.size() > i ? this.profiles.get(i).copy() : null;
            SpeedrunPractice.PracticeMode mode = SpeedrunPractice.PracticeMode.values()[i];
            if (this.profiles.stream().noneMatch(profile -> profile.getDisplayName().equals(I18n.translate(mode.getTranslationKey())))) {
                this.profiles.add(i, new Profile(mode.getTranslationKey(), mode, null, null).setEditable(false));
            }
            else{
                Optional<Profile> optionalProfile = this.profiles.stream().filter(p -> p.name.equals(mode.getTranslationKey()) || p.getDisplayName().equals(I18n.translate(mode.getTranslationKey()))).findFirst();
                if(optionalProfile.isPresent()){
                    Profile profile = optionalProfile.get();
                    if(!EqualsBuilder.reflectionEquals(currentProfile.copy(), profile.copy())){
                        int index = this.profiles.indexOf(profile);
                        this.profiles.set(i, profile);
                        this.profiles.set(index, currentProfile);
                    }
                    profile.setEditable(false);
                    profile.modeName = mode.getSimplifiedName();
                }
            }
        }
        if(!currentProfiles.equals(this.profiles)){
            try {
                this.save();
            } catch (IOException ignored) {}
        }
        this.removeDuplicates();
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

    public static class Profile implements Serializable {
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
            return SerializationUtils.clone(this);
        }
    }
}
