
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Swagat Yadawad
 */
public class JChatComm
{
	/*  Input and output streams to communicate with the MasterApp*/
	private DataOutputStream streamOut =null;
	private DataInputStream streamIn=null;

	public JChatComm(Socket socket) throws IOException
	{	
		streamOut=new DataOutputStream(socket.getOutputStream());
		streamIn=new DataInputStream(socket.getInputStream());
	}

        /*! Function to send a message to a target socket
         *
         * This function sends packet to the target socket using writeUTF function of DataOutputStream object streamOut
         * @param packet
         * packet is the String to be sent tothe target socket
         * @throws IOException
         */
	public void sendMessage(String packet)
        {   try
            {	streamOut.writeUTF(packet);
                streamOut.flush();
            }
            catch(IOException ioe)
            {	//System.out.println("Sending Error: "+ioe.getMessage());
                ioe.printStackTrace();
            }
	}
        /*! Function to receive Message from a target socket
         *
         *  This function to receive a message from the target socket using readUTF function of the DataInputStream object streamIn
         *  A block function, waits until a string is received.
         */
	public String receiveMessage()
	{
            String packet = "";
            try
            {   packet = streamIn.readUTF();
                return packet;
            }
            catch(IOException e)
            {   System.out.println("Hello" + e.getMessage());}
            return packet;
	}
        /*! Function to end connection
         *
         *  This function closes the communication streams safely.
         */
	public void endChat()
	{   try
            {   if(streamOut !=null) streamOut.close();
                if(streamIn !=null) streamIn.close();
            }
            catch(IOException ioe)
            { System.out.println("Error!!! Ending Chat...");}
	}
}
