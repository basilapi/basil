package uk.ac.open.kmi.stoner.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.stoner.sparql.Specification;

public class FileStore implements Store {
	private Logger log = LoggerFactory.getLogger(FileStore.class);
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
		log.trace("writing {}", id);
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
		log.trace("reading {}", id);
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

	public boolean existsSpec(String id) {
		try {
			return getFile(id, "spec").exists();
		} catch (IOException e) {
			log.error("cannot test file: {}", id);
			return false;
		}
	}

	public List<String> listSpecs() {
		List<String> specs = new ArrayList<String>();
		for (File f : org.apache.commons.io.FileUtils.listFiles(home,
				new String[] { "spec" }, false)) {
			specs.add(f.getName()
					.substring(0, f.getName().lastIndexOf('.')));
		}
		return specs;
	}
}
