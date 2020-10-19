import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Player extends Thread {
	
	public DataOutputStream output;
	public DataInputStream input;
	TCPBuffer buffer = new TCPBuffer();
	public Socket connection;	
	public InetAddress IP;	
	public boolean connected = true,shooting=false,shootingRSB=false,ISH=false,lightning=false;
	public int port,msgID,ID,gID,rID=0,targetID=0,diminisherID=0,venomID=0,killerID=0,assistID=0,shipID=0,prevshipID=0,comp,ammo=0,dformation=1,prevdformation=1,spread=8,formationPNT=0; 
	public double formationHP=1.2,formationSH=1.1,formationDMG=0.95,formationSPD=1,formationRCT=1,SPDeffect=1,sentinel=1,spectrumDEF=1,spectrumATK=1,diminisherATK=1;
	public String username = ""; 	
	   
	public int WRB=0;	
	
	public double x=100.0,y=100.0,move_x=100.0,move_y=100.0,distance=0;
	public long x4_clock=0,x6_clock=0,rocket_clock=0,kill_clock=0,assist_clock=0,rep_clock=0,
			config_clock=0,emp_clock=0,formation_clock=0,effect_clock=0,ice_clock=0,freeze_clock=0,WRB_clock=0,BSH_clock=0,ISH_clock=0,CLK_clock=0,SMB_clock=0,
			lightning_clock=0,solace_clock=0,sentinel_clock=0,spectrum_clock=0,diminisher_clock=0,venom_clock=0,venom_second=0;
	Random generator = new Random(); 
	long now;
	
	public int 
	maxHP=0,HP=-1000000,
	maxSH=0,SH=0,
	damage=0,
	havoc =0,
	hercules=0,
	prevHP=0,prevSH=0;
	public double speed=0, prevspeed=0;
	
	//EQUIPMENT AND CONFIG VARS
	double 
	config1_maxHP,
    config1_maxSH,
    config1_SH,
    config1_DMG;
    int 
    config1_sped,
    config1_hercules,
    config1_havoc;
    
    double
    config2_maxHP,
    config2_maxSH,
    config2_SH,
    config2_DMG;
    int 
    config2_sped,
    config2_hercules,
    config2_havoc;
	
	int config=1, prevconfig=1;
    //EQUIPMENT AND CONFIG VARS
	
	public Player(Socket socket)//INITIALIZE THE PLAYER
	{
		connection = socket;		
		try {
			IP = connection.getInetAddress();
			output = new DataOutputStream(connection.getOutputStream());
			output.flush();
			input = new DataInputStream(connection.getInputStream());
			connection.setSoTimeout(600000);
			//connection.setTcpNoDelay(true);
		} catch (Exception e) {e.printStackTrace();}	
	}
	@Override
	public void run()
	{		
		buffer.write_u8(1);//SEND PLAYER HIS OWN ID 
		buffer.write_u8(ID);
		buffer.send(output);
		buffer.flush();	
		try {
			Thread.sleep(50);//WAIT FIRST PACKET TO BE SENT
		} catch (InterruptedException e1) {e1.printStackTrace();} 				
		/*
	 	for (Player p:Server.playerlist)
        {                        	
        	if (p != null && p.ID != ID)
        	{
        		buffer.write_u8(6);//INFORM CURRENTLY ACTIVE PLAYERS ABOUT NEW PLAYER AND NEW PLAYER ABOUT CURRENT PLAYERS
        		buffer.write_u8(ID);
        		buffer.send(p.output);
        		buffer.flush();   
        		buffer.write_u8(6);
        		buffer.write_u8(p.ID);
        		buffer.send(output);
        		buffer.flush();	
        		try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }	
        */
		Game room;
		Player player;		
		while(connected){//WAIT FOR THE INCOMING MESSAGES AND GIVE THE CORRESPONDING RESPONSE
		
			try {
				msgID =  buffer.read_u8(input);
				switch(msgID)
	            {
	            	case 2://REGISTER REQUEST	            		
	            		String username2 = buffer.read_string(input);	
	            		String password2 = buffer.read_string(input);
	            		File f2 = new File("Accounts/"+username2+"/Info.txt");
	            		if(f2.exists()) { 
	            			buffer.write_u8(2);//REGISTER RESPONSE
	                		buffer.write_u8(2);//2 FOR USER ALREADY EXSISTS
	                		buffer.send(output);
	                		buffer.flush();	
	            		}
	            		else
	            		{
	            			f2.getParentFile().mkdirs(); 
	            			f2.createNewFile();
	            			BufferedWriter file2 = new BufferedWriter( new FileWriter(f2));
	            			file2.write(username2+"\n");	 
	            			file2.write(password2+"\n");	 
	            			file2.close();
	            			buffer.write_u8(2);//REGISTER RESPONSE
	                		buffer.write_u8(1);//1 FOR SUCCESFUL REGISTER
	                		buffer.send(output);
	                		buffer.flush();	
	                		username = username2;
	            		}
	            		break;
	            	case 3://LOGIN REQUEST
	            		String username3 = buffer.read_string(input);	
	            		String password3 = buffer.read_string(input);
	            		File f3 = new File("Accounts/"+username3+"/Info.txt");
	            		if(f3.exists()) 
	            		{ 	            			
	            			BufferedReader file3 = new BufferedReader(new FileReader(f3));
	            			String pass = file3.readLine();
	            			pass = file3.readLine();	            			
	            			file3.close();
	            			if(password3.equals(pass))
	            			{
	            				buffer.write_u8(3);//LOGIN RESPONSE
		                		buffer.write_u8(1);//1 FOR SUCCESFUL LOGIN
		                		buffer.send(output);
		                		buffer.flush();	
		                		username = username3;
	            			}
	            			else
	            			{
	            				buffer.write_u8(3);//LOGIN RESPONSE
		                		buffer.write_u8(2);//2 FOR WRONG PASS
		                		buffer.send(output);
		                		buffer.flush();	
	            			}	            			
	            		}
	            		else
	            		{	            			
	            			buffer.write_u8(3);//LOGIN RESPONSE
	                		buffer.write_u8(3);//3 FOR WRONG USERNAME
	                		buffer.send(output);
	                		buffer.flush();	
	            		}
	            		break;	            	
	            	case 5://JOIN TO A GAME FROM LOBBY	            		
	            		int roomID = buffer.read_u8(input);
	            		room = Server.roomlist[roomID];
	            		if(room != null)
	            		{
	            			if(room.currentPlayer<room.maxPlayer)
	            			{
	            				if(rID==0)
	            				{
	            					rID = roomID;
	            					gID = room.assignID(Server.playerlist[ID]);
	            					buffer.write_u8(5);
	    	                		buffer.write_u8(1);//JOINING THE ROOM SUCCESFUL
	    	                		buffer.write_u8(gID);
	    	                		buffer.send(output);
	    	                		buffer.flush();		    	                		
	    	                		for (Player p:room.playerlist)
	    	                        {                        	
	    	                        	if (p != null && p.ID != ID)
	    	                        	{
	    	                        		buffer.write_u8(6);//INFORM CURRENTLY ACTIVE PLAYERS ABOUT NEW PLAYER AND NEW PLAYER ABOUT CURRENT PLAYERS
	    	                        		buffer.write_u8(gID);
	    	                        		buffer.write_string(username);
	    	                        		buffer.write_u8(shipID);
	    	                        		buffer.write_u16(maxHP/100);
	    	                        		buffer.write_u16(maxSH/100);
	    	                        		buffer.write_u8(damage/100);
	    	                        		buffer.write_u16((int)speed);
	    	                        		buffer.write_u8(havoc);
	    	                        		buffer.write_u8(hercules);
	    	                        		buffer.write_u8(dformation);
	    	                        		buffer.send(p.output);
	    	                        		buffer.flush();   
	    	                        		buffer.write_u8(6);
	    	                        		buffer.write_u8(p.gID);
	    	                        		buffer.write_string(p.username);
	    	                        		buffer.write_u8(p.shipID);
	    	                        		buffer.write_u16(p.maxHP/100);
	    	                        		buffer.write_u16(p.maxSH/100);
	    	                        		buffer.write_u8(p.damage/100);
	    	                        		buffer.write_u16((int)p.speed);
	    	                        		buffer.write_u8(p.havoc);
	    	                        		buffer.write_u8(p.hercules);
	    	                        		buffer.write_u8(p.dformation);
	    	                        		buffer.send(output);
	    	                        		buffer.flush();     	                        		
	    	                        	}
	    	                        }	
	            				}	            				
	            			}
	            			else
	            			{
	            				buffer.write_u8(5);
		                		buffer.write_u8(2);//ROOM IS FULL
		                		buffer.send(output);
		                		buffer.flush();	
	            			}	            			
	            		}
	            		else
	            		{
	            			buffer.write_u8(5);
	                		buffer.write_u8(3);//ROOM NO LONGER EXISTS
	                		buffer.send(output);
	                		buffer.flush();	
	            		}
	            		break;
	            	case 7://LEAVE ROOM
	            		room = Server.roomlist[rID];	            		
	            		if(room!=null)
	            		{
	            			for(Player p:room.playerlist)
	            			{
	            				if(p!=null)
	            				{
	            					if(p.gID != gID)
	            					{
		            					buffer.write_u8(7);
		                        		buffer.write_u8(gID);		                        		
		                        		buffer.send(p.output);
		                        		buffer.flush();	
	            					}
	            				}
	            			}
	            			room.playerlist[gID] = null;
	            			room.currentPlayer -= 1;		
	            		}	            		
	            		rID = 0;
	            		gID = 0;
	            		config=1;
	            		break;
	            	case 9://SELECT A PLAYER
	            		targetID = buffer.read_u8(input);
	            		room = Server.roomlist[rID];
	            		if(room!=null)
	            		{
	            			for(Player p:room.playerlist)
	            			{
	            				if(p!=null)
	            				{
	            					if(p.gID != gID)
	            					{
		            					buffer.write_u8(9);
		                        		buffer.write_u8(gID);
		                        		buffer.write_u8(targetID);
		                        		buffer.send(p.output);
		                        		buffer.flush();	
	            					}
	            				}
	            			}
	            		}
	            		break;
	            	case 10:// DESELECT A PLAYER
	            		targetID = 0;
	            		shooting=false;
	            		room = Server.roomlist[rID];
	            		if(room!=null)
	            		{
	            			for(Player p:room.playerlist)
	            			{
	            				if(p!=null)
	            				{
	            					if(p.gID != gID)
	            					{
		            					buffer.write_u8(10);
		                        		buffer.write_u8(gID);		                        		
		                        		buffer.send(p.output);
		                        		buffer.flush();	
	            					}
	            				}
	            			}
	            		}
	            		break;
	            	case 11://START ATTACK WITH X6
	            		int ammunation= buffer.read_u8(input);
	            		if(ammunation==6)
	            		{	            			
	            			if(shootingRSB==false)
	            			{
		            			shootingRSB=true;
		            			shooting=false;
	            			}
	            			else
	            			{
	            				shootingRSB=false;
	            				buffer.write_u8(11);
                        		buffer.write_u8(0);		                        		
                        		buffer.send(output);
                        		buffer.flush();	
	            			}            			
	            		}	            		
	            		//START X4 ATTACK OR SAB ATTACK
	            		else if(shooting==false)
	            		{
	            			if(targetID != 0)
	            			{
		            			shooting = true;
		            			ammo = ammunation;	  
	            			}
	            		}
	            		else
	            		{
	            			if(ammunation == ammo)
	            			{
	            				shooting=false;
	            				buffer.write_u8(11);
                        		buffer.write_u8(0);		                        		
                        		buffer.send(output);
                        		buffer.flush();	
	            			}
	            			else
	            			{
	            				ammo = ammunation;
	            			}
	            		}
	            		break;
	            	case 13://ROCKET launcher ATTACK
	            		now = System.nanoTime();  
	            		player = Server.roomlist[rID].playerlist[targetID];
    	        		if(player!=null)
    	        		{       			       			
    	        			distance = Math.sqrt((x-player.x)*(x-player.x)+(y-player.y)*(y-player.y)); 	        			
    	        			if(now-rocket_clock>=10000000000l && distance<700)
    	        			{        				
    	        				int dmg=0;
    	        				if(player.ISH==false) 
    	        				{
	    	        				if(player.killerID==0 || player.killerID == gID)
	    		        			{
	    		        				player.killerID=gID;
	    		        				player.kill_clock=now;
	    		        				if(player.assistID==gID)
	    		        				{
	    		        					player.assistID=0;
	    		        				}
	    		        			}
	    		        			else if(player.assistID==0 || player.assistID == gID)
	    		        			{
	    		        				player.assistID=gID;
	    		        				player.assist_clock=now;
	    		        			}    	 
	    	        				dmg = (int)(30000*((100-generator.nextInt(20))/100.0)*formationRCT);   
	    	        				spread = player.spread+formationPNT;if(spread<0)spread=0;
	    	        				if(player.SH>0)
	    	        				{
	    	        					if(player.SH >= dmg*player.sentinel*diminisherATK)
	    	        					{
		    	        					player.SH -= dmg*player.sentinel*diminisherATK;
		    	        					player.HP -= (dmg*spread/100);
	    	        					}
	    	        					else
	    	        					{	    	        						
	    	        						player.HP -= (player.SH*spread/100)+dmg-player.SH;;
	    	        						player.SH = 0;	    	        						
	    	        					}	    	        					
	    	        				}
	    	        				else
	    	        				{
	    	        					player.HP -= dmg;
	    	        				}
    	        				}
    	        				rocket_clock=now;	    	        				
    		                    room = Server.roomlist[rID];
    		            		if(room!=null)
    		            		{
    		            			for(Player p:room.playerlist)
    		            			{
    		            				if(p!=null)
    		            				{	    		            					
    			            				buffer.write_u8(13);
    			                        	buffer.write_u8(gID);	
    			                        	buffer.write_u8(targetID);    			                        	                    
    		    		                    buffer.write_u16(dmg/10);
    			                        	buffer.send(p.output);
    			                        	buffer.flush();		    		            					
    		            				}
    		            			}
    		            		}    		                    
    	        			}        			
    	        		}
	            		break;
	            	case 14://EMP used
	            		now = System.nanoTime();   	 
	            		if(now-emp_clock>=17000000000l)
	        			{ 
	            			emp_clock=now;
	            			if(SPDeffect<1){
	            				SPDeffect=1;
	            				if(config==1){
	            					speed = config1_sped*formationSPD*SPDeffect;
	            				}else{
	            					speed = config2_sped*formationSPD*SPDeffect;
	            				}	            					
	            			}	            			
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{	    		            					
			            				if(p.targetID==gID){p.targetID=0; p.shooting=false;p.shootingRSB=false;}		            					
		            					buffer.write_u8(14);
			                        	buffer.write_u8(gID);				                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		    		            					
		            				}
		            			}
		            		}    			            			
	        			}
	            		break;
	            	case 15://ICE ROCKET
	            		now = System.nanoTime();   	 
	            		if(now-ice_clock>=120000000000l)
	        			{
	            			ice_clock=now;
	            			player = Server.roomlist[rID].playerlist[targetID];
	            			player.SPDeffect=0;
	            			player.speed=0;
	            			player.freeze_clock=now;	            			
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(15);
			                        	buffer.write_u8(gID);	
			                        	buffer.write_u8(targetID);                    
		    		                    buffer.write_s16((short)player.x);
		    	                        buffer.write_s16((short)player.y);
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 
	        			}
	            		break;
	            	case 16://BACKUP SHIELD
	            		now = System.nanoTime();   	 
	            		if(now-BSH_clock>=120000000000l)
	        			{ 
	            			BSH_clock=now;
		            		SH +=75000;
		            		if(SH>maxSH)SH=maxSH;
		            		room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(16);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 	
	        			}
	            		break;
	            	case 17://WAR REP BOT
	            		now = System.nanoTime();   	 
	            		if(now-WRB_clock>=120000000000l)
	        			{ 
	            			WRB_clock=now+1000000000l;	
	            			WRB=10;
		            		if(SH>maxSH)SH=maxSH;
		            		room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(17);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		            		
	        			}
	            		break;
	            		
	            	case 18://ISH
	            		now = System.nanoTime();   	 
	            		if(now-ISH_clock>=30000000000l)
	        			{ 
	            			ISH=true;
	            			ISH_clock=now;            					            		
		            		room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(18);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		            		
	        			}
	            		break;
	            	case 19://SMB
	            		now = System.nanoTime();   	 
	            		if(now-SMB_clock>=30000000000l)
	        			{ 	            			
	            			SMB_clock=now;            					            		
		            		room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					int dmg=0;
		            					distance = Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
		            					if(distance<=250 && p.gID!=gID)
		            					{
		            						dmg =  p.HP*25/100;	
			    	        				spread = p.spread+formationPNT;if(spread<0)spread=0;
			    	        				if(p.SH>0)
			    	        				{
			    	        					if(p.SH >= dmg*p.sentinel*diminisherATK)
			    	        					{
				    	        					p.SH -= dmg*p.sentinel*diminisherATK;
				    	        					p.HP -= (dmg*spread/100);
			    	        					}
			    	        					else
			    	        					{	    	        						
			    	        						p.HP -= (p.SH*spread/100)+dmg-p.SH;;
			    	        						p.SH = 0;	    	        						
			    	        					}	    	        					
			    	        				}
			    	        				else
			    	        				{
			    	        					p.HP -= dmg;
			    	        				}
		            					}
		            					buffer.write_u8(19);
			                        	buffer.write_u8(gID);	
			                        	buffer.write_u16(dmg/10);
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		            		
	        			}
	            		break;
	            		
	            		
	            	case 20://SAVE INVENTORY
	            		int shpID = buffer.read_u8(input);
	            		shipID = shpID;
	            		File f20 = new File("Accounts/"+username+"/ship"+shpID+".txt");	            		
	            		f20.getParentFile().mkdirs(); 
	            		f20.createNewFile();
	            		BufferedWriter file = new BufferedWriter( new FileWriter(f20));	            		
	            		file.write(buffer.read_u8(input)+"\n");	 
	            		file.write(buffer.read_u8(input)+"\n");	 
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.write(buffer.read_u8(input)+"\n");	
	            		file.close();
	            		buffer.write_u8(20);//SAVE RESPONSE	                	
	                	buffer.send(output);
	                	buffer.flush();		                			            		
	            		break;
	            	case 21://LOAD INVENTORY
	            		int sID = buffer.read_u8(input);	            		
	            		int bbase_HP=0,bbase_sped=0,laser_slot=0,generator_slot=0,DMG_bbonus=0,SH_bbonus=0;
	            		if (sID == 1)
	            		{	            			
	            			bbase_HP=440000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;	            			
	            		}else if(sID == 2)
	            		{	            			
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;	 
	            			DMG_bbonus=5;
	            		}else if(sID == 3)
	            		{
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;	
	            			SH_bbonus=10;
	            		}else if(sID == 4)
	            		{
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;  
	            			SH_bbonus=10;
	            		}else if(sID == 5)
	            		{
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;	
	            			SH_bbonus=10;
	            		}else if(sID == 6)
	            		{
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=15;	
	            			DMG_bbonus=5;
	            		}else if(sID == 7)
	            		{
	            			bbase_HP=280000;
	            			bbase_sped=456;
	            			laser_slot=10;
	            			generator_slot=10;
	            			DMG_bbonus=5;
	            		}else if(sID == 8)
	            		{
	            			bbase_HP=360000;
	            			bbase_sped=360;
	            			laser_slot=15;
	            			generator_slot=16;
	            			DMG_bbonus=6;
	            		}
	            		File f21 = new File("Accounts/"+username+"/ship"+sID+".txt");
	            		if(f21.exists()) 
	            		{ 	                      			
	            			BufferedReader file21 = new BufferedReader(new FileReader(f21));	            			
	            			buffer.write_u8(21);//LOAD RESPONSE
	                		buffer.write_u8(1);//LOAD SUCCESSFUL
	                		buffer.write_u8(sID);
	                		buffer.write_u8(bbase_HP/2000);
	                		buffer.write_u8(bbase_sped/2);
	                		buffer.write_u8(laser_slot);
	                		buffer.write_u8(generator_slot);
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(Integer.parseInt(file21.readLine()));
	                		buffer.write_u8(DMG_bbonus);
	                		buffer.write_u8(SH_bbonus);
	                		buffer.send(output);
	                		buffer.flush();	
	            			file21.close();
	            			     			         			
	            		}
	            		else
	            		{	            			
	            			buffer.write_u8(21);//LOAD RESPONSE
	                		buffer.write_u8(2);//NOT SAVED BEFORE
	                		buffer.write_u8(sID);
	                		buffer.write_u8(bbase_HP/2000);
	                		buffer.write_u8(bbase_sped/2);
	                		buffer.write_u8(laser_slot);
	                		buffer.write_u8(generator_slot);
	                		buffer.send(output);
	                		buffer.flush();	
	            		}
	            		break;
	            	case 22://IN GAME CHANGE SHIP	            		
	            		int shID = buffer.read_u8(input);
	            		if(shID != shipID)
	            		{	            			
	            			int base_HP=0,base_sped=0,DMG_bonus=0,SH_bonus=0;
		            		if (shID == 1)
		            		{	            			
		            			base_HP=440000;
		            			base_sped=360;	            			
		            			config1_maxHP = 607200; config2_maxHP = 588720;
			            	    config1_maxSH = 833750; config2_maxSH = 634800;  
			            	    config1_DMG = 13275;    config2_DMG = 9950;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 10;  config2_hercules = 10;
			            	    config1_havoc = 0;		config2_havoc = 0; 		            	    
		            		}else if(shID == 2)
		            		{	            			
		            			base_HP=360000;
		            			base_sped=360;	            			
		            			DMG_bonus=5;
		            			config1_maxHP = 415000; config2_maxHP = 415000;
			            	    config1_maxSH = 771000; config2_maxSH = 552000;  
			            	    config1_DMG = 16230;    config2_DMG = 12630;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 0;  config2_hercules = 0;
			            	    config1_havoc = 10;		config2_havoc = 10;
		            		}else if(shID == 3)
		            		{
		            			base_HP=360000;
		            			base_sped=360;	            				 
		            			SH_bonus=10;
		            			config1_maxHP = 432000; config2_maxHP = 432000;
			            	    config1_maxSH = 1143778; config2_maxSH = 896126;  
			            	    config1_DMG = 12075;    config2_DMG = 8475;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 10;  config2_hercules = 10;
			            	    config1_havoc = 0;		config2_havoc = 0;
		            		}else if(shID == 4)
		            		{
		            			base_HP=360000;
		            			base_sped=360;	            			 
		            			SH_bonus=10;
		            			config1_maxHP = 432000; config2_maxHP = 432000;
			            	    config1_maxSH = 1143778; config2_maxSH = 896126;  
			            	    config1_DMG = 12075;    config2_DMG = 8475;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 10;  config2_hercules = 10;
			            	    config1_havoc = 0;		config2_havoc = 0;
		            		}else if(shID == 5)
		            		{
		            			base_HP=360000;
		            			base_sped=360;	            			
		            			SH_bonus=10;
		            			config1_maxHP = 432000; config2_maxHP = 432000;
			            	    config1_maxSH = 1143778; config2_maxSH = 896126;  
			            	    config1_DMG = 12075;    config2_DMG = 8475;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 10;  config2_hercules = 10;
			            	    config1_havoc = 0;		config2_havoc = 0;
		            		}else if(shID == 6)
		            		{
		            			base_HP=360000;
		            			base_sped=360;	            			
		            			DMG_bonus=5;
		            			config1_maxHP = 414000; config2_maxHP = 432000;
			            	    config1_maxSH = 495000; config2_maxSH = 780804;  
			            	    config1_DMG = 19830;    config2_DMG = 9375;
			            	    config1_sped = 360;     config2_sped = 540;
			            	    config1_hercules = 0;  config2_hercules = 10;
			            	    config1_havoc = 10;		config2_havoc = 0;
		            		}else if(shID == 7)
		            		{
		            			base_HP=280000;
		            			base_sped=456;	            			
		            			DMG_bonus=5;
		            			config1_maxHP = 322000; config2_maxHP = 336000;
			            	    config1_maxSH = 560000; config2_maxSH = 634800;  
			            	    config1_DMG = 14220;    config2_DMG = 8350;
			            	    config1_sped = 456;     config2_sped = 576;
			            	    config1_hercules = 0;  config2_hercules = 10;
			            	    config1_havoc = 10;		config2_havoc = 0;
		            		}else if(shID == 8)
		            		{
		            			base_HP=360000;
		            			base_sped=360;	            			
		            			DMG_bonus=6;
		            			config1_maxHP = 414000; config2_maxHP = 432000;
			            	    config1_maxSH = 712000; config2_maxSH = 838994;  
			            	    config1_DMG = 17430;    config2_DMG = 8925;
			            	    config1_sped = 360;     config2_sped = 552;
			            	    config1_hercules = 0;  config2_hercules = 10;
			            	    config1_havoc = 10;		config2_havoc = 0;
		            		}
		            		
		            		File f22 = new File("Accounts/"+username+"/ship"+shID+".txt");
		            		if(f22.exists()) 
		            		{ 	                      			
		            			BufferedReader file22 = new BufferedReader(new FileReader(f22));
			            		int config1_ship_LF4_HP = Integer.parseInt(file22.readLine());
			            		int config1_ship_LF4_MD = Integer.parseInt(file22.readLine());
			            		int config1_ship_LF4_PD = Integer.parseInt(file22.readLine());
			            		int config1_ship_BO2 =  Integer.parseInt(file22.readLine());      
			            		int config1_ship_G3N = Integer.parseInt(file22.readLine());
			            		int config1_drone_LF4_HP = Integer.parseInt(file22.readLine());
			            		int config1_drone_LF4_MD = Integer.parseInt(file22.readLine());
			            		int config1_drone_LF4_PD = Integer.parseInt(file22.readLine());
			            		int config1_drone_BO2 =  Integer.parseInt(file22.readLine());       
			            		int config1_drone_havoc = Integer.parseInt(file22.readLine());
			            		int config1_drone_hercules = Integer.parseInt(file22.readLine());
			            		int config2_ship_LF4_HP = Integer.parseInt(file22.readLine());
			            		int config2_ship_LF4_MD = Integer.parseInt(file22.readLine());
			            		int config2_ship_LF4_PD = Integer.parseInt(file22.readLine());
			            		int config2_ship_BO2 =  Integer.parseInt(file22.readLine());
			            		int config2_ship_G3N = Integer.parseInt(file22.readLine());
			            		int config2_drone_LF4_HP = Integer.parseInt(file22.readLine());
			            		int config2_drone_LF4_MD = Integer.parseInt(file22.readLine());
			            		int config2_drone_LF4_PD = Integer.parseInt(file22.readLine());
			            		int config2_drone_BO2 = Integer.parseInt(file22.readLine());
			            		int config2_drone_havoc = Integer.parseInt(file22.readLine());
			            		int config2_drone_hercules = Integer.parseInt(file22.readLine());
			            		file22.close();
			            		
			            		config1_maxHP = base_HP*((config1_drone_hercules*2.0+100)/100)*((config1_drone_havoc*1.5+100)/100)*(((config1_ship_LF4_HP+config1_drone_LF4_HP)*0.5+100)/100);
			            	    config1_maxSH = ((config1_ship_BO2*33000)+(config1_drone_BO2*46000))*((config1_drone_hercules*1.5+100)/100)*(((config1_ship_LF4_PD+config1_drone_LF4_PD)*1+100)/100)*((SH_bonus*1.0+100)/100);   
			            	    config1_DMG = ((config1_ship_LF4_HP*410)+(config1_ship_LF4_MD*435)+(config1_ship_LF4_PD*385)+(config1_drone_LF4_HP*475)+(config1_drone_LF4_MD*500)+(config1_drone_LF4_PD*450))*((config1_drone_havoc*2.0+100)/100)*((DMG_bonus*1.0+100)/100);
			            	    config1_sped = base_sped+config1_ship_G3N*12;  
			            	    config1_hercules = config1_drone_hercules;
			            	    config1_havoc = config1_drone_havoc;
			            	    
			            	    config2_maxHP = base_HP*((config2_drone_hercules*2.0+100)/100)*((config2_drone_havoc*1.5+100)/100)*(((config2_ship_LF4_HP+config2_drone_LF4_HP)*0.5+100)/100);
			            	    config2_maxSH = ((config2_ship_BO2*33000)+(config2_drone_BO2*46000))*((config2_drone_hercules*1.5+100)/100)*(((config2_ship_LF4_PD+config2_drone_LF4_PD)*1.0+100)/100)*((SH_bonus*1.0+100)/100); 
			            	    config2_DMG = ((config2_ship_LF4_HP*410)+(config2_ship_LF4_MD*435)+(config2_ship_LF4_PD*385)+(config2_drone_LF4_HP*475)+(config2_drone_LF4_MD*500)+(config2_drone_LF4_PD*450))*((config2_drone_havoc*2.0+100)/100)*((DMG_bonus*1.0+100)/100);
			            	    config2_sped = base_sped+config2_ship_G3N*12;
			            	    config2_hercules = config2_drone_hercules;
			            	    config2_havoc = config2_drone_havoc;			            	    
		            		}		            		
	            		}
	            		if(config==1)
	            		{
		            		maxHP = (int)(config1_maxHP*formationHP);		            		
		            		maxSH = (int)(config1_maxSH*formationSH);			            		
		            		damage = (int)(config1_DMG*formationDMG);	
		            		speed = config1_sped*formationSPD;	
		            		havoc = config1_havoc;
		            		hercules = config1_hercules;
	            		}
	            		else
	            		{
	            			maxHP = (int)(config2_maxHP*formationHP);		            		
		            		maxSH = (int)(config2_maxSH*formationSH);			            		
		            		damage = (int)(config2_DMG*formationDMG);	
		            		speed = config2_sped*formationSPD;	
		            		havoc = config2_havoc;
		            		hercules = config2_hercules;
	            		}
	            		
	            		HP = maxHP;
	            		SH = maxSH;
	            		config1_SH = config1_maxSH*formationSH;
	            		config2_SH = config2_maxSH*formationSH;            		
	            		
	            		x = generator.nextInt(10000);
	            		y = generator.nextInt(7000);
	            		move_x = x;
	            		move_y = y;
	            		
	            		shipID = shID;
	            		buffer.write_u8(22);//LOAD RESPONSE                 		
	                	buffer.write_s16((short)x);
	                	buffer.write_s16((short)y);	                		
	                	buffer.send(output);
	                	buffer.flush();	               	
	            			 			
	            		break;
	            	case 25://CHANGE CONFIG
	            		now = System.nanoTime();	    	        			
	        			if(now-config_clock>=5000000000l)
	        			{	        				
		            		config_clock=now;
	        				if(config==2)
		            		{
		            			config2_SH=SH;		            			
		            			maxHP = (int)(config1_maxHP*formationHP);	            		
			            		maxSH = (int)(config1_maxSH*formationSH);		            		
			            		damage = (int)(config1_DMG*formationDMG);
			            		speed = config1_sped*formationSPD*SPDeffect;
			            		havoc = config1_havoc;
			            		hercules = config1_hercules;
			            		SH = (int)config1_SH; 
			            		if(SH>maxSH)SH=maxSH;
			            		if(HP>maxHP)HP=maxHP;
			            		config=1;
		            		}
		            		else
		            		{
		            			config1_SH=SH;		            			
		            			maxHP = (int)(config2_maxHP*formationHP);	            		
			            		maxSH = (int)(config2_maxSH*formationSH);		            		
			            		damage = (int)(config2_DMG*formationDMG);
			            		speed = config2_sped*formationSPD*SPDeffect;
			            		havoc = config2_havoc;
			            		hercules = config2_hercules;
			            		SH = (int)config2_SH; 
			            		if(SH>maxSH)SH=maxSH;
			            		if(HP>maxHP)HP=maxHP;
			            		config=2;
		            		}
	        				buffer.write_u8(25); 	                	         		
		                	buffer.send(output);
		                	buffer.flush();	  
	        			}
	            		break;
	            	case 26://CHANGE DRONE FORMATION
	            		now = System.nanoTime();
	            		int dfID = buffer.read_u8(input);
	            		if(dfID!=dformation)
	            		{
		            		if(now-formation_clock>=3000000000l)
		            		{
		            			switch(dfID)
		            			{
		            				case 1://HEART 
		            					formationHP=1.20;
		            					formationSH=1.10;
		            					formationDMG=0.95;
		            					formationSPD=1;
		            					formationRCT=1;
		            					formationPNT=0;
		            					spread =8;
		            					break;
		            				case 2://RING
		            					formationHP=1;
		            					formationSH=2.20;
		            					formationDMG=0.75;
		            					formationSPD=0.95;
		            					formationRCT=0.75;
		            					formationPNT=0;
		            					spread =8;
		            					break;
		            				case 3://DIAMOND
		            					formationHP=0.7;
		            					formationSH=1;
		            					formationDMG=1;
		            					formationSPD=1;
		            					formationRCT=1;
		            					formationPNT=0;
		            					spread =8;
		            					break;
		            				case 4://MOTH
		            					formationHP=1.20;
		            					formationSH=1;
		            					formationDMG=1;
		            					formationSPD=1;
		            					formationRCT=1;
		            					formationPNT=20;
		            					spread =8;
		            					break;
		            				case 5://DRILL
		            					formationHP=1;
		            					formationSH=0.75;
		            					formationDMG=1.2;
		            					formationSPD=0.95;
		            					formationRCT=1;
		            					formationPNT=0;
		            					spread =13;
		            					break;
		            				case 6://WHEEL
		            					formationHP=1;
		            					formationSH=1;
		            					formationDMG=0.8;
		            					formationSPD=1.05;
		            					formationRCT=1;
		            					formationPNT=0;
		            					spread =8;
		            					break;
		            				case 7://CRAB
		            					formationHP=1;
		            					formationSH=1;
		            					formationDMG=1;
		            					formationSPD=0.8;
		            					formationRCT=1;
		            					formationPNT=0;
		            					spread =-12;
		            					break;
		            				case 8://CHEVRON
		            					formationHP=0.8;
		            					formationSH=1;
		            					formationDMG=1;
		            					formationSPD=1;
		            					formationRCT=1.5;
		            					formationPNT=0;
		            					spread =8;
		            					break;
		            			}
		            			if(config==1)
			            		{
		            				maxHP = (int)(config1_maxHP*formationHP);	            		
				            		maxSH = (int)(config1_maxSH*formationSH);		            		
				            		damage = (int)(config1_DMG*formationDMG);
				            		speed = config1_sped*formationSPD*SPDeffect;	
				            		havoc = config1_havoc;
				            		hercules = config1_hercules;
				            		if(SH>maxSH)SH=maxSH;
				            		if(HP>maxHP)HP=maxHP;
			            		}
			            		else
			            		{
			            			maxHP = (int)(config2_maxHP*formationHP);	            		
				            		maxSH = (int)(config2_maxSH*formationSH);		            		
				            		damage = (int)(config2_DMG*formationDMG);
				            		speed = config2_sped*formationSPD*SPDeffect;	
				            		havoc = config2_havoc;
				            		hercules = config2_hercules;
				            		if(SH>maxSH)SH=maxSH;
				            		if(HP>maxHP)HP=maxHP;
			            		}
		            			dformation = dfID;
		            			formation_clock=now;	
		            			buffer.write_u8(26); 	                	         		
			                	buffer.send(output);
			                	buffer.flush();	  
		            		}
	            		}
	            		break;
	            		
	            	case 27://CLOACK CPU
	            		now = System.nanoTime();   	 
	            		if(now-CLK_clock>=40000000000l)
	        			{ 	            			
	            			CLK_clock=now;            					            		
		            		room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(27);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		            		
	        			}
	            		break;
	            	case 29://LIGHTNING SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==7 && now-lightning_clock>=90000000000l)
	        			{ 	            			
	            			SPDeffect=1.3;
            				if(config==1){
            					speed = config1_sped*formationSPD*SPDeffect;
            				}else{
            					speed = config2_sped*formationSPD*SPDeffect;
            				}	 
	            			lightning=true;
	            			lightning_clock=now;      				           				
		            		buffer.write_u8(29);			                			                        	
			                buffer.send(output);
			                buffer.flush();	   						            		
	        			}
	            		break;
	            	case 30://SOLACE SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==4 && now-solace_clock>=90000000000l)
	        			{ 	            			
	            			HP+=maxHP/2;
	            			if(HP>maxHP)HP=maxHP;
	            			solace_clock=now;      				           				
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(30);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		     						            		
	        			}
	            		break;
	            	case 31://SENTINEL SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==5 && now-sentinel_clock>=30000000000l)
	        			{ 	            			
	            			sentinel=0.7;
	            			sentinel_clock=now;      				           				
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(31);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		     						            		
	        			}
	            		break;
	            	case 32://SPECTRUM SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==3 && now-spectrum_clock>=120000000000l)
	        			{ 	            			
	            			spectrumATK=0.5;
	            			spectrumDEF=0.2;
	            			spectrum_clock=now;      				           				
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(32);
			                        	buffer.write_u8(gID);			                        	
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		     						            		
	        			}
	            		break;
	            		
	            	case 33://DIMINISHER SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==6 && now-diminisher_clock>=120000000000l && targetID!=0)
	        			{ 	            			
	            			diminisherID = targetID;
	            			diminisherATK=1.5;
	            			diminisher_clock=now;      				           				
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(33);
			                        	buffer.write_u8(gID);
			                        	buffer.write_u8(targetID);		
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		     						            		
	        			}
	            		break;
	            		
	            	case 34://VENOM SKILL
	            		now = System.nanoTime();   	 
	            		if(shipID==2 && now-venom_clock>=120000000000l && targetID!=0)
	        			{ 	            			
	            			venomID = targetID;	            			
	            			venom_clock=now;      				           				
	            			room = Server.roomlist[rID];
		            		if(room!=null)
		            		{
		            			for(Player p:room.playerlist)
		            			{
		            				if(p!=null)
		            				{			            				
		            					buffer.write_u8(34);
			                        	buffer.write_u8(gID);
			                        	buffer.write_u8(targetID);		
			                        	buffer.send(p.output);
			                        	buffer.flush();		        		            					
		            				}
		            			}
		            		} 		     						            		
	        			}
	            		break;
	            	
	            	
	            	
	            		
	            }	
			} catch (Exception e) {//IF ANY ERROR OCCURS DISCONNECT FROM PLAYER
				connected = false;				
				e.printStackTrace();
			}		
		}
		System.out.println("Player disconnected");
		Server.online -= 1;
		Server.playerlist[ID] = null;	
		room = Server.roomlist[rID];	            		
		if(room!=null)
		{
			for(Player p:room.playerlist)
			{
				if(p!=null)
				{
					if(p.gID != gID)
					{
    					buffer.write_u8(7);
                		buffer.write_u8(gID);		                        		
                		buffer.send(p.output);
                		buffer.flush();	
					}
				}
			}
			room.playerlist[gID] = null;
			room.currentPlayer -= 1;		
		}	            		
		rID = 0;
		gID = 0;
		try{
			output.close();
			input.close();
			connection.close();			
		}catch(IOException ioe){ioe.printStackTrace();}
	}	
}
