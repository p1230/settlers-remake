/*
 * Copyright (c) 2017
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
 */

package jsettlers.main.android.gameplay.presenters;

import android.util.Log;

import java.util.List;

import java8.util.stream.Collectors;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.map.partition.IBuildingCounts;
import jsettlers.common.map.partition.IPartitionData;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.ShowConstructionMarksAction;
import jsettlers.graphics.map.controls.original.panel.content.EBuildingsCategory;
import jsettlers.main.android.core.controls.ActionControls;
import jsettlers.main.android.core.controls.PositionChangedListener;
import jsettlers.main.android.core.controls.PositionControls;
import jsettlers.main.android.gameplay.navigation.MenuNavigator;
import jsettlers.main.android.gameplay.ui.views.BuildingsCategoryView;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by tompr on 22/11/2016.
 */
public class BuildingsCategoryMenu implements PositionChangedListener {
	private final BuildingsCategoryView view;
	private final ActionControls actionControls;
	private final PositionControls positionControls;
	private final MenuNavigator menuNavigator;
	private final EBuildingsCategory buildingsCategory;

	public BuildingsCategoryMenu(BuildingsCategoryView view, ActionControls actionControls, PositionControls positionControls, MenuNavigator menuNavigator, EBuildingsCategory buildingsCategory) {
		this.view = view;
		this.actionControls = actionControls;
		this.positionControls = positionControls;
		this.menuNavigator = menuNavigator;
		this.buildingsCategory = buildingsCategory;
	}

	public void start() {
		positionControls.addPositionChangedListener(this);
		positionChanged();
	}

	public void finish() {
		positionControls.removePositionChangedListener(this);
	}

	public void buildingSelected(EBuildingType buildingType) {
		Action action = new ShowConstructionMarksAction(buildingType);
		actionControls.fireAction(action);
		menuNavigator.dismissMenu();
	}

	/**
	 * DrawListener implementation
	 */
	@Override
	public void positionChanged() {
		IBuildingCounts buildingCounts = null;

		if (positionControls.isInPlayerPartition()) {
			buildingCounts = positionControls.getCurrentPartitionData().getBuildingCounts();
		}

		view.setBuildings(buildings(buildingCounts));
	}



	private List<Building> buildings(IBuildingCounts buildingCounts) {

		return stream(buildingsCategory.buildingTypes)
				.map(buildingType -> new Building(buildingType, buildingCounts))
				.collect(Collectors.toList());
	}
}
