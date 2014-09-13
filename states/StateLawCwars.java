package misc.scripts.runecrafter.states;

import misc.scripts.runecrafter.MiscRunecrafter;
import misc.scripts.runecrafter.util.Util;
import misc.scripts.runecrafter.util.Values;

import org.hexbot.api.methods.Calculations;
import org.hexbot.api.methods.Camera;
import org.hexbot.api.methods.Walking;
import org.hexbot.api.methods.helper.ChatOptions;
import org.hexbot.api.methods.helper.Equipment;
import org.hexbot.api.methods.helper.Tab;
import org.hexbot.api.methods.interactable.GameObjects;
import org.hexbot.api.methods.interactable.Npcs;
import org.hexbot.api.methods.interactable.Players;
import org.hexbot.api.methods.node.Inventory;
import org.hexbot.api.methods.node.Widgets;
import org.hexbot.api.util.Area;
import org.hexbot.api.util.Filter;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.interactable.GameObject;
import org.hexbot.api.wrapper.interactable.Npc;
import org.hexbot.api.wrapper.node.WidgetComponent;
import org.hexbot.core.concurrent.script.Condition;

public enum StateLawCwars {
	/*
	 * TODO
	 *  long pause at balloon landing
	 *  figure out how to actually open the cwars doors
	 */
	TO_BALLOON,
	TO_ENTRANA,
	TO_RUINS,
	TO_ALTAR,
	DO_RUNECRAFT,
	TO_CWARS;

	public static final int ID_RUINS = 10777;
	public static final int ID_ALTAR = 9389;
	public static final int ID_CWARS_CHEST = 4483;
	public static final int ID_NPC_ASSISTANT = 3956;
	public static final int ID_WIDGET_BALLOON_SCREEN = 469;
	public static final int ID_COMPONENT_ENTRANA = 19;
	public static final int ID_WIDGET_FLYCHAT1 = 242;
	public static final int ID_COMPONENT_FLYCHAT1 = 4;
	public static final int ID_WIDGET_FLYCHAT2 = 210;
	public static final int ID_COMPONENT_FLYCHAT2 = 1;
	public static final int CAMERA_ANGLE_CWARS_DOOR = 270;
	public static final int CAMERA_ANGLE_RUINS = 5;
	public static final int CAMERA_ANGLE_BALLOON = 335;

	public static final Tile TILE_CWARS_CHEST = new Tile(2443,3083,0);
	public static final Tile TILE_CWARS_DOOR = new Tile(2445,3089,0);
	public static final Tile TILE_CWARS_MIDDLE = new Tile(2441,3089,0);
	public static final Tile TILE_RUINS = new Tile(2858,3378,0);
	public static final Tile TILE_ALTAR = new Tile(2464,4829,0);
	public static final Tile TILE_CWARS_OUTSIDE = new Tile(2447,3089,0);
	public static final Tile TILE_CWARS_BALLOON = new Tile(2459,3107,0);

	public static final Tile[] PATH_CWARS_TO_BALLOON = {
		TILE_CWARS_BALLOON,
		new Tile(2456,3099,0),
		new Tile(2455,3092,0),
	};
	public static final Tile[] PATH_BALLOON_TO_RUINS = {
		TILE_RUINS,
		new Tile(2858,3375,0),
		new Tile(2858,3368,0),
		new Tile(2858,3359,0),
		new Tile(2859,3349,0),
		new Tile(2850,3348,0),
		new Tile(2842,3348,0),
		new Tile(2834,3344,0),
		new Tile(2826,3344,0),
		new Tile(2817,3344,0),
		new Tile(2813,3351,0),
	};

	public static final Area AREA_CWARS_BANK = new Area(new Tile(2436,3098,0), new Tile(2446,3082,0));
	public static final Area AREA_ENTRANA_BALLOON = new Area(new Tile(2780,3365,0), new Tile(2818,3345,0));
	public static final Area AREA_ALTAR = new Area(new Tile(2445,4852,0), new Tile(2485,4810,0));

