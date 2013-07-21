package brevis;

public class BrShape {
	public enum BrShapeType {
		BOX, SPHERE, CONE, MESH
	};
	
	public BrShapeType type;
	
	BrShape() {
		type = BrShapeType.SPHERE;
	}
	
	public void draw() {
		
	}
}
