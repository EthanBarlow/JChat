import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
This is the ChatClient class that will be run by the user and will be the interface through which messages are sent
 */
public class ChatClient
{
    private static String USERNAME="";

    public static void main(String[] a)
    {
        try
        {
            String message="";

            //the socket object that will connect to the server
            Socket server = new Socket("148.137.223.190", 4336);
            //sends messages out to the server, use true as a parameter to make sure that the stream auto-flushes
            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
            //receives messages from the server
            Scanner input = new Scanner(server.getInputStream());
            //used for keyboard input
            Scanner keyboard = new Scanner(System.in);

            ServerResponse sr = new ServerResponse(input);
            KeyboardThenSend kts = new KeyboardThenSend(keyboard,out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//end main method

    //Two classes that extend the Thread class: one for server input, one for keyboard input and for client output

    //This class will handle received messages from the server
    private static class ServerResponse extends Thread
    {
        String msgFromServer ="";
        Scanner serverInput;
        private ServerResponse(Scanner i)
        {
            serverInput=i;
            this.start();
        }

        public void run()
        {
            while(true)
            {
                msgFromServer=serverInput.nextLine();
                System.out.println(msgFromServer);
            }//end while loop
        }
    }//end ServerResponse class

    //This class will handle keyboard input and sending messages
    private static class KeyboardThenSend extends Thread
    {
        Scanner keyboard;
        PrintWriter lineToServer;
        private KeyboardThenSend(Scanner i, PrintWriter pw)
        {
            keyboard=i;
            lineToServer=pw;
            this.start();
        }//end constructor

        public void run()
        {
            while(true)
            {
                String msgToSend=keyboard.nextLine();
                lineToServer.println(msgToSend);
            }//end while loop
        }//end run method
    }//end KeyboardThenSend class

}//end ChatClient class
