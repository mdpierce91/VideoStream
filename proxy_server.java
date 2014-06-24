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
import java.util.concurrent.TimeUnit;

public class proxy_server extends JFrame
{
	private Container content;
	private Component c;
	private JScrollPane panel1;
	private Panel panel2;
	private JTextArea log;
	private JButton start;
	//private JButton stop;
	private JButton show;
	private JLabel status;
	private String name = "-999";
	private int port = -999;
	private String ip = "-999";
	private int size = 4;
	private String members[];
	private String addresses[];
	private int ports[];
	private boolean started = false;
	
	public proxy_server()
	{
		super("Proxy Server"); // Set the Window Title
		// Initialize the Buttons and panels
		log = new JTextArea();
		log.setColumns(20);
		log.setLineWrap(true);
		log.setRows(8);
		log.setWrapStyleWord(true);
		log.setEditable(false);
		start = new JButton("Start Server");
		//stop = new JButton("Stop Server");
		show = new JButton("Show current members");
		status = new JLabel("Server Status: Offline");
		panel1 = new JScrollPane(log);
		panel2 = new Panel();
		members = new String[size];
		addresses = new String[size];
		ports = new int[size];
	}
	//initialize the GUI
	public void GUI_Init()
	{
		content = getContentPane();
		content.add(panel1, BorderLayout.NORTH);
		content.add(panel2, BorderLayout.SOUTH);
		panel2.setLayout(new GridLayout(1,1));
		panel2.add(status);
		panel2.add(start);
		//panel2.add(stop);
		panel2.add(show);
		panel2.setBounds(0, 0, 500, 500);
	}
	
