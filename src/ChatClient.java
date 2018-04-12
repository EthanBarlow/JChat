import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    private Socket server;
    private PrintWriter out;
    private String USERNAME="";
    private Scanner input;
    private TextArea messages;
    private TextArea typeArea;
    private Stage ps;
    private ListView<String> viewOnline;
    private ObservableList<String> onlineUsersList;

    public static void main(String[] a)
    {
        launch(a);
    }//end main method

    @Override
    public void start(Stage primaryStage) throws Exception
    {

        Label lblUsers = new Label("Online Users:");
        lblUsers.setStyle("-fx-font-size: 16px; -fx-font-family: Impact;");
        onlineUsersList = FXCollections.observableArrayList();
        viewOnline= new ListView<>();
        viewOnline.setItems(onlineUsersList);


        ps=primaryStage;
        messages = new TextArea();
        messages.setEditable(false);
        messages.setWrapText(true);
        typeArea = new TextArea();


        try
        {
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
        VBox vbOnline = new VBox();
        msgCenter.getChildren().addAll(messages, typeArea);
        vbOnline.getChildren().addAll(lblUsers,viewOnline);
        root.setCenter(msgCenter);
        root.setRight(vbOnline);

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
        out.println(txt);
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

                    //checking to see if a new user has been added
                    if(msgFromServer.startsWith("@server : @") && msgFromServer.endsWith("is now available to chat!"))
                    {
                        updateUserList(msgFromServer, 'a');
                    }

                    else if(msgFromServer.contains("\t@") && !msgFromServer.contains("/") && !msgFromServer.contains("@server") && !msgFromServer.contains(":"))
                    {
                        updateUserList(msgFromServer,'m');
                    }

                    else if(msgFromServer.startsWith("@server : @") && msgFromServer.endsWith("has left the chat. (To all)"))//@server : @jake has left the chat. (To all)

                    {
                        updateUserList(msgFromServer, 'r');
                    }

                    messages.appendText(msgFromServer+"\n");
                    System.out.println(msgFromServer);
                }
            }//end while loop
        }

        //m is the String that should be added or removed and type is a char that will determine which procedure to follow
        private void updateUserList(String m, char type)
        {
            //adding one user to the "Online Users" list
            if(type=='a')
            {
                String[] parts = m.split("@");
                String[] user = parts[2].split(" ");
                onlineUsersList.add("@"+user[0]);
                Platform.runLater(new Runnable() {@Override public void run() {viewOnline.setItems(onlineUsersList);}});
                System.out.println(onlineUsersList.toArray());
            }

            /*adding all users to the "Online Users" list --- mainly used when a new user logs in and needs to see
            all the active users currently online*/
            else if(type=='m')
            {
                System.out.println("In the updateUserList method where multiple people will be added to the list view");
                System.out.println(m + " in user list");
                System.out.println("After printing all the users");
                if(!onlineUsersList.contains(m.substring(1,m.length())))
                    onlineUsersList.add(m.substring(1,m.length()));
                Platform.runLater(new Runnable() {@Override public void run() {viewOnline.setItems(onlineUsersList);}});
                System.out.println(onlineUsersList.toArray());
            }

            /*removing a user from the "Online Users" list. When someone leaves the chat.*/
            else if(type=='r')
            {
                System.out.println("In the updateUserList method where users are removed.");
                String[] parts = m.split("@");
                String[] user = parts[2].split(" ");
                onlineUsersList.remove("@"+user[0]);
                Platform.runLater(new Runnable() {@Override public void run() {viewOnline.setItems(onlineUsersList);}});
                System.out.println(onlineUsersList.toArray());
            }

        }
    }//end ServerResponse class


}//end ChatClient class

/*
Protocol:

First, the Socket representing the server tries to make a connection to the server at the designated ip address on the specific port number.
When the sendMessage() method is called, the text from the input box is taken.
The text is then shortened in order to remove the newline character.
That text is then sent out to the server.
The messages box is then scrolled down to show all the most recent messages.

A separate class is then made that extends the Thread class. This is used for receiving messages from the server
and printing them out to the GUI. This class extends the Thread class so that messages can be sent and received
at the same time without having to wait for one to happen before the other.

The exit command from the server will cause the client to shutdown, but the command is obscure so that another
user cannot send the exit command to you and shutdown your client.

Messages starting with "@server : @" and ending with "is now available to chat!" updates the ListView of users.
The type of update depends on the char passed in to the method.
'a' - adds a single user
'm' - adds multiple users
'r' - removes multiple users

 */
