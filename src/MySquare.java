import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class MySquare extends StackPane implements Updateable
{
	Circle circle;
	Color startColor;
	public boolean isInfected = false;
	public boolean isDead = false;
	public int id;
	public static int numInstances = 0;
	boolean isImmune = false;
	public int turnsInfected = 0;
	
	
	public MySquare(int width, int height, double cRadius, Color cFill)
	{
		this.setPrefHeight(height);
		this.setPrefWidth(width);
		circle = new Circle(cRadius, cFill);
		startColor = cFill;
		this.getChildren().add(circle);
		isInfected = false;
		isDead = false;
		turnsInfected = 0;
		numInstances++;
		id = numInstances;
	}
	
	@Override
	public void update(double dt) 
	{
		// TODO:
		if(isInfected)
			turnsInfected++;
		else
			turnsInfected = 0;
		
		if(turnsInfected >= 20)
			isDead = true;
		else
			isDead = false;
		
		if(isDead == true)
			circle.setFill(Color.BLACK);
	}
	
	public void toggle()
	{
		if( circle.getFill().equals(startColor) )
		{
			circle.setFill(Color.RED);
			isInfected = true;
		}
		else
		{
			circle.setFill(startColor);
			isInfected = false;
			isImmune = false;
			isDead = false;
		}
	}
	
	public void toggleImmune()
	{
		if( circle.getFill().equals(startColor) )
		{
			circle.setFill(Color.CORNFLOWERBLUE);
			isImmune = true;
		}
		else
		{
			circle.setFill(startColor);
			isImmune = false;
			isInfected = false;
			isDead = false;
		}
	}
	
	public void reset()
	{
		circle.setFill(startColor);
		isInfected = false;
		isImmune = false;
		isDead = false;
		turnsInfected = 0;
	}
	
	public void setImmune(boolean immune)
	{
		this.isImmune = immune;
	}
	public boolean getImmune()
	{
		return this.isImmune;
	}
	
	
}
