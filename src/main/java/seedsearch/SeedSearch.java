package seedsearch;

import java.util.ArrayList;
import java.util.HashSet;
import java.lang.Math;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.characters.AbstractPlayer.PlayerClass;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import static java.lang.System.exit;

public class SeedSearch {

    public static boolean loadingEnabled = true;
    public static SearchSettings settings;

    public static void search() {
        loadingEnabled = false;
        settings = SearchSettings.loadSettings();

        if (settings.findExtras) {
            Set<String> extraCards = new HashSet<>();
            Set<String> extraRelics = new HashSet<>();
            for (PlayerClass pc : getPlayerClasses()) {
                settings.playerClass = pc;
                extraCards.addAll(findExtraCards());
                extraRelics.addAll(findExtraRelics());
            }
            System.out.println("Extra Cards:" + extraCards);
            System.out.println("Extra Relics" + extraRelics);
        } else if (settings.findMissingCards || settings.findMissingRelics) {
            settings.verbose = false;
            settings.endlessSearch = false;
            settings.exitAfterSearch = false;
            settings.requiredCards.clear();
            settings.requiredRelics.clear();
            settings.requiredAct1Cards.clear();
            settings.requiredAct1Relics.clear();

            Collection<String> missingCards = getAllCards();
            if (settings.findMissingCards) {
                System.out.println("Searching for cards: " + missingCards);
            }
            Collection<String> missingRelics = getAllRelics();
            if (settings.findMissingRelics) {
                System.out.println("Searching for cards: " + missingRelics);
            }

            Collection<PlayerClass> classes = getPlayerClasses();

            for (PlayerClass pc: classes) {
                System.out.println("Searching " + pc.name());
                settings.playerClass = pc;
                if (settings.findMissingCards) {
                    missingCards = searchForMissingCards(missingCards);
                    settings.requiredCards.clear();
                }
                if (settings.findMissingRelics) {
                    missingRelics = searchForMissingRelics(missingRelics);
                    settings.requiredRelics.clear();
                }
            }
            if (settings.findMissingCards) {
                System.out.println("Misssing cards: " + missingCards);
            }
            if (settings.findMissingRelics) {
                System.out.println("Misssing relics: " + missingRelics);
            }
        } else {
            List<Long> foundSeeds = runSearch(false);
            System.out.print("seeds found: " + convertSeeds(foundSeeds));
        }

        if (SeedSearch.settings.exitAfterSearch) {
            exit(0);
        } else {
            System.out.println("Search complete. Manually close this program when finished.");
        }
    }

    private static Collection<String> searchForMissingCards(Collection<String> cardList) {
        Set<String> missingCards = new HashSet<>();
        for (String card: cardList) {
            settings.requiredCards.clear();
            settings.requiredCards.add(card);
            List<Long> successfulRuns = runSearch(true);
            if (successfulRuns.size() == 0) {
                missingCards.add(card);
            }
        }
        return missingCards;
    }

    private static Collection<String> searchForMissingRelics(Collection<String> relicList) {
        Set<String> missingRelics = new HashSet<>();
        for (String relic: relicList) {
            settings.requiredRelics.clear();
            settings.requiredRelics.add(relic);
            List<Long> successfulRuns = runSearch(true);
            if (successfulRuns.size() == 0) {
                missingRelics.add(relic);
            }
        }
        return missingRelics;
    }

    private static Collection<PlayerClass> getPlayerClasses() {
        Set<PlayerClass> classes = new HashSet<>();
        classes.add(PlayerClass.IRONCLAD);
        classes.add(PlayerClass.THE_SILENT);
        classes.add(PlayerClass.DEFECT);
        classes.add(PlayerClass.WATCHER);
        return classes;
    }

    private static Collection<String> getAllCards() {
        return CardLibrary.getAllCards().stream().map(c -> c.cardID).collect(Collectors.toSet());
    }

    private static Collection<String> getAllRelics()
    {
        ArrayList<ArrayList<AbstractRelic>> relicLists = new ArrayList<>();
        relicLists.add(RelicLibrary.starterList);
        relicLists.add(RelicLibrary.commonList);
        relicLists.add(RelicLibrary.uncommonList);
        relicLists.add(RelicLibrary.rareList);
        relicLists.add(RelicLibrary.bossList);
        relicLists.add(RelicLibrary.specialList);
        relicLists.add(RelicLibrary.shopList);
        Set<String> relics = new HashSet<>();
        for (List<AbstractRelic> relicList: relicLists) {
            for(AbstractRelic relic: relicList) {
                relics.add(relic.relicId);
            }
        }
        return relics;
    }

    private static ArrayList<Long> runSearch(boolean abort) {
        SeedRunner runner = new SeedRunner(settings);
        ArrayList<Long> foundSeeds = new ArrayList<>();
        long startSeed = settings.startSeed;
        long numSeeds = settings.endSeed - settings.startSeed;
        while ((settings.endlessSearch || startSeed == settings.startSeed) && foundSeeds.size() == 0) {
            for(long seed = startSeed; seed < startSeed + numSeeds; seed++) {
                printSeed(seed);
                if (runner.runSeed(seed)) {
                    foundSeeds.add(seed);
                    if(settings.verbose) {
                        runner.getSeedResult().printSeedStats();
                    }
                    if (abort) {
                        break;
                    }
                }
            }
            startSeed += numSeeds;
        }
        for (Long seed: foundSeeds) {
            if(settings.verbose) {
                runner.runSeed(seed);
                runner.getSeedResult().printSeedStats();
            }
        }
        return foundSeeds;
    }

    private static Collection<String> findExtraCards() {
        Collection<String> allCards = getAllCards();
        Set<String> extraCards = new HashSet<>();
        SeedRunner runner = new SeedRunner(settings);
        for(long seed = settings.startSeed; seed < settings.endSeed; seed++) {
            runner.runSeed(seed);
            for (String card : runner.getSeedResult().getAllCardIds()) {
                if (!allCards.contains(card)) {
                    extraCards.add(card);
                }
            }
        }
        return extraCards;
    }

    private static Collection<String> findExtraRelics() {
        Collection<String> allRelics = getAllRelics();
        Set<String> extraRelics = new HashSet<>();
        SeedRunner runner = new SeedRunner(settings);
        for(long seed = settings.startSeed; seed < settings.endSeed; seed++) {
            runner.runSeed(seed);
            for (String relic : runner.getSeedResult().getAllRelicIds()) {
                if (!allRelics.contains(relic)) {
                    extraRelics.add(relic);
                }
            }
        }
        return extraRelics;
    }

    private static void printSeed(long seed) {
        long seedsChecked = Math.max(1, seed - settings.startSeed);
        long digits = Math.max(1, Math.round(Math.floor(Math.log10(seedsChecked))));
        long finalDigits = seedsChecked % Math.round(Math.pow(10, digits - 1));
        if (finalDigits == 0 && seedsChecked >= 1000) {
            System.out.println(seedsChecked);
        }
    }

    private static List<String> convertSeeds(List<Long> seeds) {
        return seeds.stream().map(s -> SeedHelper.getString(s)).collect(Collectors.toList());
    }

}
