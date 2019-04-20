package var;

public class Plateau {

	private int size;
	private Point[][] points;
	private Point start, end;
	
	public Plateau(int size) {
		points = new Point[size][size];
		this.size = size;
		for(int i = 0 ; i < size ; i++) {
			for(int j = 0 ; j < size ; j++) {
				points[i][j] = new Point(i,j);
			}
		}
	}
	
	public Point getStart() {
		return start;
	}
	public Point getEnd() {
		return end;
	}
	public void setStart(int x, int y) {
		start = points[x][y];
	}
	public void setEnd(int x, int y) {
		end = points[x][y];
	}
	public Point getPoint(int x, int y) {
		if(x < 0 || x >= points.length || y < 0 || y >= points.length) return null;
		return points[x][y];
	}
	public void clear() {
		points = new Point[size][size];
		for(int i = 0 ; i < size ; i++) {
			for(int j = 0 ; j < size ; j++) {
				points[i][j] = new Point(i,j);
			}
		}
		start = null;
		end = null;
	}
}
