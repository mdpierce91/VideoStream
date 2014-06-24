//Page 31 of pdf

import javax.swing.*;
import javax.media.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.rtp.*;
import javax.media.format.*;

public class Sender
{
	private String ipAddress; //The IP address we will be using
	private String fileName; //The name of the file we will open
	private int port; //the port number we will be using
	
	private DataSource outSource; 
	private TrackControl track[];
	private RTPManager rtp_Manager[];
	private Processor pro;
	
	public Sender(String loc, String ip, int portNum)
	{
		//set the name, IPaddress, and port number as sent from the tester
		fileName = loc;  
		port = portNum; 
		ipAddress = ip;
	}
	
	public boolean beginSession()
	{
		//open the file
		MediaLocator m_Loc = new MediaLocator(fileName);
		//if it is null then return an error
		if (m_Loc == null)
		{
			System.out.println("no media locator for" + fileName);
			return false;
		}
		try
		{
			//Create a processor and make it open the file
			pro = Manager.createProcessor(m_Loc);
			//add a control listener on the processor
			pro.addControllerListener(new ProcessorEventHandler());
			
			//System.out.println("1");
			pro.configure();
		}
		catch ( IOException ioException )
		{
			ioException.printStackTrace();
			return false;
		}
		catch( NoProcessorException noProcessorException)
		{
			noProcessorException.printStackTrace();
			return false;
		}
		return true;
	}
	
	private class ProcessorEventHandler extends ControllerAdapter
	{
		public void configureComplete(ConfigureCompleteEvent configureCompleteEvent)
		{
			//Configure the processor
			setOutputFormat();
			pro.realize();
		}
		public void realizeComplete(RealizeCompleteEvent realizeCompleteEvent)
		{
			//if the following code was uncommented it would auto start the transmission upon opening the file
			/*if(transmitMedia() != true)
			{
				System.out.println("2");
			}
			else
				System.out.println("ok");*/
		}
		public void endOfMedia( EndOfMediaEvent mediaEndEvent )
		{
			//stop the transmission when the file is completed
			stopTransmission();
		}
	}
	
	public void setOutputFormat()
	{
		//make the processor outputs into rtp format
		pro.setContentDescriptor(new ContentDescriptor( ContentDescriptor.RAW_RTP ) );
		track = pro.getTrackControls();
		Format rtpFormats[];
		//for each track, convert them into rtp format
		for (int i=0; i < track.length; i++)
		{
			System.out.println( "\nTrack #" + ( i + 1) + "supports ");
			
			if (track[i].isEnabled() )
			{
				rtpFormats = track[i].getSupportedFormats();
				if (rtpFormats.length > 0)
				{
					for(int j=0; j<rtpFormats.length; j++)
					{
						System.out.println(rtpFormats[j]);
					}
					track[i].setFormat(rtpFormats[0]);
					System.out.println("Track format set to " + track[i].getFormat() );
					
				}
				else
				{
					System.err.println("No Supported rtp formats for track");
				}
			}
		}
	}
	
	public boolean transmitMedia()
	{
		//get the output from the processor
		outSource = pro.getDataOutput();
		//if it is not null, then send it over the rtp stream which we create below
		if (outSource == null)
		{
			System.out.println("No data source from media");
			return false;
		}
		rtp_Manager = new RTPManager[track.length];
		SessionAddress localAddress, remoteAddress;
		SendStream sendStream;
		InetAddress ip;
		try
		{
			//send each track seperatly
			for(int i = 0; i<track.length;i++)
			{
				rtp_Manager[i] = RTPManager.newInstance();
				port += (2*i);
				ip = InetAddress.getByName(ipAddress);
				//increase the local port number so that we can send and receive on the same machine
				localAddress = new SessionAddress(ip.getLocalHost(), port+1000);
				remoteAddress = new SessionAddress(ip,port);
				rtp_Manager[i].initialize(localAddress);
				rtp_Manager[i].addTarget(remoteAddress);
				System.out.println("\nStarted RTP session" + ipAddress + " " + port);
				sendStream = rtp_Manager[i].createSendStream(outSource, i);
				sendStream.start();
				System.out.println( "Transmitting Track #" + (i+1) + " ... ");				
			}
			//begin playing/transmitting
			pro.start();
		}
		catch( InvalidSessionAddressException addressError)
		{
			addressError.printStackTrace();
			return false;
		}
		catch( IOException ioException )
		{
			ioException.printStackTrace();
			return false;
		}
		catch( UnsupportedFormatException formatException)
		{
			formatException.printStackTrace();
			return false;
		}
		return true;
	}

	public void stopTransmission()
	{
		if (pro != null)
		{
			//stop playing
			pro.stop();
			pro.close();
			//close the rtp manager to stop the transmission
			if(rtp_Manager != null)
			{
				for (int i=0;i<rtp_Manager.length; i++)
				{
					rtp_Manager[i].removeTargets("Session stoppped.");
					rtp_Manager[i].dispose();
				}
			}
			System.out.println("Transmission stopped.");
		}
	}
}