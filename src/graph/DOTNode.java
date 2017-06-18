package graph;

public class DOTNode {
	private String shape;
	private String name;
	
	public DOTNode(String shape, String name) {
		super();
		this.shape = shape;
		this.name = name;
	}
	public String getShape() {
		return shape;
	}
	public void setShape(String shape) {
		this.shape = shape;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
