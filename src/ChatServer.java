/*
This is the chat server class that will setup all connections with clients

This class needs the following attributes:
    ServerSocket - to listen for new connections
    Map<String username, ClientThread client>



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

        /*Constructor taking in a Socket object and a map object.
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
                //userName must not already be in the map, and the userName must contain "@"
                if (!USERMAP.keySet().contains(userName) && userName.contains("@"))
                {
                    out.println("Successful connection!");
                    break;
                }
                out.println("Invalid username...");
            }//end while loop
            System.out.println("Listening for messages");

            USERMAP.put(userName, client);
            System.out.println(USERMAP);

            while(true)//waiting for input from the client
            {
                System.out.println("In the waiting loop");
                String msg="";
                out.println(userName + ": ");
                msg = in.nextLine();
                System.out.println(msg);
                out.println(userName+ " says " + msg);
            }
        }//end run method

    }//end ClientThread class

}//end class main