	public static void traverse(){
		if(Camera.getPitch() > 150)
			Camera.setPitch(false);
		switch(MiscRunecrafter.stateLawCwars){
		case TO_BALLOON:
			if(Calculations.distanceTo(TILE_CWARS_BALLOON) < 5){
				MiscRunecrafter.stateLawCwars = TO_ENTRANA;
				break;
			}
			GameObject door = GameObjects.getNearest(new Filter<GameObject>(){
				@Override
				public boolean accept(GameObject arg0) {
					return arg0.getName().equals("Large door") && arg0.getLocation().equals(TILE_CWARS_DOOR);
				}});
			if(AREA_CWARS_BANK.contains(Players.getLocal()) && door != null){
				Walking.walk(TILE_CWARS_MIDDLE);
				Camera.turnTo(door);
				if(door.interact("Open"))
					Time.sleep(2000);
				else
					break;
			}
			//			if(AREA_CWARS_BANK.contains(Players.getLocal())){
			//				//Camera.setPitch(true);
			//				if(Math.abs(Camera.getAngle() - CAMERA_ANGLE_CWARS_DOOR) > 10)
			//					Camera.setCameraRotation(CAMERA_ANGLE_CWARS_DOOR);
			//				if(Calculations.distanceTo(TILE_CWARS_MIDDLE) > 2){
			//					Walking.walk(TILE_CWARS_MIDDLE);
			//					Time.sleep(1000);
			//					break;
			//				}
			//				TILE_CWARS_OUTSIDE.interact("Open");
			//				Time.sleep(1700);
			//				if(AREA_CWARS_BANK.contains(Players.getLocal())){
			//					Walking.walk(TILE_CWARS_OUTSIDE);
			//					Time.sleep(700);
			//				}
			//				break;
			//			}
			if(Camera.getAngle() > 350 || Camera.getAngle() < 320)
				Camera.setCameraRotation(CAMERA_ANGLE_BALLOON);
			if(Util.stepPath(PATH_CWARS_TO_BALLOON))
				Time.sleep(1000);
			break;
		case TO_ENTRANA:
			if(AREA_ENTRANA_BALLOON.contains(Players.getLocal())){
				MiscRunecrafter.stateLawCwars = TO_RUINS;
				Time.sleep(2000);
				break;
			}
			WidgetComponent chatContinue1 = Widgets.getChild(ID_WIDGET_FLYCHAT1, ID_COMPONENT_FLYCHAT1);
			if(chatContinue1 != null && !chatContinue1.isHidden()){
				chatContinue1.click();
				Time.sleep(700);
				break;
			}
			WidgetComponent chatContinue2 = Widgets.getChild(ID_WIDGET_FLYCHAT2, ID_COMPONENT_FLYCHAT2);
			if(chatContinue2 != null && !chatContinue2.isHidden()){
				chatContinue2.click();
				Time.sleep(700);
				break;
			}
			Npc assistant = Npcs.getNearest(ID_NPC_ASSISTANT);
			if(assistant != null){
				if(Util.interact(assistant, "Fly")){
					MiscRunecrafter.waitFor(new Condition(){
						@Override
						public boolean validate() {
							return Widgets.getChild(ID_WIDGET_BALLOON_SCREEN, ID_COMPONENT_ENTRANA) != null;
						}}, 4000);
				}
				WidgetComponent flyButton = Widgets.getChild(ID_WIDGET_BALLOON_SCREEN, ID_COMPONENT_ENTRANA);
				if(flyButton != null && !flyButton.isHidden()){
					flyButton.click();
					Time.sleep(2500);
				}
			}
			break;
		case TO_RUINS:
			if(AREA_ALTAR.contains(Players.getLocal())){
				MiscRunecrafter.stateLawCwars = TO_ALTAR;
				break;
			}
			if(Camera.getAngle() > 15 && Camera.getAngle() < 350)
				Camera.setCameraRotation(CAMERA_ANGLE_RUINS);
			if(Calculations.distanceTo(TILE_RUINS) < 7){
				GameObject ruins = GameObjects.getNearest(ID_RUINS);
				if(ruins != null){
					if(!ruins.isVisible())
						Camera.turnTo(ruins);
					ruins.click();
					Time.sleep(2000);
					break;
				}
			}
			if(Util.stepPath(PATH_BALLOON_TO_RUINS))
				Time.sleep(500,600);
			break;
		case TO_ALTAR:
			GameObject altar1 = GameObjects.getNearest(ID_ALTAR);
			if(altar1 != null && altar1.isVisible() && Calculations.distanceTo(altar1) < 10){
				MiscRunecrafter.stateLawCwars = DO_RUNECRAFT;
				break;
			}
			if(altar1 != null && !altar1.isVisible())
				Camera.turnTo(altar1);
			if(Walking.walk(TILE_ALTAR))
				Time.sleep(700);
			break;
		case DO_RUNECRAFT:
			if(Inventory.getCount(Values.ID_RUNE_ESSENCE, Values.ID_PURE_ESSENCE) == 0 && Util.isUsingPouches()){
				Util.emptyPouches();
			}
			if(Inventory.getCount(Values.ID_RUNE_ESSENCE) == 0 && Inventory.getCount(Values.ID_PURE_ESSENCE) == 0){
				MiscRunecrafter.stateLawCwars = TO_CWARS;
				break;
			}
			GameObject altar2 = GameObjects.getNearest(ID_ALTAR);
			if(altar2 == null)
				break;
			if(!altar2.isVisible()){
				Camera.turnTo(altar2);
				Walking.walk(altar2);
			}
			if(altar2.interact("Craft-rune"))
				Time.sleep(2500);
			break;
		case TO_CWARS:
			GameObject chest = GameObjects.getNearest(ID_CWARS_CHEST);
			if(chest != null && Calculations.distanceTo(chest) < 50){
				if(Calculations.distanceTo(chest) < 3){
					MiscRunecrafter.stateLawCwars = TO_BALLOON;
					MiscRunecrafter.traversing = false;
					MiscRunecrafter.banking = true;
					--MiscRunecrafter.countDuelingRing;
					break;
				}
				else{
					Camera.turnTo(chest);
					Walking.walk(TILE_CWARS_CHEST);
					break;
				}
			}
			Tab.open(Tab.EQUIPMENT);
			Time.sleep(700,900);
			if(Equipment.getSlotWidget(Equipment.Slot.RING).interact("Operate")){
				Time.sleep(1000,1200);
				if(!AREA_CWARS_BANK.contains(Players.getLocal())){
					ChatOptions.getWithText("Castle Wars Arena.").click();
					MiscRunecrafter.waitFor(new Condition(){
						@Override
						public boolean validate() {
							return AREA_CWARS_BANK.contains(Players.getLocal());
						}}, 5000);
					Time.sleep(1000, 1200);
				}
			}

			break;
		}
	}
}
