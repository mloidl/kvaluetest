package at.loidi.kvaluetest.app.config;

import java.util.ArrayList;
import java.util.List;

public class SettingsDto {
	private List<MaterialDto> materials = new ArrayList<>();
	private String selectedMaterial;
	private int kValueStart;
	private int amountKValues;
	private int kValueStep;

	private String startGCode;
	private String endGCode;

	private int slowSpeed;
	private int fastSpeed;
	private int travelSpeed;

	private double retract;
	private double layerHeight;

	private String path;
	private boolean dontAskPath;

	public List<MaterialDto> getMaterials() {
		return materials;
	}

	public void setMaterials(List<MaterialDto> materials) {
		this.materials = materials;
	}

	public String getSelectedMaterial() {
		return selectedMaterial;
	}

	public void setSelectedMaterial(String selectedMaterial) {
		this.selectedMaterial = selectedMaterial;
	}

	public int getkValueStart() {
		return kValueStart;
	}

	public void setkValueStart(int kValueStart) {
		this.kValueStart = kValueStart;
	}

	public int getAmountKValues() {
		return amountKValues;
	}

	public void setAmountKValues(int amountKValues) {
		this.amountKValues = amountKValues;
	}

	public int getkValueStep() {
		return kValueStep;
	}

	public void setkValueStep(int kValueStep) {
		this.kValueStep = kValueStep;
	}

	public int getSlowSpeed() {
		return slowSpeed;
	}

	public void setSlowSpeed(int slowSpeed) {
		this.slowSpeed = slowSpeed;
	}

	public int getFastSpeed() {
		return fastSpeed;
	}

	public void setFastSpeed(int fastSpeed) {
		this.fastSpeed = fastSpeed;
	}

	public int getTravelSpeed() {
		return travelSpeed;
	}

	public void setTravelSpeed(int travelSpeed) {
		this.travelSpeed = travelSpeed;
	}

	public double getRetract() {
		return retract;
	}

	public void setRetract(double retract) {
		this.retract = retract;
	}

	public double getLayerHeight() {
		return layerHeight;
	}

	public void setLayerHeight(double layerHeight) {
		this.layerHeight = layerHeight;
	}

	public String getStartGCode() {
		return startGCode;
	}

	public void setStartGCode(String startGCode) {
		this.startGCode = startGCode;
	}

	public String getEndGCode() {
		return endGCode;
	}

	public void setEndGCode(String endGCode) {
		this.endGCode = endGCode;
	}

	public String getPath() {
		return path;
	}

	public void setOutputPath(String path) {
		this.path = path;
	}

	public boolean isDontAskPath() {
		return dontAskPath;
	}

	public void setDontAskPath(boolean dontAskPath) {
		this.dontAskPath = dontAskPath;
	}
}
