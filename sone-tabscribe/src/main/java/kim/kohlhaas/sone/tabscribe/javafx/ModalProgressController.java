package kim.kohlhaas.sone.tabscribe.javafx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ModalProgressController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label message;

    @FXML
    void initialize() {
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'ModalProgress.fxml'.";
        assert message != null : "fx:id=\"message\" was not injected: check your FXML file 'ModalProgress.fxml'.";

    }

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Label getMessage() {
		return message;
	}
    
    
}
