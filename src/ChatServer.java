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
    private static final Map<String, String> COMMANDS = new HashMap<>(); //will hold the commands that the server recognizes as the key, and the corresponding descriptions

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
            The map will be used to send a list of online users*/
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
            loadCommands();
            System.out.println("In clientThread run method");
            //testing for a username
            while(true)
            {
                out.println("Please enter a username starting with '@' (max length: 30 characters): ");

                if(client.isClosed())
                {
                    USERMAP.remove(userName);
                    this.interrupt();
                    System.out.println("Stopping client socket now");
                }

                userName = in.nextLine();
                if (validUsername(userName))
                    break;

                out.println("Invalid username...");
            }//end while loop
            System.out.println("Listening for messages");

            USERMAP.put(userName, client);

            sendMessage(userName, "@server", "\n\n--------Welcome to JChat!--------\nTo " +
                    "send a message to someone use the following format:\n\"@username/message\"\nand " +
                    "for multi-user message:\n\"@username/@username/...usernames.../message\"\n\n" +
                    "If you need help, type \"#help\" "+"Have fun!");

            //alert the other users that this particular user is now online
            sendMessage(USERMAP.keySet(), "@server", userName + " is now available to chat!");
            listUsers();
            System.out.println(USERMAP);

            while(true)//waiting for input from the client
            {
                System.out.println("In the waiting loop");
                String msg="";
                msg = in.nextLine();

                //processing the message
                String[] msgParts = msg.split("/");

                if(msgParts[0].contains("#")) //checks for a server command
                    processCommands(msgParts);

                else
                {
                    //the last element in msgParts should contain the actual message
                    for (String s : msgParts) {
                        System.out.println(s);
                    }

                    Set<String> recipient = new HashSet<String>();
                    for (String user : msgParts) {
                        if (USERMAP.containsKey(user))
                            recipient.add(user);
                        System.out.println(user);
                    }
                    for (String s : recipient)
                        System.out.println(s);

                    System.out.println("username " + userName);
                    System.out.println(msgParts[msgParts.length - 1]);

                    //if a message is sent to more than one person, then append a message listing the recipients
                    if(msgParts.length>2)
                    {
                        String message=msgParts[msgParts.length-1];
                        message += " (also sent to: ";
                        int i=0;
                        for(;i<msgParts.length-2; i++)//loops through all but the last recipient adding them to the list of recipients
                            if(!msgParts[i].contains(userName))//excludes the sender from the list
                                message+=msgParts[i]+", ";
                        if(!msgParts[i].contains(userName))
                            message+=msgParts[i]+")";//adds the last user
                        else
                            message=message.substring(0,message.length()-2)+")";
                        sendMessage(recipient, userName, message);
                        //sends the message back to the user so that it displays correctly in the textarea
                        sendMessage(userName, userName, message);
                    }
                    else//one recipient
                    {
                        sendMessage(recipient, userName, msgParts[msgParts.length - 1]);
                        sendMessage(userName, userName, msgParts[msgParts.length-1]);
                    }

                    System.out.println("still in the loop..." + msg);
                }

                if(client.isClosed())
                {
                    USERMAP.remove(userName);
                    System.out.println("Stopping client socket now");
                    this.interrupt();
                }
            }
        }//end run method

        private boolean validUsername(String user)
        {
            if(user.length()<2)
                return false;

            if(user.length()>30)
                return false;

            if(user.contains(" "))
                return false;

            if(user.charAt(0)!='@')
                return false;

            if(user.substring(1,user.length()).contains("@"))
                return false;

            for(String s: COMMANDS.keySet())
                if(user.contains(s))
                    return false;

            for(String s: USERMAP.keySet())
                if(s.contains(user))
                    return false;

            if(user.contains("@server"))
                return false;

            return true;
        }

        private String getUserName(){return userName;}

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
            System.out.println("In alternative send message before any processing");
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

     //sends a message with the list of users currently online to this user
     private void listUsers()
     {
         String allUsers="Here is a list of all the users that are online: \n";
         for(String str: USERMAP.keySet())
         {
             if(!str.equals(userName))
                 allUsers+= "\t" + str + "\n";
         }
         sendMessage(userName, "@server", allUsers);
     }

     private void loadCommands() //this method loads the names of server commands into the set
     {
            COMMANDS.put("#allUsers", "Lists all of the users that are online and ready to chat.");
            COMMANDS.put("#exit", "Disconnects from the chat server and clears the session history.");
            COMMANDS.put("#about", "This program was designed and created by Ethan Barlow. \n You can find more of my projects on my website: https://ethanbarlow.github.io/");
            COMMANDS.put("#help", "Gives a list of commands that the server will accept and their corresponding descriptions.\nOnly one command can be sent at a time and must precede the message.");
            COMMANDS.put("#all", "Sends your message to everyone who is online. \n Format: #all/message");
     }

     private void processCommands(String[] msgPieces)
     {
         if(msgPieces[0].contains("#allUsers"))
             listUsers();
         else if(msgPieces[0].equals("#exit"))
         {
             sendMessage(getUserName(), "@server", "#exitNOW"+"exitNOW".hashCode());
             sendMessage(USERMAP.keySet(), "@server", getUserName()+" has left the chat. (To all)");
             try {this.client.close();}
             catch (IOException e) {e.printStackTrace();}
             this.interrupt();
         }
         else if(msgPieces[0].contains("#about"))
             sendMessage(getUserName(), "@server", COMMANDS.get("#about"));
         else if(msgPieces[0].contains("#help"))
         {
             sendMessage(getUserName(),"@server", "Here is a list of the available server commands: ");
             for(String str: COMMANDS.keySet())
             {
                 sendMessage(getUserName(), "", str);
                 sendMessage(getUserName(), "", "\t"+COMMANDS.get(str));
             }
         }
        else if(msgPieces[0].contains("#all"))
         {
             sendMessage(USERMAP.keySet(), getUserName(), "(To all)" + msgPieces[1]);
         }

         else
             sendMessage(getUserName(),"@server", "!!!That is not a valid command.!!!");

     }//end processCommands method

    }//end ClientThread class


}//end class main

/*
Protocol:

In the main method there is a while true loop waiting for connections from clients.
Then, a socket object is created and passed into the ClientThread class.
The server, first, requires a valid username from the client.
Once a valid username is entered the socket client will be added to a map.
After some initial welcome messages and directions, the ClientThread then enters a while true loop and listens for messages.
Then, the message is processed and appropriate actions are taken.

Commands to the server start with "#", any message containing "#" is then sent to the processCommands method. The
corresponding procedures are then followed. If the message containing "#" is not a valid command, the server send back
a message notifying the user that the command is not valid.

Sending a message from one client to another is taken care of in the sendMessage method:
- Messages will only be sent to recipients other than the sender.
- The recipient then receives a message in the following format:
    username: message

Usernames are required to start with "@"
Multiple recipients should be separated with "/"
And the message itself should be separated from the users in the same way

Message strings are split up into an array of Strings. The splitting up is based on the "/"

So, @drlu/How are you? is split into
    @drlu
    How are you?

Then, "How are you?" is sent to the user, @drlu
The recipient then sees a message like this
    @ethan: How are you?

 */

