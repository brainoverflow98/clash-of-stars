var buffer = argument[0];
var size = argument[1];

while(size>0)
{    
    var msgID = buffer_read(buffer,buffer_u8);
    switch(msgID)
    {
           
        case 1://CONNECT TO THE CLIENT             
            var pID = buffer_read(buffer,buffer_u8);            
            global.myID = pID;                     
            //global.player_list[pID] = player;                    
        break;
        case 2://REGISTER             
            var res = buffer_read(buffer,buffer_u8);  
            if(res==1)
            {
                room = rm_lobby;
            }
            else
            {
                log = instance_create(0,0,obj_splash);
                log.str = "Change username";  
            }
        break;
        case 3://LOGIN            
            var res = buffer_read(buffer,buffer_u8);  
            if(res==1)
            {
                room = rm_lobby;
            }
            else if(res==2)
            {
                log = instance_create(0,0,obj_splash);
                log.str = "Wrong password";  
            }
            else if(res==3)
            {
                log = instance_create(0,0,obj_splash);
                log.str = "Wrong username";               
            }
        break;
        case 4://JOIN TO THE ROOM REQUEST            
            buffer_seek(global.buffer,buffer_seek_start,0);
            buffer_write(global.buffer,buffer_u8,5);
            buffer_write(global.buffer,buffer_u8,global.rID);
            network_send_raw(global.socket,global.buffer,buffer_tell(global.buffer));        
        break;
        case 5://JOIN TO THE ROOM RESPONSE            
            window_set_cursor(cr_default);
            var res = buffer_read(buffer,buffer_u8);  
            if(res==1)
            {
                var ggID = buffer_read(buffer,buffer_u8);             
                global.gID = ggID;
                global.comp = ggID;                        
                global.room_players[ggID] = instance_create(100,100,obj_player); 
                room = rm_game;                
            }
            else if(res==2)
            {
                lg= instance_create(0,0,obj_splash);
                lg.str = "Room is full" ;
            }
            else if(res==3)
            {
                lg= instance_create(0,0,obj_splash);
                lg.str = "Room is deleted" ;
            }        
        break;
        case 6://NEW COMING PLAYER            
            var player = instance_create(100,100,obj_other);
            var ggID = buffer_read(buffer,buffer_u8);             
            player.gID =  ggID;
            player.username = read_string(buffer);   
            player.shipID = buffer_read(buffer,buffer_u8); 
            player.maxHP=buffer_read(buffer,buffer_u16)*100; 
            player.maxSH=buffer_read(buffer,buffer_u16)*100; 
            player.DMG=buffer_read(buffer,buffer_u8)*100; 
            player.sped=buffer_read(buffer,buffer_u16); 
            player.havoc=buffer_read(buffer,buffer_u8); 
            player.hercules=buffer_read(buffer,buffer_u8); 
            player.drone_formation=buffer_read(buffer,buffer_u8);       
            global.room_players[ggID] = player;   
            switch(player.shipID)
            {
                case 1: player.sprite_index=spr_ship1;  break;
                case 2: player.sprite_index=spr_ship2;  break;
                case 3: player.sprite_index=spr_ship3;  break;
                case 4: player.sprite_index=spr_ship4;  break;
                case 5: player.sprite_index=spr_ship5;  break;
                case 6: player.sprite_index=spr_ship6;  break;
                case 7: player.sprite_index=spr_ship7;  break;
                case 8: player.sprite_index=spr_ship8;  break;            
            }  
            log = instance_create(0,0,obj_log);
            log.str = player.username+" joined the game";      
        break;
        case 7://LEAVE THE GAME ROOM
            ggID = buffer_read(buffer,buffer_u8);            
            player = global.room_players[ggID]; 
            if(instance_exists(player))  
            {             
                log = instance_create(0,0,obj_log);
                log.str = player.username+" left the game";
                with(player){instance_destroy()} 
            }          
            global.room_players[ggID]=0;            
        break;
        case 8://Update player movements including yours    
            var ggID = buffer_read(buffer,buffer_u8);
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                player.move_x = buffer_read(buffer,buffer_s16);
                player.move_y = buffer_read(buffer,buffer_s16);        
                player.x = buffer_read(buffer,buffer_s16);
                player.y = buffer_read(buffer,buffer_s16);             
                if(player.sped>0)              
                with(player){move_towards_point(move_x,move_y,sped/50-2);}
            }
            else{buffer_seek(buffer,buffer_seek_relative,8);}        
        break;  
        case 9://SELECT A PLAYER FOR SHOOTING
             var ggID = buffer_read(buffer,buffer_u8);
             var ttargetID = buffer_read(buffer,buffer_u8);
             var player = global.room_players[ggID]; 
             var ttargetObj = global.room_players[ttargetID]; 
             if(instance_exists(player) and instance_exists(ttargetObj))  
             {
                 player.targetID = ttargetID;
                 player.targetObj = ttargetObj;
             }
        break;
        case 10://DESELECT A PLAYER 
             var ggID = buffer_read(buffer,buffer_u8);         
             var player = global.room_players[ggID];
             if(instance_exists(player))  
             {          
                 player.targetID = 0;
                 player.targetObj = 0;
             }
        break;
        case 11://SOMEBODY IS SHOOTING LASERS    
            var ggID = buffer_read(buffer,buffer_u8); 
            if(ggID!=0)
            {
                var ttargetID = buffer_read(buffer,buffer_u8); 
                var ammo = buffer_read(buffer,buffer_u8); 
                var ddmg = buffer_read(buffer,buffer_u16); 
                var player = global.room_players[ggID];  
                var ttargetObj = global.room_players[ttargetID]; 
                if(instance_exists(player) and instance_exists(ttargetObj))  
                {
                    dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                    if(dis<1000)
                    {
                        vol=(200/round(dis));                
                        if(ammo==6)
                        {
                            sprite = spr_laser_x66;            
                            player.alarm[2]=room_speed/6;
                            ddmg = ddmg*6;
                            if(ggID == global.gID)
                            {
                                with(obj_UIbox)
                                {
                                    if(item=1)                        
                                        time=0;                        
                                }
                            }
                            sound=audio_play_sound(snd_x6,1,false);                    
                            audio_sound_gain(sound,vol,0);
                        }
                        else if(ammo==4)
                        {
                            sprite = spr_laser_x44;            
                            player.alarm[1]=room_speed/2;
                            ddmg = ddmg*4;
                            sound=audio_play_sound(snd_x4,1,false);                    
                            audio_sound_gain(sound,vol,0);
                        }
                        else if(ammo==2)
                        {
                            sprite = spr_laser_x22;                    
                            ddmg = ddmg*2;
                            sound=audio_play_sound(snd_x2,1,false);                    
                            audio_sound_gain(sound,vol,0);
                        }
                        if(ammo!=2)
                        {
                            laser1 = instance_create(player.x+lengthdir_x(25,player.new_dir+50),player.y+lengthdir_y(25,player.new_dir+50),obj_laser);
                            laser1.targetObj = ttargetObj;
                            laser1.sprite_index = sprite;
                            laser1.direction = player.new_dir;
                            laser1.dmg = ddmg;
                            laser2 = instance_create(player.x+lengthdir_x(25,player.new_dir-50),player.y+lengthdir_y(25,player.new_dir-50),obj_laser);  
                            laser2.targetObj = ttargetObj;
                            laser2.sprite_index = sprite;
                            laser2.direction = player.new_dir;
                            laser2.dmg = -1;    
                        }
                        else
                        {
                            laser1 = instance_create(ttargetObj.x+lengthdir_x(25,ttargetObj.new_dir+50),ttargetObj.y+lengthdir_y(25,ttargetObj.new_dir+50),obj_laser);
                            laser1.targetObj = player;
                            laser1.sprite_index = sprite;
                            laser1.direction = ttargetObj.new_dir;
                            laser1.dmg = ddmg;      
                            laser1.color = c_blue;      
                        }
                        if(ammo!=6)
                        {
                            var rdmg = buffer_read(buffer,buffer_u16);
                            rocket = instance_create(player.x,player.y,obj_rocket1);
                            rocket.targetObj = ttargetObj;        
                            rocket.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                            rocket.speed = irandom_range(19,23);;
                            rocket.dmg = rdmg;    
                        }
                    }
                }
            }
            else
            {
                with(obj_UIbox)
                {
                    if(group=1)                        
                    selected=0;                       
                }
            }
                
        break;
        case 12://HP AND SHIELD UPDATE
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                player.HP = buffer_read(buffer,buffer_u16)*100;
                player.SH = buffer_read(buffer,buffer_u16)*100; 
            }else{buffer_seek(buffer,buffer_seek_relative,4);}       
        break;
        case 13://SOMEBODY IS SHOOTING  ROCKET LAUNCHER     
            var ggID = buffer_read(buffer,buffer_u8); 
            var ttargetID = buffer_read(buffer,buffer_u8);         
            var ddmg = buffer_read(buffer,buffer_u16)*10; 
            var player = global.room_players[ggID];  
            var ttargetObj = global.room_players[ttargetID];
            if(instance_exists(player) and instance_exists(ttargetObj))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                { 
                    if(ggID == global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=13)                        
                                time=0;                        
                        }
                    }
                    vol=(200/round(dis));
                    sound=audio_play_sound(snd_launcher,1,false);                    
                    audio_sound_gain(sound,vol,0);
                    rocket1 = instance_create(player.x,player.y,obj_rocket2);
                    rocket1.targetObj = ttargetObj;        
                    rocket1.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                    rocket1.speed = irandom_range(16,20);
                    rocket1.dmg = ddmg;  
                    rocket2 = instance_create(player.x,player.y,obj_rocket2);
                    rocket2.targetObj = ttargetObj;        
                    rocket2.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                    rocket2.speed = irandom_range(16,20);;
                    rocket2.dmg = -1; 
                    rocket3 = instance_create(player.x,player.y,obj_rocket2);
                    rocket3.targetObj = ttargetObj;        
                    rocket3.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                    rocket3.speed = irandom_range(16,20);;
                    rocket3.dmg = -1; 
                    rocket4 = instance_create(player.x,player.y,obj_rocket2);
                    rocket4.targetObj = ttargetObj;        
                    rocket4.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                    rocket4.speed = irandom_range(16,20);;
                    rocket4.dmg = -1; 
                    rocket5 = instance_create(player.x,player.y,obj_rocket2);
                    rocket5.targetObj = ttargetObj;        
                    rocket5.direction = irandom_range(player.prev_dir+90,player.prev_dir+270);
                    rocket5.speed = irandom_range(16,20);;
                    rocket5.dmg = -1;      
                }
            }       
        break;
        case 14://USE EMP
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {
                    vol=(200/round(dis));
                    sound=audio_play_sound(snd_emp,1,false);                    
                    audio_sound_gain(sound,vol,0);
                    emp = instance_create(player.x,player.y,obj_emp)
                    emp.targetObj=player;
                }
                with(obj_iceCube)
                {
                    if(targetObj==player)instance_destroy();
                }
                if(player.sped>0)
                with(player){move_towards_point(move_x,move_y,sped/50-2);}     
                if(ggID == global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=3)                        
                            time=0;                        
                    }
                }
                with(obj_other)
                {
                    if(targetID==ggID)
                    {
                        targetID = 0;
                        targetObj = 0;
                    }
                }
                with(obj_player)
                {
                    if(targetID==ggID)
                    {
                        targetObj.target=false;
                        targetID = 0;
                        targetObj = 0;
                    }
                } 
            }                  
        break;
        case 15://ICE ROCKET
            var ggID = buffer_read(buffer,buffer_u8); 
            var ttargetID = buffer_read(buffer,buffer_u8);  
            var xx = buffer_read(buffer,buffer_u16); 
            var yy = buffer_read(buffer,buffer_u16);                   
            var player = global.room_players[ggID];  
            var ttargetObj = global.room_players[ttargetID]; 
            if(instance_exists(player) and instance_exists(ttargetObj))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {                    
                    vol=(200/round(dis));
                    sound=audio_play_sound(snd_rocket,1,false);                    
                    audio_sound_gain(sound,vol,0);
                    rocket1 = instance_create(player.x,player.y,obj_rocket3);
                    rocket1.targetObj = ttargetObj;        
                    rocket1.direction = player.prev_dir;
                    rocket1.speed = 20;
                }
                ttargetObj.x = xx;
                ttargetObj.y = yy;            
                ttargetObj.speed = 0;
                if(ggID == global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=7)                        
                            time=0;                        
                    }
                }
            }
        break;
        case 16://BACKUP SHIELD
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {                
                    bsh = instance_create(player.x,player.y,obj_bushield)
                    bsh.targetObj=player;
                    if(ggID==global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=9)                        
                                time=0;                        
                        }
                        draw = instance_create(player.x,player.y,obj_dmgdrawer)
                        draw.ID = player;
                        draw.DMG = 75000;
                        draw.color = c_blue; 
                        
                    }
                }
            }
        break;
        case 17://WAR REP
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {                     
                    wrp = instance_create(player.x,player.y,obj_warrep)
                    wrp.targetObj=player;
                    if(ggID==global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=6)                        
                                time=0;                        
                        }               
                    }
                }
            }
        break;
        case 18://USE ISH
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {
                    vol=(200/round(dis));
                    sound=audio_play_sound(snd_ish,1,false);                    
                    audio_sound_gain(sound,vol,0); 
                    ish = instance_create(player.x,player.y,obj_ish)
                    ish.targetObj=player;   
                    if(ggID == global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=4)                        
                                time=0;                        
                        }
                    }  
                } 
            }                         
        break;
        case 19://USE SMB
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            var dmg = buffer_read(buffer,buffer_u16)*10; 
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {
                    vol=(200/round(dis));
                    sound=audio_play_sound(snd_smb,1,false);                    
                    audio_sound_gain(sound,vol,0); 
                    instance_create(player.x,player.y,obj_smb)            
                    if(ggID == global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=8)                        
                                time=0;                        
                        }
                    }
                    if(dmg>0)
                    {
                        draw = instance_create(obj_player.x,obj_player.y,obj_dmgdrawer)
                        draw.ID = obj_player.id;
                        draw.DMG = dmg;
                        draw.color = c_red; 
                    }   
                }
            }                         
        break;
        case 20://SAVE EQUIPMENT
            lg= instance_create(0,0,obj_splash);
            lg.str = "EQUIPMENT SAVED" ;
        break;
        case 21://LOAD EQUIPMENT
        lg= instance_create(0,0,obj_splash);
        lg.str = "EQUIPMENT LOADED" ;        
            var resp = buffer_read(buffer,buffer_u8); 
            obj_inventory.shipID=buffer_read(buffer,buffer_u8);         
            obj_inventory.base_HP=buffer_read(buffer,buffer_u8)*2000; 
            obj_inventory.base_sped=buffer_read(buffer,buffer_u8)*2; 
            obj_inventory.laser_slot=buffer_read(buffer,buffer_u8); 
            obj_inventory.generator_slot=buffer_read(buffer,buffer_u8); 
            obj_inventory.drone_number=10;        
            if(resp == 1)
            {
                obj_inventory.config1_ship_LF4_HP = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_ship_LF4_MD = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_ship_LF4_PD = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_ship_BO2 = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_ship_G3N = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_LF4_HP = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_LF4_MD = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_LF4_PD = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_BO2 = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_havoc = buffer_read(buffer,buffer_u8);
                obj_inventory.config1_drone_hercules = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_ship_LF4_HP = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_ship_LF4_MD = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_ship_LF4_PD = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_ship_BO2 = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_ship_G3N = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_LF4_HP = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_LF4_MD = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_LF4_PD = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_BO2 = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_havoc = buffer_read(buffer,buffer_u8);
                obj_inventory.config2_drone_hercules = buffer_read(buffer,buffer_u8);
                obj_inventory.DMG_bonus = buffer_read(buffer,buffer_u8);
                obj_inventory.SH_bonus = buffer_read(buffer,buffer_u8);            
            }
            else
            {
                obj_inventory.config1_ship_LF4_HP = 0;
                obj_inventory.config1_ship_LF4_MD = 0;
                obj_inventory.config1_ship_LF4_PD = 0;
                obj_inventory.config1_ship_BO2 = 0;
                obj_inventory.config1_ship_G3N = 0;
                obj_inventory.config1_drone_LF4_HP = 0;
                obj_inventory.config1_drone_LF4_MD = 0;
                obj_inventory.config1_drone_LF4_PD = 0;
                obj_inventory.config1_drone_BO2 = 0;
                obj_inventory.config1_drone_havoc = 0;
                obj_inventory.config1_drone_hercules = 0;
                obj_inventory.config2_ship_LF4_HP = 0;
                obj_inventory.config2_ship_LF4_MD = 0;
                obj_inventory.config2_ship_LF4_PD = 0;
                obj_inventory.config2_ship_BO2 = 0;
                obj_inventory.config2_ship_G3N = 0;
                obj_inventory.config2_drone_LF4_HP = 0;
                obj_inventory.config2_drone_LF4_MD = 0;
                obj_inventory.config2_drone_LF4_PD = 0;
                obj_inventory.config2_drone_BO2 = 0;
                obj_inventory.config2_drone_havoc = 0;
                obj_inventory.config2_drone_hercules = 0;
                obj_inventory.DMG_bonus = 0;
                obj_inventory.SH_bonus = 0;
            }
            
            obj_inventory.created = false
        break;
        case 22://SELECT SHIP TO START THE GAME         
            obj_player.x=buffer_read(buffer,buffer_s16); 
            obj_player.y=buffer_read(buffer,buffer_s16); 
            obj_player.move_x=obj_player.x; 
            obj_player.move_y=obj_player.y;        
        break;
        case 23://UPDATE THE CHANGE IN CONFIG,SHIP LOAD OR DRONE FORMATION                  
            var ggID =buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            var sID = buffer_read(buffer,buffer_u8);
            if(instance_exists(player))  
            { 
                player.maxHP=buffer_read(buffer,buffer_u16)*100; 
                player.maxSH=buffer_read(buffer,buffer_u16)*100; 
                player.DMG=buffer_read(buffer,buffer_u8)*100; 
                player.sped=buffer_read(buffer,buffer_u16); 
                player.havoc=buffer_read(buffer,buffer_u8); 
                player.hercules=buffer_read(buffer,buffer_u8); 
                player.drone_formation=buffer_read(buffer,buffer_u8);
                if(player.sped>0) 
                with(player){move_towards_point(move_x,move_y,sped/50-2);}
                if(player.shipID!=sID)
                {
                    switch(sID)
                    {
                        case 1: player.sprite_index=spr_ship1; 
                        if(ggID==global.gID){obj_UIedit.box8.item=-1; obj_UIedit.box8.time=-1; obj_UIedit.box8.timer=-1;} break;
                        case 2: player.sprite_index=spr_ship2; 
                        if(ggID==global.gID){obj_UIedit.box8.item=27; obj_UIedit.box8.time=7199; obj_UIedit.box8.timer=7199;} break;
                        case 3: player.sprite_index=spr_ship3; 
                        if(ggID==global.gID){obj_UIedit.box8.item=25; obj_UIedit.box8.time=7199; obj_UIedit.box8.timer=7199;} break;
                        case 4: player.sprite_index=spr_ship4;
                        if(ggID==global.gID){obj_UIedit.box8.item=23; obj_UIedit.box8.time=5399; obj_UIedit.box8.timer=5399;} break;
                        case 5: player.sprite_index=spr_ship5;
                        if(ggID==global.gID){obj_UIedit.box8.item=24; obj_UIedit.box8.time=1799; obj_UIedit.box8.timer=1799;} break;
                        case 6: player.sprite_index=spr_ship6; 
                        if(ggID==global.gID){obj_UIedit.box8.item=26; obj_UIedit.box8.time=7199; obj_UIedit.box8.timer=7199;} break;
                        case 7: player.sprite_index=spr_ship7; 
                        if(ggID==global.gID){obj_UIedit.box8.item=22; obj_UIedit.box8.time=5399; obj_UIedit.box8.timer=5399;} break;
                        case 8: player.sprite_index=spr_ship8; 
                        if(ggID==global.gID){obj_UIedit.box8.item=-1; obj_UIedit.box8.time=-1; obj_UIedit.box8.timer=-1;} break;          
                    }   
                    player.shipID=sID;   
                }
            }else{buffer_seek(buffer,buffer_seek_relative,10);}
        break;
        case 24://A PLAYER IS KILLED
            var ggID =buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            var killerID=buffer_read(buffer,buffer_u8); 
            var assistID=buffer_read(buffer,buffer_u8);            
            if(instance_exists(player))  
            {              
                if(ggID==global.gID)
                {                    
                    audio_play_sound(snd_dead,1,false);                
                }
                else
                {
                    dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                    if(dis<1000)
                    {  
                        vol=(200/round(dis));
                        sound=audio_play_sound(snd_explode,1,false);                    
                        audio_sound_gain(sound,vol,0);
                    }
                }
                instance_create(player.x,player.y,obj_explosion);      
                player.HP=0            
                player.targetID = 0;
                player.targetObj = 0; 
                player.speed=0
                if(obj_player.targetObj==player)
                {
                    obj_player.targetObj.target=false;
                    obj_player.targetID=0;
                    obj_player.targetObj=0;    
                }
                with(obj_other)
                {
                    if(targetObj==player)
                    {
                        targetID=0;
                        targetObj=0; 
                    }
                    if(ggID == global.gID)target=false;                   
                }            
                if(ggID == global.gID)
                {                
                    var xx = view_xview[0]+view_wview[0]/2
                    var yy = view_yview[0]+view_hview[0]/2
                    instance_create(xx,yy,obj_shipmenu); 
                    obj_player.targetID=0;
                    obj_player.targetObj=0;               
                }
                var killer = global.room_players[killerID];
                if(instance_exists(killer))
                {
                    lg1 = instance_create(0,0,obj_log);
                    lg1.str = killer.username+" killed "+player.username;  
                }          
                if(assistID!=0)
                {
                    var assist = global.room_players[assistID];
                    instance_exists(assist)
                    {
                        lg2 = instance_create(0,0,obj_log);
                        lg2.str = "assist "+assist.username;
                    }
                }
                if(killerID==global.gID and killerID!=0)
                {
                    lg3= instance_create(0,0,obj_splash);
                    lg3.str = "KILL" ;
                }else if(assistID==global.gID and assistID!=0)
                {
                    lg3= instance_create(0,0,obj_splash);
                    lg3.str = "ASSIST";
                }
            }
        break;
        case 25://CONFIG CHANGE
            obj_player.can_change=false;
            obj_player.alarm[3]=299;
            if(obj_player.config=1){obj_player.config=2}
            else {obj_player.config=1}
        break;
        case 26://DRONE FORMATION CHANGE
            with(obj_UIbox)
            {
                if(group=2)                        
                    time=0;                        
            }
        break;
        case 27://DRONE FORMATION CHANGE
            var ggID = buffer_read(buffer,buffer_u8);             
            if(ggID==global.gID)
            {
                obj_player.alpha=0.4;
                obj_player.alarm[4]=120
                with(obj_UIbox)
                {
                    if(item=5)                        
                        time=0;                        
                }
            }
            else
            {
                var player = global.room_players[ggID];
                if(instance_exists(player))  
                {
                    player.alpha=0;
                    player.alarm[4]=120
                }
            }            
        break;
        case 28://CHANGE IN SPEED OF THE PLAYER
            var ggID =buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID]; 
            if(instance_exists(player))  
            {          
                player.sped=buffer_read(buffer,buffer_u16);             
                if(player.sped>0) 
                with(player){move_towards_point(move_x,move_y,sped/50-2);}
            }else{buffer_seek(buffer, buffer_seek_relative, 2);}
        break;
        case 29://LIGHTNING SKILL
            with(obj_UIbox)
            {
                if(item=22)                        
                    time=0;                        
            }
        break;
        case 30://SOLACE SKILL
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            if(instance_exists(player))  
            {
                dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
                if(dis<1000)
                {                
                    sol = instance_create(player.x,player.y,obj_solace_effect)
                    sol.targetObj=player;
                    if(ggID==global.gID)
                    {
                        with(obj_UIbox)
                        {
                            if(item=23)                        
                                time=0;                        
                        }                   
                    }
                }
            }
        break;
        case 31://SENTINEL SKILL
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            //dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
            //if(dis<1000)
            if(instance_exists(player))           
            {                
                sen = instance_create(player.x,player.y,obj_sentinel_effect)
                sen.targetObj=player;
                if(ggID==global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=24)                        
                            time=0;                        
                    }                   
                }
            }
        break;
        case 32://SPECTRUM SKILL
            var ggID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            //dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
            //if(dis<1000)
            if(instance_exists(player))              
            {                
                spe = instance_create(player.x,player.y,obj_spectrum_effect)
                spe.targetObj=player;
                if(ggID==global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=25)                        
                            time=0;                        
                    }                   
                }
            }
        break;
        
        case 33://DIMINISHER SKILL
            var ggID = buffer_read(buffer,buffer_u8); 
            var ttID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            var target = global.room_players[ttID];
            //dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
            //if(dis<1000)
            if(instance_exists(player) and instance_exists(target))            
            {                
                dim1 = instance_create(player.x,player.y,obj_diminisher_effect)
                dim1.targetObj=player;
                dim1.owner = player;
                dim1.ttID = ttID;
                dim2 = instance_create(target.x,target.y,obj_diminisher_effect)
                dim2.targetObj=target;
                dim2.owner = player;
                dim2.ttID = ttID;
                if(ggID==global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=26)                        
                            time=0;                        
                    }                   
                }
            }
        break;
        
        case 34://VENOM SKILL
            var ggID = buffer_read(buffer,buffer_u8); 
            var ttID = buffer_read(buffer,buffer_u8); 
            var player = global.room_players[ggID];
            var target = global.room_players[ttID];
            //dis = point_distance(obj_player.x,obj_player.y,player.x,player.y)+1;
            //if(dis<1000)
            if(instance_exists(player) and instance_exists(target)) 
            {                
                ven1 = instance_create(player.x,player.y,obj_venom_effect)
                ven1.targetObj=player;
                ven1.owner = player;
                ven1.ttID = ttID;
                ven1.dmg=-1;
                ven2 = instance_create(target.x,target.y,obj_venom_effect)
                ven2.targetObj=target;
                ven2.owner = player;
                ven2.ttID = ttID;
                ven2.dmg=10000;
                if(ggID==global.gID)
                {
                    with(obj_UIbox)
                    {
                        if(item=27)                        
                            time=0;                        
                    }                   
                }
            }
        break;
        
        case 35://INCOMING MESSAGE FROM SERVER        
        log = instance_create(0,0,obj_log);
        log.str = read_string(buffer); 
        break;
    }
    size -= buffer_seek(buffer,buffer_seek_relative,0);
}
