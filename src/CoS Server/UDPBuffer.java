public class UDPBuffer {
	//BUFFER SYSTEM THAT HOLDS DATA FOR READING AND WRITING DONT FORGET TO CLEAR THE BUFFER AFTER READING AND WRTING 
	//SENDING AND READING SHOULD BE DONE THROUGH WRAPING THE BUFFERS WITH DATAGRAM PACKETS IN THE UDP SERVER 
	
	//FUNCTIONS FOR WRTING DATA
	byte[] sendData = new byte[1024];
	int sendIndex = 0;	
	
	public void write_s8(byte n)
	{
		sendData[sendIndex++]=n;		
	}
	
	public void write_u8(int n)
	{
		sendData[sendIndex++]=(byte)n;		
	}
	public void write_s16(short n)
	{		
		sendData[sendIndex++]=(byte)((n  >>> 0) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 8) & 0xFF);		
	}
	
	public void write_u16(int n)
	{		
		sendData[sendIndex++]=(byte)((n  >>> 0) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 8) & 0xFF);		
	}
	
	public void write_s32(int n)
	{		
		sendData[sendIndex++]=(byte)((n  >>> 0) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 8) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 16) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 24) & 0xFF);	
	}
	
	public void write_u32(long n)
	{		
		sendData[sendIndex++]=(byte)((n  >>> 0) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 8) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 16) & 0xFF);
		sendData[sendIndex++]=(byte)((n  >>> 24) & 0xFF);	
	}
	
	public void write_f32(float n)
	{		
		write_s32(Float.floatToIntBits(n));		
	}
	
	public void write_string (String string)
	{
		byte[] bytes = string.getBytes();
		int length = string.length();
		sendData[sendIndex++]=(byte)length;		
		for (int i = 0;i<length;i++)
		{
			sendData[sendIndex+i] = bytes[i];			
		}		
		sendIndex += length;
	}
	
	public byte[] flush()
	{
		byte [] buffer = new byte[sendIndex]; 
		for (int i=0;i<sendIndex;i++)
		{
			buffer[i] = sendData[i];			
		}
		//sendData = new byte[1024];
		sendIndex = 0;
		return buffer;
	}
	
	//FUNCTIONS FOR READING DATA
	byte[] receiveData = new byte[1024];
	int receiveIndex = 0;
	
	public byte read_s8()
	{
		return receiveData[receiveIndex++];
	}
	
	public int read_u8()
	{
		return receiveData[receiveIndex++]& 0xFF;
	}
	
	public short read_s16()
	{
		int ch1 = receiveData[receiveIndex++]& 0xFF;
        int ch2 = receiveData[receiveIndex++]& 0xFF;        
        return (short)((ch1 <<  0) + (ch2 <<  8));				
	}
	
	public int read_u16()
	{
		int ch1 = receiveData[receiveIndex++]& 0xFF;
        int ch2 = receiveData[receiveIndex++]& 0xFF;        
        return ((ch1 <<  0) + (ch2 <<  8));				
	}
	
	public int read_s32()
	{
		int ch1 = receiveData[receiveIndex++]& 0xFF;
        int ch2 = receiveData[receiveIndex++]& 0xFF;
        int ch3 = receiveData[receiveIndex++]& 0xFF;
        int ch4 = receiveData[receiveIndex++]& 0xFF;       
        return ((ch1 <<  0) + (ch2 <<  8) + (ch3 <<  16) + (ch4 <<  24));						
	}
	
	public long read_u32()
	{
		int ch1 = receiveData[receiveIndex++]& 0xFF;
        int ch2 = receiveData[receiveIndex++]& 0xFF;
        int ch3 = receiveData[receiveIndex++]& 0xFF;
        int ch4 = receiveData[receiveIndex++]& 0xFF;       
        return ((ch1 <<  0) + (ch2 <<  8) + (ch3 <<  16) + (ch4 <<  24))& 0x00000000ffffffffL;						
	}
	
	public float read_f32()
	{
		return Float.intBitsToFloat(read_s32());					
	}
	
	public String read_string ()	
	{	
		int lenght = receiveData[receiveIndex++];			
		StringBuilder string = new StringBuilder();        
		for (int i = 0;i<lenght;i++)
		{
			string.append((char)(receiveData[receiveIndex+i]));			
		}
		receiveIndex += lenght;
		return string.toString();	 
	}
	
	public void clear()
	{
		receiveIndex = 0;
	}
}
