package dad.contactos.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
	
	private static Stage primaryStage;
	
	private ContactosMainController Maincontroller;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Maincontroller=new ContactosMainController();
		Scene scene=new Scene(Maincontroller.getView());
				
		primaryStage.setScene(scene); 
		primaryStage.setTitle("FaltApp");
		primaryStage.getIcons().add(new Image("images/contacts-icon-32x32.png"));
		primaryStage.show();
	}
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
