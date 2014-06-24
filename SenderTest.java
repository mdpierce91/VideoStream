import javax.swing.*;
import javax.media.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;

public class SenderTest extends JFrame 
{
	private Sender rtpServer; //create a server
	private int port;	//the port we will use
	private String ip = "224.1.1.0";
	private String mediaLocation; //the ipaddress we will be using, and the location of the file we are sending
	private File mediaFile; // the file we are sending
	private JButton transmitFileButton;  //the buttons
	private JButton stop;
	private JButton start;
	
	public SenderTest(int portNumber)
	{
		//set the name of the window
		super("Stream Sender");
		//if the window is closed then stop the transmission
		port = portNumber;
		addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent windowEvent)
				{
					if (rtpServer != null)
					{
						rtpServer.stopTransmission();
					}
				}
			}
		
		);
		//create all the buttons we need, they each have their own handler
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel);
		transmitFileButton = new JButton("Transmit File");
		buttonPanel.add(transmitFileButton);
		transmitFileButton.addActionListener(new ButtonHandler());
		stop = new JButton("Stop");
		buttonPanel.add(stop);
		start = new JButton("Start");
		buttonPanel.add(start);
		Control_Start start_act = new Control_Start();
		start.addActionListener(start_act);	
		Control_Stop stop_act = new Control_Stop();
		stop.addActionListener(stop_act);
		setSize(250,150);
		setLocation(300,300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent actionEvent)
		{
			mediaFile = getFile();
			if( mediaFile != null)
			{
				try
				{
					//get the file's location
					mediaLocation = mediaFile.toURL().toString();
				}
				catch(MalformedURLException badURL)
				{
					badURL.printStackTrace();
				}
			}
			else
			{
				return;
			}
			if(mediaLocation == null)
			{
				return;
			}
			//get the ipaddress
			/*ip = getIP();
			if(ip ==null)
			{
				return;
			}
			//get the port number
			port = getPort();
			if(port <= 0)
			{
				//if it is the error port number -999 do not send
				if(port != -999)
				{
					System.out.println("Invalid port number");
				}
				return;
			}*/
			rtpServer = new Sender(mediaLocation, ip,port);
			rtpServer.beginSession();
		}
	}
	public File getFile()
	{
		//open a window for the user to choose a file to open
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = fileChooser.showOpenDialog(this);
		if(result == JFileChooser.CANCEL_OPTION)
		{
			return null;
		}
		else
		{
			return fileChooser.getSelectedFile();
		}
	}
	/*public String getIP()
	{
		//open a window for the user to input the IP address they want to send to
		String input = JOptionPane.showInputDialog(this, "Enter IP Address:");
		if(input !=null && input.length() == 0)
		{
			System.err.println("No input");
			return null;
		}
		return input;
	}
	public int getPort()
	{
		//open a window for the user to input the port number they want to use
		String input = JOptionPane.showInputDialog(this,"Enter port number: ");
		if (input != null && input.length() == 0)
		{
			System.err.println("no input");
			return -999;
		}
		if (input == null)
		{
			return -999;
		}
		return Integer.parseInt(input);
	}*/
class Control_Start implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			//begin transmitting media
			if(rtpServer.transmitMedia())
			{
				return;
			}
		}
	}
	public class Control_Stop implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			//stop transmitting media
			rtpServer.stopTransmission();
		}
	}
	
	public static void main(String args[])
	{
		//create the server and set its starting parameters
		//SenderTest serverTest = new SenderTest();
		
	}
}