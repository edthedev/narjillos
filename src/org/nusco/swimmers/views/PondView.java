package org.nusco.swimmers.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import org.nusco.swimmers.pond.Pond;
import org.nusco.swimmers.pond.PondEvent;
import org.nusco.swimmers.shared.things.Thing;

public class PondView {

	private final Pond pond;
	private final Viewport viewport;
	private final Node background;
	private final Map<Thing, ThingView> thingsToViews = new HashMap<>();

	public PondView(Pond pond) {
		this.pond = pond;
		viewport = new Viewport(pond);
		background = createBackground(pond);

		for (Thing thing : pond.getThings())
			addThingView(thing);

		pond.addEventListener(new PondEvent() {
			@Override
			public void thingAdded(Thing thing) {
				addThingView(thing);
			}
		});
	}

	public Viewport getViewport() {
		return viewport;
	}

	public Node toNode() {
		Group group = new Group();
		group.getChildren().add(getBackground());
		group.getChildren().addAll(getNodesForThings());

		group.getTransforms().add(new Translate(-viewport.getPositionPC().x, -viewport.getPositionPC().y));
		group.getTransforms().add(new Scale(viewport.getZoomLevel(), viewport.getZoomLevel(),
											viewport.getPositionPC().x, viewport.getPositionPC().y));

		setZoomBlurEffect(group);

		return group;
	}

	private void setZoomBlurEffect(Group group) {
		if(viewport.getZoomLevel() <= 1)
			return;
		int blurAmount = (int)(15 * (viewport.getZoomLevel() - 1));
		group.setEffect(new BoxBlur(blurAmount, blurAmount, 3));
	}

	private Node getBackground() {
		return background;
	}
	
	private List<Node> getNodesForThings() {
		List<Node> result = new LinkedList<>();
		synchronized (thingsToViews) {
			for (ThingView view : thingsToViews.values())
				if (viewport.isVisible(view.getThing().getPosition(), Pond.MAX_THING_SIZE))
					result.add(view.toNode());
		}
		return result;
	}

	private Node createBackground(Pond pond) {
		Rectangle result = new Rectangle(0, 0, pond.getSize(), pond.getSize());
		result.setFill(Color.ANTIQUEWHITE);
		return result;
	}

	private ThingView addThingView(Thing thing) {
		synchronized(thingsToViews) {
			return thingsToViews.put(thing, ThingView.createViewFor(thing));
		}
	}

	public void tick() {
		pond.tick();
		viewport.tick();
	}

	public Pond getPond() {
		return pond;
	}

	public void show(Group root) {
		root.getChildren().add(toNode());
	}
}