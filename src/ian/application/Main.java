package ian.application;
	
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {

	public static MwcData info = new MwcData();
	public static McuSocket mcuSocket = new McuSocket();
	public static ScheduledThreadPoolExecutor s;
	
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (s != null) {
					s.shutdownNow();
				}
			}
		});
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("/ian/application/MainPage.fxml"));
		try {
			Pane rootView = (Pane) loader.load();
			
			Scene scene = new Scene(rootView);

			primaryStage.setTitle("Tdk");
			primaryStage.setScene(scene);
			primaryStage.show();
			rootView.setPrefHeight(primaryStage.getHeight());
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
