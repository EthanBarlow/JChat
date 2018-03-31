import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
This is the ChatClient class that will be run by the user and will be the interface through which messages are sent
 */
public class ChatClient
{
    public static void main(String[] a)
    {
        try
        {
            //the socket object that will connect to the server
            Socket server = new Socket("148.137.223.190", 4336);
            //sends messages out to the server, use true as a parameter to make sure that the stream auto-flushes
            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
            //receives messages from the server
            Scanner input = new Scanner(server.getInputStream());

            String message="";

            //used for keyboard input
            Scanner keyboard = new Scanner(System.in);

            while(true)
            {
                System.out.println(input.nextLine());
                message = keyboard.nextLine();

                out.println(message);
                //System.out.println("Your message echoed back by the server: " + input.nextLine());
                System.out.println(input.nextLine());
            }

            /*while(true)
            {
                System.out.println("From the server: "+input.nextLine());

                if(keyboard.hasNextLine())
                {
                    message = keyboard.nextLine();

                    //send the message to the server
                    out.println(message);
                    //get a response back from the server
                }
            }*/

           // System.out.println("Connected to server: " + server.getInetAddress());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
