package shunKBD;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Shun", category =Category.COMBAT, description = 
"shunKBD"
		+ "<br><br> Will kill KBD",
		discord = "Shun#2997", name = "shunKBD", servers = { "Zaros" }, version = "1.0")

public class Main extends TaskScript {

	WorldPoint[] wildyPath = {
			new WorldPoint(3032, 3838, 0),
			new WorldPoint(3032, 3838, 0),
			new WorldPoint(3026, 3838, 0),
			new WorldPoint(3020, 3838, 0),
			new WorldPoint(3014, 3838, 0),
			new WorldPoint(3008, 3839, 0),
			new WorldPoint(3007, 3845, 0),
			new WorldPoint(3006, 3849, 0)
	};

	public ArrayList<String> lootName = new ArrayList<String>();
	private List<Task> tasks = new ArrayList<Task>();

	private WorldArea kingBlackDragon = new WorldArea(new WorldPoint(3352, 10165, 0), new WorldPoint(3375, 10134, 0));

	private WorldArea edge = new WorldArea(new WorldPoint(3064, 3523, 0), new WorldPoint(3134, 3458, 0));

	private WorldArea wildyArea = new WorldArea(new WorldPoint(3064, 3523, 0), new WorldPoint(3134, 3458, 0));

	private WorldArea leverArea = new WorldArea(new WorldPoint(2547, 7895, 0), new WorldPoint(1245, 3458, 0));

	private final WorldPoint bankTile = new WorldPoint(3089, 3498, 0);

	public long antifireTimer = 0;

	public boolean shouldDrinkAntifire = true;
	
	public boolean poisoned;

