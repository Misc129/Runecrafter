package misc.scripts.runecrafter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

import misc.scripts.runecrafter.states.StateAirFalador;
import misc.scripts.runecrafter.states.StateFireCwars;
import misc.scripts.runecrafter.states.StateLawCwars;
import misc.scripts.runecrafter.util.Util;
import misc.scripts.runecrafter.util.Values;
import misc.scripts.runecrafter.workers.BankItems;
import misc.scripts.runecrafter.workers.Traverse;

import org.hexbot.api.listeners.Paintable;
import org.hexbot.api.methods.Skills;
import org.hexbot.api.methods.input.Mouse;
import org.hexbot.api.methods.input.Mouse.Speed;
import org.hexbot.api.util.Area;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.node.Item;
import org.hexbot.core.concurrent.script.Condition;
import org.hexbot.core.concurrent.script.Info;
import org.hexbot.core.concurrent.script.TaskScript;
import org.hexbot.core.concurrent.script.Type;
/*
 * TODO
 * mark points of time where state of script changes, if idle for more than some time, stop script
 */
@Info(
		name = "MiscRunecrafter",
		author = "Misc",
		description = "v2",
		type = Type.RUNECRAFTING
		)
public class MiscRunecrafter extends TaskScript implements Paintable{

	public static long timeStart, timeLastRemovePoint, timeNextRemovePoint, millis, seconds, minutes, hours;
	public static int countDuelingRing = 0,innerOrbitAngle = 0, innerOrbitAngle2 = 180, 
			outerOrbitAngle = 90, outerOrbitAngle2 = 270, startExp, gainedExp;
	public static boolean debugging = true, traversing, banking, useRuneEssence, usePureEssence,
			useDuelingRing, useSmallPouch, useMediumPouch, useLargePouch, useGiantPouch;

	public static Values.RuneType runeType;
	public static StateAirFalador stateAirFalador = null;
	public static StateFireCwars stateFireCwars = null;
	public static StateLawCwars stateLawCwars = null;

	public static Area areaBank = null;
	public static Tile tileBank = null;

	public static LinkedList<Item> inventoryItems = null;
	private static LinkedList<Point> points = null;

	public MiscRunecrafter(){
		points = new LinkedList<Point>();
		inventoryItems = new LinkedList<Item>();
		startExp = Skills.RUNECRAFTING.getExperience();
		timeStart = timeLastRemovePoint = timeNextRemovePoint = System.currentTimeMillis();

		usePureEssence = true;

		//useSmallPouch = true;inventoryItems.add(new Item(Values.ID_SMALL_POUCH, 1));
		//useMediumPouch = true;inventoryItems.add(new Item(Values.ID_MEDIUM_POUCH, 1));
		//useLargePouch = true;inventoryItems.add(new Item(Values.ID_LARGE_POUCH, 1));

		tempDoLaw();
		int numEmptySpaces = 28 - inventoryItems.size();
		inventoryItems.add(new Item(Values.ID_PURE_ESSENCE,numEmptySpaces));

		submit(new Traverse(), new BankItems());
	}

	private void tempDoAir(){
		runeType = Values.RuneType.AIR_FALADOR;
		areaBank = StateAirFalador.AREA_BANK;
		tileBank = StateAirFalador.TILE_BANK;

		stateAirFalador = StateAirFalador.TO_BANK;
		traversing = true;
	}
	private void tempDoFire(){
		runeType = Values.RuneType.FIRE_CWARS;
		areaBank = StateFireCwars.AREA_CWARS_BANK;
		tileBank = StateFireCwars.TILE_CWARS_BANK;
		stateFireCwars = StateFireCwars.TO_DUELARENA;
		useDuelingRing = true;
		countDuelingRing = 0;
		banking = true;
	}
	private void tempDoLaw(){
		runeType = Values.RuneType.LAW_CWARS;
		areaBank = StateLawCwars.AREA_CWARS_BANK;
		tileBank = StateLawCwars.TILE_CWARS_CHEST;
		inventoryItems.add(new Item(Values.ID_REGULAR_LOG, 1));
		stateLawCwars = StateLawCwars.TO_BALLOON;
		useDuelingRing = true;
		countDuelingRing = 0;
		banking = true;
	}

