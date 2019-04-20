package var;

public class Point {

	private int x, y;
	private int GCost;
	private int HCost;
	private int FCost;
	private Point precedent;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getDistance(Point other) {
		int deltaX = Math.abs(other.x - this.x);
		int deltaY = Math.abs(other.y - this.y);
		int ligne = Math.abs(deltaX - deltaY);
		int diag;
		if(deltaX < deltaY) diag = deltaX;
		else diag = deltaY;
		return diag * 14 + ligne * 10;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	public void setGCost(int i) {
		this.GCost = i;
	}
	public void setHCost(int i) {
		this.HCost = i;
	}
	public void setFCost(int i) {
		this.FCost = i;
	}
	public int getHCost() {
		return HCost;
	}
	public int getFCost() {
		return FCost;
	}
	public int getGCost() {
		return GCost;
	}
	public String toString() {
		return "X : "+x+" ; Y : " + y + " , " + GCost + " , " + HCost + " , " + FCost;
	}

	public Point getPrecedent() {
		return precedent;
	}

	public void setPrecedent(Point precedent) {
		this.precedent = precedent;
	}
}
