package dad.contactos.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import dad.contactos.ui.model.Telefono;
import dad.contactos.ui.model.TipoTelefono;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TelefonoDialog extends Dialog<Telefono> implements Initializable {

    @FXML
    private GridPane view;

    @FXML
    private ComboBox<TipoTelefono> añadirCB;

    @FXML
    private TextField NumeroTF;

    private Stage stage;
    
    public TelefonoDialog(){
		FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/TelefonoDialog.fxml"));
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		stage=(Stage)getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image("images/contacts-icon-32x32.png"));
		
		añadirCB.getItems().addAll(TipoTelefono.values());
		setTitle("Nuevo teléfono");
		setHeaderText("Introduzca los datos del número de teléfono");
		getDialogPane().setContent(view);
		getDialogPane().getButtonTypes().addAll(new ButtonType("Añadir",ButtonData.OK_DONE),ButtonType.CANCEL);
		
		setResultConverter(b->onAddTeléfonoAction(b));
	}

	private Telefono onAddTeléfonoAction(ButtonType b) {
		
		if(b.getButtonData()==ButtonData.OK_DONE) {
			
			Telefono telefono=new Telefono();
			telefono.setNumero(NumeroTF.textProperty().get());
			telefono.setTipo(añadirCB.getValue());
			if((!telefono.getNumero().isEmpty()) && (añadirCB.getValue()!=null)) {
				return telefono;
			}else {
				alertAñadirTelefono();
				return null;
			}
		}else {
			return null;
		}
	}
	
	private void alertAñadirTelefono() {
    	Stage stage=(Stage)view.getScene().getWindow();

    	Alert alert=new Alert(AlertType.INFORMATION);	
    	alert.initModality(Modality.APPLICATION_MODAL);
    	alert.initOwner(stage);
    	alert.setTitle("El formulario no esta completo");
    	alert.setHeaderText("Error al intentar introducir un nuevo telefono");
    	alert.setContentText("Debe rellenar todos los campos");
    	alert.showAndWait();
	}
}
