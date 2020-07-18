package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Property<T, PropertyType> extends Decorator<T> {
	private static final long serialVersionUID = 6714370606784586530L;

	private       PropertyType                          oldValue;
	private final PropertyType                          newValue;
	private final ISetPropertyConsumer<T, PropertyType> setter;
	private final IGetPropertyProducer<T, PropertyType> getter;

	public Property(ISetPropertyConsumer<T, PropertyType> setter, IGetPropertyProducer<T, PropertyType> getter, PropertyType value, Node<T> child) {
		super(child);
		this.newValue = value;
		this.setter = setter;
		this.getter = getter;
	}

	@Override
	protected void onEnter(Tick<T> tick) {
		oldValue = getter.apply(tick.target);
		setter.accept(tick.target, newValue);
	}

	@Override
	protected void onClose(Tick<T> tick) {
		super.onClose(tick);
		setter.accept(tick.target, oldValue);
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		return child.execute(tick);
	}
}
