/* This class represents the single method that must be implemented by anything in the game
 * that updates on a regular interval based on the framerate. This is mostly for the graphical aspects*/
public interface Updateable 
{
	public abstract void update(double dt);
}