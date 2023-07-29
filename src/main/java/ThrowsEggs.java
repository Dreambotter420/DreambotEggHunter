
import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.wrappers.widgets.message.MessageType;
import paint.CustomPaint;
import paint.PaintInfo;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

@ScriptManifest(
        name = "Egg Thrower",
        author = "Dreambotter420",
        description = "Throws Eggs",
        version = 420.69,
        category = Category.MISC,
        image = "lFwvTuW.jpg")
public class ThrowsEggs extends AbstractScript implements ChatListener, PaintInfo {

    private static final String REPL_TOKEN = "egghunterblacklistpw369";
    private static final String BASE_URL = "https://egghunterblacklist.dreambotter420.repl.co";
    public static Map<String, Instant> busiedPlayers = new LinkedHashMap<>();
    public static boolean eggWieldCheck = false;
    public static boolean eggThrown = false;
    public static boolean playerBusy = false;
    public static boolean playerIron = false;
    public static int eggCount = 0;
    public static int eggStock = 0;
    public static boolean buyMoreEggs = false;

    public static boolean noMoCoins = false;
    public static BlacklistClient blacklistClient = null;
    public static List<String> targets = new ArrayList<>();
    public static void reset() {
        eggWieldCheck = false;
        eggThrown = false;
        playerBusy = false;
        buyMoreEggs = false;
        noMoCoins = false;
        targets.clear();
        busiedPlayers.clear();
        blacklistClient = null;
        replThread = null;
    }

    final int GREEN_EGG = 22358;
    final int BLUE_EGG = 22355;
    final int RED_EGG = 22361;
    final Tile DIANGO_TILE = new Tile(3081,3247,0);
    private static GUI gui;
    final Filter<Item> eggFilter = i -> i.getName().equals("Holy handegg") || i.getName().equals("Peaceful handegg") || i.getName().equals("Chaotic handegg");

    private static Thread replThread = null;
    public static volatile boolean stopReplThread = false;

    @Override
    public void onStart(String[] args) {
        start();
    }
    @Override
    public void onStart() {
        start();
    }
    public static void start() {
        SwingUtilities.invokeLater(
                () -> gui = new GUI());
    }
    @Override
    public void onExit() {
        gui.close();
        stopReplThread = true;
        reset();
    }
    @Override
    public int onLoop() {
        if (GUI.stopScript) {
            return -1;
        }
        if (!GUI.startLoop) {
            return 100;
        }
        if (!Client.getGameState().equals(GameState.LOGGED_IN)) {
            return 100;
        }
        if (replThread == null) {
            String ourName = Players.getLocal().getName();
            if (ourName != null) {
                replThread = new Thread(new BlacklistClient(BASE_URL, REPL_TOKEN, ourName));
                replThread.start();
            }
        }
        eggStock = Bank.count(eggFilter) + Inventory.count(eggFilter);
        if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 10 ) {
            if (Walking.toggleRun()) {
                Sleep.sleepTick();
            }
            return Calculations.random(300,800);
        }
        if (buyMoreEggs) {
            if (Bank.count(eggFilter) > 1000 || noMoCoins) {
                if (BankLocation.GRAND_EXCHANGE.getCenter().distance() > 10) {
                    if (Walking.shouldWalk()) {
                        Walking.walk(BankLocation.GRAND_EXCHANGE);
                    }
                    Sleep.sleepTicks(Calculations.random(1,7));
                    return Calculations.random(300,800);
                }
                buyMoreEggs = false;
                noMoCoins = false;
                return Calculations.random(300,800);
            }
            if (Bank.isOpen()) {
                if (Bank.depositAllExcept(995)) {
                    if (!Bank.contains(995)) {
                        if (!Inventory.contains(995)) {
                            Sleep.sleepTicks(2);
                            if (!Inventory.contains(995)) {
                                if (!Bank.contains(eggFilter) && !Inventory.contains(eggFilter)) {
                                    Logger.log("All out of money and eggs :-( Stopping script...");
                                    reset();
                                    return -1;
                                }
                                Logger.log("No more coins to buy eggs! But have some eggs");
                                noMoCoins = true;
                                return Calculations.random(300,800);
                            }
                        }
                        Bank.close();
                    }
                    Bank.withdrawAll(995);
                    Sleep.sleepTick();
                }
                return Calculations.random(300,800);
            }
            if (Inventory.isFull() || !Inventory.contains(995)) {
                if (Shop.isOpen()) {
                    if (!Shop.close()) {
                        return Calculations.random(300,800);
                    }
                }
                if (Walking.shouldWalk()) {
                    Bank.open();
                    Sleep.sleepTicks(Calculations.random(1,7));
                }
                return Calculations.random(300,800);
            }
            if (Shop.isOpen()) {
                int randEgg;
                int randCalc = Calculations.random(1,100);
                if (randCalc <= 33) {
                    randEgg = GREEN_EGG;
                } else if (randCalc <= 66) {
                    randEgg = BLUE_EGG;
                } else {
                    randEgg = RED_EGG;
                }
                if (Shop.get(randEgg).interact("Buy 50")) {
                    Sleep.sleepTicks(2);
                }
                return Calculations.random(300,800);
            }
            if (DIANGO_TILE.distance() > 15) {
                if (Walking.shouldWalk()) {
                    Walking.walk(DIANGO_TILE);
                    Sleep.sleepTicks(Calculations.random(1,7));
                }
                return Calculations.random(300,800);
            }
            NPC diango = NPCs.closest("Diango");
            if (diango == null) {
                if (DIANGO_TILE.equals(Players.getLocal().getTile())) {
                    Logger.log("Diango is deleted from the game, stopping script");
                    return -1;
                }
                if (Walking.shouldWalk()) {
                    Walking.walk(DIANGO_TILE);
                    Sleep.sleepTicks(Calculations.random(1,7));
                }
                return Calculations.random(300,800);
            }
            if (diango.interact("Trade")) {
                Sleep.sleepUntil(() -> Shop.isOpen(), () -> Players.getLocal().isMoving(), 4000, 100);
            }
            return Calculations.random(300,800);
        }
        if (eggWieldCheck || Equipment.isSlotFull(EquipmentSlot.WEAPON)) {
            if (Inventory.isFull()) {
                if (!Bank.isOpen()) {
                    if (Walking.shouldWalk()) {
                        Bank.open();
                        Sleep.sleepTicks(Calculations.random(1,7));
                    }
                    return Calculations.random(300,800);
                }
                Bank.depositAllEquipment();
                Sleep.sleepTick();
                eggWieldCheck = false;
                return Calculations.random(300,800);
            }
            Equipment.interact(EquipmentSlot.WEAPON, "Remove");
            Sleep.sleepTick();
            eggWieldCheck = false;
            return Calculations.random(300,800);
        }

