/*******************************************************************************
 * Copyright (c) 2015 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.graphics.map.controls.original.panel.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.buildings.IBuildingMaterial;
import jsettlers.common.buildings.IBuildingOccupier;
import jsettlers.common.buildings.stacks.ConstructionStack;
import jsettlers.common.map.partition.IStockSettings;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.material.EPriority;
import jsettlers.common.movable.ESoldierClass;
import jsettlers.common.movable.IGraphicsMovable;
import jsettlers.graphics.localization.Labels;

/**
 * This class saves the state parts of the building that is displayed by the gui, to detect changes.
 * 
 * @author Michael Zangl
 */
public class BuildingState {

	private final EPriority priority;
	private final EPriority[] supportedPriorities;
	private final ArrayList<StackState> stackStates = new ArrayList<>();
	private final boolean construction;

	/**
	 * An array: soldier class -> available places.
	 */
	private final Hashtable<ESoldierClass, ArrayList<OccupierState>> occupierStates;
	private final BitSet stockStates;
	private final int[] tradingCounts;
	private final boolean isSeaTrading;
	private final boolean isDockyard;
	private final boolean isWorkingDockyard;

	/**
	 * This is the state for a building stack.
	 * 
	 * @author Michael Zangl
	 *
	 */
	public static class StackState {
		private final EMaterialType type;
		private final int count;
		private final boolean offering;
		private final int required;

		/**
		 * Create a new stack state
		 * 
		 * @param mat
		 *            The material stack to create the state for.
		 */
		public StackState(IBuildingMaterial mat) {
			this(mat.getMaterialType(), mat.getMaterialCount(), mat.isOffering(), -1);
		}

		/**
		 * Create a new stack state
		 * @param type the type of this tack
		 * @param count the amount of material to offer
		 * @param offering is this an offer or request stack
		 * @param required the required amount of this resource
		 */
		public StackState(EMaterialType type, int count, boolean offering, int required) {
			this.type = type;
			this.count = count;
			this.offering = offering;
			this.required = required;
		}

		/**
		 * Check if the stack state is still the same.
		 * 
		 * @param mat
		 *            The material stack to check against.
		 * @return <code>true</code> if the state is the same.
		 */
		public boolean isStillInState(IBuildingMaterial mat) {
			return mat.getMaterialType() == type && mat.getMaterialCount() == count && mat.isOffering() == offering;
		}

		/**
		 * @return the type
		 */
		public EMaterialType getType() {
			return type;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @return true if this is an offer stack.
		 */
		public boolean isOffering() {
			return offering;
		}

		public int getRequired() {
			return required;
		}

		public String getInfoString() {
			String label;
			if (required < 0) {
				label = "building-material-count";
			} else {
				label = "building-material-required";
			}
			return Labels.getString(label, count, required);
		}

		public boolean isValid() {
			return type != null;
		}
	}

	/**
	 * Creates a new occupyer state.
	 * 
	 * @author Michael Zangl
	 *
	 */
	public static class OccupierState {
		private final IGraphicsMovable movable;
		private final boolean comming;

		private OccupierState(IGraphicsMovable movable) {
			this.movable = movable;
			comming = false;
		}

		private OccupierState(boolean comming) {
			this.comming = comming;
			movable = null;
		}

		/**
		 * @return <code>true</code> if the comming image should be displayed.
		 */
		public boolean isComming() {
			return comming;
		}

		/**
		 * @return <code>true</code> if the missing image should be displayed.
		 */
		public boolean isMissing() {
			return movable == null && !isComming();
		}

		/**
		 * @return The movable that is in this stack or <code>null</code> for none.
		 */
		public IGraphicsMovable getMovable() {
			return movable;
		}
	}

	/**
	 * Stores the current state of the building.
	 * 
	 * @param building
	 *            the building
	 */
	public BuildingState(IBuilding building) {
		priority = building.getPriority();
		supportedPriorities = building.getSupportedPriorities();
		construction = building.getStateProgress() < 1;
		occupierStates = computeOccupierStates(building);
		stockStates = computeStockStates(building);
		tradingCounts = computeTradingCounts(building);
		for (IBuildingMaterial mat : building.getMaterials()) {
			StackState newState = new StackState(mat);
			if(newState.isValid()) {
				stackStates.add(newState);
			}
		}
		mergeConstructionStacks(building);
		isSeaTrading = building instanceof IBuilding.ITrading && ((IBuilding.ITrading) building).isSeaTrading();
		isDockyard = building.getBuildingVariant().isVariantOf(EBuildingType.DOCKYARD);
		isWorkingDockyard = (building instanceof IBuilding.IShipConstruction && ((IBuilding.IShipConstruction) building).getOrderedShipType() != null);
	}

	private void mergeConstructionStacks(IBuilding building) {
		if(building.getStateProgress() >= 0.99f) return;

		Map<EMaterialType, Integer> requiredBuildingMaterials = new HashMap<>();
		for(ConstructionStack stack : building.getBuildingVariant().getConstructionStacks()) {
			requiredBuildingMaterials.compute(stack.getMaterialType(), (k, v) -> (v != null ? v : 0) + stack.requiredForBuild());
		}
		Map<EMaterialType, Integer> buildingMaterials = new HashMap<>(requiredBuildingMaterials);

		Iterator<StackState> iter = stackStates.iterator();
		while(iter.hasNext()) {
			StackState stack = iter.next();
			if(buildingMaterials.containsKey(stack.getType())) {
				iter.remove();
				buildingMaterials.compute(stack.getType(), (k, v) -> v-stack.getCount());
			}
		}

		for(Map.Entry<EMaterialType, Integer> buildingMat : buildingMaterials.entrySet()) {
			stackStates.add(new StackState(buildingMat.getKey(), buildingMat.getValue(), false, requiredBuildingMaterials.get(buildingMat.getKey())));
		}
	}

