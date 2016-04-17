public class MyLoop extends GameLoop
{
	public static final float PREF_STEP = 0.0333f;
	
	public Runnable game;
	protected long previous = 0;
	
	public MyLoop(){
		
	}

	@Override
	public void handle(long current) 
	{
		if (previous == 0) // (previous == 0) implies (Nothing needs update yet) 
		{
			previous = current;
			return;
		}

		double secondsElapsed = (current - previous) / GameLoop.NANO_TO_SECONDS;
		
        // Upper Bound for 
        if (secondsElapsed > PREF_STEP) 
            secondsElapsed = PREF_STEP;
        
        previous = current;
    }
	
	@Override
	public void stop()
	{
		previous = 0;
		super.stop();
	}
	
}
