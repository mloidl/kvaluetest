package at.loidi.kvaluetest.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import at.loidi.kvaluetest.app.config.MaterialDto;
import at.loidi.kvaluetest.app.config.SettingsDto;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

public class GCodeWriter {

	private final String MATERIAL_PLACEHOLDER = "$[material]";
	private final String EXTRUDER_TEMP_PLACEHOLDER = "$[extruder_temp]";
	private final String BED_TEMP_PLACEHOLDER = "$[bed_temp]";

	private final String SLOW_SPEED_PLACEHOLDER = "$[slow_speed]";
	private final String FAST_SPEED_PLACEHOLDER = "$[fast_speed]";
	private final String TRAVEL_SPEED_PLACEHOLDER = "$[travel_speed]";

	private final String RETRACT_PLACEHOLDER = "$[retract]";
	private final String LAYER_HEIGHT_PLACEHOLDER = "$[layer_height]";

	private final String K_START_PLACEHOLDER = "$[k_start]";
	private final String K_AMOUNT_PLACEHOLDER = "$[k_amount]";
	private final String K_STEP_PLACEHOLDER = "$[k_step]";

	private final int X_START = 10;
	private final int X_END = 80;
	private final int Y_START = 10;
	private final int Y_STEP = 10;

	private final Map<String, String> variableMap = new HashMap<>();

	final private SettingsDto settings;
	final private String fileName;

	public GCodeWriter(String fileName, SettingsDto dto) {
		this.settings = dto;
		this.fileName = fileName;

		setupVariableMap();
	}

	private void setupVariableMap() {
		Optional<MaterialDto> material = settings.getMaterials().stream()
				.filter(m -> m.getName().equals(settings.getSelectedMaterial())).findAny();
		if (material.isPresent()) {
			variableMap.put(MATERIAL_PLACEHOLDER, material.get().getName());
			variableMap.put(EXTRUDER_TEMP_PLACEHOLDER, String.format("%d", material.get().getExtruderTemp()));
			variableMap.put(BED_TEMP_PLACEHOLDER, String.format("%d", material.get().getBedTemp()));

			variableMap.put(SLOW_SPEED_PLACEHOLDER, String.format("%d", settings.getSlowSpeed()));
			variableMap.put(FAST_SPEED_PLACEHOLDER, String.format("%d", settings.getFastSpeed()));
			variableMap.put(TRAVEL_SPEED_PLACEHOLDER, String.format("%d", settings.getTravelSpeed()));

			variableMap.put(RETRACT_PLACEHOLDER, String.format(Locale.ROOT, "%.5f", settings.getRetract()));
			variableMap.put(LAYER_HEIGHT_PLACEHOLDER, String.format(Locale.ROOT, "%.3f", settings.getLayerHeight()));

			variableMap.put(K_START_PLACEHOLDER, String.format("%d", settings.getkValueStart()));
			variableMap.put(K_AMOUNT_PLACEHOLDER, String.format("%d", settings.getAmountKValues()));
			variableMap.put(K_STEP_PLACEHOLDER, String.format("%d", settings.getkValueStep()));
		}
	}

	public void writeFile() {
		String gCode = getCode();

		StringBuilder fnBuilder = new StringBuilder();
		File path = null;

		if (!settings.getPath().isEmpty()) {
			fnBuilder.append(settings.getPath());
			fnBuilder.append(File.separator);

			path = new File(settings.getPath());
		} else {
			fnBuilder.append(System.getProperty("user.home"));
			fnBuilder.append(File.separator);
			path = new File(System.getProperty("user.home"));
		}

		fnBuilder.append(this.fileName);

		File file = new File(fnBuilder.toString());

		// check if we have to ask the user for input
		if (!settings.isDontAskPath()) {
			FileChooser fc = new FileChooser();
			fc.setInitialFileName(file.getAbsoluteFile().getName());
			fc.setInitialDirectory(path);
			file = fc.showSaveDialog(null);

			if (file == null) {
				return; // user aborted
			}
		}

		try (FileWriter writer = new FileWriter(file); BufferedWriter br = new BufferedWriter(writer)) {
			br.write(gCode);
		} catch (IOException e) {
			e.printStackTrace();

			showExceptionAlertBox(e, "Error Dialog", "Error Saving file");
		}
	}

