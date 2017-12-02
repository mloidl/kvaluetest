package at.loidi.kvaluetest.app.config;

public class MaterialDto {
	private String name;
	private int extruderTemp;
	private int bedTemp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getExtruderTemp() {
		return extruderTemp;
	}

	public void setExtruderTemp(int extruderTemp) {
		this.extruderTemp = extruderTemp;
	}

	public int getBedTemp() {
		return bedTemp;
	}

	public void setBedTemp(int bedTemp) {
		this.bedTemp = bedTemp;
	}
}
