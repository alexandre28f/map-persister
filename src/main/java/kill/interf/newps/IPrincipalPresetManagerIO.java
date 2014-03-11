package mi.interf.newps;

import java.io.IOException;

public interface IPrincipalPresetManagerIO extends IGenericPresetManagerIO<IPrincipalPresetManager> {
	IPrincipalPresetManager bareBonesRead(String fileStem) throws IOException, ClassNotFoundException;
}
