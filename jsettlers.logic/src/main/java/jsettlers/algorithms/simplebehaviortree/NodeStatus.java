package jsettlers.algorithms.simplebehaviortree;

public enum NodeStatus {
	SUCCESS,
	FAILURE,
	RUNNING;

	public static NodeStatus of(boolean value) {
		return value ? SUCCESS : FAILURE;
	}
}