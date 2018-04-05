import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/*
This is the ChatClient class that will be run by the user and will be the interface through which messages are sent
 */
public class ChatClient extends Application
{
    private String USERNAME="";
    private Scanner input;
    private TextArea messages;
    private TextArea typeArea;
    private Button send;

    public static void main(String[] a)
    {
        launch(a);
    }//end main method

    @Override
    public void start(Stage primaryStage) throws Exception
    {

        messages = new TextArea();
        messages.setEditable(false);
        typeArea = new TextArea();
        send = new Button("Send");

        try
        {
            String message="";

            //the socket object that will connect to the server
            Socket server = new Socket("148.137.223.190", 4336);
            //sends messages out to the server, use true as a parameter to make sure that the stream auto-flushes
            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
            //receives messages from the server
            input = new Scanner(server.getInputStream());
            //used for keyboard input
            Scanner keyboard = new Scanner(System.in);

            ServerResponse sr = new ServerResponse(input);
            KeyboardThenSend kts = new KeyboardThenSend(keyboard,out);

            send.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    String txt = "";
                    txt = typeArea.getText();
                    typeArea.clear();
                    out.println(txt);
                    messages.appendText(txt+"\n");
                }

            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        BorderPane root = new BorderPane();
        VBox msgCenter = new VBox();
        msgCenter.getChildren().addAll(messages, typeArea, send);
        root.setCenter(msgCenter);

        Scene sc = new Scene(root);
        primaryStage.setScene(sc);
        primaryStage.show();
    }

    //Two classes that extend the Thread class: one for server input, one for keyboard input and for client output

    //This class will handle received messages from the server
    private class ServerResponse extends Thread
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
                if(msgFromServer.contains("#quit"))
                {
                    System.out.println("Thank you for using JChat! Have a nice day!");
                    System.exit(0);
                }
                if(msgFromServer!=null)
                {
                    messages.appendText(msgFromServer+"\n");
                    System.out.println(msgFromServer);
                }
                /*new JFXPanel();
                Media sound = new Media(new File("Resources/received.mp3").toURI().toString());
                MediaPlayer mp = new MediaPlayer(sound);
                mp.play();*/
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
                new JFXPanel();
                Media sound = new Media(new File("Resources/sent.mp3").toURI().toString());
                MediaPlayer mp = new MediaPlayer(sound);
                mp.play();
            }//end while loop
        }//end run method
    }//end KeyboardThenSend class

}//end ChatClient class
