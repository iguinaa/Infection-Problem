
import javafx.application.Application;
import javafx.stage.Stage;


public class GameOfLife extends Application
{

	final boolean DBGMODE = true; // TODO: change to false
	public static void main(String[] args) 
	{
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		Game game = new MyGame(primaryStage);
		primaryStage.setTitle("Firefighter / Infection Problem - Andrew Iguina");
		try
		{
			game.run();
		} catch(Exception ex){System.out.println("Hit Exception: "+ex);}
	}

}
