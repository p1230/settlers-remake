package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.IIntegerSupplier;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Leaf;
import jsettlers.algorithms.simplebehaviortree.Tick;
import jsettlers.logic.constants.MatchConstants;

public final class Sleep<T> extends Leaf<T> {
	private static final long serialVersionUID = 8774557186392581042L;

	private int endTime;
	private final IIntegerSupplier<T> delaySupplier;

	public Sleep(IIntegerSupplier<T> delaySupplier) {
		super();
		this.delaySupplier = delaySupplier;
	}

	@Override
	public NodeStatus onTick(Tick<T> tick) {
		int remaining = endTime - MatchConstants.clock().getTime();
		if (remaining <= 0) {
			return NodeStatus.SUCCESS;
		} else {
			tick.target.entity.setInvocationDelay(remaining);
			return NodeStatus.RUNNING;
		}
	}

	@Override
	public void onOpen(Tick<T> tick) {
		endTime = MatchConstants.clock().getTime() + delaySupplier.apply(tick.target);
	}
}
