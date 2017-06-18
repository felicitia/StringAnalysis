package graph;

public class DOTEdge {

	private String label; // can be NULL
	private String parent; // can NOT be NULL
	private String child; // can NOT be NULL

	public DOTEdge(){
		
	}
	public DOTEdge(String label, String parent, String child) {
		super();
		this.label = label;
		this.parent = parent;
		this.child = child;
	}

	@Override
	public boolean equals(Object obj) {
		// If the object is compared with itself then return true
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof DOTEdge)) {
			return false;
		}

		DOTEdge edge = (DOTEdge) obj;

		// Compare the data members and return accordingly
		if (label == null || edge.getLabel() == null) {
			if (label == null && edge.getLabel() == null
					&& parent.equals(edge.getParent())
					&& child.equals(edge.getChild())) {
				return true;
			}
		} else if (label.equals(edge.getLabel())
				&& parent.equals(edge.getParent())
				&& child.equals(edge.getChild())) {
			return true;
		}
		return false;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getChild() {
		return child;
	}

	public void setChild(String child) {
		this.child = child;
	}
}
