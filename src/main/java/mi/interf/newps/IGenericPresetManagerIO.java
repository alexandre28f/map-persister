package mi.interf.newps;

import java.io.IOException;

public interface IGenericPresetManagerIO<T> {
	void write(T bank, String fileStem) throws IOException;
	//	"read" isn't here because it's protected: we need to provide custom read()
	//	functions which restore buffer state after the read is done.
}
