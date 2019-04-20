package graphics;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import var.Plateau;
import var.Point;

public class MainWindow extends Application {

	private int taille_case = 40;
	static int size;
	ArrayList<Point> murs = new ArrayList<Point>();
	GraphicsContext gc;
	static Plateau p;
	boolean setStart, setEnd;
	Point current;
	static ArrayList<Point> road;
	static ArrayList<Point> tested;
	static boolean alreadyCheck[][];
	boolean finish = false;

	public static void main(String[] args) {
		size = 20;
		alreadyCheck = new boolean[size][size];
		road = new ArrayList<Point>();
		tested = new ArrayList<Point>();
		p = new Plateau(size);
		Application.launch();
	}

	@Override
	public void start(Stage s) throws Exception {
		VBox root = new VBox();
		Canvas canvas = new Canvas(taille_case * size, taille_case * size);
		gc = canvas.getGraphicsContext2D();
		canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int x = (int) (event.getX() / taille_case), y = (int) (event.getY() / taille_case);
				if (setStart) {
					p.setStart(x, y);
					current = p.getStart();
					current.setGCost(0);
					road.add(current);
					setStart = false;
				} else if (setEnd) {
					p.setEnd(x, y);
					setEnd = false;
				} else {
					if (p.getPoint(x, y) != p.getStart() && p.getPoint(x, y) != p.getEnd()
							&& !murs.contains(p.getPoint(x, y))) {
						murs.add(p.getPoint(x, y));
						System.out.println("Ajout d'un mur en [" + x + ";" + y + "]");
					} else
						System.err.println("Impossible d'ajouter ce mur !");
				}
				repaint();
			}

		});
		repaint();
		root.getChildren().add(canvas);
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int x = (int) (event.getX() / taille_case), y = (int) (event.getY() / taille_case);
				if (p.getPoint(x, y) != p.getStart() && p.getPoint(x, y) != p.getEnd()
						&& !murs.contains(p.getPoint(x, y))) {
					murs.add(p.getPoint(x, y));
					System.out.println("Ajout d'un mur en [" + x + ";" + y + "]");
				}
			}

		});
		addMouseScrolling(root);

		HBox bottom = new HBox();
		Button start_point = new Button("Start point");
		Button end_point = new Button("End point");
		Button search = new Button("Search Path");
		Button next = new Button("Next");
		Button clear = new Button("Clear");
		search.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (p.getStart() != null && p.getEnd() != null) {
					current = p.getStart();
					while (!doNext()) {}
				} else
					System.err.println("Start or end isn't define !");
			}
		});
		start_point.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setStart = true;
			}
		});
		end_point.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setEnd = true;
			}
		});
		next.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (finish)
					System.out.println("Trouvé");
				else if (p.getStart() != null && p.getEnd() != null) {
					doNext();
				} else
					System.err.println("Start or end isn't define !");
			}
		});
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				murs = new ArrayList<Point>();
				boolean setStart, setEnd;
				current = null;
				p.clear();
				alreadyCheck = new boolean[size][size];
				road = new ArrayList<Point>();
				tested = new ArrayList<Point>();
				finish = false;
				repaint();
			}
		});

		bottom.getChildren().addAll(start_point, end_point, search, next, clear);
		root.getChildren().add(bottom);

		Scene scene = new Scene(root);
		s.setTitle("A* pathfinder");
		s.setScene(scene);
		s.show();
	}

	private void repaint() {
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.BLACK);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				gc.fillRect(i * taille_case, j * taille_case, taille_case, taille_case);
				gc.strokeRect(i * taille_case, j * taille_case, taille_case, taille_case);
			}
		}
		
		gc.setFill(Color.BLACK);
		for (int i = 0; i < murs.size(); i++) {
			gc.fillRect(murs.get(i).getX() * taille_case, murs.get(i).getY() * taille_case, taille_case, taille_case);
		}
		
		gc.setFill(Color.RED);
		if (p.getStart() != null)
			gc.fillRect(p.getStart().getX() * taille_case, p.getStart().getY() * taille_case, taille_case, taille_case);
		
		gc.setFill(Color.GREEN);
		if (p.getEnd() != null)
			gc.fillRect(p.getEnd().getX() * taille_case, p.getEnd().getY() * taille_case, taille_case, taille_case);
		
		gc.setFill(Color.BLUE);
		for (int i = 0; i < road.size(); i++) {
			gc.fillRect(road.get(i).getX() * taille_case, road.get(i).getY() * taille_case, taille_case, taille_case);
		}
		
		gc.setStroke(Color.BLACK);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				afficheStats(p.getPoint(i, j));
			}
		}
		
		if(current != null) {
			//gc.setFill(Color.rgb((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
			gc.setFill(Color.PURPLE);
			Point temp = current;
			while(temp.getPrecedent() != null) {
				gc.fillRect(temp.getX() * taille_case, temp.getY() * taille_case, taille_case, taille_case);
				afficheStats(temp);
				temp = temp.getPrecedent();
			}
		}
	}

	public void addMouseScrolling(Node node) {
		node.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double zoomFactor = 1.05;
			double deltaY = event.getDeltaY();
			if (deltaY < 0) {
				zoomFactor = 2.0 - zoomFactor;
			}
			node.setScaleX(node.getScaleX() * zoomFactor);
			node.setScaleY(node.getScaleY() * zoomFactor);
		});
	}

	private void afficheStats(Point p) {
		String s;
		if(p.getGCost() != 0) {
			s = String.valueOf(p.getGCost());
			gc.strokeText(s, p.getX() * taille_case + 2, p.getY() * taille_case + 10);
		}

		if(p.getHCost() != 0) {
			s = String.valueOf(p.getHCost());
			gc.strokeText(s, p.getX() * taille_case + 25, p.getY() * taille_case + 10);
		}
		
		if(p.getFCost() != 0) {
			s = String.valueOf(p.getFCost());
			gc.strokeText(s, p.getX() * taille_case + 13, p.getY() * taille_case + 25);
		}
	}

	private boolean doNext() {
		Point temp;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (!(i == 0 && j == 0)) {
					temp = p.getPoint(current.getX() + i, current.getY() + j);
					if(temp != null && temp != p.getStart()) {
						if(!murs.contains(temp)) { //Temp n'est pas un mur
							if(temp.getPrecedent() == null) { //Les stats n'ont pas été initialisées
								temp.setPrecedent(current);
								int fromStart, toEnd;
								fromStart = temp.getDistance(current) + current.getGCost();
								toEnd = temp.getDistance(p.getEnd());
								temp.setGCost(fromStart);
								temp.setHCost(toEnd);
								temp.setFCost(fromStart + toEnd);
								tested.add(temp);
								System.out.println("Point ajouté");
							} else {
								Point p2 = temp;
								int fromStart, toEnd;
								fromStart = temp.getDistance(current) + current.getGCost();
								toEnd = temp.getDistance(p.getEnd());
								if(fromStart < temp.getGCost()) {
									temp.setGCost(fromStart);
									temp.setHCost(toEnd);
									temp.setFCost(fromStart + toEnd);
									System.out.println("Point MODIFIE!");
								}
							}
						}
					}
				}
			}
		}
		current = getBest();
		road.add(current);
		if(current == null) {
			System.out.println("Aucun passage possible");
			finish = true;
			return true;
		}
		if (current.getHCost() == 0) {
			finish = true;
			System.out.println("Terminé");
			repaint();
			return true;
		}
		repaint();
		return false;
	}

	private Point getBest() {
		Point temp = null;
		for (int i = 0; i < tested.size(); i++) {
			if (!road.contains(tested.get(i))) {
				if (temp == null)
					temp = tested.get(i);
				if (tested.get(i).getFCost() < temp.getFCost())
					temp = tested.get(i);
				else if (tested.get(i).getFCost() == temp.getFCost() && tested.get(i).getHCost() < temp.getHCost())
					temp = tested.get(i);
			}
		}
		return temp;
	}
}
