/*
This is the chat server class that will setup all connections with clients

This class needs the following attributes:
    ServerSocket - to listen for new connections
    Map<String username, ClientThread client>



 */

import com.sun.deploy.util.SessionState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer
{

    //this main method will run the ChatServer
    public static void main(String[] a)
    {
        Map<String, ClientThread> userMap = new HashMap<>(); // will hold the usernames as the key, and the ClientThread object as the value

        try
        {
            ServerSocket s = new ServerSocket(4336);
            while(true)
            {
                Socket client = s.accept();
                System.out.println("Connected: " + client.getInetAddress());
                new ClientThread(client);
                /*Socket client = s.accept();//continuously waiting for a connection
                System.out.println("Connected: " + client.getInetAddress());

                ClientThread ct = new ClientThread(client, userMap.keySet());
                //add the ClientThread object to the map after processing out the username
                System.out.println("Username --- "+ct.getUserName());
                userMap.put(ct.getUserName(), ct);
                for(String str: userMap.keySet())
                    System.out.println(str);
                ct.start();*/
            }


        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
