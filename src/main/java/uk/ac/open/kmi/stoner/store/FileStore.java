package uk.ac.open.kmi.stoner.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import uk.ac.open.kmi.stoner.sparql.Specification;

public class FileStore implements Store {

	private File home;

	public FileStore(File home) {
		this.home = home;
	}

	protected File getFile(String id, String ext) throws IOException {
		if (!home.isDirectory()) {
			throw new IOException("Not a directory: " + home);
		}
		File newFile = new File(home, id + "." + ext);
		return newFile;
	}

	void write(String id, Serializable o, String ext) throws IOException {
		try {
			File file = getFile(id, ext);
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(o);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			throw i;
		}
	}

	Object read(String id, String ext) throws IOException,
			ClassNotFoundException {
		try {
			Object o;
			File file = getFile(id, ext);
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			o = in.readObject();
			in.close();
			fileIn.close();
			return o;
		} catch (IOException i) {
			throw i;
		} catch (ClassNotFoundException c) {
			throw c;
		}
	}

	public Specification loadSpec(String id) throws IOException {
		try {
			return (Specification) read(id, "spec");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} catch (IOException e) {
			throw e;
		}
	}

	public void saveSpec(String id, Specification spec) throws IOException {
		write(id, spec, "spec");
	}
}
