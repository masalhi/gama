package msi.gama.gui.displays;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.commands.*;
import msi.gaml.compilation.*;
import msi.gaml.species.ISpecies;

public class AWTDisplaySurfaceMenu {

	final IDisplaySurface surface;
	private final PopupMenu agentsMenu;

	AWTDisplaySurfaceMenu(final IDisplaySurface s) {
		surface = s;
		agentsMenu = new PopupMenu();
		((AWTDisplaySurface) s).add(agentsMenu); // TODO

	}

	public void selectAgents(final int x, final int y, final List<IDisplay> displays) {
		agentsMenu.removeAll();
		if ( displays.isEmpty() ) { return; }
		GamaPoint p = displays.get(0).getModelCoordinatesFrom(x, y);
		SelectedAgent world = new SelectedAgent(GAMA.getFrontmostSimulation().getWorld(), p);
		world.buildMenuItems(agentsMenu, displays.get(0), "World agent");
		agentsMenu.addSeparator();
		for ( IDisplay display : displays ) {
			Set<IAgent> agents = display.collectAgentsAt(x, y);
			if ( agents.isEmpty() ) {
				continue;
			}
			p = display.getModelCoordinatesFrom(x, y);
			java.awt.Menu m = new java.awt.Menu(display.getName());
			MenuItem grey = new MenuItem("Selected agents");
			grey.setEnabled(false);
			m.add(grey);
			if ( !agents.isEmpty() ) {
				m.addSeparator();

				for ( IAgent agent : agents ) {
					SelectedAgent sa = new SelectedAgent(agent, p);
					sa.buildMenuItems(m, display);
				}
			}
			agentsMenu.add(m);
		}
		agentsMenu.show((Component) surface, x, y);
	}

	ActionListener menuListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
			AWTDisplaySurfaceMenu.AgentMenuItem source =
				(AWTDisplaySurfaceMenu.AgentMenuItem) e.getSource();
			IAgent a = source.getAgent();
			if ( a != null ) {
				surface.fireSelectionChanged(a);
			}
		}

	};

	ActionListener focusListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
			AWTDisplaySurfaceMenu.AgentMenuItem source =
				(AWTDisplaySurfaceMenu.AgentMenuItem) e.getSource();
			IAgent a = source.getAgent();
			if ( a != null ) {
				surface.focusOn(a.getGeometry(), source.getDisplay());
			}
		}

	};

	ActionListener highlightListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
			AWTDisplaySurfaceMenu.AgentMenuItem source =
				(AWTDisplaySurfaceMenu.AgentMenuItem) e.getSource();
			IAgent a = source.getAgent();
			if ( a != null ) {
				GuiUtils.setHighlightedAgent(a);
			}
		}

	};

	static ActionListener commandListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
			AgentMenuItem source = (AgentMenuItem) e.getSource();
			final IAgent a = source.getAgent();
			final ICommand c = source.getCommand();
			final ILocation p = source.getLocation();
			if ( c != null && a != null && !a.dead() ) {
				IScheduledAction action = new ScheduledAction() {

					@Override
					public void execute(final IScope scope) throws GamaRuntimeException {
						if ( p != null ) {
							scope.addVarWithValue(IKeyword.USER_LOCATION, p);
						}
						if ( !a.dead() ) {
							scope.execute(c, a);
						}
					}

				};
				GAMA.getFrontmostSimulation().getScheduler().executeOneAction(action);
			}
		}

	};

	public class SelectedAgent {

		final IAgent macro;
		final GamaPoint userLocation;
		Map<ISpecies, java.util.List<SelectedAgent>> micros;

		SelectedAgent(final IAgent agent, final GamaPoint point) {
			macro = agent;
			userLocation = point;

		}

		void buildMenuItems(final Menu parentMenu, final IDisplay display) {
			buildMenuItems(parentMenu, display, macro.getName());
		}

		void buildMenuItems(final Menu parentMenu, final IDisplay display, final String name) {
			Menu macroMenu = new Menu(name);
			parentMenu.add(macroMenu);

			MenuItem inspectItem =
				new AWTDisplaySurfaceMenu.AgentMenuItem("Inspect", macro, display);
			inspectItem.addActionListener(menuListener);
			macroMenu.add(inspectItem);

			MenuItem focusItem = new AWTDisplaySurfaceMenu.AgentMenuItem("Focus", macro, display);
			focusItem.addActionListener(focusListener);
			macroMenu.add(focusItem);

			MenuItem highlightItem =
				new AWTDisplaySurfaceMenu.AgentMenuItem("Highlight", macro, display);
			highlightItem.addActionListener(highlightListener);
			macroMenu.add(highlightItem);

			Collection<UserCommandCommand> commands = macro.getSpecies().getUserCommands();
			if ( !commands.isEmpty() ) {
				macroMenu.addSeparator();
				for ( UserCommandCommand c : commands ) {
					MenuItem actionItem =
						new AWTDisplaySurfaceMenu.AgentMenuItem(macro, c, userLocation);
					actionItem.addActionListener(commandListener);
					macroMenu.add(actionItem);
				}
			}
			if ( micros != null && !micros.isEmpty() ) {
				Menu microsMenu = new Menu("Micro agents");
				macroMenu.add(microsMenu);

				Menu microSpecMenu;
				for ( ISpecies microSpec : micros.keySet() ) {
					microSpecMenu = new Menu("Species " + microSpec.getName());
					microsMenu.add(microSpecMenu);

					for ( SelectedAgent micro : micros.get(microSpec) ) {
						micro.buildMenuItems(microSpecMenu, display);
					}
				}
			}
		}
	}

	static class AgentMenuItem extends MenuItem {

		private final IAgent agent;
		private final IDisplay display;
		private final ICommand command;
		private final GamaPoint userLocation;

		AgentMenuItem(final String name, final IAgent agent, final IDisplay display) {
			super(name);
			this.agent = agent;
			this.display = display;
			command = null;
			userLocation = null;
		}

		AgentMenuItem(final IAgent agent, final ICommand command, final GamaPoint point) {
			super(command.getName());
			this.agent = agent;
			this.display = null;
			this.command = command;
			userLocation = point;
		}

		ILocation getLocation() {
			return userLocation;
		}

		IAgent getAgent() {
			return agent;
		}

		ICommand getCommand() {
			return command;
		}

		IDisplay getDisplay() {
			return display;
		}
	}
}