	public static void waitFor(Condition condition, int timeout){
		for(int i = 0; i < timeout; i += 100){
			if(condition.validate())
				return;
			Time.sleep(100);
		}
	}

	private void updatePoints(){
		if(points.isEmpty()){
			points.add(0,Mouse.getLocation());
			timeNextRemovePoint = System.currentTimeMillis() + 500;
		}
		else if(!Mouse.getLocation().equals(points.get(0))){
			points.add(0,Mouse.getLocation());
		}
		if(!points.isEmpty() && System.currentTimeMillis() > timeNextRemovePoint){
			timeLastRemovePoint = timeNextRemovePoint;
			timeNextRemovePoint = System.currentTimeMillis() + 30;
			points.removeLast();
		}
	}

	private void drawPoints(Graphics g){
		if(points.size() > 1){
			Point prev = points.getFirst();
			for(Point p : points){
				if(!prev.equals(p))
					g.drawLine(prev.x, prev.y, p.x, p.y);
				prev = p;
			}
		}
	}

	private void updateOrbits(){
		if(innerOrbitAngle >= 360)
			innerOrbitAngle = 0;
		if(innerOrbitAngle2 >= 360)
			innerOrbitAngle2 = 0;
		if(outerOrbitAngle >= 360)
			outerOrbitAngle = 0;
		if(outerOrbitAngle2 >= 360)
			outerOrbitAngle2 = 0;
		innerOrbitAngle+=2;
		innerOrbitAngle2+=2;
		outerOrbitAngle-=2;
		outerOrbitAngle2-=2;
	}

	private void drawCursorOrbit(Graphics g){
		g.setColor(Color.green);
		g.drawArc(Mouse.getX() - 10, Mouse.getY() - 10, 20, 20, innerOrbitAngle, 135);
		g.drawArc(Mouse.getX() - 10, Mouse.getY() - 10, 20, 20, innerOrbitAngle2, 135);
		g.drawArc(Mouse.getX() - 15, Mouse.getY() - 15, 30, 30, outerOrbitAngle, 135);
		g.drawArc(Mouse.getX() - 15, Mouse.getY() - 15, 30, 30, outerOrbitAngle2, 135);

	}

	private void drawMouse(Graphics g){
		g.setColor(Color.red);
		g.fillRect(Mouse.getX() - 1, Mouse.getY() - 5, 2, 10);
		g.fillRect(Mouse.getX() - 5, Mouse.getY() - 1, 10, 2);
	}

	private void formatTime(){
		millis = System.currentTimeMillis() - timeStart;
		hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		seconds = millis / 1000;
	}

	@Override
	public void paint(Graphics g) {
		drawMouse(g);

		updatePoints();
		drawPoints(g);

		updateOrbits();
		drawCursorOrbit(g);

		//g.setColor(Color.white);
		//g.fillRect(350, 340, 170, 130);
		g.setColor(Color.red);

		if(debugging){
			g.drawString("debugging", 410, 200);
			g.drawString("traversing: "+traversing, 390, 215);
			g.drawString("banking: "+banking, 390, 230);
			switch(runeType){
			case AIR_FALADOR:
				g.drawString("state: "+stateAirFalador, 390, 245);
				break;
			case FIRE_CWARS:
				g.drawString("state: "+stateFireCwars, 390, 260);
				g.drawString("countDuelingRing: "+countDuelingRing, 390, 275);
				break;
			case LAW_CWARS:
				g.drawString("state: "+stateLawCwars, 390, 260);
				g.drawString("countDuelingRing: "+countDuelingRing, 390, 275);
			}
		}

		g.setColor(Color.gray);
		g.fillRoundRect(350, 347, 147, 112, 10, 10);
		g.setColor(Color.BLACK);
		g.drawString("MiscRunecrafter", 380, 360);
		formatTime();
		g.drawString("Runtime: " + hours +":"+ minutes + ":" + seconds, 355, 375);
		g.drawString("runes:" + Util.getRuneCount(), 355, 390);
		gainedExp = Skills.RUNECRAFTING.getExperience() - startExp;
		g.drawString("exp:"+gainedExp, 355, 405);

		//StateLawCwars.AREA_CWARS_BANK.draw(g);
	}

}
