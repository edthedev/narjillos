package org.nusco.narjillos;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

import org.nusco.narjillos.creature.Narjillo;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.utilities.Chronometer;
import org.nusco.narjillos.utilities.PetriDishState;
import org.nusco.narjillos.utilities.Speed;
import org.nusco.narjillos.utilities.StoppableThread;
import org.nusco.narjillos.views.EnvirommentView;
import org.nusco.narjillos.views.MicroscopeView;
import org.nusco.narjillos.views.PetriStatusView;

/**
 * The main JavaFX Application class. It binds model and view together, and also
 * manages the user interface.
 */
public class PetriDish extends ApplicationBase {

	private static final long PAN_SPEED = 200;

	private static String[] programArguments = new String[0];

	private PetriDishState state = new PetriDishState();

	@Override
	protected String[] getProgramArguments() {
		return PetriDish.programArguments;
	}

	@Override
	protected String getName() {
		return "Petri Dish";
	}

	@Override
	protected void registerInteractionHandlers(final Scene scene) {
		registerKeyboardHandlers(scene);
		registerMouseClickHandlers(scene);
		registerMouseDragHandlers(scene);
		registerMouseScrollHandlers(scene);
		registerTouchHandlers(scene);
	}

	@Override
	protected StoppableThread createModelThread(final String[] arguments, final boolean[] isModelInitialized) {
		return new StoppableThread() {
			public void run() {
				CommandLineOptions options = CommandLineOptions.parse(arguments);
				if (options == null)
					System.exit(1);

				setLab(new ExperimentalLab(options));

				isModelInitialized[0] = true;

				while (!hasBeenAskedToStop()) {
					long startTime = System.currentTimeMillis();
					if (state.getSpeed() != Speed.PAUSED)
						if (!tick())
							Platform.exit();
					waitFor(state.getSpeed().getTicksPeriod(), startTime);
				}
			}
		};
	}

