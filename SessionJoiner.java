import javax.swing.*;
import javax.media.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;

public class SessionJoiner extends JFrame 
{
	private JButton[] buttons;
	private String[] members;
	private String[] addresses;
	private int[] ports;
	private int size;
	private String myName;
	
	
	public SessionJoiner(String inMyName, int inSize, String inMembers[], String inAddresses[], int inPorts[])
	{
		super("Active Streams");
		myName = inMyName;
		size = inSize;
		members = new String[size];
		addresses = new String[size];
		ports = new int[size];
		members = inMembers;
		addresses = inAddresses;
		ports = inPorts;
		JButton buttons[] = new JButton[size];
		//test();
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,1));
		getContentPane().add(buttonPanel);
		//create an array of buttons, one for each member
		for(int i=0;i<size;i++)
		{
			System.out.println("making button "+i);
			if((!members[i].equals("-999"))&&(!(members[i].equals(myName))))
			{
				//System.out.println("1");
				buttons[i] = new JButton(members[i]);
				//System.out.println("2");
				buttonPanel.add(buttons[i]);
				//System.out.println("3");
				Control_button button_act = new Control_button();
				//System.out.println("4");
				buttons[i].addActionListener(button_act);	
				//System.out.println("5");
			}
		}
		
		setSize(650,150);
		setLocation(300,300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	//used for testing purposes only
	public void test()
	{
		for(int i=0;i<size;i++)
		{
			members[i] = ""+i;
			addresses[i] = ""+i;
			ports[i] = i;
			System.out.println(members[i]+addresses[i]+ports[i]);
		}
		System.out.println("initialization complete");
	}
	//if a button is pressed, open the player for the corresponding member
	class Control_button implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() instanceof JButton) 
			{
				String text = ((JButton) e.getSource()).getText();
				System.out.println(text);
				for(int j=0;j<size;j++)
				{
					if(members[j].equals(text))
					{
						ReceivePlayer player = new ReceivePlayer(ports[j]);
					}
				}
			}
		}
	}	
	public static void main(String args[])
	{
		//SessionJoiner x = new SessionJoiner();
	}
}