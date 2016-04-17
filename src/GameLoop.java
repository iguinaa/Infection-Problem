/* This uses javafx's animation timer as a 60 fps gameloop
 * There is no single person to which this idea can be given full credit, however
 * I would like to reference a few projects which helped me design this such as:
 * TODO: insert list of projects like svanimpe's pong
 * 
 *   */
import javafx.animation.AnimationTimer;

public abstract class GameLoop extends AnimationTimer {

	private float maxStep = Float.MAX_VALUE; // In case subclass needs to know
	public static final double NANO_TO_SECONDS = 1_000_000_000.0;
	
	public float getMaxStep()
	{
		return maxStep;
	}
	
	public void setMaxStep(float maxStep)
	{
		this.maxStep = maxStep;
	}
	
	@Override
	public void stop()
	{
		super.stop();
	}
	
}
