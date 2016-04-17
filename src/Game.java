import java.io.FileNotFoundException;

import javafx.stage.Stage;

public abstract class Game implements Runnable, Updateable 
{
	/* GameStates */
	public enum GameState{
		Loading,
		Selecting,
		Running,
		Paused,
		Closing,
	}
	
	public ThisLoop gameLoop;
	public Stage mainStage;
	double secondsElapsed;
	private class ThisLoop extends MyLoop 
	{
		@Override
		public void handle(long current)
		{
			super.handle(current);
			secondsElapsed = (current - previous) / super.NANO_TO_SECONDS; /* Convert nanoseconds to seconds. */
			update(secondsElapsed);
		}
	}
	
	/** Initialize, Load Content, Begin Running */
	@Override
	public void run()
	{
		this.initialize();
		this.loadContent();
		this.startTimer();
	}
	
	/** Begins Animation Timer ticks. Rk: Attempts 60fps */
	public void startTimer(){
		gameLoop = new ThisLoop();
		gameLoop.start();
	}
	
	/** Mostly Constructors, but can include anything that doesn't require loading and needs a value. */
	public abstract void initialize();
	
	/** Load all external content here as necessary */
	public abstract void loadContent();
}
