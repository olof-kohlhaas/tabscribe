package kim.kohlhaas.sone.tabscribe.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Screen;
import kim.kohlhaas.javafx.css.StyleParser;
import kim.kohlhaas.sone.javafx.RuntimeSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class Main extends Application {

    final static Logger log = LoggerFactory.getLogger(Main.class);
    private File file = null;
    private Stage stage = null;
    private Scene scene;
    private MainController mainController;
    
    public static void main(String[] args) {
    	File decoderFile = new File("decoder_log.txt");
    	if(decoderFile.exists() && decoderFile.canWrite()) {
    		decoderFile.delete();
    	}
        log.info("Starting Java application. " + Locale.getDefault());
        log.info("Runtime version: {}", System.getProperty("java.version"));
        log.info("Classpath: {}", System.getProperty("java.class.path") );
        log.info("Available processors: {}", Runtime.getRuntime().availableProcessors());
        launch(args);
        log.debug("main exit");
    }
    
    @Override
    public void init() throws Exception {
        log.debug("Initializing JavaFX application.");
        
        Parameters p = getParameters();
        List<String> pRaw = p.getRaw();
        String filePath = "";
        
        File pFile;
        
        for (String parameter : pRaw) {
            log.debug("parameter: " + parameter);
            filePath = parameter;
        }
        
        filePath = filePath.trim();
        
        if (filePath.length() > 0) {
            pFile = new File(filePath);
            
            if(pFile.canRead()) {
                log.info("Setting file passed by paramter: " + filePath);
                this.file = pFile;
            } else {
                log.info("Failed to read file: " + pFile);
            }
        }
        
        Font.loadFont(getClass().getResource("Carlito-Regular.ttf").toExternalForm(), 10);
        Font.loadFont(getClass().getResource("Carlito-Bold.ttf").toExternalForm(), 10);
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping JavaFX application.");
        mainController.close();
        super.stop();
        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting JavaFX application.....");
        this.stage = primaryStage;
        for (Screen screen : Screen.getScreens()) {
        	log.info("screen: {}, DPI: {}, scale X: {}, scale Y: {}", Screen.getScreens().indexOf(screen), screen.getDpi(), screen.getOutputScaleX(), screen.getOutputScaleY());
        }
        try {
        	ModuleLayer bootLayer =  ModuleLayer.boot();
            Optional<Module> moduleOpt = bootLayer.findModule("kim.kohlhaas.sone.tabscribe");
            log.info("module: {}, {}", Main.class.getModule().getName(), moduleOpt);            

            this.stage.getIcons().add(new Image(Main.class.getModule().getResourceAsStream("/kim/kohlhaas/sone/tabscribe/javafx/logo_square100.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            initUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initUI() throws IOException {
        String resourcePath = this.getClass().getPackage().getName().replace(".", "/");
        ResourceBundle labelBundle = ResourceBundle.getBundle(resourcePath + "/labels");
        RuntimeSettings.getInstance().setLabelBundle(labelBundle);
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("Main.fxml"), labelBundle);
        
        VBox root = mainLoader.load();  

        scene = new Scene(root, 300, 250);
        log.info("check style access {}", StyleParser.getStyleRuleValueAsString(root, "SpectrogramTimeLine > Line", "-fx-stroke"));
        
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        
        this.stage.setTitle(labelBundle.getString("application_title"));
        this.stage.setScene(scene);
        this.stage.setX(bounds.getMinX() + bounds.getWidth() / 8);
        this.stage.setY(bounds.getMinY() + bounds.getHeight() / 8);
        this.stage.setWidth(bounds.getWidth() * 3 / 4);
        this.stage.setHeight(bounds.getHeight() * 3 / 4);
        //this.stage.setMaximized(true);
        this.stage.show();
        
        mainController = mainLoader.getController();
        mainController.setPrimaryStage(stage);
        mainController.setFile(file);
        
        
    }

}
