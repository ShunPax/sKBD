package shunKBD;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class LeverTask extends Task {
	
	private WorldArea leverArea = new WorldArea(new WorldPoint(3060, 10264, 0), new WorldPoint(3075, 10248, 0));
	
	private Main main;

	public LeverTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean condition() {
		// TODO Auto-generated method stub
		return leverArea.containsPoint(ctx.players.getLocal().getLocation());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		SimpleObject lever = ctx.objects.populate().filter(1816).nearest().next();
		
		if(lever !=null && lever.validateInteractable()) {
			lever.click("Pull");
			ctx.onCondition(() ->!leverArea.containsPoint(ctx.players.getLocal().getLocation()), 250,15);
		}
		
	}

	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "Lever Area";
	}

}
