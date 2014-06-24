import javax.swing.*;
import javax.media.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.media.rtp.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.rtp.event.*;
import java.util.Vector;


public class ReceivePlayer extends JFrame
{
	private int port;  //the port number we will use
	private Container content;  //our container
	private String ip = "224.1.1.0"; //the ip address we will use
	private Panel panel1; // the panel the player will use
	private Player video,audio; //a player for each track
	private JButton play; //the play button
	private PlayerWindow []playerwindows; //create an array of windows we can use to play our tracks
	private int count=0; //a counter to keep track of our windows
	public static final int number_of_tracks = 5; //the number of tracks we use, usually only need 2(video and audio)
	
	public ReceivePlayer(int portNumber)
	{
		//set the title of our window
		super("Stream Player");
		port = portNumber;
		//create the panel and our buttons
		play = new JButton("Play");
		panel1 = new Panel();
		playerwindows = new PlayerWindow[number_of_tracks];
		Player_GUI_Init();
		Control_Handler();
		setSize(200,100);
		setLocation(300,300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void Player_GUI_Init()
	{
		//set out GUI by adding the button and panel to our container
		content = getContentPane();
		content.add(panel1, BorderLayout.NORTH);
		panel1.setLayout(new GridLayout(1,1));
        panel1.add(play);
	}
	public void Control_Handler()
	{
		//set up the handler for our button
		Control_Play play_act = new Control_Play();
		play.addActionListener(play_act);
	}
	public class Control_Play implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			//open a window so the user can input the IP address they want to connect to
			/*ip = JOptionPane.showInputDialog("enter IP Address:");
			//open a window so the user can input the port number they want to connect to
			String stPort = JOptionPane.showInputDialog("enter Port Number:");
			//if the port number is corrupted then set it to the error value
			if (stPort != null&&stPort.length()==0)
			{
				port = -999;
			}
			else if (stPort == null)
			{
				port = -999;
			}
			else
			{
				//convert it to an int
				port = Integer.parseInt(stPort);
			}*/
			System.out.println("ip:"+ip+"\nport:"+port);
			//attept to create the connection
			if(createSession())
				System.out.println("success, I am listening on ip "+ip+ " port "+port);
		}
	}
	//create a window class which we will use to play our tracks
	public PlayerWindow window(Player p) {
		for (int i = 0; i < count; i++) 
		{
			if (playerwindows[i].player == p)
				return playerwindows[i];
		}
		return null;
	}

	public boolean createSession()
	{
		try
		{
			//create a medialocator to open the video files from the rtp stream
			String url = "rtp://"+ip+":"+port+"/video/1";
			MediaLocator mrl = new MediaLocator(url);
			if(mrl==null)
			{
				System.err.println("mrl failure");
				return false;
			}
			//create a medialocator to open the audio files from the rtp stream
			String arl = "rtp://"+ip+":"+(port+2)+"/audio/1";
			video=Manager.createPlayer(mrl);
			mrl = new MediaLocator(arl);
			if(mrl==null){
				System.err.println("mrl failure");
				return false;
			}
			audio = Manager.createPlayer(mrl);
			System.out.println("Player created successfully!");
			if(video != null)
			{
				//realize the player if it is not null
				video.realize();
				//create a listener on the player
				video.addControllerListener(new ControllerEventHandler());
				//create a window for the player
				PlayerWindow v_window = new PlayerWindow(video);
				playerwindows[count] = v_window;
				System.out.println(count);
				count++;
			}
			if(audio != null)
			{
				//realize the player if it is not null
				audio.realize();
				//create a listener on the player
				audio.addControllerListener(new ControllerEventHandler());
				//create a window for the player
				PlayerWindow a_window = new PlayerWindow(audio);
				playerwindows[count] = a_window;
				System.out.println(count);
				count++;
			}
		}
		catch(NoPlayerException e)
		{
			System.err.println("Error:"+e);
			return false;
		}
		catch(MalformedURLException e)
		{
			System.err.println("Error: "+e);
			return false;
		}
		catch(IOException e)
		{
			System.err.println("Error: " +e);
			return false;
		}
		//return success
		return true;
	}
	
	public class ControllerEventHandler extends ControllerAdapter
	{
		public void realizeComplete(RealizeCompleteEvent dataHere)
		{
			//create the player from the source input
			Player p=(Player)dataHere.getSourceController();
			//create the window for our player
			PlayerWindow pw = window(p);
			//set the parameters of the player
			if(pw!=null)
			{
				pw.initialize();
				pw.setSize(600,600);
				pw.setVisible(true);
				p.start();
			}
		}
	}
	//the class to create windows for our players
	public class PlayerWindow extends Frame
	{
		Player player;
		ReceiveStream stream;
		//constructors
		public PlayerWindow(Player p)
		{
			player=p;
			//close the window if the 'X' is pressed
			addWindowListener(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent windowEvent)
				{
					player.close();
					setVisible(false);
					dispose();
					count --;
				}
			}
		
		);
		}
		public PlayerWindow(Player p,ReceiveStream strm)
		{
			player=p;
			stream=strm;
		}
		//create a new panel
		public void initialize()
		{
			add(new PlayerPanel(player));
			
		}
		//close the window
		public void close()
		{
			player.close();
			setVisible(false);
			dispose();
			count --;
		}
		public void addNotify()
		{
			super.addNotify();
			pack();
		}
	}
	
	public class PlayerPanel extends Panel
	{
		Component visual;
		//constructor
		public PlayerPanel(Player p)
		{
			//set the layout of the panel
			setLayout(new BorderLayout());
			if((visual=p.getVisualComponent())!=null)
				add("Center",visual);
		}
	}
	//create the player and set its starting parameters
	public static void main(String argv[])
	{
		
	}
}