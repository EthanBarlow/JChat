/*
This is the chat server class that will setup all connections with clients

This class needs the following attributes:
    ServerSocket - to listen for new connections
    Map<String, Socket>



 */

import com.sun.deploy.util.SessionState;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer
{
     private static final Map<String, Socket> USERMAP = new HashMap<>(); // will hold the usernames as the key, and the ClientThread object as the value

    //this main method will run the ChatServer
    public static void main(String[] a)
    {
        try
        {
            ServerSocket s = new ServerSocket(4336);
            while(true)
            {
                Socket client = s.accept();//waiting for a connection
                System.out.println("Connected!");
                new ClientThread(client);
            }
        }//end try block

        catch (Exception e)
        {
            e.printStackTrace();
        }

    }//end main method

    /*
Will handle all the message sending, receiving and processing
 */
    private static class ClientThread extends Thread
    {
        //socket representing the client that makes a connection to the server
        private Socket client;
        private PrintWriter out; //used to send messages out to the actual client
        private Scanner in; //used to take in messages from the actual client
        private String userName; //the username of the client that this ClientThread is interacting with

        /*Constructor taking in a Socket object.
            The map will be used to send a list of online users
        */
        public ClientThread(Socket s) throws Exception
        {
            System.out.println("In clientThread constructor");
            client = s;
            out = new PrintWriter(s.getOutputStream(), true);
            in = new Scanner(s.getInputStream());
            this.start();
        }

        public void run()
        {
            System.out.println("In clientThread run method");
            //testing for a username
            while(true)
            {
                out.println("Please enter a username starting with '@': ");
                userName = in.nextLine();
                /*userName must not already be in the map, and the userName must contain "@".
                It cannot == "@" or contain "@server" (
                this is reserved for messages that the server itself will send to clients)*/
                if (!USERMAP.keySet().contains(userName) && userName.contains("@") && !userName.equals("@") && !userName.contains("@server"))
                    break;

                out.println("Invalid username...");
            }//end while loop
            System.out.println("Listening for messages");

            USERMAP.put(userName, client);

            sendMessage(userName, "@server", "\n\n--------Welcome to JChat!--------\nTo " +
                    "send a message to someone use the following format:\n\"@username%message\"\nand " +
                    "for multiuser message:\n\"@username%@username%...usernames...%message\"\n\n" +
                    "Have fun!");

            sendMessage(USERMAP.keySet(), "@server", userName + " is now available to chat!");
            System.out.println(USERMAP);

            while(true)//waiting for input from the client
            {
                System.out.println("In the waiting loop");
                String msg="";
                msg = in.nextLine();

                //processing the message
                String[] msgParts = msg.split("%");
                //the last element in msgParts should contain the actual message
                for (String s:msgParts) {
                    System.out.println(s);
                }

                Set<String> recipient = new HashSet<String>();
                for(String user: msgParts)
                {
                    if(USERMAP.containsKey(user))
                        recipient.add(user);
                    System.out.println(user);
                }
                for(String s: recipient)
                    System.out.println(s);

                System.out.println("username " + userName);
                System.out.println(msgParts[msgParts.length-1]);

                sendMessage(recipient, userName, msgParts[msgParts.length-1]);

                System.out.println("still in the loop..." + msg);
            }
        }//end run method

        /**
         * This method will loop through the recipientSet to send a message to each user in the set.
         * The loop will access the input stream of each recipient in the set using a getter method to send the message
         * @param recipientSet The set of users to receive the message.
         * @param sender The user sending the message.
         * @param message The message to be sent.
         */
        private void sendMessage(Set<String> recipientSet, String sender, String message)
        {
            System.out.println("In send message before any processing");
            PrintWriter tempWrite;
            System.out.println("set's size: " + recipientSet.size());
            for(String recipient: recipientSet)
            {
                System.out.println("in for recipientSet loop");
               if(!recipient.equals(userName))//don't send this message to the sender
               {
                   try {
                       System.out.println("In try block");
                       Socket partner = (Socket) USERMAP.get(recipient);
                       PrintWriter partnerOut = new PrintWriter(partner.getOutputStream(), true);
                       partnerOut.println(sender + " : " + message);
                       System.out.println("In sendMessage method " + sender + ": " + message);
                   } catch (IOException e) {
                       System.out.println("In catch block");
                       e.printStackTrace();
                       System.out.println("The message from " + sender + " could not be sent to " + recipient);
                   }
                   System.out.println("end of for loop");
               }//end if statement
            }//end for loop
            System.out.println("end of sendMessage method");
        }

 private void sendMessage(String recip, String sender, String message)
        {
            System.out.println("In alternatice send message before any processing");
            PrintWriter tempWrite;

            try
            {
                System.out.println("In try block");
                Socket partner = (Socket) USERMAP.get(recip);
                PrintWriter partnerOut = new PrintWriter(partner.getOutputStream(), true);
                partnerOut.println(sender + " : " + message);
                System.out.println("In sendMessage method " +sender + ": " + message);
            }
            catch (IOException e)
            {
                System.out.println("In catch block");
                e.printStackTrace();
                System.out.println("The message from " + sender + " could not be sent to " + recip);
            }

            System.out.println("end of for loop");
        }//end alternative sendMessage method

    }//end ClientThread class

}//end class main
