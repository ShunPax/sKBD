package shunKBD;

import java.util.function.Predicate;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class WildyTask extends Task {

	WorldPoint[] wildyPath = {
			new WorldPoint(3039, 3837, 0),
			new WorldPoint(3039, 3837, 0),
			new WorldPoint(3033, 3837, 0),
			new WorldPoint(3027, 3837, 0),
			new WorldPoint(3021, 3837, 0),
			new WorldPoint(3015, 3837, 0),
			new WorldPoint(3009, 3838, 0),
			new WorldPoint(3007, 3844, 0),
			new WorldPoint(3008, 3849, 0)
	};

	private WorldArea wildyArea = new WorldArea(new WorldPoint(2987, 3868, 0), new WorldPoint(3069, 3809, 0));

	private Main main;

	public WildyTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean condition() {
		// TODO Auto-generated method stub
		return wildyArea.containsPoint(ctx.players.getLocal().getLocation());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		/*SimpleObject gate = ctx.objects.populate().filter(1727,1728).filter(new Predicate<SimpleObject>() {
			@Override
			public boolean test(SimpleObject object) {
				return object.getLocation().getX() == 3008;
			}
		}).nearest().next();*/

		SimpleObject gate = ctx.objects.populate().filter(1727, 1728).nearest().next();

		SimpleObject ladder = ctx.objects.populate().filter(18987).nearest().next();

		//ctx.pathing.walkPath(wildyPath);
		
		ctx.prayers.prayer(Prayers.PROTECT_ITEM);

		if(gate.getLocation().getX() == 3008) {
			gate.click("Open");
			ctx.onCondition(() -> !(gate.getLocation().getX() == 3008) && !(gate.getLocation().getY() == 3849 || gate.getLocation().getX() == 3008) && !(gate.getLocation().getY() == 3850) ,250,15);
		} else {
			if(ladder != null && ladder.validateInteractable()) {
				ladder.click("Climb-down");
				ctx.onCondition(() -> !wildyArea.containsPoint(ctx.players.getLocal().getLocation()), 250,15);
			}
		}
	}

	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "Wildy Area";
	}

}
