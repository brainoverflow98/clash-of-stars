
import java.util.Scanner;

//SERVER COMMANDS AND CHAT  
public class ServerCH extends Thread{
	private Scanner scan;
	TCPBuffer buffer = new TCPBuffer();
	@Override
	public void run()
	{		
	while(true){
	try{		
		scan = new Scanner(System.in);
			String s = scan.nextLine();		
			if(!s.equals(""))
			{			
				for (Player p:Server.playerlist)
				{					
					if(p!=null)
    				{
						buffer.write_u8(35); 
						buffer.write_string(s);
	            		buffer.send(p.output);
	            		buffer.flush();	
    				}
				}					
			}
			
		} finally{} //catch (IOException e) {e.printStackTrace();}
	}
		
	}	

}
