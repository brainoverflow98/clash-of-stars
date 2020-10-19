import java.net.*;

class UDPServer  extends Thread
{
	public static DatagramSocket serverSocket;	
    UDPBuffer buffer = new UDPBuffer();  
    Player player;
    Player[] playerlist = null;
	@Override
	public void run() 			
      {         
		try {
			serverSocket = new DatagramSocket(35000);  
			int ID,msgID,port;
			InetAddress IP;
			DatagramPacket receivePacket,sendPacket;
			byte [] data;
			int mouse_x;
            int mouse_y;
            while(true)
               {
                  receivePacket = new DatagramPacket(buffer.receiveData, buffer.receiveData.length);
                  serverSocket.receive(receivePacket);                  
                  msgID =  buffer.read_u8();                    
                  switch(msgID)
                  {
                  	
                  case 4://GET THE UDP PORT AND SAVE TO PLAYER OBJECT SEND A MESSAGE TO CHECK IF UDP IS WORKING
                	  ID =  buffer.read_u8();  
                	  buffer.clear();
                	  Server.playerlist[ID].port = receivePacket.getPort();	
                	  buffer.write_u8(4);
                	  data = buffer.flush();               	  	                            
                  	  sendPacket = new DatagramPacket(data,data.length, receivePacket.getAddress(), receivePacket.getPort());
                      serverSocket.send(sendPacket); 
                	  System.out.println("Port set !");
                	  break;
                  	
                  case 8://GET THE PLAYER MOVEMENT INFORMATION AND INFORM OTHER PLAYERS 
                  		ID =  buffer.read_u8();                   		
                  		mouse_x =  buffer.read_s16();
                        mouse_y =  buffer.read_s16();
                        if(mouse_x<0)mouse_x=-mouse_x;
                        if(mouse_y<0)mouse_y=-mouse_y;
                        buffer.clear();
                        player = Server.playerlist[ID];
                        playerlist = Server.roomlist[player.rID].playerlist;
                        player.move_x=mouse_x;
                        player.move_y=mouse_y;
                        buffer.write_u8(8);
                        buffer.write_u8(player.gID);
                        buffer.write_s16((short)mouse_x);
                        buffer.write_s16((short)mouse_y);
                        buffer.write_s16((short)player.x);
                        buffer.write_s16((short)player.y);                                                
                        data = buffer.flush();  
                        if(playerlist != null)
                        for (Player p: playerlist)
                        {                        	
                        	if (p != null)
                        	{
	                        	IP = p.IP;	                            
	                            port = p.port;	                            
	                        	sendPacket = new DatagramPacket(data,data.length, IP, port);
	                            serverSocket.send(sendPacket);  
                        	}
                        }                 		
                        break;
                  }                                 
               }
		} catch (Exception e) {e.printStackTrace();}			
      }
}