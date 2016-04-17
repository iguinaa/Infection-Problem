import java.util.ArrayList;
import java.util.Random;

import javafx.beans.binding.StringExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class MyGame extends Game 
{
	/* Constants */
	public static final int DEFAULT_WIDTH = 900;
	public static final int DEFAULT_HEIGHT = 750;
	public final int NUM_ROWS = 150;
	public final int NUM_COLS = 150;	
	public final int TOTAL_CELLS = NUM_ROWS * NUM_COLS;
	public final int NUM_TICKS_PER_STEP = 5;  // ticks are calls to update(), steps are actual updates 
	public final int DEFAULT_IMMUNE_PERCENT = 90;
	
	/* GameStates */
	public enum GameState{
		Loading,
		Selecting,
		Running,
		Paused,
		Closing,
	}
	
	public enum VaccinationStyle{
		Random,
		Quadrant,
		
	}
	
	private class Infected
	{
		public MySquare cell;
		public int iRow;
		public int jCol;

		public Infected (MySquare cell, int row, int col)
		{
			this.cell = cell;
			iRow = row;
			jCol = col;
		}
	}
	
	private class Immune
	{
		public MySquare cell;
		public int iRow;
		public int jCol;

		public Immune (MySquare cell, int row, int col)
		{
			this.cell = cell;
			iRow = row;
			jCol = col;
		}
	}
	
	private class Vulnerable
	{
		public MySquare cell;
		public int iRow;
		public int jCol;

		public Vulnerable (MySquare cell, int row, int col)
		{
			this.cell = cell;
			iRow = row;
			jCol = col;
		}
	}
	
	GameState currentState;
	VaccinationStyle vaccinationStyle = VaccinationStyle.Random;
	MySquare[][] cells = new MySquare[NUM_ROWS][NUM_COLS];
	public int numTicks;
	public ArrayList<Infected> currentInfected = new ArrayList<Infected>();
	public ArrayList<Immune> currentImmune = new ArrayList<Immune>();
	public ArrayList<Vulnerable> currentVulnerable = new ArrayList<Vulnerable>();
	public boolean includeDiagonals = false;
	public boolean immunized = false;
	public boolean hasResetSinceLastRun = true;
	boolean vaccinationOn = true;
	
	/* Renderables */
	BorderPane root;
	GridPane grid;
	VBox controls;
	Scene scene;
	private int numIterations = 0;
	private HBox data;
	private int numInfected = 0;
	private int numVaccinated;
	private int numImmune;
	private IntegerProperty percentImmune = new SimpleIntegerProperty();
	private Random rng;
	
	StringProperty dataString = new SimpleStringProperty("# Infected: " + numInfected + "\n" +
			"Initial % Immune: " + percentImmune.get() + "%\n" +
			"# Vaccinated: " + numVaccinated + "\n" +
			"Total Alive: " + (TOTAL_CELLS - numInfected) + "\t\n" + 
			"# Immune: " + numImmune + "\n" +
			"Last Iteration completed: " + numIterations + "\n");

	StringProperty resultsString = new SimpleStringProperty("Results of Last Run:\n"+
			"\tNumber saved: " +
			"\n\t# Non-Immune Saved: " + 
			"\n\tPercent Saved: " +
			"\n\tNumber of Turns Required: "
			);
	
	public MyGame(Stage stage)
	{
		mainStage = stage;
		
	}
	/** Initialize, Load Content, Begin Running */
	@Override
	public void run()
	{
		currentState = GameState.Loading;
		super.run();
		currentState = GameState.Selecting;
	}
	@Override
	public void update(double dt) 
	{
		
		int prevNumInfected = numInfected;
		int infectedAlive = 0;
		boolean keepRunning = false;
		numTicks++;
		// TODO Update everything here. probably build a Consumable<Updateable> list to update everything that needs updating
		//CAN REACH! YAY! So update scene here.
//		if(scene.getFill() != Color.BLACK)
//			scene.setFill(Color.BLACK);
//		else if(scene.getFill()!=Color.MAROON)
//			scene.setFill(Color.MAROON);
		if(currentState == GameState.Running && (numTicks % NUM_TICKS_PER_STEP) == 0)
		{
			// TODO: IF ALL STATES DEAD -> break;
			currentInfected.clear();
			currentImmune.clear();
			currentVulnerable.clear();
			boolean someInfected = false;
			
			// ***** Determine Whether Game Is Over AND Update Lists ******
			for(int i=0; i<NUM_ROWS; i++)
			{
				for(int j=0; j<NUM_COLS; j++)
				{
					if (cells[i][j].isInfected)
					{
						someInfected = true;
						break;
					}
				}
				if (someInfected)
					break;
			}
			
			
			// ****** Start infection randomly if not specified *********
			if(!someInfected && hasResetSinceLastRun)
			{
				pickFirstInfected();
				someInfected = true;
				numIterations = -2;
			}
			
			if(numIterations <= 0 && immunized == false && hasResetSinceLastRun)
			{
				pickFirstImmune(percentImmune.get());
				immunized = true;
			}
			
			numInfected = 0;
			for(int i=0; i<NUM_ROWS; i++)
			{
				for(int j=0; j<NUM_COLS; j++)
				{
					if(cells[i][j].isInfected)
					{
						currentInfected.add(new Infected(cells[i][j], i , j));
						numInfected++;
						if( !(cells[i][j].isDead) )
							infectedAlive++;
					}
				}
			}
			
			if(prevNumInfected != numInfected || numIterations < 0 || infectedAlive != 0 )
				keepRunning = true;
			
			if(keepRunning)
			{
				if(prevNumInfected != numInfected || numIterations < 0 )
					numIterations++;
			}		
			else
			{			
//				System.out.println("Hit Quit Case");
				currentState = GameState.Loading;
				updateResultsText();
				numIterations = 0;
				immunized = false;
				hasResetSinceLastRun = false;
			}
			
			numImmune = 0;
			for(int i=0; i<NUM_ROWS; i++)
			{
				for(int j=0; j<NUM_COLS; j++)
				{
					if(cells[i][j].isImmune)
					{
						currentImmune.add(new Immune(cells[i][j], i , j));
						numImmune++;
					}
				}
			}
			
			// ******* Spread Infection **********
			if(someInfected && numIterations >= 0)
				spreadInfection();
			
			
			
			
			// ******* get currentVulnerable cells ******
			for(int i=0; i<NUM_ROWS; i++)
			{
				for(int j =0; j<NUM_COLS; j++)
				{
//					System.out.println(i +", " +j + " is: infected? " + cells[i][j].isInfected + "is: immune? " + cells[i][j].isImmune ); // TODO Remove
					if( !(cells[i][j].isInfected) && !(cells[i][j].isImmune) )
							currentVulnerable.add( new Vulnerable(cells[i][j], i, j) );
				}
			}
			
			
			// ******* Vaccinate or Quarantine ********
			if(vaccinationOn && prevNumInfected != numInfected && numIterations >= 10 && currentVulnerable.size() > 0) //TODO uncomment
			{
				vaccinatePeople();
				numVaccinated++;
			}
			
			
			
			// ******* Kill long term infected ******
			for(int i=0; i < currentInfected.size(); i++)
			{
				currentInfected.get(i).cell.update(dt);
			}
			
			// ***** Update Data String ********
			updateDataText();
			
				
		}// PAUSE OR END GAMELOOP
		
		if( (NUM_TICKS_PER_STEP * 10000) == numTicks) // Every 10000 steps, reset ticker to prevent overflow
			numTicks = 0;
		
	} // End Update()
	
	private void pickFirstImmune(int percent) 
	{
		double dblPercent = ((double)(percent)/(double)(100));
		int numToImmunize = (int) (TOTAL_CELLS * dblPercent);
		int next = 0;
		int row = 0;
		int col = 0;
//		System.out.println("num to immune: " + numToImmunize + "percent: " + percent + "  doublePercent: " + dblPercent);//TODO remove
		for(int i = 0; i<numToImmunize; i++)
		{
			next = rng.nextInt(TOTAL_CELLS);// might be -1 but shouldnt be 
			row = (int)(next / NUM_COLS);
			col = next % NUM_COLS;
//			System.out.println("Attempt immunize number, row, column: " + next + ", " + row + ", " + col); //TODO remove

			if( !( cells[row][col].isInfected || cells[row][col].isImmune ) )
				cells[row][col].toggleImmune();
			else
				i--;
		}
		
		
	}
	private void pickFirstInfected() 
	{
		int next = rng.nextInt(TOTAL_CELLS); // might be -1 but shouldnt be
		int row = (int)(next / NUM_COLS);
		int col = next % NUM_COLS;
		
//		System.out.println("Infected number, row, column: " + next + ", " + row + ", " + col);
		cells[row][col].toggle();
	}
	
	private void vaccinatePeople() 
	{
		//		System.out.println("vaccinated: number " + currentVulnerable.size() );
		int next = rng.nextInt( currentVulnerable.size() );
		int numIterations = 0;
		boolean isGoodCandidate = false;
		Vulnerable cell = currentVulnerable.get(next);
		
		while( !isGoodCandidate && numIterations < 100)
		{
			//North
			if( (cell.iRow == 0 || (cell.iRow > 0 && cells[cell.iRow - 1][cell.jCol].isImmune) ) && // FIXME: Array out of bounds on one of these conditions
					//South
					( cell.iRow == NUM_ROWS-1   || (cell.iRow < NUM_ROWS - 1 && cells[cell.iRow + 1][cell.jCol].isImmune) ) &&
					//West
					( cell.jCol == 0  || (cell.jCol > 0 && cells[cell.iRow][cell.jCol - 1].isImmune) ) &&
					//East
					( cell.jCol == NUM_COLS-1  || (cell.jCol < NUM_COLS - 1 && cells[cell.iRow][cell.jCol + 1].isImmune) ) )
			{
				if( ((RadioButton)controls.getChildren().get(5)).isSelected() == true )
				{
					//North West
					if( ( (cell.iRow == 0 && cell.jCol == 0) || (cell.iRow > 0 && cell.jCol > 0 && cells[cell.iRow - 1][cell.jCol - 1].isImmune) ) &&
							//South West
							( (cell.iRow == NUM_ROWS-1 && cell.jCol == 0) || (cell.iRow < NUM_ROWS-1 && cell.jCol > 0 && cells[cell.iRow + 1][cell.jCol - 1].isImmune) ) &&
							//North East
							( (cell.iRow == 0 && cell.jCol == NUM_COLS - 1) || (cell.iRow > 0 && cell.jCol < NUM_COLS - 1 && cells[cell.iRow - 1][cell.jCol + 1].isImmune) ) &&
							//South East
							( (cell.iRow == NUM_ROWS-1 && cell.jCol == NUM_COLS - 1) || (cell.iRow < NUM_ROWS-1 && cell.jCol < NUM_COLS - 1 && cells[cell.iRow + 1][cell.jCol + 1].isImmune) ) ) 
					{
						isGoodCandidate = false;
					}
					else
					{
						isGoodCandidate = true;
					}
				}
				else
					isGoodCandidate = false;
			}
			else
			{
				isGoodCandidate = true;
			}
			
			if(!isGoodCandidate)
			{
				next = rng.nextInt( currentVulnerable.size() );
				cell = currentVulnerable.get(next);
				numIterations++;
			}
		}
		
		cell.cell.toggleImmune();
		
	}
	
	public void updateDataText()
	{
		dataString.setValue("# Infected: " + numInfected + "\n" +
				"Initial % Immune: " + percentImmune.get() + "%\n" +
				"# Vaccinated: " + numVaccinated + "\n" +
				"Total Alive: " + (TOTAL_CELLS - numInfected) + "\t\n" + 
				"# Immune: " + numImmune + "\n" +
				"Last Iteration completed: " + numIterations + "\n"
			);
	}
	
	public void updateResultsText()
	{
		resultsString.setValue("Results of Last Run:\n" +
				"\tNumber saved: " + (TOTAL_CELLS - numInfected) +
				"\n\t# Non-Immune Saved: " + (TOTAL_CELLS - numInfected - numImmune) +
				"\n\tPercent Saved: " + String.format("%.2f", ( ((double)(TOTAL_CELLS - numInfected)) / ((double)TOTAL_CELLS))*100 ) + "%" +
				"\n\tNumber of Turns Required: " + numIterations
				
				);
	}
	
	public void spreadInfection()
	{
		for(int i=0; i<currentInfected.size(); i++)
		{
			Infected cell = currentInfected.get(i);
			//North
			if( cell.iRow > 0  && !(cells[cell.iRow - 1][cell.jCol].isInfected) )
				if( !(cells[cell.iRow - 1][cell.jCol].isImmune) )
					cells[cell.iRow - 1][cell.jCol].toggle();
			//South
			if( cell.iRow < NUM_ROWS-1   && !(cells[cell.iRow + 1][cell.jCol].isInfected) )
				if( !(cells[cell.iRow + 1][cell.jCol].isImmune) )
					cells[cell.iRow + 1][cell.jCol].toggle();
			//West
			if( cell.jCol > 0  && !(cells[cell.iRow][cell.jCol - 1].isInfected) )
				if( !(cells[cell.iRow][cell.jCol - 1].isImmune) )
					cells[cell.iRow][cell.jCol - 1].toggle();
			//East
			if( cell.jCol < NUM_COLS-1   &&!(cells[cell.iRow][cell.jCol + 1].isInfected) )
				if( !(cells[cell.iRow][cell.jCol + 1].isImmune) )
					cells[cell.iRow][cell.jCol + 1].toggle();
			
			if( ((RadioButton)controls.getChildren().get(5)).isSelected() == true )
			{
//				System.out.println("Diagonals too.");
				
				//North West
				if (cell.iRow > 0 && cell.jCol > 0)
					if( !(cells[cell.iRow - 1][cell.jCol - 1].isInfected || cells[cell.iRow - 1][cell.jCol - 1].isImmune))
						cells[cell.iRow - 1][cell.jCol - 1].toggle();
				
				//South West
				if (cell.iRow < NUM_ROWS-1 && cell.jCol > 0)
					if( !(cells[cell.iRow + 1][cell.jCol - 1].isInfected || cells[cell.iRow + 1][cell.jCol - 1].isImmune))
						cells[cell.iRow + 1][cell.jCol - 1].toggle();
				
				//North East
				if (cell.iRow > 0 && cell.jCol < NUM_COLS - 1)
					if( !(cells[cell.iRow - 1][cell.jCol + 1].isInfected || cells[cell.iRow - 1][cell.jCol + 1].isImmune))
						cells[cell.iRow - 1][cell.jCol + 1].toggle();
				
				//South East
				if (cell.iRow < NUM_ROWS-1 && cell.jCol < NUM_COLS - 1)
					if( !(cells[cell.iRow + 1][cell.jCol + 1].isInfected || cells[cell.iRow + 1][cell.jCol + 1].isImmune))
						cells[cell.iRow + 1][cell.jCol + 1].toggle();
				
			}
		}
		
		
	}
	
	

	@Override
	public void initialize() 
	{
		// TODO initialize all basic stuff here scenes, panes, etc.
		rng = new Random();
		
		
		root = new BorderPane();
		grid = new GridPane();
		controls = new VBox();
		scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT); // TODO: Change color
		data = new HBox();
		Text current = new Text();
		Text results = new Text();
		
		current.textProperty().bind(dataString);
		results.textProperty().bind(resultsString);
		

		data.getChildren().addAll(current,results);
		root.setBottom(data);
		
		root.setLeft(controls);
		root.setCenter(grid);
//		dbgPane(controls, Color.RED);
		dbgPane(grid, Color.web("#231f20"));
		grid.setStyle(" -fx-border-color: rgb(75,75,75); -fx-border-width: 5;");
		data.setPadding(new Insets(5,5,5,5));
		//test adjust grid size to fit
		//grid.setMaxHeight(DEFAULT_HEIGHT);
		grid.setAlignment(Pos.CENTER);
		/*
		ColumnConstraints column = new ColumnConstraints(5,5,5);
		RowConstraints row = new RowConstraints(5,5,5);
		column.setPercentWidth(.5);
		for (int i=0; i<=NUM_COLS; i++)
			grid.getColumnConstraints().add(new ColumnConstraints(10,10,10));
		for (int i=0; i<=NUM_ROWS; i++)
			grid.getRowConstraints().add(new RowConstraints(10,10,10));
		*/
		data.setSpacing(5.0);
		controls.setPrefWidth(DEFAULT_WIDTH/7 + 10);
		controls.setPrefHeight(DEFAULT_HEIGHT);
		controls.setPadding(new Insets(3,3,3,3));
		controls.setSpacing(5.0);
		makeControlPane(controls);
		
		
		
		for(int i=0; i<NUM_ROWS; i++)
		{
			for(int j=0; j<NUM_COLS; j++)
			{
				cells[i][j] = makeGridNode();
				grid.add(cells[i][j], j, i);
//				grid.add((StackPane)makeGridNode(), j, i);
			}
		}
		mainStage.setScene(scene);
		
		mainStage.show();
	}
	
	public void makeControlPane(VBox controls)
	{
		Button btnRun = new Button("Run Simulation");
//		Button btnStop = new Button("End Simulation");
		Button btnPause = new Button("Pause Simulation");
		Button btnReset = new Button("Reset");
		RadioButton rdCardinal = new RadioButton("NSEW");
		RadioButton rdDiagonalsToo = new RadioButton("NSEW and Diagonal");
		ToggleGroup defAdjacent = new ToggleGroup();
		rdCardinal.setToggleGroup(defAdjacent);
		rdCardinal.setSelected(true);
		rdDiagonalsToo.setToggleGroup(defAdjacent);
		
		Label adjacent = new Label("Adjacent Definition:");
		adjacent.setPadding(new Insets(10.0,0,0,0));
		adjacent.setFont(Font.font(null, FontWeight.BOLD, 12 ));
		
		Label vaccinating = new Label("Vaccination On/Off:");
		vaccinating.setPadding(new Insets(10.0,0,0,0));
		vaccinating.setFont(Font.font(null, FontWeight.BOLD, 12 ));
		
		HBox vacs = new HBox();
		vacs.setSpacing(5.0);
		RadioButton vacOn = new RadioButton("On");
		RadioButton vacOff = new RadioButton("Off");
		ToggleGroup toggleVacs = new ToggleGroup();
		vacOn.setToggleGroup(toggleVacs);
		vacOff.setToggleGroup(toggleVacs);
		vacs.getChildren().addAll(vacOn, vacOff);
		vacOn.setSelected(true);
		toggleVacs.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> arg0,
					Toggle arg1, Toggle arg2) {
				RadioButton selected = (RadioButton)toggleVacs.getSelectedToggle();
				if(selected.getText() == "On")
					vaccinationOn = true;
				else
					vaccinationOn = false;
				
			}
			
		});
	
		Label defImmune = new Label("Start Immune Percent:");
		defImmune.setPadding(new Insets(10.0,0,0,0));
		defImmune.setFont(Font.font(null, FontWeight.BOLD, 12 ));
		
		Slider percent = new Slider(0,80, DEFAULT_IMMUNE_PERCENT);
		Label lblPercent = new Label();
