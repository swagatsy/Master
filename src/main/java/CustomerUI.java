
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.sql.*;
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
public class CustomerUI extends javax.swing.JFrame {

    JServer MasterAppServer;
    
    /**
     * Creates new form CustomerUI
     */
    public CustomerUI() {
        this.setTitle("MasterApp - Greendzine");
        initComponents();
    }
    
    
     public void close()
    {
        WindowEvent win = new WindowEvent(this,WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(win);
    }
    
    public CustomerUI(String user, boolean val, Connection con) throws SQLException, FileNotFoundException {
        this.setTitle("MasterApp - Greendzine");
        if(val == false)
        {
            Statement sta = con.createStatement();
            ResultSet rst = sta.executeQuery(String.format("select macadd from deviceinfo where owner='%s'",user));
            System.out.println("Fetching MAC Addresses...");
            PrintWriter writer=null;
            try {
                writer = new PrintWriter("Mac.txt", "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CustomerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            while(rst.next())
            {
                System.out.println(rst.getString(1));
                writer.println(rst.getString(1));
                writer.close();
            }
        }
        initComponents();
 
    }
    
    static ArrayList<String> ip=new ArrayList<String>();
    static ArrayList<String> macs = new ArrayList<String>();
   

    
    public int setWifiStateLabel()
    {
        int ret=0;
        try{
            int flag = 0;
            Runtime rt = Runtime.getRuntime();
            /*  Using commandline to get the wlan/wireless network interface status*/
            Process pr = rt.exec("netsh wlan show interfaces");
            BufferedReader bf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String tmp = "";
            for(int i = 0; i < 8; i++)
            {
                tmp = bf.readLine();
            }
            /* checking the 8th line*/
            if(tmp.equals("    State                  : connected"))
            {
                ret=1;
            }
            
            else{
                ret=0;
                }
        }
        catch(IOException ioe){ 
            
            System.out.println(ioe.getMessage()); 
        }
        return ret;
    }
    
    /**
     *  Function to display the "Moptro - MAC address - ip address(if connected)" list in jList1, in jScrollPane2.
     *  Also, jProgressBar1 is activated to indicate processing is going on...(in GetMACAddresses())
     *  See GetMACAddresses().
     */
    public void displayChoices()
    {   
        jList1.setModel(new javax.swing.AbstractListModel() {
        ArrayList<String> strings = GetMACAddresses("Mac.txt");
        public int getSize() { return strings.size(); }
        public Object getElementAt(int i) { return strings.get(i); }
        });
//            }
        
        jScrollPane2.setAutoscrolls(true);
    }
    
    
    public void UpdateIPAddresses()
    {
        Runtime rt = Runtime.getRuntime();
        Process pr1;
        try{   pr1 = rt.exec("arp -d *");
        }
        catch(IOException ioe){ioe.printStackTrace();}

        /*  Default is set to 192.168.2.   if exception occurs*/
        String ipconfigOutput = "192.168.1.";
        try{
         String[] commands = {"ipconfig","/all"};
         pr1 = rt.exec("ipconfig");

         BufferedReader ipconfig_reader = new BufferedReader(new InputStreamReader(pr1.getInputStream()));
         String next ="";    int counter=0;
         /*  Reading lines in result of ipconfig */
         while((next=ipconfig_reader.readLine())!=null)
         {
             if(next.length()>=15)
             {  //System.out.println(next);
                String temp = next.substring(0, 15);
                //System.out.println(next);
                if(temp.equals("   IPv4 Address")) break;
             }
                 /*  IP address can be found at line:     IPv4 Address. . . . . . . . . . . : X.Y.Z.W*/
         }

         /* next is the 46th line of the result*/
         ipconfigOutput = next;
         System.out.println(ipconfigOutput);
         /* NOTE that if device is not connected a different line is read and we need to take care of that case.
            We check for length of the line, check if it has string of the form X.Y.Z.W(a-z)*  where X,Y, Z, W are numbers 0-255
           This is done using StringTokenizer with delimeter "."
         */
         if(ipconfigOutput!=null && ipconfigOutput.length()>39){
             /* IP address starts at the 39th charecter*/
            ipconfigOutput = ipconfigOutput.substring(39);
            System.out.println("IP address: "+ipconfigOutput);
            StringTokenizer ipChar = new StringTokenizer(ipconfigOutput,".");
            ipconfigOutput = "";
            /*  To get the first 3 charecters/8-bit numbers of IP address of the network. X.X.X. in IP address X.X.X.Y */
            for(int i=0;i<3;i++){
                if(ipChar.hasMoreTokens())
                    ipconfigOutput = ipconfigOutput + ipChar.nextToken() + ".";
                }
            }
         }
         catch(IOException ioe){ioe.printStackTrace();}
        
        /* We need a final/constant variable in the Local Class PingingThread */
         final String IPv4Address = ipconfigOutput ;
         System.out.println("IP address: "+IPv4Address);
            class PingingThread extends Thread{
                @Override
                public void run(){
                    Runtime rt = Runtime.getRuntime();
                    for(int i=1;i<255;i++)
                    {
                        final int j=i;
                    /*  ping all 255 IP addresses on the current network */
                        String localNetworkIPAddr = "ping " + IPv4Address + new Integer(j).toString();
                        try{
                           rt.exec(localNetworkIPAddr);
                        }
                        catch(IOException ioe){ioe.printStackTrace();
                        }
                    }
                }
             }
            PingingThread pt = new PingingThread();
            pt.start();
         
    }
    
    
        /*!  Function to get the list of Moptro's MAC-IP address pairs
     *
     *   Reads from the file, whose name is given as parameter here. Coresponding to each MAC address in the file
     *   Function finds the corresponding IP address and creates both a MACArrayList and ip ArrayList objects.
     *   First ARP Cache is updated using UpdateIPAddress() and then for each MAC Address, IP address is looked up using getIP()
     *   @param filename
     *   @returns MACArrayList
     */
    public ArrayList<String> GetMACAddresses(String filename)
    {
        ArrayList<String> MACArrayList = new ArrayList<String>();
        try{
          //  InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
            InputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String next = "";
            int i = 1;
            UpdateIPAddresses();
            while((next = br.readLine()) != null)
            {
                System.out.println("Print MAC "+next);
                macs.add(next);
                ip.add(getIP(next));
                MACArrayList.add("Moptro" + (i) + " - " + next + " - " + ip.get(i-1));
                System.out.println("IP      "+ip.get(i-1));
                i++;
            }
        }
        catch(Exception e){ e.printStackTrace();}
        return MACArrayList;
    }
    
    public String getIP(String mac) throws IOException
    {
        Runtime rt;
        //while(true)
        //{ 
        System.out.println(Thread.activeCount());
            /*  To ensure all the pinging threads are dead/closed and only the 4(threshold) required threads are running-(based on observation)
             [therse include main thread, wifi thread, current thread , ? ]
             if ever any new thread is added please update the threshold of 4(threads)*/
           // if(Thread.activeCount() > 4) continue;
            rt = Runtime.getRuntime();
            Process pr = rt.exec("arp -a");
            BufferedReader bf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            String ip="";
            while((line = bf.readLine()) != null)
            {   
//                System.out.println(line);
                StringTokenizer st = new StringTokenizer(line);
                int len = st.countTokens();
                if(len == 3)
                {   ip = st.nextToken();
                    String mac_try = st.nextToken();
                    if(mac_try.equals(mac))
                    {   
//                        System.out.println("IP address of device is: " + ip);
                        break;
                    }
                    else
                    {
                        ip="";
                    }
                }
            }
           return ip;
        //}
        
        
             
        
    }

    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jButton4 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("List of Devices");

        jButton1.setText("Diagnose");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Forward");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel2.setText("Report");

        jButton3.setText("EXIT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Sitka Heading", 1, 36)); // NOI18N
        jLabel4.setText("Customer Master App");

        jScrollPane2.setViewportView(jList1);

        jButton4.setText("LOAD");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Logout");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(147, 147, 147)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(241, 241, 241))
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(66, 66, 66)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(38, 38, 38))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton4)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(166, 166, 166)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(106, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(57, 57, 57)
                                .addComponent(jLabel5)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        // load the devices' MAC button
       if(setWifiStateLabel()==1)
       {
        jLabel6.setText("");
        displayChoices();           
       }
       else
       {
           jLabel6.setText("WiFi Disconnected");
       }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        // Diagnose button and display in the report block on right 
        int i = jList1.getSelectedIndex();
        if(i == -1){
            System.out.println("No Moptro Connected");
        }
        else{
            System.out.println("New Moptro Selected!!");
            String ip1 = ip.get(i);
            String mac = macs.get(i);
            System.out.println(ip1);
            //the flag value for diagnosing is 0
            MasterAppServer = new JServer(this,ip1,mac,0);
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        //Forward to cloud
        int i = jList1.getSelectedIndex();
        if(i == -1){
            System.out.println("No Moptro Connected");
//            jTextField4.setText("No devices connected");
//            jDialog1.setVisible(true);
        }
        else{
            System.out.println("New Moptro Selected!!");
            String ip1 = ip.get(i);
            String mac = macs.get(i);
            System.out.println(ip1);
            //the flag value for forwarding is 1
            MasterAppServer = new JServer(this,ip1,mac,1);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        //Logout button
        Login lg =new Login();
        lg.setVisible(true);
        close();
        
    }//GEN-LAST:event_jButton5ActionPerformed

    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CustomerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CustomerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CustomerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CustomerUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CustomerUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
