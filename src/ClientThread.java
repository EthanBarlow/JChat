import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/*
Will handle all the message sending, receiving and processing
 */
public class ClientThread extends Thread
{
    //socket representing the client that makes a connection to the server
    private Socket client;
    private PrintWriter out; //used to send messages out to the actual client
    private Scanner in; //used to take in messages from the actual client
    private Set<String> userSet; //holds all the usernames
    private String userName; //the username of the client that this ClientThread is interacting with

    /*Constructor taking in a Socket object and a map object.
        The map will be used to send a list of online users
    */
    public ClientThread(Socket s) throws Exception
    {
       client = s;
       out = new PrintWriter(s.getOutputStream(), true);
       in = new Scanner(s.getInputStream());
       this.start();
    }

    public void run()
    {
        while(in.hasNextLine())
        {
            String message = in.nextLine();
            System.out.println(this.getName()+" " + message);
            out.println(message);
        }
    }
   /* public ClientThread(Socket s, Set<String> m) throws Exception
    {
        userSet=new HashSet<>();
        //copies the entire set over to the ClientThread's version of the set
        userSet.addAll(m);
        client = s;
        out = new PrintWriter(s.getOutputStream(), true); //getOutputStream, true for autoflush
        in = new Scanner(s.getInputStream());
        //this.start();//starts the thread right away
    }

    public void run()
    {
        while(true)
        {
            out.println("Enter your username (starting with '@': ");
            userName = in.nextLine();
            if(userSet.add(userName)&&userName.contains("@"))//checks for a valid username
            {
                System.out.println("Valid username: " + userName);
                break;
            }
        }

        out.println("Here are a list of users that are currently online:");
        for(String usr : userSet)//prints out all of the users that are online
            //if(!usr.equals(userName))//excludes the username of this ClientThread
            out.println(usr);
        in.nextLine();
        while(in.hasNextLine())
        {
            String message = in.nextLine(); //read the message sent from a client
            out.println(message);
        }
    }*/

    //public Map<String, ClientThread> getUpdatedUserMap(){return userMap;}

    public String getUserName(){return userName;}

}