//		lblPercent.textProperty().stringExpression(percentImmune);
		SimpleStringProperty strPercent = new SimpleStringProperty();
		strPercent.bind(StringExpression.stringExpression(percentImmune));
		lblPercent.textProperty().bind(strPercent);
		Label lblPercentSign = new Label("%");
		HBox percentLabels = new HBox();
		percentLabels.getChildren().addAll(lblPercent, lblPercentSign);
		
		
		
		btnRun.setOnAction(e -> {
			currentState = GameState.Running;});
		btnPause.setOnAction(e -> {
			currentState = GameState.Paused;});
		btnReset.setOnAction(e -> {

			reset();
		});
		
		
		percentImmune.set(DEFAULT_IMMUNE_PERCENT);
		percentImmune.bind(percent.valueProperty());
		
		
		
		
		
		// BE CAREFUL REMOVING STUFF FROM THIS, need to change hardcoded index for radio button
		controls.getChildren().addAll(btnRun, btnPause, btnReset, adjacent, rdCardinal, rdDiagonalsToo, vaccinating, vacs, defImmune, 
				percentLabels, percent);
		
	}

	private void reset()
	{
		
		currentState = GameState.Loading;
		numIterations = 0;
		numVaccinated = 0;
		immunized = false;
		for(int i=0; i<NUM_ROWS; i++)
		{
			for(int j=0; j<NUM_COLS; j++)
			{
				cells[i][j].reset();
				
			}
		}
		hasResetSinceLastRun = true;
		
	}
	
	private MySquare makeGridNode() {
		MySquare square = new MySquare(4,4,1.5, Color.GHOSTWHITE); //TODO Change size
		((Node) square).setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event)
			{
				if(event.getButton().equals(MouseButton.PRIMARY))
				{
//					System.out.println("a square was clicked"); //TODO Remove
					square.toggle();
					event.consume();
				}
				else if (event.getButton().equals(MouseButton.SECONDARY))
				{
//					System.out.println("a square was right-clicked"); //TODO Remove
					square.toggleImmune();
					event.consume();
				}
			}
		});
		
		dbgPane(square, Color.web("#231f20"));
		return square;
	}
	
	protected void dbgPane(Pane p, Color c)
	{
		p.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, new Insets(0,0,0,0) )));
	}

	@Override
	public void loadContent() {
		// TODO Auto-generated method stub

	}

}
