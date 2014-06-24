import java.io.*;
import java.net.*;
import javax.media.rtp.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.rtp.event.*;
import javax.swing.*;
import javax.media.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
	

public class stream_client extends JFrame
{
	private Container content;
	private Component c;
	private Panel panel2;
	private JTextArea log;
	private JButton start;
	private JButton stop;
	private JButton show;
	private JLabel status;
	private String myName = "-999";
	private String myIP = "-999";
	private int myPort = -999;
	private String name = "-999";
	private int port = -999;
	private String ip = "-999";
	private int size = 4;
	private String members[];
	private String addresses[];
	private int ports[];
	private boolean started = false;
	private JScrollPane panel1;
	private String serverIP;
	
	public stream_client()
	{
		super("Connection Info"); // Set the Window Title
		// Initialize the Buttons and panels
		log = new JTextArea();
		log.setColumns(20);
		log.setLineWrap(true);
		log.setRows(8);
		log.setWrapStyleWord(true);
		log.setEditable(false);
		//log.append("Hello World\n");
		//log.append("Hello World\n");
		start = new JButton("Connect to Server");
		stop = new JButton("Disconnect");
		show = new JButton("Display Connected Users");
		status = new JLabel("Server Status: Disconnected");
		panel1 = new JScrollPane(log);
		panel2 = new Panel();
		members = new String[size];
		addresses = new String[size];
		ports = new int[size];
	}
	//create the gui
	public void GUI_Init()
	{
		content = getContentPane();
		content.add(panel1, BorderLayout.NORTH);
		content.add(panel2, BorderLayout.SOUTH);
		panel2.setLayout(new GridLayout(1,1));
		panel2.add(status);
		panel2.add(start);
		panel2.add(stop);
		panel2.add(show);
		panel2.setBounds(0, 0, 500, 500);
	}
	//create the control handler
	public void Control_Handler()
	{
		Control_Start start_act = new Control_Start();
		start.addActionListener(start_act);
		
		Control_Stop stop_act = new Control_Stop();
		stop.addActionListener(stop_act);
		
		Control_Show show_act = new Control_Show();
		show.addActionListener(show_act);
	}
	//the button handler that outputs the current users in the system
	public class Control_Show implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			who();
		}
	}
	//connect to the server
	public class Control_Start implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println(started);
			if(!started)
			{
				SwingWorker myWorker= new SwingWorker<String, Void>() 
				{
					@Override
					protected String doInBackground() throws Exception 
					{
						populate();
						started = true;
						Connect();
						//Execute your logic
						return null;
					}
				};
				myWorker.execute();
				

			}
		}
	}
	//disconnect from the server
	public class Control_Stop implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println(started);
			if(started)
			{
				SwingWorker myWorker= new SwingWorker<String, Void>() 
				{
					@Override
					protected String doInBackground() throws Exception 
					{
						started = false;
						Disconnect();
						//Execute your logic
						return null;
					}
				};
				myWorker.execute();
				

			}
		}
	}
	//open a file to send
	public File OpenFile()
	{	
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(chooser.FILES_ONLY);
		chooser.showOpenDialog(this);
		return chooser.getSelectedFile();
	}
	public String getIP(String title)
	{
		//open a window for the user to input the IP address they want to send to
		String input = JOptionPane.showInputDialog(this, title);
		if(input !=null && input.length() == 0)
		{
			System.err.println("No input");
			return null;
		}
		return input;
	}
	public String getPort(String title)
	{
		//open a window for the user to input the IP address they want to send to
		String input = JOptionPane.showInputDialog(this, title);
		if(input !=null && input.length() == 0)
		{
			System.err.println("No input");
			return null;
		}
		return input;
	}
	public String getName()
	{
		//open a window for the user to input the IP address they want to send to
		String input = JOptionPane.showInputDialog(this, "Enter Name:");
		if(input !=null && input.length() == 0)
		{
			System.err.println("No input");
			return null;
		}
		return input;
	}

	public void Disconnect()
	{
		int portNumber = 9000;
		String input;
		boolean connected = true;
		//open the socket that the server should be listening on and then send a leave request
		try ( 
			Socket clientSocket = new Socket(serverIP,portNumber);
			PrintWriter out= new PrintWriter(clientSocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		) 
		{
			out.println("leave:"+myName+":"+myIP+":"+myPort);
			while(connected)
			{
				System.out.println("waiting on remove conformation");
				if((input=in.readLine())!=null)
				{
					String pieces[] = input.split(":");
					if(pieces.length > 0)
					{
						if(pieces[0].equals("closing"))
						{
							log.append(pieces[1]+"\n");
							log.append("please close any current streaming processes before reconnecting\n");
							System.out.println("leaving conference");
							port = -999;
							ip = "-999";
							name = "-999";
							status.setText("Server Status: Disconnected");
							connected = false;
							clientSocket.close();
						}
					}
				}
			}
		}
		catch ( IOException ioException )
		{
			ioException.printStackTrace();
			return;
		}
	}
	
	public void Connect()
	{
		String serverIP = getIP("Enter the server's IP address");
		int portNumber = Integer.parseInt(getPort("Enter the servers port"));;
		boolean connected = false;
		name = getName();
		ip = getIP("Enter your IP address");
		port = Integer.parseInt(getPort("Enter your broadcast port"));
		myIP = ip;
		myName = name;
		myPort = port;
		String input;
		log.append("welcome "+name+"\n");
		//open the socket that the server should be listening on and then send a leave request
		try ( 
			Socket clientSocket = new Socket(serverIP,portNumber);
			PrintWriter out= new PrintWriter(clientSocket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		) 
		{
			out.println("join:"+name+":"+ip+":"+port);
			add_Member();
			while(!connected)
			{
				if((input=in.readLine())!=null)
				{
					String pieces[] = input.split(":");
					if(pieces.length == 4)
					{
						String request = pieces[0];
						//if the server has other members in the system it will send add requests for each one
						if(request.equals("add"))
						{
							name = pieces[1];
							ip = pieces[2];
							port = Integer.parseInt(pieces[3]);
							System.out.println(name+"@"+ip+":"+port);
							log.append(name+"@"+ip+":"+port+"\n");
							//add member
							add_Member();
							//create the session joiner to allow the client to play other members streams
							SessionJoiner sessions = new SessionJoiner(myName, size, members, addresses, ports);
							//ReceivePlayer player = new ReceivePlayer(port);
							//playerThread player = new playerThread(port);
						}
					}
					else 
					{
						for(int i=0; i<pieces.length;i++)
						{
							System.out.print(pieces[i]);
							log.append(pieces[i]);
						}
						System.out.print("\n");
						log.append("\n");
						//handle closing the socket
						if(pieces.length > 0)
						{
							if(pieces[0].equals("closing"))
							{
								if(pieces.length ==1)
								{
									//open the sender
									SenderTest sender = new SenderTest(myPort);
									System.out.println("closing initial connection\n");
									connected = true;
									status.setText("Server Status: Connected");
									//output the current members
									who();
									clientSocket.close();
								}
								break;
							}
						}
					}
				}
			}
		}
		catch ( IOException ioException )
		{
			System.out.println("exception in connect");
			ioException.printStackTrace();
			return;
		}
		ip = "-999";
		port = -999;
		name = "-999";
		//wait on its own socket for input
		process();
	}
	//add a new member
	public void add_Member()
	{
		//log.append(name+"joined @"+ip+":"+port+"\n");
		int added = 0;
		for(int i=0;i<size;i++)
		{
			if(ports[i] == -999)
			{
				ports[i] = port;
				members[i] = name;
				addresses[i] = ip;
				added = 1;
				return;
			}
		}
		if(added ==0)
		{
			int tempSize = size;
			if(increase_size())
			{
				ports[tempSize] = port;
				members[tempSize] = name;
				addresses[tempSize] = ip;
				added = 1;
				return;
			}
		}
		System.out.println("Add Failed");
	}
	//remove an old member
	public void remove_Member()
	{
		for(int i=0;i<size;i++)
		{
			if(name.equals(members[i]))
			{
				members[i] = "-999";
				ports[i] = -999;
				addresses[i] = "-999";
				System.out.println("removed "+name+"@"+ip+":"+port);
				return;
			}
		}
		System.out.println("Remove Failed");
	}

	//increase the size of the arrays as long as the new array would not be larger than allowed by the data type
	public boolean increase_size()
	{
		long temp = size;
		temp = temp*2;
		if( temp > (long)(Integer.MAX_VALUE -5))
		{
			String temp_Mem[] = new String[size*2];
			String temp_Add[] = new String[size*2];
			int temp_Ports[] = new int[size*2];
			for(int i=0;i<size;i++)
			{
				temp_Mem[i] = members[i];
				temp_Add[i] = addresses[i];
				temp_Ports[i] = ports[i];
			}
			members = temp_Mem;
			addresses = temp_Add;
			ports = temp_Ports;
			return true;
		}
		else
		{
			return false;
		}
	}
	//output the current users in the system to the text area
	public void who()
	{
		log.append("Users currently connected to the Server:\n");
		boolean displayed = false;
		for(int i=0;i<size;i++)
		{
			if(ports[i] > 2000)
			{
				log.append(members[i]+" @"+addresses[i]+":"+ports[i]+"\n");
				displayed = true;
			}
		}
		if(!displayed)
		{
			log.append("No users currently connected\n");
		}
	}
	//initialize the arrays
	public void populate()
	{
		for(int i=0;i<size;i++)
		{
			members[i] = "-999";
			addresses[i] = "-999";
			ports[i] = -999;
		}
	}
	//wait on a socket for input from the server
	public void process()
	{
		int portNumber = myPort+100;
		String input;
		System.out.println("waiting on "+portNumber);
		while(true)
		{
			try ( 
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			) 
			{
				if((input=in.readLine())!=null)
				{
					String pieces[] = input.split(":");
					if(pieces.length == 4)
					{
						String request = pieces[0];
						//add request
						if(request.equals("add"))
						{
							name = pieces[1];
							ip = pieces[2];
							port = Integer.parseInt(pieces[3]);
							System.out.println(name+"@"+ip+":"+port);
							log.append(name+"@"+ip+":"+port+"\n");
							add_Member();
							//playerThread player = new playerThread(port);
							//create a new session joiner for the updated list
							SessionJoiner sessions = new SessionJoiner(myName, size, members, addresses, ports);
							
						}
						//remove request
						else if(request.equals("remove"))
						{
							name = pieces[1];
							ip = pieces[2];
							port = Integer.parseInt(pieces[3]);
							remove_Member();
							who();
							out.println("closing");
						}
					}
					//output any errors which are passed from the server
					else 
					{
						for(int i=0; i<pieces.length;i++)
						{
							System.out.print(pieces[i]);
							log.append(pieces[i]);
						}
						log.append("\n");
						System.out.println("");
					}
				}
			}
			catch ( IOException ioException )
			{
				ioException.printStackTrace();
				return;
			}
		}
	}
	public static void main(String args[])
	{
		stream_client client = new stream_client();
		client.GUI_Init();
		client.Control_Handler();
		client.setSize(750,200);
		client.setLocation(300,300);
		client.setDefaultCloseOperation(EXIT_ON_CLOSE);
		client.setVisible(true);
	}
}
	