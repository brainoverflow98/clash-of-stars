import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Random;

public class Game extends Thread{	
	public String name;	
	public int roomID,type,maxPlayer,currentPlayer,time,currentRound,maxRound;	
	public Player[] playerlist;
	TCPBuffer bufferTCP = new TCPBuffer();
	public Game(String nm, int ty, int mPlayer, int mRound,int t)
	{		
		name = nm;
		type = ty;
		maxPlayer = mPlayer;
		playerlist = new Player[mPlayer];
		currentPlayer = 0;
		maxRound = mRound;
		currentRound = 0;
		time = t;
		for(int i=0;i<playerlist.length;i++)
		{
			playerlist[i]=null;
		}
	}
	@Override
	public void run()
	{	   
	   long lastLoopTime = System.nanoTime();
	   final int TARGET_FPS = 60;
	   final long OPTIMAL_TIME = 1000000000 / TARGET_FPS; 	   
	   @SuppressWarnings("unused")
	int fps=0;
	   long now,updateLength;
	   long lastFpsTime=0;
	   double delta;
	   boolean running = true;
	   //||||||||||||||||||||||||||||||||||||
	   UDPBuffer buffer = new UDPBuffer();	   
	   DatagramPacket sendPacket;	
	   byte [] data=null;
	   boolean send1=false,send2=false,send3=false,send4=false,send5=false,send6 = false;
	   double dx,dy,speed,distance;
	   double dis,sin,cos;
	   Player player;
	   Random generator = new Random(); 	 
	   int spread;
	   while (running)
	   {	   
	      now = System.nanoTime();
	      updateLength = now - lastLoopTime;
	      lastLoopTime = now;
	      delta = updateLength / ((double)OPTIMAL_TIME);//multiply every value that related with time like movement, timer...

	      // update the frame counter
	      lastFpsTime += updateLength;
	      fps++;
	      
	      // update our FPS counter 	      
	      if (lastFpsTime >= 1000000000)
	      {
	         //System.out.println("(FPS: "+fps+")");
	         lastFpsTime = 0;
	         fps = 0;
	      }	      
	      
	      //doGameUpdates(delta);	      
	      for (Player p:playerlist)//UPDATE EACH PLAYER
          {                        	
          	if (p != null)
          	{
          		now = System.nanoTime();
          		if(p.HP<=0)//IF PLAYER IS DEAD TELL EVERYONE THIS PLAYER IS DEAD ADN WHO KILLED HIM
          		{
          			if(p.HP!=-1000000)
          			{
	          			send4 = true;	 
	        			bufferTCP.write_u8(24);
	        			bufferTCP.write_u8(p.gID);
	        			bufferTCP.write_u8(p.killerID);	
	        			bufferTCP.write_u8(p.assistID);
	        			p.x = -900;
	        			p.y = -900;
	        			p.move_x = -900;
	        			p.move_y = -900;
	        			p.targetID = 0;
	        			p.shooting = false;    
	        			p.HP=-1000000;
          			}
          		}//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
          		else
          		{	          		
          			//RESET THE KILLER1 AND KILLER2 ID's THEY ARE NOT SHOOTING
          			if(p.killerID!=0 && now-p.kill_clock>=10000000000l)
	          		{
	          			p.killerID=0;
	          		}
	          		if(p.assistID!=0 && now-p.assist_clock>=5000000000l)
	          		{
	          			p.assistID=0;
	          		}
	          		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	          		
	          		//START REPEARINNG THE SHIP IF NOT IN BATTLE
	          		if(p.killerID==0 && now-p.rep_clock>=1000000000l)	          		
	          		{
	          			if(p.HP!=p.maxHP || p.SH!=p.maxSH)
		          		{
		          			p.HP += p.maxHP/60;
		          			p.SH += p.maxSH/20;
		          			if(p.HP > p.maxHP)p.HP=p.maxHP;
			          		if(p.SH > p.maxSH)p.SH=p.maxSH;
			          		p.rep_clock = now;
		          		}
	          		}
	          		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	          		
	          		//IF WAR REP BOT IS ACTIVE REPAIR THE SHIP EACH SECOND
	          		if(p.WRB>0 && now-p.WRB_clock>=1000000000l)	          		
	          		{
	          			p.HP += 10000;		          			
		          		if(p.HP > p.maxHP)p.HP=p.maxHP;			          		
			          	p.WRB-= 1;
			          	p.WRB_clock=now;
		          		
	          		}
	          		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	          		
	          		//IF HP AND SHIELD HAS CHANGED SEND THE UPDATE	          		
          			if(p.HP != p.prevHP || p.SH != p.prevSH)
	          		{
	          			send1 = true;
	          			buffer.write_u8(12);
	        			buffer.write_u8(p.gID);	                                      
	                    buffer.write_u16(p.HP/100);
	                    buffer.write_u16(p.SH/100);               
	                    p.prevSH=p.SH;
	                    p.prevHP=p.HP;
	          		}
          			//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
          			
          			//IF SPEED IS NOT ZERO(NOT FREEZED) KEEP MOVING THE SHIP TO SELECTED POSITION
          			//OTHERWISE CHECK IF SHOULD END THE FREEZE STATE
          			if(p.speed>0)
          			{
	          			if(p.move_x!=p.x || p.move_y!=p.y)
	          			{
		          			dx=p.move_x-p.x;                    	
			        		dy=p.move_y-p.y; 
			        		dis = Math.sqrt((dx)*(dx)+(dy)*(dy));
			        		sin = (dy)/dis;
			          		cos = (dx)/dis;	
			          		speed = p.speed/50-2;
			        		if(dis>speed/2)		
			        		{		      	  		
			        			p.x+=speed*cos*delta;
			        			p.y+=speed*sin*delta;      	
			        			dis = Math.sqrt((p.move_x-p.x)*(p.move_x-p.x)+(p.move_y-p.y)*(p.move_y-p.y));		        			
			        		}
			        		else
			        		{
			        			p.move_x=p.x;
			        			p.move_y=p.y;
			        		}
	          			}
          			}else if(now-p.freeze_clock>=3000000000l){
          				p.SPDeffect=1;          				
	    				if(p.config==1){
	    					p.speed = p.config1_sped*p.formationSPD*p.SPDeffect;
	    				}else{
	    					p.speed = p.config2_sped*p.formationSPD*p.SPDeffect;
	    				}	 
    				}
          			//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
          			//CHECK FOR RSB SHOOTING AND SET THE KILLER ID OF THE TARGET TO YOUR ID
          			//IF TARGET HAS ACTIVE ISH SET DMG TO 0
	        		player = playerlist[p.targetID];	        		
	        		if(p.shootingRSB==true && player!=null)
	        		{     			
	        			distance = Math.sqrt((p.x-player.x)*(p.x-player.x)+(p.y-player.y)*(p.y-player.y));
	        			if(now-p.x6_clock>=3000000000l && distance<700)
    	        		{        				
	        				int dmg=0;
	        				if(player.ISH==false)
	        				{
		        				if(player.killerID==0 || player.killerID == p.gID)
			        			{
			        				player.killerID=p.gID;
			        				player.kill_clock=now;
			        				if(player.assistID==p.gID)
			        				{
			        					player.assistID=0;
			        				}
			        			}
			        			else if(player.assistID==0 || player.assistID == p.gID)
			        			{
			        				player.assistID=p.gID;
			        				player.assist_clock=now;
			        			}
		        				dmg =  (int)( 6*p.damage*(100-generator.nextInt(10))/100*player.spectrumDEF*p.spectrumATK);	
	    	        			spread = player.spread+p.formationPNT;if(spread<0)spread=0;
	    	        			if(player.SH>0)
	    	        			{
	    	        				if(player.SH >= dmg*player.sentinel*p.diminisherATK)
	    	        				{
		    	        				player.SH -= dmg*player.sentinel*p.diminisherATK;
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
    	        			p.x6_clock=now; 
    	        			send5=true;
    			           	bufferTCP.write_u8(11);
    			           	bufferTCP.write_u8(p.gID);	
    			           	bufferTCP.write_u8(p.targetID);
    			           	bufferTCP.write_u8(6);                    
    			           	bufferTCP.write_u16(dmg/6);    			           		           			    		            			    		                    
    	        		}       		
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK FOR X4 AND SAB SHOOTING AND SET THE KILLER ID OF THE TARGET TO YOUR ID
	        		//IF TARGET HAS ACTIVE ISH SET DMG TO 0
	        		//ALSO FIRE THE ROCKET WITH EACH LASER SHOT
	        		if(p.shooting==true && player!=null)
	        		{	        	        			
	        			p.shootingRSB=false;
	        			distance = Math.sqrt((p.x-player.x)*(p.x-player.x)+(p.y-player.y)*(p.y-player.y));
	        			if(now-p.x4_clock>=1000000000l && distance<700)
	        			{   
	        				send2 = true;
	        				int dmg=0;
	        				int rdmg=0;
	        				if(player.ISH==false)
	        				{
	        					if(player.killerID==0 || player.killerID == p.gID)
			        			{
			        				player.killerID=p.gID;
			        				player.kill_clock=now;
			        				if(player.assistID==p.gID)
			        				{
			        					player.assistID=0;
			        				}
			        			}
			        			else if(player.assistID==0 || player.assistID == p.gID)
			        			{
			        				player.assistID=p.gID;
			        				player.assist_clock=now;
			        			}
	        					if(p.ammo==4)//SHOOT X4
		        				{
			        				dmg = (int)( 4*p.damage*(100-generator.nextInt(10))/100*player.spectrumDEF*p.spectrumATK);	 
			        				spread = player.spread+p.formationPNT;if(spread<0)spread=0;
			        				if(player.SH>0)
			        				{
			        					if(player.SH >= dmg*player.sentinel*p.diminisherATK)
			        					{
		    	        					player.SH -= dmg*player.sentinel*p.diminisherATK;
		    	        					player.HP -= (spread*dmg/100.0);
			        					}
			        					else
			        					{	        						 
			        						player.HP -= (spread*player.SH/100.0)+dmg-player.SH;
			        						player.SH = 0;	    	        						
			        					}
			        				}
			        				else
			        				{
			        					player.HP -= dmg;
			        				}
			        				dmg = dmg/4;
		        				}
		        				else//SHOOT SAB
		        				{
		        					dmg =  (int)( 2*p.damage*(100-generator.nextInt(10))/100*player.spectrumDEF*p.spectrumATK);
		        					if(player.SH>0)
			        				{
			        					if(player.SH >= dmg*player.sentinel*p.diminisherATK)
			        					{
		    	        					player.SH -= dmg*player.sentinel*p.diminisherATK;
		    	        					if(p.SH+dmg<p.maxSH)
		    	        					{
		    	        						p.SH += dmg;
		    	        					}
		    	        					else
		    	        					{
		    	        						p.SH = p.maxSH;
		    	        					}
			        					}
			        					else
			        					{	        						 
			        						dmg = player.SH;
			        						player.SH = 0;
		    	        					if(p.SH+dmg<p.maxSH)
		    	        					{
		    	        						p.SH += dmg;
		    	        					}
		    	        					else
		    	        					{
		    	        						p.SH = p.maxSH;
		    	        					}
			        					}		        					
			        				}
			        				else
			        				{
			        					dmg = 0;
			        				}
		        					dmg = dmg/2;
		        				}
		        				 
		        				rdmg = (int)(10000*((100-generator.nextInt(15))/100.0)*p.formationRCT);   
		        				spread = player.spread+p.formationPNT;if(spread<0)spread=0;
		        				if(player.SH>0)
		        				{
		        					if(player.SH >= rdmg*player.sentinel)
		        					{
	    	        					player.SH -= rdmg*player.sentinel;
	    	        					player.HP -= (spread*rdmg/100.0);
		        					}
		        					else
		        					{	        						 
		        						player.HP -= (player.SH*spread/100.0)+rdmg-player.SH;
		        						player.SH = 0;	    	        						
		        					}
		        				}
		        				else
		        				{
		        					player.HP -= rdmg;
		        				}		        				
	        				}	        				
	        				
	        				p.x4_clock=now;
	        				buffer.write_u8(11);
		        			buffer.write_u8(p.gID);
		                    buffer.write_u8(p.targetID);
		                    buffer.write_u8(p.ammo);                    
		                    buffer.write_u16(dmg); 
		                    buffer.write_u16(rdmg);                    
	        			}        			
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK IF SHOULD END ISH
	        		if(p.ISH==true){
	        			if(now-p.ISH_clock>=3000000000l){p.ISH=false;}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK IF SHOULD END LIGHTNING SKILL
	        		if(p.lightning==true){
	        			if(now-p.lightning_clock>=5000000000l){
	        				p.lightning=false;
	        				p.SPDeffect=1;
            				if(p.config==1){
            					p.speed = p.config1_sped*p.formationSPD*p.SPDeffect;
            				}else{
            					p.speed = p.config2_sped*p.formationSPD*p.SPDeffect;
            				}	 
	        			}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK IF SHOULD END SENTINEL EFFECT
	        		if(p.sentinel==0.7){
	        			if(now-p.sentinel_clock>=10000000000l){p.sentinel=1;}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK IF SHOULD END SPECTRUM EFFECT
	        		if(p.spectrumATK==0.5){
	        			if(now-p.spectrum_clock>=30000000000l){p.spectrumATK=1; p.spectrumDEF=1;}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK IF SHOULD END DIMINISHER EFFECT
	        		if(p.diminisherID!=0){
	        			if(now-p.diminisher_clock>=15000000000l || p.diminisherID!=p.targetID){
	        				p.diminisherID=0;p.diminisherATK=1; 
	        				p.SH-=p.SH*3/10; if(p.SH<0)p.SH=0;
	        						}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//CHECK VENOM EFFECT
	        		if(p.venomID!=0){
	        			if(now-p.venom_clock>=10500000000l || p.venomID!=p.targetID){
	        				p.venomID=0;	        				
	        			}else{
	        				if(now-p.venom_second>=1000000000l)
	        				{
	        					player = playerlist[p.venomID];
	        					if(player.ISH==false)
	        					{		        					
			        				player.HP-=10000;
	        					}
		        				p.venom_second=now;
	        				}
	        			}
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//IF HAS AN DRONE FORMATION EFFECT APPLY IT EVERY SECOND	        		
	        		if(now-p.effect_clock>=1000000000l)
	        		{
		        		if(p.dformation==3)
		        		{
		        			if(p.SH!=p.maxSH)
		        			{
		        				if(p.maxSH/100>5000)
		        				{
		        					p.SH += 5000;
		        				}
		        				else
		        				{
		        					p.SH += p.maxSH/100;
		        				}
		        				if(p.SH>p.maxSH)p.SH=p.maxSH;
		        			}
		        		} else if(p.dformation==4)
		        		{
		        			if(p.SH!=0)
		        			{
		        				p.SH -= p.maxSH/100;
		        				if(p.SH<0)p.SH=0;
		        			}
		        			//p.rep_clock=now+1000000000l;
		        		} else if(p.dformation==6)
		        		{
		        			if(p.SH!=0)
		        			{
		        				p.SH -= p.maxSH/20;
		        				if(p.SH<0)p.SH=0;
		        			}
		        			//p.rep_clock=now+1000000000l;
		        		}
		        		p.effect_clock=now;
	        		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//IF THERE IS A CHANGE IN THESE CRIDENTIALS SEND THE UPDATE
	        		if(p.config != p.prevconfig || p.shipID != p.prevshipID || p.prevdformation!=p.dformation)
	          		{	          			
	        			send3 = true;	 
	        			bufferTCP.write_u8(23);
            			bufferTCP.write_u8(p.gID);
            			bufferTCP.write_u8(p.shipID);
            			bufferTCP.write_u16(p.maxHP/100);	
            			bufferTCP.write_u16(p.maxSH/100);
            			bufferTCP.write_u8(p.damage/100);                    
            			bufferTCP.write_u16((int)p.speed);
            			bufferTCP.write_u8(p.havoc);
            			bufferTCP.write_u8(p.hercules);	  
            			bufferTCP.write_u8(p.dformation);	 
	                    p.prevconfig = p.config;
	                    p.prevshipID = p.shipID;
	                    p.prevdformation = p.dformation;
	                    p.prevspeed=p.speed;
	          		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
	        		
	        		//IF THERE IS CHANGE IN SPEED SEND IT TO PLAYERS 
	        		if(p.prevspeed != p.speed)
	          		{	          			
	        			send6 = true;	 
	        			bufferTCP.write_u8(28);
            			bufferTCP.write_u8(p.gID);            			           			                 
            			bufferTCP.write_u16((int)p.speed);            			
	                    p.prevspeed=p.speed;
	          		}
	        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
          		}
          		
          		//LOOP THROUGH OTHER PLAYERS AND UPDATE IF THERE IS SOMETHING YOU SHOULD SEND
          		data = buffer.flush();
          		for(Player pl:playerlist)
                {
                	if(pl!=null)
                	{
                		try {               			
                		if(send1==true || send2==true)
                    	{
                			sendPacket = new DatagramPacket(data,data.length, pl.IP, pl.port);                        
    						UDPServer.serverSocket.send(sendPacket);    						
                    	}                     		         
                		if(send3==true || send5==true || send6==true)
                		{               			
                			bufferTCP.send(pl.output);                  			               			
                		}
                		if(send4==true)
                		{               			
                			bufferTCP.send(pl.output);       
                			if(pl.targetID==p.gID)
                			{
                				pl.targetID=0;
                				pl.shooting=false;
                			}
                		}
						} catch (IOException e) {e.printStackTrace();}  
                	}
                }
          		send1=false;
        		send2=false;
        		send3=false; 
        		send4=false;
        		send5=false;
        		send6=false;
        		bufferTCP.flush();
        		//||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
          	}
          }    
	      
	      // we want each frame to take 10 milliseconds, to do this
	      // we've recorded when we started the frame. We add 10 milliseconds
	      // to this and then factor in the current time to give 
	      // us our final value to wait for
	      // remember this is in ms, whereas our lastLoopTime etc. vars are in ns.
	      try {
	    	  long time = (lastLoopTime-System.nanoTime() + OPTIMAL_TIME)/1000000;
	    	  if (time < 0) time = -time;
	    	  Thread.sleep(time);
	      	  } catch (InterruptedException e) {			
	      		  e.printStackTrace();
	      	  }
	   }
	}
	public int assignID(Player p)
	{
		for(int i=1;i<playerlist.length;i++)
		{
			if(playerlist[i]==null)
			{
				playerlist[i]=p;
				p.comp=i;//ASSIGN THE COMPANY IDENTICAL FOR DEATHMATCH
				currentPlayer += 1;
				System.out.println("Room: "+currentPlayer);
				return i;
			}
		}
		return 0;
	}	
	
}