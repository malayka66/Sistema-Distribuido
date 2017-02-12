package BITalino;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;


import BITalino.DeviceDiscoverer;


public class BITalino {
	
	private int[] analogChannels = null;
	private int number_bytes = 0;
	private StreamConnection hSocket = null;
	private DataInputStream iStream = null;
	private DataOutputStream oStream = null;
	
	public BITalino() {}
	
	public Vector<RemoteDevice> findDevices() throws InterruptedException
	{
		/**
		 * 
		 * Search for bluetooth devices nearby
	     * Output: Vector with devices macAddress found with the name equal to bitalino
		 * 
		 */
		
		DeviceDiscoverer finder = new  DeviceDiscoverer();	
		while (finder.inqStatus == null) 
		{
			Thread.sleep(1000);
		}
		finder.inqStatus = null;
		return finder.remoteDevices;
		
	}
	
	public void open(String macAdd) throws Throwable 
	{
		open(macAdd, 1000);
	}
	
	public void open(String macAdd, int samplingRate) throws BITalinoException 
	{
		/**
		 * Connect to bluetooth device with the mac address provided. 
		 * Configure the sampling Rate.
		 * 
		 * @param:
		 * 		macAdd (string): MAC address of the bluetooth device
		 * 		SamplingRate(int): Sampling frequency (Hz); values available: 1000, 100, 10 and 1.
		 * 
		 */
			if (macAdd.split(":").length > 1) 
			{
				macAdd = macAdd.replace(":", "");
			}
			if (macAdd.length() != 12) 
			{
				throw new BITalinoException(BITalinoErrorTypes.MACADDRESS_NOT_VALID);
			}
		
		try 
		{
			hSocket = (StreamConnection)Connector.open("btspp://" + macAdd + ":1", Connector.READ_WRITE);
			iStream = hSocket.openDataInputStream();
			oStream = hSocket.openDataOutputStream();
			Thread.sleep(2000);
			
		} 
		catch (Exception e) 
		{
			close();
		}
		
		try 
		{
			int variableToSend = 0;
		// Configure sampling rate
		
			switch(samplingRate)
			{
			case 1000:
				variableToSend = 0x3;
				break;
			case 100:
				variableToSend = 0x2;
				break;
			case 10:
				variableToSend = 0x1;
				break;
			case 1:
				variableToSend = 0x0;
				break;
			default:
				close();
			}
			variableToSend = (variableToSend<<6)|0x03;
			Write(variableToSend);
		} 
		catch (Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.SAMPLING_RATE_NOT_DEFINED);
		}
	}
	
	public void start(int[] anChannels) throws Throwable 
	{
		
		/**
		 * 
		 * Starts Acquisition in the analog channels set.
		 * 
		 * @param:	anChannels (int[]): channels to be acquired (from 0 to 5)
		 * 
		 */
		analogChannels = anChannels;
		if (analogChannels.length > 6 | analogChannels.length == 0) {
			throw new BITalinoException(BITalinoErrorTypes.ANALOG_CHANNELS_NOT_VALID);
		} else {
			int bit = 1;
			for (int i : anChannels) {
				if (i<0 | i>5) 
				{
					throw new BITalinoException(BITalinoErrorTypes.ANALOG_CHANNELS_NOT_VALID);
				}
				else 
				{
					bit = bit | 1<<(2+i);
				}
			}
			int nChannels = analogChannels.length;
			if (nChannels <= 4) {
				number_bytes = (int) Math.ceil(((float)12 + (float)10 *nChannels)/8);
				
				
			} else {
				number_bytes = (int) Math.ceil(((float)52 + (float)6*(nChannels-4))/8);
			}
			try 
			{
				Write(bit);
			} 
			catch(Exception e) 
			{
				throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
			}
		}
		
	}
	
	public void stop() throws BITalinoException 
	{
		/**
		 * Sends state value 0 to stop BITalino acquisition.
		 */
		try 
		{
			Write(0);
		} 
		catch(Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
		}
	}
	
	public void close() throws BITalinoException 
	{
		
		/**
		 * Closes bluetooth stream connection
		 */
		try 
		{
			hSocket.close();
			iStream.close();
			oStream.close();
			hSocket=null;
			iStream = null;
			oStream = null;
		} 
		catch(Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.BT_DEVICE_NOT_CONNECTED);
		}
		
	}
	
	public void Write(int data) throws BITalinoException 
	{
		/**
		 * Send a command to BITalino
		 * @param: data (int): value to send to BITalino
		 */
		try 
		{
			oStream.write(data);
			oStream.flush();
			Thread.sleep(1000);
		} 
		catch (Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
		}
	}
	
	public void battery(int value) throws BITalinoException 
	{
		
		/**
		 * 
		 * Set the battery threshold of BITalino
		 * ATTENTION: Works only in idle mode!
		 * 
		 * @param: value (int): threshold value from 0 to 63
		 */
		
		int Mode;
		if (value >= 0 && value<=63) 
		{
			Mode = value << 2;
			Write(Mode);
			
		} 
		else 
		{
			throw new BITalinoException(BITalinoErrorTypes.THRESHOLD_NOT_VALID);
		}
		
	}
	
	public void trigger(int[] digitalArray) throws BITalinoException 
	{
		
		/**
		 * 
		 * Act on digital output channels of BITalino
		 * ATTENTION: Works only during acquisition mode!
		 * 
		 * @param:
		 * 		digitalArray (array): array of size 4 which act on digital outputs according to the value: 0 or 1
		 * 							Each position of the array corresponds to a digital output, in ascending order.
		 * 							Example:
		 * 								digitalArray = [1,0,1,0] -> Digital 0 and 2 will be set to one and Digital 1 and 3 to zero
		 */
		
		if (digitalArray.length != 4) 
		{
			throw new BITalinoException(BITalinoErrorTypes.DIGITAL_CHANNELS_NOT_VALID);
		} 
		else 
		{ 
			int data  = 3;
			for (int i= 0;i<digitalArray.length;i++) 
			{
				if (digitalArray[i]<0 | digitalArray[i]>1)
				{
					throw new BITalinoException(BITalinoErrorTypes.DIGITAL_CHANNELS_NOT_VALID);
				}
				else 
				{
					data = data | digitalArray[i]<<(2+i);
				}
				
			}
			Write(data);
		}
	}
	
	public String version() throws BITalinoException, IOException 
	{
		
		/**
		 * 
		 * Get BITalino version.
		 * ATTENTION: Works only in idle mode!
		 * 
		 * Output: version (String)
		 */
		try 
		{
			Write(7);
			byte[] version = new byte[30];
			String test = "";
			int i = 0;
			while (true) 
			{
				iStream.read(version,i,1);
				i++;
				test = new String(new byte[] {version[i-1]});
				if (test.equals("\n")) 
				{
					break;
				}
			}
			return new String(version);
		} 
		catch(Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.LOST_COMMUNICATION);
		}
	}
	
	private Frame[] decode(byte[] buffer) throws IOException, BITalinoException 
	{
		
		/**
		 * Unpack data samples in frames
		 * 
		 * @param:
		 * 		nframes (int): number of samples to read and decode;
		 * 
		 * Output:
		 * 		Frame[]: data unpacked (see Frame Class)
		 * 
		 */
		try 
		{
			Frame[] frames = new Frame[1];
			int j=(number_bytes-1), i=0, CRC = 0,x0=0,x1=0,x2=0,x3=0,out=0,inp=0;
			CRC= (buffer[j-0]&0x0F)&0xFF;
			// check CRC
			for (int bytes = 0; bytes<number_bytes;bytes++) 
			{
				for (int bit=7;bit>-1;bit--)
				{
					inp=(buffer[bytes])>>bit & 0x01;
					if (bytes == (number_bytes - 1) && bit<4) 
					{
						inp = 0;
					}
					out=x3;
					x3=x2;
					x2=x1;
					x1=out^x0;
					x0=inp^out;
				}
			}
			//if the message was correctly received, it starts decoding	
			if (CRC == ((x3<<3)|(x2<<2)|(x1<<1)|x0)) 
			{
			
				/*parse frames*/
				
				frames[i]=new Frame();            
				frames[i].seq = (short) ((buffer[j-0]&0xF0)>>4)&0xf;
				frames[i].digital[0] = (short)((buffer[j-1]>>7)&0x01);
				frames[i].digital[1] = (short)((buffer[j-1]>>6)&0x01);
				frames[i].digital[2] = (short)((buffer[j-1]>>5)&0x01);
				frames[i].digital[3] = (short)((buffer[j-1]>>4)&0x01);
											
				/*parse buffer frame*/
				switch(analogChannels.length-1)
				{
				
				case 5:
					frames[i].analog[5]= (short)((buffer[j-7]&0x3F));	
				case 4:
					
					frames[i].analog[4] = (short)((((buffer[j-6]&0x0F)<<2)|((buffer[j-7]&0xc0)>>6))&0x3f);
				case 3:
					
					frames[i].analog[3] = (short)((((buffer[j-5]&0x3F)<<4)|((buffer[j-6]&0xf0)>>4))&0x3ff);
				case 2:
					
					frames[i].analog[2] = (short)((((buffer[j-4]&0xff)<<2)|(((buffer[j-5]&0xc0)>>6)))&0x3ff);
				case 1:
					
					frames[i].analog[1] = (short)((((buffer[j-2]&0x3)<<8)|(buffer[j-3])&0xff)&0x3ff);
				case 0:
					
					frames[i].analog[0] = (short)((((buffer[j-1]&0xF)<<6)|((buffer[j-2]&0XFC)>>2))&0x3ff);
				}
				
				
			} 
			else 
			{
				frames[i]=new Frame();
				frames[i].seq = -1;
			}
			return frames;
		} 
		catch (Exception e) 
		{
			throw new BITalinoException(BITalinoErrorTypes.INCORRECT_DECODE);
		}
	}
	
	public Frame[] read(int nSamples) throws BITalinoException 
	{
		
		/**
		 * 
		 * Acquire defined number of samples from BITalino
		 * 
		 * @param:
		 * 		nSamples (int): number of samples
		 * 
		 * Output:
		 * 		Frame[]: data unpacked (see Frame Class)
		 */
		try 
		{
			Frame[] frames = new Frame[nSamples];
			byte[] buffer = new byte[number_bytes];
			byte[] bTemp = new byte[1];
			int i=0;
			while (i<nSamples) 
			{
				iStream.readFully(buffer,0,number_bytes);
				Frame[] f = decode(buffer);
				if (f[0].seq == -1) 
				{
					while (f[0].seq == -1) 
					{
						iStream.readFully(bTemp,0,1);
						for (int j = number_bytes-2; j >= 0; j--) 
						{                
						    buffer[j+1] = buffer[j];
						}
						buffer[0] = bTemp[0];
						f = decode(buffer);
					}
					frames[i] = f[0];
				} 
				else 
				{
					
					frames[i] = f[0];
				}
				i++;
			}
			return frames;
		} 
		catch (Exception e) 
		{
			throw new BITalinoException (BITalinoErrorTypes.LOST_COMMUNICATION);
		}
		
	}
	
}
