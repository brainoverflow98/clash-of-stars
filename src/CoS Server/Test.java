
public class Test {
	
	
	public static void main(String[] args) {	 
		
		float dx=p.move_x-p.x;                    	
		float dy=p.move_y-p.y; 
		double dis = Math.sqrt((dx)*(dx)+(dy)*(dy));
		double sin = (dy)/dis;
  		double cos = (dx)/dis;
  		int speed = 300;  		
		while(!(dis<speed/2))		
		{		      	  		
			p.x+=speed*cos*delta/100;
			p.y+=speed*sin*delta/100;      	
			dis = Math.sqrt((p.move_x-p.x)*(p.move_x-p.x)+(p.move_y-p.y)*(p.move_y-p.y));        	
		}
		
		int dx=p.move_x-p.x;                    	
      	int dy=p.move_y-p.y;     
      	int speed = 3;
  		double sin = (dy)/Math.sqrt((dx)*(dx)+(dy)*(dy));
  		double cos = (dx)/Math.sqrt((dx)*(dx)+(dy)*(dy));
      	p.x+=speed*cos*delta;
      	p.y+=speed*sin*delta;
	}
	
	

}