        List<Item> eggz = Inventory.all(eggFilter);
        Item egg = null;
        if (!eggz.isEmpty()) {
            Collections.shuffle(eggz);
            egg = eggz.get(0);
        }
        if (egg == null) {
            if (!Bank.isOpen()) {
                if (Walking.shouldWalk()) {
                    Bank.open();
                    Sleep.sleepTicks(Calculations.random(1,7));
                }
                return Calculations.random(300,800);
            }
            if (!Bank.contains(eggFilter)) {
                Logger.log("No more eggs ! Buying more...");
                buyMoreEggs = true;
            }
            if (Bank.getWithdrawMode() != BankMode.ITEM) {
                Bank.setWithdrawMode(BankMode.ITEM);
                Sleep.sleepTick();
                return Calculations.random(300,800);
            }
            int totalSlots = Inventory.getEmptySlots();
            boolean BLUE = Bank.contains(BLUE_EGG);
            boolean GREEN = Bank.contains(GREEN_EGG);
            boolean RED = Bank.contains(RED_EGG);
            int numTypes = 0;
            if (BLUE) numTypes++;
            if (GREEN) numTypes++;
            if (RED) numTypes++;

            int withdrawAmount = totalSlots / numTypes;

            int remaining = totalSlots % numTypes;
            if (BLUE) {
                Bank.withdraw(BLUE_EGG, withdrawAmount + remaining);
                remaining = 0;
            }
            if (GREEN) {
                Bank.withdraw(GREEN_EGG, withdrawAmount + remaining);
                remaining = 0;
            }
            if (RED) {
                Bank.withdraw(RED_EGG, withdrawAmount + remaining);
                remaining = 0;
            }
            Sleep.sleepTick();
            return Calculations.random(300,800);
        }
        if (Bank.isOpen()) {
            Bank.close();
            return Calculations.random(300,800);
        }
        if (!targets.isEmpty()) {
            Player needsEgg = Players.closest(p -> targets.stream().anyMatch(name -> !BlacklistClient.blacklisted.stream().anyMatch(notThrow -> p.getName().toLowerCase(Locale.ROOT).contains(notThrow)) && p.getName().toLowerCase(Locale.ROOT).contains(name)));
            if (needsEgg != null) {
                if (!playerNeedsEgg(needsEgg)) {
                    return Calculations.random(50,200);
                }
                useEggOnPlayer(egg, needsEgg);
                Sleep.sleepTick();
                return Calculations.random(300,800);
            }
        }
        for (Map.Entry<String, Instant> oldBusyPlayer : busiedPlayers.entrySet()) {
            if (Instant.now().isBefore(oldBusyPlayer.getValue())) {
                continue;
            }
            Player playerNeedsEgg = Players.closest(p -> p.getName().equals(oldBusyPlayer.getKey()));
            if (playerNeedsEgg == null || !playerNeedsEgg(playerNeedsEgg)) {
                busiedPlayers.remove(oldBusyPlayer.getKey());
            }
            useEggOnPlayer(egg, playerNeedsEgg);
            Sleep.sleepTick();
            return Calculations.random(300,800);
        }