	public String getCode() {
		StringBuilder sb = new StringBuilder();

		sb.append(getSettingsComment());

		sb.append("\n;Start G-code - begin\n");
		sb.append(settings.getStartGCode());
		sb.append("\n;Start G-code - end\n");

		sb.append("\n;Test Pattern - begin\n");

		int y = Y_START;

		sb.append("\nG21 ; set units to millimeters\n");
		sb.append("G90 ; use absolute coordinates\n");
		sb.append("M83 ; use relative distances for extrusion\n");
		sb.append("G1 Z$[layer_height] F$[travel_speed]\n");
		sb.append("M204 S500");
		sb.append("G1 E-$[retract] F2100.00000\n");
		sb.append("G1 X10 Y" + y + " F$[travel_speed]\n");
		sb.append("G1 E$[retract] F2100.00000\n");

		int k = settings.getkValueStart();
		boolean xAsc = true;
		y += Y_STEP;
		for (int amount = 0; amount < settings.getAmountKValues(); amount++) {
			sb.append(getGCodeForOneLine(k, xAsc, y));
			k += settings.getkValueStep();
			xAsc = !xAsc;
			y += Y_STEP;
		}

		sb.append("\n;Test Pattern - end\n");

		sb.append("\n;End G-code - begin\n");
		sb.append(settings.getEndGCode());
		sb.append("\n;End G-code - end\n");

		return replacePlaceHolder(sb.toString());
	}

	private String getGCodeForOneLine(int k, boolean xAsc, int y) {
		StringBuilder sb = new StringBuilder();
		int x = xAsc ? X_START : X_END;

		sb.append("M900 K" + k + "\n");
		sb.append("G1 X" + x + " Y" + y + " E0.37418 F$[slow_speed] ;Prime, travel to first testline\n");
		x = xAsc ? x + 20 : x - 20;
		sb.append("G1 X" + x + " Y" + y + " E0.74835 F$[slow_speed] ;Slow part\n");
		x = xAsc ? x + 30 : x - 30;
		sb.append("G1 X" + x + " Y" + y + " E1.12253 F$[fast_speed] ;Accelerate - cruise - decelerate\n");
		x = xAsc ? x + 20 : x - 20;
		sb.append("G1 X" + x + " Y" + y + " E0.74835 F$[slow_speed] ;Slow part\n");

		return sb.toString();
	}

	private String getSettingsComment() {
		StringBuilder sb = new StringBuilder();

		sb.append(";Generated: " + LocalDateTime.now().toString() + "\n");
		sb.append(";Material: " + MATERIAL_PLACEHOLDER + "\n");
		sb.append(";Extruder Temperature:  " + EXTRUDER_TEMP_PLACEHOLDER + "\n");
		sb.append(";Bed Temperature: " + BED_TEMP_PLACEHOLDER + "\n");
		sb.append(";K-Value Start: " + K_START_PLACEHOLDER + "\n");
		sb.append(";Amount of K-Values to Test: " + K_AMOUNT_PLACEHOLDER + "\n");
		sb.append(";Step size: " + K_STEP_PLACEHOLDER + "\n");
		sb.append(";Slow Speed [mm/min]: " + SLOW_SPEED_PLACEHOLDER + "\n");
		sb.append(";Fast Speed [mm/min]: " + FAST_SPEED_PLACEHOLDER + "\n");
		sb.append(";Travel Speed [mm/min]: " + TRAVEL_SPEED_PLACEHOLDER + "\n");
		sb.append(";Retract [mm]: " + RETRACT_PLACEHOLDER + "\n");
		sb.append(";Layer Height [mm]: " + LAYER_HEIGHT_PLACEHOLDER + "\n\n");

		return sb.toString();
	}

	private String replacePlaceHolder(String s) {
		for (String key : variableMap.keySet()) {
			s = s.replace(key, variableMap.get(key));
		}
		return s;
	}

	private void showExceptionAlertBox(Exception e, String title, String header) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(e.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();
	}
}
