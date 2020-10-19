import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class TCPBuffer {
	//BUFFER SYSTEM THAT HOLDS DATA FOR READING AND WRITING DONT FORGET TO CLEAR THE BUFFER AFTER READING AND WRTING 
	//SENDING AND READING SHOULD BE DONE WITH USING INPUT AND OUTPUT STREAMS INITIALIZED BEFORE USING THE FUNCTIONS IN THIS OBJECT
	
	//TCP READING FUNCTIONS
	
	public  byte read_s8(DataInputStream in) throws IOException 	{		
		return in.readByte();
	}	
	
	public  int read_u8(DataInputStream in) throws IOException 	{		
		return in.readUnsignedByte();
	}		
	
	public  int read_u16(DataInputStream in) throws IOException 	{		
		int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) <  0)
        throw new EOFException();
        return ((ch1 <<  0) + (ch2 <<  8));		
	}
	
	public  short read_s16(DataInputStream in) throws IOException 	{		
		int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) <  0)
        throw new EOFException();
        return (short)((ch1 <<  0) + (ch2 <<  8));		
	}	
	
	public  int read_s32(DataInputStream in) throws IOException 
	{
		int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) <  0)
        throw new EOFException();
        return ((ch1 <<  0) + (ch2 <<  8) + (ch3 <<  16) + (ch4 <<  24));		 
	}	
	
	public  long read_u32(DataInputStream in) throws IOException 
	{		
		int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) <  0)
        throw new EOFException();
        return ((ch1 <<  0) + (ch2 <<  8) + (ch3 <<  16) + (ch4 <<  24))& 0x00000000ffffffffL;	
	}
	
	public  long read_u64(DataInputStream in) throws IOException 
	{		
		int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        int ch5 = in.read();
        int ch6 = in.read();
        int ch7 = in.read();
        int ch8 = in.read();
        if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) <  0)
            throw new EOFException();
	    return (((ch1 & 255) <<  0) +
	    		((ch2 & 255) <<  8) +
	            ((ch3 & 255) <<  16) +
	            ((long)(ch4 & 255) <<  24) +
	            ((long)(ch5 & 255) <<  32) +
	            ((long)(ch6 & 255) <<  40) +
	            ((long)(ch7 & 255) <<   48) +
	            ((long)(ch8 & 255) <<   56));
	}
		
	public  float read_f32(DataInputStream in) throws IOException 
	{
		return Float.intBitsToFloat(read_s32(in));
	}	
	
	public  double read_f64(DataInputStream in) throws IOException 
	{
		return Double.longBitsToDouble(read_u64(in));
	}
	
	public  String read_string(DataInputStream input) throws IOException 
	{
		int lenght = input.readUnsignedByte();		
		StringBuilder string = new StringBuilder();        
		for (int i = 0;i<lenght;i++)
		{
			string.append((char)(input.readUnsignedByte()));
			
		}
		return string.toString();	 
	}
	
	//TCP WRITING FUNCTIONS
	
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
	
	public void send(DataOutputStream o)
	{
		try {
			o.write(sendData, 0, sendIndex);
			o.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void flush()
	{				
		//sendData = new byte[1024];
		sendIndex = 0;		
	}
}
