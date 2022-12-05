package shunKBD;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.task.Task;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class BankTask extends Task {

	private WorldArea edge = new WorldArea(new WorldPoint(3064, 3523, 0), new WorldPoint(3134, 3458, 0));

	private final WorldPoint bankTile = new WorldPoint(3089, 3498, 0);

	private Main main;

	public BankTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean condition() {
		// TODO Auto-generated method stub
		return edge.containsPoint(ctx.players.getLocal().getLocation());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		if(main.shouldRestock()) {
			bank();
		}
		
			if(ctx.game.tab(Game.Tab.MAGIC) && ctx.widgets.getWidget(218, 6) != null) {		
				ctx.widgets.getWidget(218, 6).click(3);
				ctx.sleep(500);
				ctx.dialogue.clickDialogueOption(4);
				ctx.onCondition(() -> !edge.containsPoint(ctx.players.getLocal().getLocation()), 250,10);
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

	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "Bank Task";
	}

}
