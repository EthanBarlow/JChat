import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
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
    private Socket server;
    private Stage ps;
    private PrintWriter out;

    public static void main(String[] a)
    {
        launch(a);
    }//end main method

    @Override
    public void start(Stage primaryStage) throws Exception
    {

        ps=primaryStage;
        messages = new TextArea();
        messages.setEditable(false);
        messages.setWrapText(true);
        typeArea = new TextArea();


        try
        {
            //String message="";

            //the socket object that will connect to the server
            server = new Socket("148.137.223.190"/*"148.137.141.18"*/, 4336);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        //sends messages out to the server, use true as a parameter to make sure that the stream auto-flushes
        out = new PrintWriter(server.getOutputStream(), true);
        //receives messages from the server
        input = new Scanner(server.getInputStream());
        //used for keyboard input
        Scanner keyboard = new Scanner(System.in);

        ServerResponse sr = new ServerResponse(input);

        BorderPane root = new BorderPane();
        VBox msgCenter = new VBox();
        msgCenter.getChildren().addAll(messages, typeArea);
        root.setCenter(msgCenter);

        Scene sc = new Scene(root);
        ps.setScene(sc);
        if(server.isConnected())//if the server is not running the client will shutdown
            ps.show();

        ps.getIcons().add(new Image("file:Resources/SmileLogo.png"));
        ps.setTitle("JChat");
        hookupEvents();
        typeArea.requestFocus();

        ps.setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event) {out.println("#exit");}
        });

    }//end start method

    public void hookupEvents()
    {
        typeArea.setOnKeyReleased(new EventHandler<KeyEvent>()
        {

            @Override
            public void handle(KeyEvent event)
            {
                if(event.getCode()== KeyCode.ENTER)
                {
                    sendMessage();
                    typeArea.setText("");
                }

            }
        });

        //setting up the autoscroll feature
        messages.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                messages.setScrollTop(Double.MAX_VALUE);
            }
        });

    }

    public void sendMessage()
    {
        String txt = typeArea.getText();
        txt=txt.substring(0,txt.length()-1);
        //typeArea.clear();
        out.println(txt);
        //messages.appendText(txt+"\n");
        messages.positionCaret(messages.getText().length());
    }

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
                //adding the hashcode at the end makes sure that a user will only close his chat if he sends the
                //"#exit" command or is trying really hard to break the server
                if(msgFromServer.contains("#exitNOW"+"exitNOW".hashCode()))
                {
                    System.out.println("Thank you for using JChat! Have a nice day!");
                    try {server.close();}
                    catch (IOException e) {e.printStackTrace();}

                    System.exit(0);
                }
                if(msgFromServer!=null)
                {
                    messages.appendText(msgFromServer+"\n");
                    System.out.println(msgFromServer);
                }
            }//end while loop
        }
    }//end ServerResponse class


}//end ChatClient class
