package at.loidi.kvaluetest.ui.controller;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.UnaryOperator;

import at.loidi.kvaluetest.app.config.Material;
import at.loidi.kvaluetest.app.config.Settings;
import at.loidi.kvaluetest.helper.GCodeWriter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class MainController {

	private final Settings settings;

	public MainController(Settings config) {
		this.settings = config;
	}

	@FXML
	private ChoiceBox<Material> cMaterial;

	@FXML
	private TextField efExtruder;

	@FXML
	private TextField efBed;

	@FXML
	private TextField efStartValue;

	@FXML
	private TextField efAmount;

	@FXML
	private TextField efStepSize;

	@FXML
	private Label lblFileName;

	@FXML
	private Button bGenerate;

	@FXML
	private Button bClose;

	@FXML
	private Button bRemoveMaterial;

	@FXML
	private TextArea efStartGCode;

	@FXML
	private TextArea efEndGCode;

	@FXML
	private TextField efSlowSpeed;

	@FXML
	private TextField efFastSpeed;

	@FXML
	private TextField efTravelSpeed;

	@FXML
	private TextField efRetract;

	@FXML
	private TextField efLayerHeight;

	@FXML
	private TextField efPath;

	@FXML
	private CheckBox cbDontAskPath;

	private final UnaryOperator<Change> integerFilter = change -> {
		String input = change.getText();
		if (input.matches("[0-9]*")) {
			return change;
		}
		return null;
	};

	private final UnaryOperator<Change> doubleFilter = change -> {
		if (change.getControlNewText().isEmpty()) {
			return change;
			// pre-filter some characters
		} else if (change.getText().matches("[A-Za-z\\-]")) {
			return null;
		}
		try {
			DecimalFormat.getInstance().parse(change.getControlNewText());
			return change;
		} catch (NullPointerException | NumberFormatException | ParseException ex) {
			return null;
		}
	};

	private final FileNameRelevantChangeListener fileNameRelevantChangeListener = new FileNameRelevantChangeListener();

	@FXML
	void initialize() {
		addListener();
		createBindings();
		applyFormatter();
		refreshFileName();

		createButtonBindings();
	}

	private void createButtonBindings() {
		bGenerate.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			return isGenerateButtonDisabled();
		}, lblFileName.textProperty()));

		bRemoveMaterial.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			return cMaterial.valueProperty().get() == null;
		}, cMaterial.valueProperty()));
	}

	private void addListener() {

		efStartValue.textProperty().addListener(fileNameRelevantChangeListener);
		efAmount.textProperty().addListener(fileNameRelevantChangeListener);
		efStepSize.textProperty().addListener(fileNameRelevantChangeListener);
		efBed.textProperty().addListener(fileNameRelevantChangeListener);
		efExtruder.textProperty().addListener(fileNameRelevantChangeListener);

		cMaterial.valueProperty().addListener(new ChangeListener<Material>() {

			@Override
			public void changed(ObservableValue<? extends Material> observable, Material oldValue, Material newValue) {

				if (oldValue != null) {
					efBed.textProperty().unbindBidirectional(oldValue.getBedTemp());
					efExtruder.textProperty().unbindBidirectional(oldValue.getExtruderTemp());
				}

				if (newValue != null) {
					settings.setSelectedMaterial(newValue.getName().get());

					efBed.textProperty().bindBidirectional(newValue.getBedTemp(), new NumberStringConverter());
					efExtruder.textProperty().bindBidirectional(newValue.getExtruderTemp(),
							new NumberStringConverter());

				}

				refreshFileName();
			}
		});
	}

	private void activateSelectedMaterial() {
		if (settings.getSelectedMaterial() != null) {
			for (Material material : settings.getMaterials()) {
				if (material.getName().get().equals(settings.getSelectedMaterial())) {
					cMaterial.setValue(material);
				}
			}
		} else {
			cMaterial.setValue(null);
		}
	}

	private void applyFormatter() {
		efStartValue.setTextFormatter(new TextFormatter<>(integerFilter));
		efAmount.setTextFormatter(new TextFormatter<>(integerFilter));
		efStepSize.setTextFormatter(new TextFormatter<>(integerFilter));

		efExtruder.setTextFormatter(new TextFormatter<>(integerFilter));
		efBed.setTextFormatter(new TextFormatter<>(integerFilter));

		efSlowSpeed.setTextFormatter(new TextFormatter<>(integerFilter));
		efFastSpeed.setTextFormatter(new TextFormatter<>(integerFilter));
		efTravelSpeed.setTextFormatter(new TextFormatter<>(integerFilter));

		efRetract.setTextFormatter(new TextFormatter<>(doubleFilter));
		efLayerHeight.setTextFormatter(new TextFormatter<>(doubleFilter));
	}

	private void createBindings() {
		efStartValue.textProperty().bindBidirectional(settings.getKValueStart(), new NumberStringConverter("#"));
		efAmount.textProperty().bindBidirectional(settings.getAmountKValues(), new NumberStringConverter("#"));
		efStepSize.textProperty().bindBidirectional(settings.getKValueStep(), new NumberStringConverter("#"));

		cMaterial.setItems(settings.getMaterials());
		cMaterial.setConverter(new StringConverter<Material>() {

			@Override
			public String toString(Material m) {
				return m.getName().get();
			}

			@Override
			public Material fromString(String string) {
				return null;
			}
		});

		efStartGCode.textProperty().bindBidirectional(settings.getStartGCode());
		efEndGCode.textProperty().bindBidirectional(settings.getEndGCode());

		efSlowSpeed.textProperty().bindBidirectional(settings.getSlowSpeed(), new NumberStringConverter("#"));
		efFastSpeed.textProperty().bindBidirectional(settings.getFastSpeed(), new NumberStringConverter("#"));
		efTravelSpeed.textProperty().bindBidirectional(settings.getTravelSpeed(), new NumberStringConverter("#"));

		efRetract.textProperty().bindBidirectional(settings.getRetract(),
				new NumberStringConverter(new DecimalFormat()));
		efLayerHeight.textProperty().bindBidirectional(settings.getLayerHeight(),
				new NumberStringConverter(new DecimalFormat()));

		efPath.textProperty().bindBidirectional(settings.getPath());
		cbDontAskPath.selectedProperty().bindBidirectional(settings.getDontAskPath());

		activateSelectedMaterial();
	}

	private boolean isGenerateButtonDisabled() {
		try {
			return (Integer.parseInt(efStartValue.getText()) < 0 || Integer.parseInt(efAmount.getText()) <= 0
					|| Integer.parseInt(efStepSize.getText()) <= 0 || Integer.parseInt(efExtruder.getText()) <= 0
					|| Integer.parseInt(efBed.getText()) <= 0);
		} catch (NumberFormatException e) {
			return true; // disable generate button in case values could not be parsed
		}
	}

	private void refreshFileName() {
		StringBuilder sb = new StringBuilder();
		sb.append("K-Value.");
		sb.append(cMaterial.valueProperty().get() == null ? "NONE" : cMaterial.getValue().getName().get());
		sb.append(".K");
		sb.append(efStartValue.getText());
		sb.append("-");
		sb.append(efAmount.getText());
		sb.append("-");
		sb.append(efStepSize.getText());

		sb.append(".gcode");

		lblFileName.textProperty().set(sb.toString());
	}

	@FXML
	void onClose(ActionEvent event) {
		settings.save();
		Platform.exit();
	}

	@FXML
	void onGenerate(ActionEvent event) {

		GCodeWriter writer = new GCodeWriter(lblFileName.getText(), settings.toDto());
		writer.writeFile();
	}

	@FXML
	void onAddMaterial(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Add Material");
		dialog.setHeaderText("Please enter the name of the Material to add");
		dialog.setContentText("Name:");

		Optional<String> result = dialog.showAndWait();

		result.ifPresent(name -> addMaterial(name));
	}

	private void addMaterial(String name) {
		Material newMaterial = new Material(name, 0, 0);
		settings.getMaterials().add(newMaterial);

		settings.setSelectedMaterial(newMaterial.getName().getValue());
		activateSelectedMaterial();
	}

	@FXML
	void onRemoveMaterial(ActionEvent event) {
		if (settings.getSelectedMaterial() != null) {
			Iterator<Material> it = settings.getMaterials().iterator();
			while (it.hasNext()) {
				Material material = it.next();
				if (material.getName().getValue().equals(settings.getSelectedMaterial())) {
					it.remove();
					settings.setSelectedMaterial(null);
					efBed.setText("0");
					efExtruder.setText("0");
					break;
				}
			}
			activateSelectedMaterial();
			refreshFileName();
		}
	}

	@FXML
	void onBrowsePath(ActionEvent event) {
		System.out.println("Browse Path called");
		DirectoryChooser dc = new DirectoryChooser();

		if (!settings.getPath().getValue().isEmpty()) {
			dc.setInitialDirectory(new File(settings.getPath().getValue()));
		}

		dc.setTitle("Choose Default Output Path");
		File dir = dc.showDialog(null);
		if (dir != null) {
			settings.getPath().setValue(dir.getAbsolutePath());
		}
	}

	private class FileNameRelevantChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			refreshFileName();
		}
	}
}
