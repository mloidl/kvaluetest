package at.loidi.kvaluetest.app.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {
	private final String CONFIG_FILE = "kvaluetest.json";

	private final ObservableList<Material> observableMaterials = FXCollections.observableArrayList();

	private final IntegerProperty kValueStart = new SimpleIntegerProperty();
	private final IntegerProperty amountKValues = new SimpleIntegerProperty();
	private final IntegerProperty kValueStep = new SimpleIntegerProperty();

	private final StringProperty startGCode = new SimpleStringProperty();
	private final StringProperty endGCode = new SimpleStringProperty();

	private final IntegerProperty slowSpeed = new SimpleIntegerProperty();
	private final IntegerProperty fastSpeed = new SimpleIntegerProperty();
	private final IntegerProperty travelSpeed = new SimpleIntegerProperty();

	private final DoubleProperty retract = new SimpleDoubleProperty();
	private final DoubleProperty layerHeight = new SimpleDoubleProperty();

	private final StringProperty path = new SimpleStringProperty();
	private final BooleanProperty dontAskPath = new SimpleBooleanProperty();

	private String selectedMaterial;

	public void load() {
		System.out.println("Load config");

		try {

			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new FileReader(CONFIG_FILE));
			SettingsDto settingsDto = gson.fromJson(reader, SettingsDto.class);

			this.fromDto(settingsDto);

		} catch (FileNotFoundException e) {
			System.err.println("Config file does not exist. Will be created when closing application");
			setupDefaultValues();
		}
	}

	private void setupDefaultValues() {
		observableMaterials.add(new Material("ABS", 255, 100));
		observableMaterials.add(new Material("PLA", 215, 55));
		observableMaterials.add(new Material("PETG", 230, 70));

		kValueStart.setValue(0);
		amountKValues.setValue(10);
		kValueStep.setValue(10);

		slowSpeed.setValue(1200);
		fastSpeed.setValue(4200);
		travelSpeed.setValue(7200);

		retract.setValue(0.4);
		layerHeight.setValue(0.2);
	}

	public void save() {

		GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
		Gson gson = builder.create();

		try {

			FileWriter file = new FileWriter(CONFIG_FILE);
			file.write(gson.toJson(this.toDto()));
			file.flush();
			file.close();

		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public SettingsDto toDto() {
		SettingsDto dto = new SettingsDto();
		for (Material material : observableMaterials) {
			dto.getMaterials().add(material.toDto());
		}

		if (selectedMaterial != null) {
			dto.setSelectedMaterial(selectedMaterial);
		}

		dto.setkValueStart(kValueStart.intValue());
		dto.setAmountKValues(amountKValues.intValue());
		dto.setkValueStep(kValueStep.intValue());

		dto.setStartGCode(startGCode.get());
		dto.setEndGCode(endGCode.get());

		dto.setSlowSpeed(slowSpeed.intValue());
		dto.setFastSpeed(fastSpeed.intValue());
		dto.setTravelSpeed(travelSpeed.intValue());

		dto.setRetract(retract.doubleValue());
		dto.setLayerHeight(layerHeight.doubleValue());

		dto.setOutputPath(path.get());
		dto.setDontAskPath(dontAskPath.get());

		return dto;
	}

	public void fromDto(SettingsDto dto) {
		for (MaterialDto matDto : dto.getMaterials()) {
			Material mat = new Material();
			mat.fromDto(matDto);
			observableMaterials.add(mat);
		}

		selectedMaterial = dto.getSelectedMaterial();

		kValueStart.set(dto.getkValueStart());
		amountKValues.set(dto.getAmountKValues());
		kValueStep.set(dto.getkValueStep());

		startGCode.set(dto.getStartGCode());
		endGCode.set(dto.getEndGCode());

		slowSpeed.set(dto.getSlowSpeed());
		fastSpeed.set(dto.getFastSpeed());
		travelSpeed.set(dto.getTravelSpeed());

		retract.set(dto.getRetract());
		layerHeight.set(dto.getLayerHeight());

		path.set(dto.getPath());
		dontAskPath.set(dto.isDontAskPath());
	}

	public IntegerProperty getKValueStart() {
		return kValueStart;
	}

	public IntegerProperty getAmountKValues() {
		return amountKValues;
	}

	public IntegerProperty getKValueStep() {
		return kValueStep;
	}

	public String getSelectedMaterial() {
		return selectedMaterial;
	}

	public void setSelectedMaterial(String material) {
		selectedMaterial = material;
	}

	public ObservableList<Material> getMaterials() {
		return this.observableMaterials;
	}

	public IntegerProperty getSlowSpeed() {
		return slowSpeed;
	}

	public IntegerProperty getFastSpeed() {
		return fastSpeed;
	}

	public IntegerProperty getTravelSpeed() {
		return travelSpeed;
	}

	public DoubleProperty getRetract() {
		return retract;
	}

	public DoubleProperty getLayerHeight() {
		return layerHeight;
	}

	public StringProperty getStartGCode() {
		return startGCode;
	}

	public StringProperty getEndGCode() {
		return endGCode;
	}

	public StringProperty getPath() {
		return path;
	}

	public BooleanProperty getDontAskPath() {
		return dontAskPath;
	}
}