	//handler for the buttons
	public void Control_Handler()
	{
		Control_Start start_act = new Control_Start();
		start.addActionListener(start_act);
		
		//Control_Stop stop_act = new Control_Stop();
		//stop.addActionListener(stop_act);
		
		Control_Show show_act = new Control_Show();
		show.addActionListener(show_act);
	}
	//start the server when this is pressed
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
						started = true;
						//new serverThread().start();
						//change the server status to online
						status.setText("Server Status: Online");
						log.append("Server started"+"\n");
						System.out.println("Starting");
						//IF IT ISN'T WORKING TRY UNCOMMENTING THE BELOW CODE IT FIXED SOMETHING BUT I DON'T REMEMBER WHAT
						/*try { 				
							TimeUnit.MILLISECONDS.sleep(500);
						} catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}*/
						//start the server
						Go();
						return null;
					}
				};
				myWorker.execute();	
			}
		}
	}
	//shows the users currently online
	public class Control_Show implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			log.append("The currently connected users are:\n");
			for(int i=0;i<size;i++)
			{
				if(ports[i] > 2000)
				{
					log.append(members[i]+"@"+addresses[i]+":"+ports[i]+"\n");
				}
			}
		}
	}
	/*public class Control_Stop implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println(started);
			if(started)
			{
				started = false;
				log.append("Server Stopped\n");
				status.setText("Server Status: Offline");
			}
		}
	}*/
	public String getPort()
	{
		//open a window for the user to input the IP address they want to send to
		String input = JOptionPane.showInputDialog(this, "Enter Port Number:");
		if(input !=null && input.length() == 0)
		{
			System.err.println("No input");
			return null;
		}
		return input;
	}
	public void Go()
	{
		String error = "-";
		//get the port you listen on
		int portNumber = Integer.parseInt(getPort());
		System.out.println("ready for connections1");
		int changed = 0;
		while(started)
		{
			changed = 0;
			System.out.println("ready for connections2");
			//create a socket and listen for incoming requests
			try ( 
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			) 
			{
				if(started)
				{
					System.out.println("ready for connections3");
					String input;
					if((input=in.readLine())!=null)
					{
						log.append("received packet\n");
						//read in the incoming packet and split it into the proper format
						String pieces[] = input.split(":");
						if(pieces.length == 4)
						{
							String request = pieces[0];
							name = pieces[1];
							ip = pieces[2];
							port = Integer.parseInt(pieces[3]);
							System.out.println("request: "+ request +" name: "+name+" ip address: "+ip+" port number: "+port);
							//if it is a join request:
							if(request.equals("join"))
							{
								//process the packet with the join code
								error = process(0);
								//if it was successful
								if(error.equals("+"))
								{
									for(int i=0; i<size;i++)
									{
										//System.out.println(members[i]);
										//System.out.println("name"+name);
										
										//if there are any other members tell the new member
										if((!(name.equals(members[i]))&&(!(ports[i]==-999))))
										{
											System.out.println("Sending "+members[i]+"'s information to "+name);//go to text box
											log.append("Sending "+members[i]+"'s information to "+name+"\n");
											out.println("add:"+members[i]+":"+addresses[i]+":"+ports[i]+"\n");
											
										}
									}
									//close the connection and reset the socket
									log.append("closing connection with "+name+"\n");
									out.println("closing");
									System.out.println("closing connection with "+name);
									changed = 1;

								}
								//if it is unsuccessful output an error and inform the sender it did not work and close the socket
								else if(error.equals("-"))
								{
									out.println("an unexpected error occurred");
									System.out.println("an unexpected error occurred");//go to text box
									log.append("closing:an unexpected error occurred\n");
								}
								//if it is an invalid request inform the sender and close the socket
								else
								{
									System.out.println("an invalid "+request+ " request was received: "+error);//go to text box
									log.append("an invalid "+request+ " request was received: "+error+"\n");
									out.println("closing:There was an error with your request: "+error+"\n");
								}
							}
							//if it is a leave request:
							else if(request.equals("leave"))
							{
								//process the packet with the leave code
								error = process(1);
								//if it is successful inform the client and close the socket
								if(error.equals("+"))
								{
									log.append("sent closing packet to removed client\n");
									out.println("closing:you have successfully been removed from the system.  Thank you and goodbye");
								}
								//if it is unsuccessful output an error and inform the sender it did not work and close the socket 
								else if(error.equals("-"))
								{
									out.println("an unexpected error occurred");
									System.out.println("closing:an unexpected error occurred");//go to text box
									log.append("an unexpected error occurred\n");
								}
								//if it is an invalid request inform the sender and close the socket
								else
								{
									System.out.println("an invalid "+request+ " request was received: "+error);//go to text box
									log.append("an invalid "+request+ " request was received: "+error+"\n");
									out.println("closing:There was an error with your request: "+error);
								}
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
			if(changed == 1)
			{
				//if a new member joined, inform the old members of a new member
				updateMembers();
			}
			name = "-999";
			port = -999;
			ip = "-999";
		}
	}

	public String process(int request)
	{
		//check that it is a valid request
		if(name.equals(null))
		{
			return "no name";
		}
		if(port < 2000)
		{
			return "invalid port number "+port+" please use a port number above 2000\n";
		}
		if(ip.equals("-999"))
		{
			return "no IP received";
		}
		//process join request
		if(request == 0)
		{

			for(int i=0; i<size;i++)
			{
				//check for duplicate values
				if(name.equals(members[i]))
				{
					return "member "+name+" already in use\n";
				}
				else if(port == ports[i])
				{
					return "port Number already in use: "+port+"\n";
				}
			}
			//add member
			add_Member();
		}
		//process leave requests
		else if(request == 1)
		{
			//check to see if it is in the system
			for(int i=0;i<size;i++)
			{
				if(name.equals(members[i]))
				{
					if(ip.equals(addresses[i]))
					{
						if(port == ports[i])
						{
							//remove member
							if(remove_Member())
							{	
								//return successful
								return"+";
							}
							else
							{
								//return unsuccessful
								System.out.println("remove failed");
							}
						}
					}
				}
			}
			//if it is not in the system return unsuccessful
			return name+"@"+ip+":"+port+" is not a member\n";
		}
		return "+";
	}
	
	public void updateMembers()
	{
		System.out.println("updating members");
		for(int i=0;i<size;i++)
		{
			//for each member which did not just join
			if((ports[i] != -999)&&(members[i] != name))
			{
				System.out.println("updating "+members[i]+" with "+name);
				String serverIP = addresses[i];
				int portNumber = ports[i]+100;
				//open a socket on the port they should be listening on and inform them of a new member
				try(
				Socket clientSocket = new Socket(serverIP,portNumber);
				PrintWriter out= new PrintWriter(clientSocket.getOutputStream(),true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				)
				{
					out.println("add:"+name+":"+ip+":"+port);
				}
				catch ( IOException ioException )
				{
					ioException.printStackTrace();
					return;
				}
			}
		}
	}

	public void add_Member()
	{
		int added = 0;
		for(int i=0;i<size;i++)
		{
			if(ports[i] == -999)
			{
				//add a new member to an empty slot
				ports[i] = port;
				members[i] = name;
				addresses[i] = ip;
				output_Text();
				added = 1;
				return;
			}
		}
		//if there are no new slots, increase the size of the array then add it
		if(added ==0)
		{
			int tempSize = size;
			if(increase_size())
			{
				ports[tempSize] = port;
				members[tempSize] = name;
				addresses[tempSize] = ip;
				output_Text();
				added = 1;
				return;
			}
		}
		System.out.println("Add Failed");
	}

	public boolean remove_Member()
	{
		boolean removed = false;
		for(int i=0;i<size;i++)
		{
			if(name.equals(members[i]))
			{
				//remove the member
				members[i] = "-999";
				ports[i] = -999;
				addresses[i] = "-999";
				System.out.println("removed "+name+"@"+ip+":"+port);
				removed = true;
			}
		}
		boolean done = true;
		//inform the other members that the member has been removed
		for(int i=0;i<size;i++)
		{
			if((ports[i] > 2000)&&(!members[i].equals(name)))
			{
				System.out.println("updating "+members[i]+" with "+name+"(removed)");
				String serverIP = addresses[i];
				int portNumber = ports[i]+100;
				try(
				Socket clientSocket = new Socket(serverIP,portNumber);
				PrintWriter out= new PrintWriter(clientSocket.getOutputStream(),true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				)
				{
					System.out.println("sending remove instruction to "+name);
					out.println("remove:"+name+":"+ip+":"+port);
					while(done)
					{
						//wait for a reply before closing the socket
						System.out.println("waiting for reply");
						String input;
						if((input=in.readLine())!=null)
						{
							log.append("received packet\n");
							String pieces[] = input.split(":");
							if(pieces[0].equals("closing"))
							{
								log.append("remove send complete\n");
								done = false;
								clientSocket.close();
							}
						}
					}
				}
				catch ( IOException ioException )
				{
					ioException.printStackTrace();
					return removed;
				}
			}
		}
		return removed;
	}

	public void output_Text()
	{
		//print to textbox 
		log.append("Member: "+name+" joined at ip: "+ip+" on port: "+port+"\n");
		System.out.println("Member: "+name+" joined at ip: "+ip+" on port: "+port);
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

	public static void main(String args[])
	{
		proxy_server myServer = new proxy_server();
		myServer.GUI_Init();
		myServer.Control_Handler();
		myServer.setSize(650,200);
		myServer.setLocation(300,300);
		myServer.setDefaultCloseOperation(EXIT_ON_CLOSE);
		myServer.setVisible(true);
		myServer.populate();
		//myServer.Go();
	}
}