package mi.interf.newps;

import java.io.IOException;

public interface ISecondaryPresetManagerIO extends IGenericPresetManagerIO<ISecondaryPresetManager> {
	ISecondaryPresetManager bareBonesRead(String fileStem) throws IOException, ClassNotFoundException;
}
