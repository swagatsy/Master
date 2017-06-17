
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Swagat Yadawad
 */
public class JServer {
    /*  NewJFrame object corresponding to the start window*/
    /*  socket at MOPTro*/
    Socket socket = null;
    JChatComm messenger=null;
    /*  Strings corresponding to message to be sent and received*/
    String lineSent="", lineReceived="";
    /*  integer corresponding to the error which occurred during the connection  session*/
    int error = 0;
    public JServer(CustomerUI cs,String ipAddress,String mac, int flag)         //s is the ip address of the "Client Moptro"
    {
        try
        {   System.out.println("Trying to connect to mobile...");
            InetAddress ip = InetAddress.getByName(ipAddress);
            System.out.println(ip.getHostName());
            System.out.println(ip);
            socket = new Socket(ip.getHostName(), 5233);
            callServer(mac,flag);
        }
        catch(UnknownHostException uhe)
        {   System.out.println("Host unknown: " + uhe.getMessage());
            error=1;
        }

        catch(IOException ioe)
        {   System.out.println("Unexpected Exception: "+ ioe.getMessage());
            error=2;
        }
    }
    /* Greendzine-MasterApp "Chat-Protocol" :
     * The MasterApp is expected to send "Greendzine_Client_MasterApp" to ensure that GDT_Picker app
     * connects to only a Greendzine MasterApp "server".
     * Also tha app is expected to send back "Client_Moptro" to complete the verification from the MasterApp side*/

    /*  After protocol is satisfied, the function waits in a loop untill lineSent is set to some command/request
     *  and then sends the command/request using the JChatComm object messenger's sendMessag() function.
     *  Then it waits to receive some status from the MOPTro and calls settingText() function of NewJFrame object
     *  njf1 to display the status on the corresponding window.
     *  It only comes out of the loop if lineSent is set "End Chat".
     *
     *  If protocol is not satisfied the Sockets are simply closed using messenger's endChat() function.
     *
     */
    public void callServer(String mac,int flag) throws IOException
    {   
          
            System.out.println("MAC Address is "+mac);
            
//          socket = server.accept();
            messenger = new JChatComm(socket);
            System.out.println("Socket opened");
            messenger.sendMessage("Greendzine_Client_MasterApp");
            
//            while(JServer.this.lineReceived == null || JServer.this.lineReceived == "" )
//            {
                JServer.this.lineReceived = messenger.receiveMessage();
//            }
//            JServer.this.lineReceived = "Client_Moptro";
            /*  enter this block only if protocol is satisfied*/
            System.out.println("This is it "+JServer.this.lineReceived);
            
//            while(!JServer.this.lineReceived.equals("End_Chat"))
//            {
                if(JServer.this.lineReceived.equals("Client_Moptro"))
                {
                    //System.out.println(JServer.this.lineReceived);
                    
                    // creating database and editing it; this is temporary 
                    
//                    class DBedit extends Thread
//                    {
//                        // insert here runtime code to run on command prompt and manage cloud database;
//                        public void run()
//                        {
//                         
//                            String databaseName = "moptro",instanceConnectionName="gdtdata-168800:asia-east1:gdtdata";
//                            String username="GDT",password="gdt@12345";
//                            String jdbcUrl = String.format("jdbc:mysql://google/%s?cloudSqlInstance=%s&"
//                                    + "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
//                                    databaseName,instanceConnectionName);
// 
//                            Connection connection = null;
//                            try 
//                            {
//                                connection = DriverManager.getConnection(jdbcUrl, username, password);
//                                //[END doc-example]
//                            } catch (SQLException ex) {
//                                Logger.getLogger(JServer.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//
//                            try (Statement statement = connection.createStatement()) 
//                            {
//                                String check = String.format("select exists(select * from test where mac='%s')",mac);
//                                String query ;
//                                 
//                                ResultSet res = statement.executeQuery(check);
//                                res.next();
//                                int result = Integer.parseInt(res.getString(1));
//                                if(result ==1)
//                                {
//                                    // query to remove previous data and put new data
//                                    query = "";
//                                    System.out.println("Data already exists");
////                                    statement.executeUpdate(query);
//                                }
//                                else
//                                {
//                                    // query to put new data
//                                    query = String.format("insert into test(mac) values('%s')",mac);
//                                    statement.executeUpdate(query);
//                                    System.out.println("Updated");
//                                }
//                             }
//                            catch (SQLException ex) 
//                            {
//                                Logger.getLogger(JServer.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                    }
//                    
                    
                    class Run extends Thread
                    {
                        public void run()
                        {
                            while(!JServer.this.lineReceived.equals("End_Chat"))
                            {   

//                                if(!JServer.this.lineSent.equals(""))
//                                {
                                   // System.out.println(JServer.this.lineSent);                               
                                   // System.out.println(JServer.this.lineSent);
                                    messenger.sendMessage("View Diagnostic");
//                                    JServer.this.lineSent="";
                                    JServer.this.lineReceived=messenger.receiveMessage();
                                    System.out.println(JServer.this.lineReceived);
//                                    njf1.settingText(JServer.this.lineReceived);
//                                }
                                    if(lineReceived.equals("Diag"))
                                    {
//                                        DBedit db = new DBedit();
//                                        db.start();
                                        lineSent="End_Chat";
                                        messenger.sendMessage("End_Chat");
                                    }
//                                else
//                                {
//                                    continue;
//                                }
                            }
//                            if(JServer.this.lineReceived.equals("End_Chat"))
//                                messenger.sendMessage(JServer.this.lineSent);

                            System.out.println("The ending statement "+JServer.this.lineSent);
                            JServer.this.lineSent = "";
                            messenger.endChat();
                        }
                    }
//                    DBedit db = new DBedit();
//                    db.start();
                    if(flag==1)
                    {
//                        DBedit db = new DBedit();
//                        db.start();
                    }
                    else
                    {
                        Run r1 = new Run();
                        r1.start();
                    }
                }
                else
                {   
//                    System.out.println("TimeOut!!! No affirmative response received!");
//                    error=1;
//                    messenger.endChat();
                    while(JServer.this.lineReceived == null || JServer.this.lineReceived == "" )
                    {
                        JServer.this.lineReceived = messenger.receiveMessage();
                    }
                    
                }
            }
        
//        catch(IOException ioe)
//        {   if(messenger != null)
//               messenger.endChat();
//            error = 2;
//        }
    
}
