package at.loidi.kvaluetest.app.config;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Material {
	private StringProperty name = new SimpleStringProperty();
	private IntegerProperty extruderTemp = new SimpleIntegerProperty();
	private IntegerProperty bedTemp = new SimpleIntegerProperty();

	public Material() {
	}

	public Material(String name, int extruderTemp, int bedTemp) {
		this.name.setValue(name);
		this.extruderTemp.setValue(extruderTemp);
		this.bedTemp.setValue(bedTemp);
	}

	public StringProperty getName() {
		return this.name;
	}

	public IntegerProperty getExtruderTemp() {
		return this.extruderTemp;
	}

	public IntegerProperty getBedTemp() {
		return this.bedTemp;
	}

	public void fromDto(MaterialDto dto) {
		name.set(dto.getName());
		extruderTemp.set(dto.getExtruderTemp());
		bedTemp.set(dto.getBedTemp());
	}

	public MaterialDto toDto() {
		MaterialDto dto = new MaterialDto();
		dto.setName(name.get());
		dto.setExtruderTemp(extruderTemp.intValue());
		dto.setBedTemp(bedTemp.intValue());
		return dto;
	}
}