	@Override
	protected StoppableThread createViewThread(final Group root) {
		return new StoppableThread() {
			private final Chronometer framesChronometer = new Chronometer();
			private volatile boolean renderingFinished = false;

			private final MicroscopeView foregroundView = new MicroscopeView(getViewport());
			private final EnvirommentView ecosystemView = new EnvirommentView(getEcosystem(), getViewport(), state);
			private final PetriStatusView statusBarView = new PetriStatusView();

			@Override
			public void run() {
				while (!hasBeenAskedToStop()) {
					long startTime = System.currentTimeMillis();
					renderingFinished = false;

					getTracker().tick();
					ecosystemView.tick();

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							update(root);
							renderingFinished = true;
						}
					});

					waitFor(state.getFramesPeriod(), startTime);
					while (!renderingFinished && !hasBeenAskedToStop())
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					framesChronometer.tick();
				}
			}

			private void update(final Group root) {
				root.getChildren().clear();
				root.getChildren().add(ecosystemView.toNode());
				root.getChildren().add(foregroundView.toNode());

				Node statusInfo = statusBarView.toNode(framesChronometer.getTicksInLastSecond(), getEnvironmentStatistics(), getPerformanceStatistics(), state.getSpeed(), state.getEffects(),
						getTracker().isTracking(), isBusy());
				root.getChildren().add(statusInfo);
			}
		};
	}

	private void registerKeyboardHandlers(final Scene scene) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(final KeyEvent keyEvent) {
				if (keyEvent.getCode() == KeyCode.RIGHT)
					panViewport(PAN_SPEED, 0, keyEvent);
				else if (keyEvent.getCode() == KeyCode.LEFT)
					panViewport(-PAN_SPEED, 0, keyEvent);
				else if (keyEvent.getCode() == KeyCode.UP)
					panViewport(0, -PAN_SPEED, keyEvent);
				else if (keyEvent.getCode() == KeyCode.DOWN)
					panViewport(0, PAN_SPEED, keyEvent);
				else if (keyEvent.getCode() == KeyCode.P)
					state.speedUp();
				else if (keyEvent.getCode() == KeyCode.O)
					state.speedDown();
				else if (keyEvent.getCode() == KeyCode.L)
					state.toggleLight();
				else if (keyEvent.getCode() == KeyCode.I)
					state.toggleInfrared();
				else if (keyEvent.getCode() == KeyCode.E)
					state.toggleEffects();
			}

			private void panViewport(long velocityX, long velocityY, KeyEvent event) {
				getTracker().stopTracking();
				getViewport().moveBy(Vector.cartesian(velocityX, velocityY));
				event.consume();
			};
		});
	}

	private void registerMouseClickHandlers(final Scene scene) {
		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				Vector clickedPositionSC = Vector.cartesian(event.getSceneX(), event.getSceneY());
				Vector clickedPositionEC = getViewport().toEC(clickedPositionSC);

				if (event.getClickCount() == 1)
					getTracker().stopTracking();

				if (event.getClickCount() == 2)
					getTracker().startTracking(clickedPositionEC);

				if (event.getClickCount() == 3)
					copyIsolatedDNAToClipboard(clickedPositionEC);
			}

			private void copyIsolatedDNAToClipboard(Vector clickedPositionEC) {
				Narjillo narjillo = (Narjillo) getLocator().findNarjilloAt(clickedPositionEC);

				if (narjillo == null)
					return;

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(narjillo.getDNA().toString()), null);
			}
		});
	}

	private void registerMouseDragHandlers(final Scene scene) {
		final boolean[] isDragging = new boolean[] { false };
		final double[] mouseX = new double[] { 0 };
		final double[] mouseY = new double[] { 0 };

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				isDragging[0] = true;
				mouseX[0] = event.getX();
				mouseY[0] = event.getY();
			}
		});
		scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				isDragging[0] = false;
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getTracker().stopTracking();
				double translateX = event.getX() - mouseX[0];
				double translateY = event.getY() - mouseY[0];
				getViewport().translateBy(Vector.cartesian(-translateX, -translateY));
				mouseX[0] = event.getX();
				mouseY[0] = event.getY();
			}
		});
	}

	private void registerMouseScrollHandlers(final Scene scene) {
		// Prevent scrolling at start until the eggs are visible, to avoid
		// confusing the user who might be inadvertently scrolling out.
		new Thread(new Runnable() {
			@Override
			public void run() {
				waitUntilViewportHasZoomedToEggs();
				scene.setOnScroll(createMouseScrollHandler());
			}

			private void waitUntilViewportHasZoomedToEggs() {
				while (!getViewport().isZoomCloseToTarget() && !isMainApplicationStopped()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}

			private EventHandler<ScrollEvent> createMouseScrollHandler() {
				return new EventHandler<ScrollEvent>() {
					@Override
					public void handle(ScrollEvent event) {
						if (event.getDeltaY() <= 0)
							getViewport().zoomIn();
						else {
							getViewport().zoomOut();
							if (getViewport().isZoomedOutCompletely())
								getTracker().stopTracking();
						}
					}
				};
			}
		}, "mouse scroll handler creator").start();
	}

	private void registerTouchHandlers(Scene scene) {
		final double[] initialZoomLevel = new double[] { 0 };

		scene.setOnZoomStarted(new EventHandler<ZoomEvent>() {
			@Override
			public void handle(ZoomEvent event) {
				initialZoomLevel[0] = getViewport().getZoomLevel();
			}
		});
		scene.setOnZoom(new EventHandler<ZoomEvent>() {
			@Override
			public void handle(ZoomEvent event) {
				double zoomFactor = event.getTotalZoomFactor();
				getViewport().zoomTo(initialZoomLevel[0] * zoomFactor);
				if (getViewport().isZoomedOutCompletely())
					getTracker().stopTracking();
			}
		});
	}

	public static void main(String... args) throws Exception {
		PetriDish.programArguments = args;
		launch(args);
	}
}
