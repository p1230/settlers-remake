/*******************************************************************************
 * Copyright (c) 2015
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
package jsettlers.main.swing;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import go.graphics.area.Area;
import go.graphics.swing.AreaContainer;
import go.graphics.swing.sound.SwingSoundPlayer;
import jsettlers.common.CommitInfo;
import jsettlers.common.CommonConstants;
import jsettlers.common.map.MapLoadException;
import jsettlers.common.utils.MainUtils;
import jsettlers.common.utils.OptionableProperties;
import jsettlers.graphics.JSettlersScreen;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.startscreen.interfaces.IStartingGame;
import jsettlers.graphics.startscreen.progress.StartingGamePanel;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.MapLoader;
import jsettlers.logic.map.save.DirectoryMapLister;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.ReplayStartInformation;
import jsettlers.main.StartScreenConnector;
import jsettlers.network.client.OfflineNetworkConnector;

/**
 * 
 * @author Andreas Eberle
 * @author michael
 */
public class OldSwingManagedJSettlers {
	static {
		CommonConstants.USE_SAVEGAME_COMPRESSION = true;
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws MapLoadException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, MapLoadException {
		// UI will be changed later with the new Swing implementation, but will also be based on Nimbus
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}

		OptionableProperties options = MainUtils.loadOptions(args);

		NewSwingManagedJSettlers.loadOptionalSettings(options);
		NewSwingManagedJSettlers.setupResourceManagers(options, "config.prp");

		JSettlersScreen content = startGui();
		generateContent(options, content);
	}

	/**
	 * Creates a new SWING GUI for the game.
	 * 
	 * @param argsList
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static JSettlersScreen startGui() {
		Area area = new Area();
		JSettlersScreen content = new JSettlersScreen(new StartScreenConnector(), new SwingSoundPlayer(), getBuild());
		area.add(content.getRegion());

		startJogl(area);

		startRedrawTimer(content);
		return content;
	}

	private static void generateContent(OptionableProperties options, JSettlersScreen content) throws IOException, MapLoadException {
		String mapfile = null;
		long randomSeed = 0;
		File loadableReplayFile = null;
		int targetGameTime = 0;

		mapfile = options.getProperty("mapfile");
		if (options.containsKey("random")) {
			randomSeed = Long.parseLong(options.getProperty("random"));
		}
		if (options.containsKey("replayFile")) {
			String loadableReplayFileString = options.getProperty("replayFile");
			File replayFile = new File(loadableReplayFileString);
			if (replayFile.exists()) {
				loadableReplayFile = replayFile;
				System.out.println("Found loadable replay file and loading it: " + loadableReplayFile);
			} else {
				System.err.println("Found replayFile parameter, but file can not be found!");
			}
		}
		if (options.containsKey("targetTime")) {
			targetGameTime = Integer.valueOf(options.getProperty("targetTime")) * 60 * 1000;
		}

		if (mapfile != null || loadableReplayFile != null) {
			IStartingGame game;
			if (loadableReplayFile == null) {
				MapLoader mapLoader = MapLoader.getLoaderForListedMap(new DirectoryMapLister.ListedMapFile(new File(mapfile)));
				byte playerId = 0;
				PlayerSetting[] playerSettings = PlayerSetting.createDefaultSettings(playerId, (byte) mapLoader.getMaxPlayers());
				game = new JSettlersGame(mapLoader, randomSeed, playerId, playerSettings).start();
			} else {
				game = JSettlersGame.loadFromReplayFile(loadableReplayFile, new OfflineNetworkConnector(), new ReplayStartInformation()).start();
			}
			StartingGamePanel toDisplay = new StartingGamePanel(game, content);
			content.setContent(toDisplay);

			if (targetGameTime > 0) {
				while (!game.isStartupFinished()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				MatchConstants.clock().fastForwardTo(targetGameTime);
			}
		} else {
			content.goToStartScreen("");
		}
	}

	private static void startRedrawTimer(final JSettlersScreen content) {
		new Timer("opengl-redraw").schedule(new TimerTask() {
			@Override
			public void run() {
				content.getRegion().requestRedraw();
			}
		}, 100, 25);
	}

	private static void startJogl(Area area) {
		JFrame jsettlersWnd = new JFrame("JSettlers - " + getBuild());

		// StartMenuPanel panel = new StartMenuPanel(new StartScreenConnector());
		AreaContainer panel = new AreaContainer(area);
		panel.setPreferredSize(new Dimension(640, 480));
		jsettlersWnd.add(panel);
		panel.requestFocusInWindow();

		jsettlersWnd.pack();
		jsettlersWnd.setSize(1200, 800);
		jsettlersWnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jsettlersWnd.setVisible(true);
		jsettlersWnd.setLocationRelativeTo(null);
	}

	private static String getBuild() {
		return Labels.getString("version-build", CommitInfo.COMMIT_HASH_SHORT);
	}
}