	@Override
	public void paint(Graphics arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onChatMessage(ChatMessage msg) {
		// TODO Auto-generated method stub

		if(msg.getMessage().contains("antifire potion is about to run out") || msg.getMessage().contains("antifire potion has expired")) {
			shouldDrinkAntifire = true;
			antifireTimer = 0;
		}

		if(msg.getMessage().contains("have been poisoned")) {
			poisoned = true;
		}
		
		if(msg.getMessage().contains("oh dear, you are dead")) {
			ctx.updateStatus("We Died :(");
			ctx.updateStatus("Sleeping for 5 Mins");
			ctx.updateStatus("Let the Pkers find a");
			ctx.updateStatus("Differant Bounty");
			ctx.updateStatus("Before we go back");
			ctx.sleep(300000);
		}

	}

	@Override
	public void onExecute() {
		// TODO Auto-generated method stub

		lootName.addAll(Arrays.asList("Ancient totem","Ancient Statuette","Ancient emblem", "Ancient relic", "Blood money", "Gold leaf",
				"Smouldering stone", "Clue scroll (hard)", "Burnt page", "Uncut dragonstone","Magic stone", "Crystal key", "Tooth half of key", "Loop half of key",
				"Clue scroll (elite)", "Ancient effigy", "Ancient medallion", "Dragon spear", "Wilderness key", "Key", 
				"Slayer casket", "Draconic visage", "Wilderness slayer casket", "Clue scroll (Wilderness)",
				"Larran's key", "Antique emblem (tier 1)", "Antique emblem (tier 2)", "Antique emblem (tier 3)", "Antique emblem (tier 4)", "Antique emblem (tier 5)", "Clue scroll (wilderness)",
				"Blighted ancient ice sack", "Blighted teleport spell sack", "Slayer's enchantment", "Dragon chainbody", "Dust battlestaff", "Dragon boots",
				"Magic shortbow scroll", "Ring of wealth scroll", "Trouver parchment", "Looting bag note",
				"Revenant cave teleport", "Abyssal whip","Wilderness key (red)", "Wilderness key (blue)", "Wilderness key (green)", "Dragon arrowtips", "Dragon dart tip", "Runite bar","Kbd heads",
				"Dragon pickaxe","Gold ore", "Dragon bones"));

		tasks.addAll(Arrays.asList(new BankTask(ctx, this), new WildyTask(ctx, this),
				new LeverTask(ctx, this), new KBDTask(ctx, this)));

	}


	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean prioritizeTasks() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Task> tasks() {
		// TODO Auto-generated method stub
		return tasks;
	}


	public boolean shouldRestock() {
		if(ctx.pathing.inArea(edge)) {
			return true;
		}
		if(!containsItem("prayer")) {
			return true;
		} 
		if(!containsItem("shark")) {
			return true;
		}
		return false;
	}

	public boolean containsItem(String itemName) {
		return !ctx.inventory.populate().filter(p -> p.getName().toLowerCase().contains(itemName.toLowerCase())).isEmpty();
	}

	public void lootItem() {
		SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();
		SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate().filter(lootName.stream().toArray(String[]::new));
		if(lootation.size() > 0) {
			SimpleGroundItem item = lootation.nearest().next();
			if(item.getLocation().distanceTo(ctx.players.getLocal().getLocation()) < 15) {
				if(item != null && item.validateInteractable()) {
					if(ctx.inventory.canPickupItem(item)) {
						if(item.click("Take")) {
							ctx.sleep(1000);
						}
					} else {
						if(food != null && ctx.inventory.populate().filterHasAction("Eat").population() >= 1) {
							food.click(1);
							food.click(1);
						}
					}
				}
			}
		}
	}

	public void bank() {
		SimpleItem glory = ctx.equipment.populate().filter(e -> e.getName().toLowerCase().contains("glory")).next();
		SimpleObject banker = ctx.objects.populate().filter("Bank booth").nearest().next();
		if(ctx.players.getLocal().getLocation().distanceTo(bankTile) < 5)  {
			if (!ctx.bank.bankOpen()) {
				if (banker != null && banker.validateInteractable()) {
					banker.click("Last-Preset");
					ctx.onCondition(() -> !ctx.inventory.populate().filter("Coins").isEmpty(), 250,10);
				}
			}
		} else {
			if(glory != null && glory.validateInteractable()) {
				glory.click("Edgeville");
				ctx.onCondition(() -> edge.containsPoint(ctx.players.getLocal().getLocation()), 250,10);

			}
		}
	}

	private void fightNpc(SimpleNpc dragon) {

		drinkCombatPotion();
		drinkStrengthPotion();
		drinkAttackPotion();


		if (ctx.players.getLocal().getInteracting() == null) {
			SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter("King black dragon").filter(n -> (n.getInteracting() != null && n.getInteracting().getName().equals(ctx.players.getLocal().getName())));
			if (npcs != null && npcs.isEmpty())
				npcs = ctx.npcs.populate().filter("Black dragon").filter(n -> (n.getHealthRatio() == -1)); 
			if (npcs != null && !npcs.isEmpty()) {
				SimpleNpc npc = (SimpleNpc)npcs.nearest().next();
				if (npc != null && 
						npc.validateInteractable())
					npc.click("Attack");
			}
		}
	}

	public boolean skillShouldBeBoosted(SimpleSkills.Skills s) {
		int lvl = ctx.skills.realLevel(s);
		int lvl1 = ctx.skills.level(s);
		int diff = lvl1 - lvl;
		if (diff <= 6)
			return true; 
		return false;
	}

	public void drinkStrengthPotion() {
		if (skillShouldBeBoosted(SimpleSkills.Skills.STRENGTH)) {
			SimpleItem str = ctx.inventory.populate().filter(e -> e.getName().toLowerCase().contains("strength")).next();
			if (str != null)
				str.click(1);
			ctx.sleep(1500);
		} 
	}

	public void drinkAttackPotion() {
		if (skillShouldBeBoosted(SimpleSkills.Skills.ATTACK)) {
			SimpleItem att = ctx.inventory.populate().filter(e -> e.getName().toLowerCase().contains("attack")).next();
			if (att != null)
				att.click(0); 
			ctx.sleep(1500);
		} 
	}

	public void drinkCombatPotion() {
		if (skillShouldBeBoosted(SimpleSkills.Skills.STRENGTH)) {
			SimpleItem combat = ctx.inventory.populate().filter(e -> e.getName().toLowerCase().contains("combat")).next();
			if (combat != null)
				combat.click(0); 
			ctx.sleep(1500);
		}
	}

	public void eatFood() {

		if(!ctx.players.populate().filter("lonelytit").isEmpty()) {
			ctx.magic.castHomeTeleport();
			ctx.sleep(30000);
			ctx.updateStatus("Sleeping players in area");
		}

		SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();
		if(ctx.players.getLocal().getHealth() > 60) {
		} else if (ctx.players.getLocal().getHealth() < 60) {
			if(food != null) {
				food.click(1);
				food.click(1);
				ctx.onCondition(() -> ctx.players.getLocal().getHealth() > 60, 2000);
			}
		}
	}


	public boolean lootOnGround() {
		SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate().filter(lootName.stream().toArray(String[]::new));
		if(lootation.size() > 0) {
			return true;
		}
		return false;
	}

}
