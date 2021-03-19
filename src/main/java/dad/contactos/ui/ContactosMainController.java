package dad.contactos.ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import dad.contactos.ui.model.Agenda;
import dad.contactos.ui.model.Contacto;
import dad.contactos.ui.model.Telefono;
import dad.contactos.ui.model.TipoTelefono;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ContactosMainController implements Initializable {

	@FXML
	private BorderPane view;

    @FXML
    private TextField nameTF,subNameTF;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ImageView perfilPhoto;

    @FXML
    private Button newContactButton, deleteContactButton, addPhotoButton, deletePhotoButton,
    				previousContactButton, nextContactButton, addPhone, deletePhone;
	
    @FXML
    private ListView<Contacto> contactListView;
    
    @FXML
    private TableView<Telefono> tablePhone;

    @FXML
    private TableColumn<Telefono, String> numberColumn;

    @FXML
    private TableColumn<Telefono, TipoTelefono> typeColumn;

    @FXML
    private GridPane rightPane;
    
    public static ContactosMainController mainController;
    
    private ObjectProperty<Contacto>selectedContacto=new SimpleObjectProperty<>();
    private ObjectProperty<Telefono>selectedTelefono=new SimpleObjectProperty<>();
    private ObjectProperty<Agenda>agenda=new SimpleObjectProperty<>();
    private ObjectProperty<Image>foto=new SimpleObjectProperty<>(this,"foto");
    
    private Image NO_PHOTO=new Image("/images/no-photo-128x128.png");
    private Stage stage;
    private File currentAgendaFile;
    
    public ContactosMainController()  throws IOException{
        ContactosMainController.mainController = this;
        
		FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/ContactosView.fxml"));
		loader.setController(this);
		loader.load();
	}
    
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
				
		agenda.addListener((o,ov,nv)->{
			if(ov!=null) 
				contactListView.itemsProperty().unbind();
			if(nv!=null) 
				contactListView.itemsProperty().bind(nv.contactosProperty());
		});
		
		agenda.set(new Agenda());
		
		selectedContacto.bind(contactListView.getSelectionModel().selectedItemProperty());
		
		numberColumn.setCellValueFactory(v->v.getValue().numeroProperty());
		typeColumn.setCellValueFactory(v->v.getValue().tipoProperty());
				
		selectedContacto.addListener((o,ov,nv)->{
			if(ov!=null) {
				tablePhone.itemsProperty().unbind(); 
				nameTF.textProperty().unbind();
				subNameTF.textProperty().unbind();
				datePicker.valueProperty().unbind();
				perfilPhoto.imageProperty().unbind();
				
				tablePhone.getItems().clear();
				nameTF.clear();
				subNameTF.clear();
				datePicker.getEditor().clear();
				perfilPhoto.imageProperty().set(null);
			}
			if(nv!=null) {
				tablePhone.itemsProperty().bind(nv.telefonosProperty());
				nameTF.textProperty().bind(nv.nombreProperty());
				subNameTF.textProperty().bind(nv.apellidosProperty());
				datePicker.valueProperty().bind(nv.fechaNacimientoProperty());
				perfilPhoto.imageProperty().bind(nv.fotoProperty());
			}
		});

		foto.addListener((o,ov,nv)->{
			if(ov!=null) 
				selectedContacto.get().fotoProperty().unbind();
			if(nv!=null) 
				selectedContacto.get().fotoProperty().bind(foto);	
			if(nv==null) {
				selectedContacto.get().fotoProperty().unbind();
				selectedContacto.get().fotoProperty().set(null);
				perfilPhoto.imageProperty().set(null);
			}
		});
		
		deleteContactButton.disableProperty().bind(Bindings.when(selectedContacto.isNull()).then(true).otherwise(false));
		rightPane.disableProperty().bind(Bindings.when(selectedContacto.isNull()).then(true).otherwise(false));
		perfilPhoto.imageProperty().bind(Bindings.when(foto.isNull()).then(NO_PHOTO).otherwise(foto));
		
		selectedTelefono.bind(tablePhone.getSelectionModel().selectedItemProperty());
		
		deletePhone.disableProperty().bind(Bindings.when(selectedTelefono.isNull()).then(true).otherwise(false));
		
		ObjectBinding<Contacto>firstContact=Bindings.valueAt(agenda.get().contactosProperty(), 0);
		ObjectBinding<Contacto>lastContact=Bindings.valueAt(agenda.get().contactosProperty(), Bindings.size(agenda.get().contactosProperty()).subtract(1));
		previousContactButton.disableProperty().bind(firstContact.isEqualTo(selectedContacto));
		nextContactButton.disableProperty().bind(lastContact.isEqualTo(selectedContacto));
	}

    @FXML
    private void onNewContactButton(ActionEvent event) {
    	Contacto contacto=new Contacto();
    	contacto.setNombre("Sin Nombre");
    	contacto.setApellidos("Sin Apellidos");
    	
    	agenda.get().contactosProperty().add(contacto);
    	Platform.runLater(()->contactListView.requestFocus());
    	contactListView.getSelectionModel().select(agenda.get().contactosProperty().size()-1);
    }
    
    @FXML
    private void onDeleteContactButton(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();
    	Optional<ButtonType>result=alertInformation(stage, "Contactos", 
    			"Se va a eliminar al contacto"+selectedContacto.getName(), "¿Está seguro?").showAndWait();
    	if(result.get()==ButtonType.OK) 
    		agenda.get().contactosProperty().remove(selectedContacto.get());
    }

    @FXML
    private void onAddPhotoButton(ActionEvent event) {

    	stage=(Stage)view.getScene().getWindow();
    	
	    FileChooser chooser=new FileChooser();
	    chooser.setTitle("Abrir un curriculum");
	    chooser.getExtensionFilters().add(new ExtensionFilter("Agenda (*.jpg, *.png, *bmp)","*.jpg","*.png","*bmp"));
	    chooser.getExtensionFilters().add(new ExtensionFilter("Todos los archivos","*.*"));
	    
	    File file=chooser.showOpenDialog(stage);
	    if(file!=null) {
	    	try {
				foto.set(new Image(file.toURI().toURL().toExternalForm()));
			} catch (MalformedURLException e) {
				alertError(stage, "Poner foto", "Error al poner foto a "+selectedContacto.get().getNombre(), e.getMessage()).showAndWait();
			}
	    }
    }

    @FXML
    private void onDeletePhotoButton(ActionEvent event) {
    	selectedContacto.get().fotoProperty().set(null);
    	foto.set(NO_PHOTO);
    }
    
    @FXML
    private void onAddPhoneButton(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();
    	
    	TelefonoDialog dialog=new TelefonoDialog();
    	Optional<Telefono> result=dialog.showAndWait();
    	
    	if(result.isPresent())
    		selectedContacto.get().telefonosProperty().add(result.get());
    }

    @FXML
    private void onDeletePhoneButton(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();

    	Optional<ButtonType>result=alertInformation(stage, "Contactos", 
    			"Se va a eliminar el teléfono "+selectedTelefono.get().getNumero()+" ("+selectedTelefono.get().getTipo()+")",
    			"¿Está seguro?").showAndWait();
    	if(result.get()==ButtonType.OK) 
    		selectedContacto.get().telefonosProperty().remove(selectedTelefono.get());
    }

    @FXML
    private void onNextContactButton(ActionEvent event) {
    	contactListView.getSelectionModel().selectNext();
    }

    @FXML
    private void onPreviousContactButton(ActionEvent event) {
    	contactListView.getSelectionModel().selectPrevious();
    }
    
    /* MenuBar events */
    @FXML
    private void onNewMenu(ActionEvent event) {

    	Optional<ButtonType>result=alertWarningCancelB(stage, "Nueva agenda", 
    						"Se dispone a crear una nueva agenda.\nSi tiene información sin guardar se perderá para siempre.",
    						"¿Seguro de que desea continuar?").showAndWait();
    	
    	if(result.get()==ButtonType.OK) {
    		agenda.set(new Agenda());
    	}
    }

    @FXML
    private void onOpenMenu(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();
    	
	    currentAgendaFile=null;
	    FileChooser chooser=new FileChooser();
	    chooser.setTitle("Abrir un curriculum");
	    chooser.getExtensionFilters().add(new ExtensionFilter("Agenda (*.agenda)","*.agenda"));
	    chooser.getExtensionFilters().add(new ExtensionFilter("Todos los archivos","*.*"));
	    currentAgendaFile=chooser.showOpenDialog(stage);
	    	
	    if(currentAgendaFile!=null) {
		    try { 
				agenda.set(Agenda.load(currentAgendaFile));
			} catch (Exception e) {
				alertError(stage,"Abrir agenda","Error al cargar la agenda",e.getMessage()).showAndWait();
			}
		}
    }
    
    @FXML
    private void onSaveMenu(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();
    	
    	currentAgendaFile=null;    	
    	FileChooser chooser=new FileChooser();
    	chooser.setTitle("Guardar como");
    	chooser.getExtensionFilters().add(new ExtensionFilter("Faltas (*.agenda)", "*.agenda"));
    	chooser.getExtensionFilters().add(new ExtensionFilter("Todos los archivos","*.*"));
    	currentAgendaFile=chooser.showSaveDialog(stage);
    	
    	if(currentAgendaFile!=null) {
    		try {
				agenda.get().save(currentAgendaFile);
			} catch (Exception e) {
				alertError(stage, "Error", "Error al guardar la agenda", 
						"Se ha producido el siguiente error al guardar la agenda:\n"+e.getMessage()).showAndWait();
			}
    	}
    }

    @FXML
    private void onExitMenu(ActionEvent event) {
    	stage=(Stage)view.getScene().getWindow();
    	stage.close();
    }
    
    /**
     * Returns an alert of AlertType.Confirmation 
     * @param stage Stage
     * @param title String
     * @param headerText String
     * @param contentText String
     * @return Alert alert
     */
    public Alert alertConfirmation(Stage stage,String title, String headerText,String contentText) {
    	Alert alert=new Alert(AlertType.CONFIRMATION);
    	alert.setTitle(title);
    	alert.setHeaderText(headerText);
    	alert.setContentText(contentText);
    	alert.initModality(Modality.APPLICATION_MODAL);
    	alert.initOwner(stage);
    	return alert;
    }
    
    /**
     * Returns an alert of AlertType.Warning with ButtonType.Cancel
     * @param stage Stage
     * @param title String
     * @param headerText String
     * @param contentText String
     * @return Alert alert
     */
    public Alert alertWarningCancelB(Stage stage,String title, String headerText,String contentText) {
    	
    	Alert alert=new Alert(AlertType.WARNING);
    	alert.setTitle(title);
    	alert.setHeaderText(headerText);
    	alert.setContentText(contentText);
    	alert.initModality(Modality.APPLICATION_MODAL);
    	alert.initOwner(stage);
    	alert.getButtonTypes().add(ButtonType.CANCEL);
    	return alert;
    }

    /**
     * Returns an alert of AlertType information
     * @param stage Stage
     * @param title String
     * @param headerText String
     * @param contentText String
     * @return Alert alert
     */
    public Alert alertInformation(Stage stage, String title, String headerText, String contentText) {
    	
    	Alert alert=new Alert(AlertType.INFORMATION);
    	alert.setTitle(title);
    	alert.setHeaderText(headerText);
    	alert.setContentText(contentText);
    	alert.initModality(Modality.APPLICATION_MODAL);
    	alert.initOwner(stage);
    	return alert;
    }
    
    /**
     * Returns an alert of AlertType.Error
     * @param stage Stage
     * @param title String
     * @param headerText String
     * @param error String 
     * @return Alert alert;
     */
    public Alert alertError(Stage stage, String title, String headerText, String error) {
    	
    	Alert alert=new Alert(AlertType.ERROR);
    	alert.setTitle(title);
    	alert.setHeaderText(headerText);
    	alert.setContentText(error);
    	alert.initModality(Modality.APPLICATION_MODAL);
    	alert.initOwner(stage);
    	return alert;
    }
    
    public BorderPane getView() {
    	return this.view;
    }
	
}
