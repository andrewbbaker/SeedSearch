package seedsearch;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.megacrit.cardcrawl.cards.red.BodySlam;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.relics.JuzuBracelet;
import com.megacrit.cardcrawl.relics.Lantern;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SearchSettings {

    private static final String configName = "searchConfig.json";

    // Core search parameters

    public int ascensionLevel = 0;
    public AbstractPlayer.PlayerClass playerClass = AbstractPlayer.PlayerClass.IRONCLAD;
    public long startSeed = 0L;
    public long endSeed = 100L;
    public boolean verbose = true;

    // Navigation

    public float eliteRoomWeight = 1.2f;
    public float monsterRoomWeight = 1f;
    public float restRoomWeight = 0f;
    public float shopRoomWeight = 0.9f;
    public float eventRoomWeight = 0.9f;
    public float wingBootsThreshold = 1f; // Wing boots charges are used if weight is changed by this amount

    // General decisions

    public ArrayList<String> relicsToBuy = new ArrayList<>(); // Use the ID for cards and relics
    public ArrayList<String> cardsToBuy = new ArrayList<>();
    public ArrayList<String> bossRelicsToTake = new ArrayList<>(); // Give them in priority order to always take a relic
    public int neowChoice = 3; // 3 is the boss relic trade
    public boolean useShovel = false;
    public boolean speedrunPace = true; // Do you reach Act 3 fast enough to skip Secret Portal?
    public boolean act4 = false;
    public boolean alwaysSpawnBottledTornado = true; //Assume you always have a power for Bottled Tornado to spawn

    // Event decisions

    public boolean takeSerpentGold = false;
    public boolean takeWarpedTongs = false;
    public boolean takeBigFishRelic = false;
    public boolean takeDeadAdventurerFight = false;
    public boolean takeMausoleumRelic = false;
    public boolean takeScrapOozeRelic = false;
    public boolean takeAddictRelic = true; // Always assume you pay, no taking Shame
    public boolean takeMysteriousSphereFight = false;
    public boolean takeRedMaskAct3 = true;
    public boolean takeMushroomFight = true;
    public boolean takeMaskedBanditFight = true;
    public boolean takeGoldenIdolWithoutCurse = true;
    public boolean takeGoldenIdolWithCurse = false;
    public boolean tradeGoldenIdolForBloody = true;
    public boolean takeCursedTome = true;
    public boolean tradeFaces = false;
    public boolean takeMindBloomGold = false; // Mind Bloom choices in order of priority
    public boolean takeMindBloomFight = true;
    public boolean takeMindBloomUpgrade = false;
    public boolean tradeGoldenIdolForMoney = true; // Moai Head event
    public boolean takePortal = false;
    public int numSensoryStoneCards = 1; // Keep it between 1 and 3, please!
    public boolean takeWindingHallsCurse = false;
    public boolean takeWindingHallsMadness = false;
    public boolean takeColosseumFight = false;
    public boolean takeDrugDealerRelic = false;
    public boolean takeDrugDealerTransform = true;
    public boolean takeLibraryCard = false;
    public boolean takeWeMeetAgainRelic = true;

    // Result filters

    public ArrayList<String> requiredAct1Cards = new ArrayList<>();
    public ArrayList<String> requiredAct1Relics = new ArrayList<>();
    public ArrayList<String> requiredRelics = new ArrayList<>();
    public ArrayList<String> requiredEvents = new ArrayList<>();
    public int minimumElites = 0;
    public int maximumElites = 1;
    public int minimumCombats = 0;
    public int maximumCombats = 33;

    public SearchSettings () {
    }

    public void setDefaults() {
        relicsToBuy.add(JuzuBracelet.ID);
        requiredRelics.add(Lantern.ID);
        requiredAct1Cards.add(BodySlam.ID);
    }

    public static SearchSettings loadSettings () {
        try {
            File file = new File(configName);
            if (file.exists()) {
                Gson gson = new Gson();
                return gson.fromJson(new FileReader(file), SearchSettings.class);
            } else {
                SearchSettings settings = new SearchSettings();
                settings.setDefaults();
                settings.saveSettings();
                return settings;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(String.format("Could not load search settings: %s", e.getMessage()));
            SearchSettings settings = new SearchSettings();
            settings.setDefaults();
            return settings;
        }
    }

    public void saveSettings () {
        // Some defaults for example purposes
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(configName);
            gson.toJson(this, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not serialize the search settings.");
        }
    }

}