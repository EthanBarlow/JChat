/*
JChat stands for "Java Chat"
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import java.io.File;

public class RunMultiple extends Application
{

    @Override
    public void start(Stage primaryStage) throws Exception {
        new ChatClient().start(primaryStage);

    }
}
