package graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import var.Plateau;
import var.Point;

public class MainWindow extends Application {

	private double taille_case = 40;
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
	Timer timer;

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
		HBox up = new HBox();
		VBox root = new VBox();
		Canvas canvas = new Canvas(taille_case * size, taille_case * size);
		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.setStroke(Color.BLACK);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				gc.fillRect(i * taille_case, j * taille_case, taille_case, taille_case);
				gc.strokeRect(i * taille_case, j * taille_case, taille_case, taille_case);
			}
		}
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
					if(event.getButton().ordinal() == 1) {
						if (p.getPoint(x, y) != p.getStart() && p.getPoint(x, y) != p.getEnd()
								&& !murs.contains(p.getPoint(x, y))) {
							murs.add(p.getPoint(x, y));
							gc.setFill(Color.BLACK);
							gc.fillRect(x * taille_case, y * taille_case, taille_case, taille_case);
							//System.out.println("Ajout d'un mur en [" + x + ";" + y + "]");
						}
					} else {
						murs.remove(p.getPoint(x, y));
						gc.setFill(Color.WHITE);
						gc.fillRect(x * taille_case + 1, y * taille_case + 1, taille_case - 2, taille_case - 2);
					}
				}
				repaint();
			}

		});
		repaint();
		canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int x = (int) (event.getX() / taille_case), y = (int) (event.getY() / taille_case);
				if(event.getButton().ordinal() == 1) {
					if (p.getPoint(x, y) != p.getStart() && p.getPoint(x, y) != p.getEnd()
							&& !murs.contains(p.getPoint(x, y))) {
						murs.add(p.getPoint(x, y));
						gc.setFill(Color.BLACK);
						gc.fillRect(x * taille_case, y * taille_case, taille_case, taille_case);
						//System.out.println("Ajout d'un mur en [" + x + ";" + y + "]");
					}
				} else {
					murs.remove(p.getPoint(x, y));
					gc.setFill(Color.WHITE);
					gc.fillRect(x * taille_case + 1, y * taille_case + 1, taille_case - 2, taille_case - 2);
				}
			}

		});
		canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				gc.setFill(Color.BLACK);
				for (int i = 0; i < murs.size(); i++) {
					gc.fillRect(murs.get(i).getX() * taille_case, murs.get(i).getY() * taille_case, taille_case, taille_case);
				}
			}
			
		});
		addMouseScrolling(canvas);
		up.getChildren().add(canvas);

		HBox bottom = new HBox();
		Button start_point = new Button("Start point");
		Button end_point = new Button("End point");
		Button search = new Button("Search Path");
		Button next = new Button("Next");
		Button clear = new Button("Clear");
		Button save = new Button("Save as");
		Button open = new Button("Open");
		search.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (p.getStart() != null && p.getEnd() != null) {
					current = p.getStart();
					timer = new Timer();
					timer.schedule(
						    new TimerTask() {

						        @Override
						        public void run() {
						        	doNext();
						        	repaint();
						        }
						    }, 0, 15);
					
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
					repaint();
				} else
					System.err.println("Start or end isn't define !");
			}
		});
		clear.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				murs = new ArrayList<Point>();
				current = null;
				p.clear();
				alreadyCheck = new boolean[size][size];
				road = new ArrayList<Point>();
				tested = new ArrayList<Point>();
				finish = false;
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.BLACK);
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						gc.fillRect(i * taille_case, j * taille_case, taille_case, taille_case);
						gc.strokeRect(i * taille_case, j * taille_case, taille_case, taille_case);
					}
				}
			}
		});
		save.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {				
				FileChooser fileChooser = new FileChooser();
				 
	            //Set extension filter for text files
	            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
	            fileChooser.getExtensionFilters().add(extFilter);
	 
	            //Show save file dialog
	            File file = fileChooser.showSaveDialog(s);
	 
	            String s = "";
	            s+= "p:" + size + "\n";
	            if(p.getStart() != null) s+= "s:" + p.getStart().getX() + ";" + p.getStart().getY() + "\n";
	            if(p.getEnd() != null) s+= "e:" + p.getEnd().getX() + ";" + p.getEnd().getY() + "\n";
	            for(int i = 0 ; i < murs.size() ; i++) {
	            	s += "m:" + murs.get(i).getX() + ";" + murs.get(i).getY() + "\n";
	            }
	            if (file != null) {
	                saveTextToFile(s, file);
	            }
			}
			
		});
		open.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				 
	            //Set extension filter for text files
	            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
	            fileChooser.getExtensionFilters().add(extFilter);
	            
	            File file = fileChooser.showOpenDialog(s);
	            
	            if (file != null) {
	            	BufferedReader reader;
	            	boolean error = false;
	            	String s, xStart = "", yStart = "", xEnd ="", yEnd="", xMur="", yMur="";
	            	ArrayList<Point> temp = new ArrayList<Point>();
	            	try {
	            		reader = new BufferedReader(new FileReader(file));
	            		String line = reader.readLine();
	            		while(line != null && !error) {
	            			if(line.length() > 0) {
	            				if(line.charAt(0) == 'p') {
		            				size = Integer.valueOf(line.substring(2, line.length()));
		            			} else if(line.charAt(0) == 's') {
		            				s = line.substring(2, line.length());
		            				xStart = s.split(";")[0];
		            				yStart = s.split(";")[1];
		            			} else if(line.charAt(0) == 'e') {
		            				s = line.substring(2, line.length());
		            				xEnd = s.split(";")[0];
		            				yEnd = s.split(";")[1];
		            			} else if(line.charAt(0) == 'm') {
		            				s = line.substring(2, line.length());
		            				xMur = s.split(";")[0];
		            				yMur = s.split(";")[1];
		            				temp.add(new Point(Integer.valueOf(xMur), Integer.valueOf(yMur)));
		            			} else error = true;
	            			}
	            			line = reader.readLine();
	            		}
	            		reader.close();
	            		if(error) {
	            			System.err.println("Le fichier contient de faux paramètres.");
	            		} else {
	            			taille_case = 800 / size;
	        				p = new Plateau(size);
	        				murs = new ArrayList<Point>();
	        				current = null;
	        				p.clear();
	        				if(xStart != "" && yStart != "")p.setStart(Integer.valueOf(xStart), Integer.valueOf(yStart));
	        				if(xEnd != "" && yEnd != "") p.setEnd(Integer.valueOf(xEnd), Integer.valueOf(yEnd));
	        				alreadyCheck = new boolean[size][size];
	        				road = new ArrayList<Point>();
	        				tested = new ArrayList<Point>();
	        				finish = false;
	        				gc.setFill(Color.WHITE);
	        				gc.setStroke(Color.BLACK);
	        				for (int i = 0; i < size; i++) {
	        					for (int j = 0; j < size; j++) {
	        						gc.fillRect(i * taille_case, j * taille_case, taille_case, taille_case);
	        						gc.strokeRect(i * taille_case, j * taille_case, taille_case, taille_case);
	        					}
	        				}
	        				for(int i = 0 ; i < temp.size() ; i++) {
	        					murs.add(p.getPoint(temp.get(i).getX(), temp.get(i).getY()));
	    						gc.setFill(Color.BLACK);
	    						gc.fillRect(temp.get(i).getX() * taille_case, temp.get(i).getY() * taille_case, taille_case, taille_case);
	        				}
	        				repaint();
	            			System.out.println("Les paramètres ont été chargé.");
	            		}
	            	} catch(IOException e) {
	            		e.printStackTrace();
	            		System.out.println("Le fichier n'a pas pu être ouvert.");
	            	}
	            }
			}
			
		});
		bottom.getChildren().addAll(start_point, end_point, search, next, clear, save, open);
		root.getChildren().addAll(up, bottom);

		Scene scene = new Scene(root);
		s.setTitle("A* pathfinder");
		s.setScene(scene);
		s.show();
	}

	private void repaint() {
		gc.setFill(Color.GREEN);
		if (p.getEnd() != null)
			gc.fillRect(p.getEnd().getX() * taille_case, p.getEnd().getY() * taille_case, taille_case, taille_case);
		
		/*gc.setStroke(Color.BLACK);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				afficheStats(p.getPoint(i, j));
			}
		}*/
		
		if(current != null) {
			//gc.setFill(Color.rgb((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
			gc.setFill(Color.PURPLE);
			Point temp = current;
			while(temp.getPrecedent() != null) {
				gc.fillRect(temp.getX() * taille_case, temp.getY() * taille_case, taille_case, taille_case);
				//afficheStats(temp);
				temp = temp.getPrecedent();
			}
		}
		
		gc.setFill(Color.RED);
		if (p.getStart() != null)
			gc.fillRect(p.getStart().getX() * taille_case, p.getStart().getY() * taille_case, taille_case, taille_case);
	}

	public void addMouseScrolling(Node node) {
		node.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double deltaY = event.getDeltaY();
			if(deltaY < 0) {
				size += 4;
				taille_case = 800 / size;
				p = new Plateau(size);
				murs = new ArrayList<Point>();
				current = null;
				p.clear();
				alreadyCheck = new boolean[size][size];
				road = new ArrayList<Point>();
				tested = new ArrayList<Point>();
				finish = false;
				gc.setFill(Color.WHITE);
				gc.setStroke(Color.BLACK);
				for (int i = 0; i < size; i++) {
					for (int j = 0; j < size; j++) {
						gc.fillRect(i * taille_case, j * taille_case, taille_case, taille_case);
						gc.strokeRect(i * taille_case, j * taille_case, taille_case, taille_case);
					}
				}
			}
			else {
				System.out.println("Zoom");
			}
		});
	}

	private void afficheStats(Point p) {
		gc.setFont(new Font("Arial", taille_case * 0.35));
		String s;
		if(p.getGCost() != 0) {
			s = String.valueOf(p.getGCost());
			gc.strokeText(s, p.getX() * taille_case + 2, p.getY() * taille_case + 10);
		}

		if(p.getHCost() != 0) {
			s = String.valueOf(p.getHCost());
			gc.strokeText(s, p.getX() * taille_case + (taille_case * 0.5), p.getY() * taille_case + (taille_case * 0.2));
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
								//System.out.println("Point ajouté");
							} else {
								int fromStart, toEnd;
								fromStart = temp.getDistance(current) + current.getGCost();
								toEnd = temp.getDistance(p.getEnd());
								if(fromStart < temp.getGCost()) {
									temp.setGCost(fromStart);
									temp.setHCost(toEnd);
									temp.setFCost(fromStart + toEnd);
									temp.setPrecedent(current);
									//System.out.println("Point MODIFIE!");
								}
							}
						}
					}
				}
			}
		}
		if(current != null) {
			//gc.setFill(Color.rgb((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
			gc.setFill(Color.BLUE);
			Point temp1 = current;
			while(temp1.getPrecedent() != null) {
				gc.fillRect(temp1.getX() * taille_case, temp1.getY() * taille_case, taille_case, taille_case);
				//afficheStats(temp);
				temp1 = temp1.getPrecedent();
			}
		}
		current = getBest();
		road.add(current);
		if(current == null) {
			System.out.println("Aucun passage possible");
			finish = true;
			timer.cancel();
			return true;
		}
		if (current.getHCost() == 0) {
			finish = true;
			System.out.println("Terminé");
			timer.cancel();
			return true;
		}
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
	
	private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