	private int[] computeTradingCounts(IBuilding building) {
		if (building instanceof IBuilding.ITrading) {
			IBuilding.ITrading trading = (IBuilding.ITrading) building;
			int[] counts = new int[EMaterialType.NUMBER_OF_DROPPABLE_MATERIALS];
			for (EMaterialType m : EMaterialType.DROPPABLE_MATERIALS) {
				counts[m.ordinal] = trading.getRequestedTradingFor(m);
			}
			return counts;
		} else {
			return null;
		}
	}

	private BitSet computeStockStates(IBuilding building) {
		if (building instanceof IBuilding.IStock && !construction) {
			BitSet acceptedMaterialsSet = new BitSet();
			IStockSettings stockSettings = ((IBuilding.IStock) building).getStockSettings();
			for (EMaterialType materialType : EMaterialType.DROPPABLE_MATERIALS) {
				acceptedMaterialsSet.set(materialType.ordinal, stockSettings.isAccepted(materialType));
			}
			return acceptedMaterialsSet;
		} else {
			return null;
		}
	}

	private Hashtable<ESoldierClass, ArrayList<OccupierState>> computeOccupierStates(IBuilding building) {
		Hashtable<ESoldierClass, ArrayList<OccupierState>> newStates = null;
		if (building instanceof IBuilding.IOccupied && !construction) {
			IBuilding.IOccupied occupied = (IBuilding.IOccupied) building;
			newStates = new Hashtable<>();
			for (ESoldierClass soldierClass : ESoldierClass.VALUES) {
				newStates.put(soldierClass, new ArrayList<>());
			}

			for (IBuildingOccupier o : occupied.getOccupiers()) {
				ESoldierClass soldierClass = o.getPlace().getSoldierClass();
				OccupierState state = new OccupierState(o.getMovable());
				newStates.get(soldierClass).add(state);
			}

			for (ESoldierClass soldierClass : ESoldierClass.VALUES) {
				ArrayList<OccupierState> list = newStates.get(soldierClass);
				int coming = occupied.getComingSoldiers(soldierClass);
				for (; coming > 0; coming--) {
					list.add(new OccupierState(true));
				}
				int requested = occupied.getSearchedSoldiers(soldierClass);
				for (; requested > 0; requested--) {
					list.add(new OccupierState(false));
				}
			}
		}
		return newStates;
	}

	/**
	 * Gets a list of priorities supported by this state.
	 * 
	 * @return The priorities.
	 */
	public EPriority[] getSupportedPriorities() {
		return supportedPriorities;
	}

	public boolean isConstruction() {
		return construction;
	}

	public ArrayList<StackState> getStackStates() {
		return stackStates;
	}

	public boolean stockAcceptsMaterial(EMaterialType material) {
		return stockStates != null && stockStates.get(material.ordinal);
	}

	/**
	 * Checks if we are still in the state.
	 * 
	 * @param building
	 *            The building to check.
	 * @return <code>true</code> if that building is in this state.
	 */
	public boolean isStillInState(IBuilding building) {
		return building.getPriority() == priority
				&& Arrays.equals(supportedPriorities,
						building.getSupportedPriorities())
				&& construction == (building.getStateProgress() < 1)
				&& hasSameStacks(building)
				&& hasSameOccupiers(building)
				&& hasSameStock(building)
				&& hasSameTrading(building);
	}

	private boolean hasSameTrading(IBuilding building) {
		return isEqual(computeTradingCounts(building), tradingCounts);
	}

	private boolean hasSameStock(IBuilding building) {
		return isEqual(computeStockStates(building), stockStates);
	}

	private boolean hasSameOccupiers(IBuilding building) {
		return isEqual(computeOccupierStates(building), occupierStates);
	}

	private static boolean isEqual(Object o1, Object o2) {
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}

	private boolean hasSameStacks(IBuilding building) {
		List<IBuildingMaterial> materials = building.getMaterials();
		if (materials.size() != stackStates.size()) {
			return false;
		}
		int i = 0;
		for (IBuildingMaterial mat : materials) {
			if (!stackStates.get(i++).isStillInState(mat)) {
				return false;
			}
		}
		return true;
	}

	public List<OccupierState> getOccupiers(ESoldierClass soldierClass) {
		return Collections.unmodifiableList(occupierStates.get(soldierClass));
	}

	public int getTradingCount(EMaterialType material) {
		if (tradingCounts == null) {
			return 0;
		} else {
			return tradingCounts[material.ordinal];
		}
	}

	/**
	 * @return <code>true</code> if we are a constructed occupied building.
	 */
	public boolean isOccupied() {
		return occupierStates != null;
	}

	/**
	 * @return <code>true</code> if we are a constructed stock building.
	 */
	public boolean isStock() {
		return stockStates != null;
	}

	/**
	 * @return <code>true</code> if we are a constructed trading building.
	 */
	public boolean isTrading() {
		return tradingCounts != null;
	}

	/**
	 * @return <code>true</code> if we are sea trading building, <code>false</code> for land trading.
	 */
	public boolean isSeaTrading() {
		return isSeaTrading;
	}

	/**
	 * @return <code>true</code> if this is a dockyard building
	 */
	public boolean isDockyard() {
		return isDockyard;
	}

	/**
	 * @return <code>true</code> if this dockyard is currently building a ship.
	 */
	public boolean isWorkingDockyard() {
		return isWorkingDockyard;
	}
}
