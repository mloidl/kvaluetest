package at.loidi.kvaluetest.app;

import at.loidi.kvaluetest.app.config.Settings;
import at.loidi.kvaluetest.ui.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

public class KValueTestApp extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/MainDialog.fxml"));
			Settings settings = new Settings();
			settings.load();

			loader.setControllerFactory(new Callback<Class<?>, Object>() {

				@Override
				public Object call(Class<?> param) {
					return new MainController(settings);
				}
			});

			Parent root = loader.load();
			Scene scene = new Scene(root, 610, 440);

			stage.setTitle("K-Value Test Pattern Generator");
			stage.setScene(scene);

			stage.setOnCloseRequest(e -> {
				settings.save();
				Platform.exit();
			});

			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
