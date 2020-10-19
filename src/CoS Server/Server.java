import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//CREATE THE SERVER AND START OTHER THREADS TEHN WAIT FOR RECIEVE NEW CONNECTION AND ASSIGN ID TO NEW PLAYERS
public class Server extends Thread{
	public static ServerSocket server;
	public static Player[] playerlist = new Player[51];	// ARRAY WHICH HOLDS THE PLAYER OBJECTS STARTING FROM 1 WITH THEIR IDs	
	public static Game[] roomlist = new Game[50];	// ARRAY WHICH HOLDS THE GAME ROOM OBJECTS STARTING FROM 1 WITH THEIR IDs	
	private Socket connection;
	private Player player;
	private Game newroom;
	public static int online=0,room=0;	
	@Override
	public void run()
	{	
		try {
			server = new ServerSocket(35000,50);	
			for(int i=0;i<playerlist.length;i++)
			{
				playerlist[i]=null;
			}
			for(int i=0;i<roomlist.length;i++)
			{
				roomlist[i]=null;
			}
			ServerCH CH = new ServerCH();								
			CH.start();	
			UDPServer UDP = new UDPServer();								
			UDP.start();	
			newroom = new Game("Offical Server",1,10,10,5);
			newroom.roomID = roomID(newroom);
			newroom.start();			
			
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
	while(true)
	{
		try{				
			connection = server.accept();			
			System.out.println("\nConnected to "+connection.getInetAddress().getHostName());
			player = new Player(connection);
			player.ID = assignID(player);					
			player.start();			
		} catch (IOException e) {e.printStackTrace();}
	}		
	}	
	private int assignID(Player p)
	{
		for(int i=1;i<playerlist.length;i++)
		{
			if(playerlist[i]==null)
			{
				playerlist[i]=p;
				online += 1;
				System.out.println("Online: "+online);
				return i;
			}
		}
		return 0;
	}
	private int roomID(Game p)
	{
		for(int i=1;i<roomlist.length;i++)
		{
			if(roomlist[i]==null)
			{
				roomlist[i]=p;
				room += 1;
				System.out.println("Room: "+room);
				return i;
			}
		}
		return 0;
	}
	
}
