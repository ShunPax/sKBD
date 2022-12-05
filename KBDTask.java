package shunKBD;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class KBDTask extends Task {

	private WorldArea kingBlackDragonArea = new WorldArea(new WorldPoint(2256, 4680, 0), new WorldPoint(2287, 4711, 0));

	private final WorldPoint kbdLeverTile = new WorldPoint(2271, 4680, 0);

	private WorldArea edge = new WorldArea(new WorldPoint(3064, 3523, 0), new WorldPoint(3134, 3458, 0));

	private final WorldPoint bankTile = new WorldPoint(3089, 3498, 0);



	private Main main;

	public KBDTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean condition() {
		// TODO Auto-generated method stub
		return kingBlackDragonArea.containsPoint(ctx.players.getLocal().getLocation());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		SimpleNpc dragon = ctx.npcs.populate().filter("King black dragon").nearest().next();	

		if(kingBlackDragonArea.containsPoint(ctx.players.getLocal().getLocation()) && ctx.players.populate().population() > 1) {
			crowded();
		}

		if(main.poisoned) {
			drinkAntipoison();
		}

		eatFood();
		sipPrayer();
		checkAntiFire();

		if(!ctx.equipment.populate().filter("Amulet of glory(4)", "Amulet of glory(3)", "Amulet of glory(2)").isEmpty()) {
			if(!ctx.inventory.populate().filter("Shark").isEmpty()) {
				if(!ctx.inventory.populate().filter("Prayer potion(4)", "Prayer potion(3)", " Prayer potion(2)", "Prayer potion(1)").isEmpty()) {
					if(!ctx.pathing.onTile(kbdLeverTile)) {
						if(kingBlackDragonArea.containsPoint(ctx.players.getLocal().getLocation())) {
							if(main.lootOnGround()) {
								main.lootItem();
							} else {
								fightNpc(dragon);
							}
						}
					} else {
						ctx.mouse.click(635,32,true);
					}
				} else {
					bank();
				}
			} else {
				bank();
			}
		} else {
			bank();
		}
	}

	public void drinkAntipoison() {
		SimpleItem pot = ctx.inventory.populate().filter(p -> p.getName().contains("Antipoison")).next();
		if(pot != null && pot.click(0)) {
			main.poisoned = false;
			ctx.sleep(500);
		}
	}

	private void checkAntiFire() {
		if(main.shouldDrinkAntifire) {
			if(main.antifireTimer == 0 || main.antifireTimer != 0 && (System.currentTimeMillis() - main.antifireTimer) > 360000) {
				drinkAntiFire();
				main.antifireTimer = System.currentTimeMillis();
				main.shouldDrinkAntifire = false;
			}
		}
	}

	public void drinkAntiFire() {
		SimpleItem pot = ctx.inventory.populate().filter(p -> p.getName().contains("Antifire")).next();
		if(pot != null && pot.click(0)) {
			ctx.sleep(500);
		}
	}

	public void sipPrayer() {
		SimpleItem pPot = ctx.inventory.populate().filter("Prayer potion(4)", "Prayer potion(3)", "Prayer potion(2)", "Prayer potion(1)").next();
		if(ctx.pathing.inArea(kingBlackDragonArea)) {
			if(ctx.prayers.points() > 45) {
				ctx.prayers.prayer(Prayers.PIETY);
				ctx.prayers.prayer(Prayers.PROTECT_ITEM);
				ctx.prayers.prayer(Prayers.PROTECT_FROM_MAGIC);
			} else {
				if(ctx.prayers.points() <= 45 && pPot != null) {
					pPot.click(0);
				}
			}
		}
	}

	public void eatFood() {

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

	public void bank() {
		SimpleItem glory = ctx.equipment.populate().filter(e -> e.getName().toLowerCase().contains("glory")).next();
		SimpleObject banker = ctx.objects.populate().filter("Bank booth").nearest().next();
		if(ctx.players.getLocal().getLocation().distanceTo(bankTile) < 15)  {
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
		checkAntiFire();


		if (ctx.players.getLocal().getInteracting() == null) {
			SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter("King black dragon").filter(n -> (n.getInteracting() != null && n.getInteracting().getName().equals(ctx.players.getLocal().getName())));
			if (npcs != null && npcs.isEmpty())
				npcs = ctx.npcs.populate().filter("King black dragon").filter(n -> (n.getHealthRatio() == -1)); 
			if (npcs != null && !npcs.isEmpty()) {
				SimpleNpc npc = (SimpleNpc)npcs.nearest().next();
				if (npc != null && 
						npc.validateInteractable())
					npc.click("Attack");
			}
		}
	}

	public void crowded() {
		SimpleItem glory = ctx.equipment.populate().filter(e -> e.getName().toLowerCase().contains("glory")).next();

		if(!edge.containsPoint(ctx.players.getLocal().getLocation())) {
			if(glory != null && glory.validateInteractable()) {
				glory.click("Edgeville");
				ctx.onCondition(() -> edge.containsPoint(ctx.players.getLocal().getLocation()), 250,10);
			}
			ctx.updateStatus("Fleeing the scene - Anti-Ban");
			ctx.updateStatus("Sleep for 10 Mins");
			disablePrayers();
			ctx.sleep(10000);
			ctx.sendLogout();
			ctx.sendLogout();
			ctx.sleep(600000);
		}
	}

	public void disablePrayers() {
		ctx.prayers.prayer(Prayers.PIETY, false);
		ctx.prayers.prayer(Prayers.PROTECT_ITEM, false);
		ctx.prayers.prayer(Prayers.PROTECT_FROM_MELEE, false);
		ctx.prayers.prayer(Prayers.PROTECT_FROM_MAGIC, false);
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

	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "Kill KBD";
	}

}