        Player needsEgg = Players.closest(p -> !BlacklistClient.blacklisted.stream().anyMatch(notThrow -> p.getName().equals(notThrow)) && playerNeedsEgg(p) && !p.equals(Players.getLocal()) && !busiedPlayers.containsKey(p.getName()));
        useEggOnPlayer(egg, needsEgg);
        Sleep.sleepTick();
        return Calculations.random(300,800);
    }
    public static void useEggOnPlayer(Item egg, Player p) {
        if (p == null) {
            Logger.log("player null :-(");
            return;
        }
        Logger.log("useEggOnPlayer: "+p.getName());
        if (!p.canReach()) {
            if (!Walking.shouldWalk()) {
                Sleep.sleepTick();
                return;
            }
            if (Walking.walk(p)) {
                Sleep.sleepTicks(2);
            }
            return;
        }
        if (egg.useOn(p)) {
            Sleep.sleepUntil(() -> gotMessage(), () -> Players.getLocal().isMoving() || Players.getLocal().getInteractingCharacter() != null, 4200, 69);
        }
        if (playerBusy) {
            Logger.log("Player: " + p.getName()+" busy, gonna try later...");
            busiedPlayers.put(p.getName(), Instant.now().plusSeconds((int)4.5D * 60));
            playerBusy = false;
        } else if (eggThrown) {
            Logger.log("Player: " + p.getName()+" egged! nice");
            eggCount++;
            eggThrown = false;
        } else if(eggWieldCheck) {
            Logger.log("Have something in our hands, gonna unequip w3p0n");
        } else if(playerIron) {
            Logger.log("Skipping ironmeme: " + p.getName());
            busiedPlayers.put(p.getName(), Instant.now().plusSeconds(60 * 60 * 24 * 365)); //check again in a year
            playerIron = false;
        } else {
            Logger.log("PLAYER: " + p.getName() + " WAS THE ONE THAT GOT AWAY NOOOO");
        }
    }
    public static boolean gotMessage() {
        return eggWieldCheck || playerBusy || eggThrown || playerIron;
    }
    public static boolean playerNeedsEgg(Player p) {
        return p.getComposite().getAppearance()[3] <= 512;
    }

    @Override
    public void onMessage(Message message) {
        if (!message.getType().equals(MessageType.PLAYER) &&
                !message.getType().equals(MessageType.CHAT_CHANNEL) &&
                !message.getType().equals(MessageType.CLAN_CHAT) &&
                !message.getType().equals(MessageType.CLAN_GUEST_CHAT) &&
                !message.getType().equals(MessageType.MOD_CHAT) &&
                !message.getType().equals(MessageType.MOD_PRIVATE_CHAT) &&
                !message.getType().equals(MessageType.CLAN_MESSAGE) &&
                !message.getType().equals(MessageType.CLAN_GUEST_MESSAGE) &&
                !message.getType().equals(MessageType.CLAN_CREATION_INVITE) &&
                !message.getType().equals(MessageType.CLAN_WARS_CHALLENGE) &&
                !message.getType().equals(MessageType.CLAN_IRON_MAN_FORM_GROUP) &&
                !message.getType().equals(MessageType.CLAN_IRON_MAN_GROUP_WITH) &&
                !message.getType().equals(MessageType.AUTO) &&
                !message.getType().equals(MessageType.PRIVATE_INFO) &&
                !message.getType().equals(MessageType.PRIVATE_RECV) &&
                !message.getType().equals(MessageType.PRIVATE_SENT)) {
            if (message.getMessage().contains("You need your right hand free to pass an egg")) {
                eggWieldCheck = true;
            }
            if (message.getMessage().contains("You throw the egg.") ||
                    message.getMessage().contains("has something in") && message.getMessage().contains("hand.")) {
                eggThrown = true;
            }
            if (message.getMessage().contains(" is busy.")) {
                playerBusy = true;
            }
            if (message.getMessage().contains("won't thank you for chucking stuff")) {
                playerBusy = true;
            }
            if (message.getMessage().contains("is an Iron")) {
                playerIron = true;
            }
            return;
        }
    }
    @Override
    public String[] getPaintInfo()
    {
        List<String> paint = new ArrayList<>();
        paint.add(getManifest().name() +" "+ getManifest().version() + " by Dreambotter420 ^_^");
        paint.add("Egg stock: "+eggStock);
        paint.add("Players egged: "+eggCount);
        if (!BlacklistClient.blacklisted.isEmpty()) {
            StringBuilder list = new StringBuilder();
            boolean comma = false;
            for (String blacklizted : BlacklistClient.blacklisted) {
                if (comma) {
                    list.append(", ");
                }
                list.append(blacklizted);
                comma = true;
            }
            paint.add(list.toString());
        }
        return paint.toArray(new String[0]);
    }

    // Instantiate the paint object. This can be customized to your liking.
    private final CustomPaint CUSTOM_PAINT = new CustomPaint(this,
            CustomPaint.PaintLocations.BOTTOM_LEFT_PLAY_SCREEN,
            new Color[] {new Color(255, 251, 255)},
            "Impact",
            new Color[] {new Color(50, 50, 50, 175)},
            new Color[] {new Color(28, 28, 29)},
            1, false, 5, 3, 0);
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    @Override
    public void onPaint(Graphics2D graphics2D)
    {
        // Set the rendering hints
        graphics2D.setRenderingHints(aa);
        // Draw the custom paint
        CUSTOM_PAINT.paint(graphics2D);
    }
}