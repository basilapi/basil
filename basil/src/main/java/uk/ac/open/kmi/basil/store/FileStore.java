package uk.ac.open.kmi.basil.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.basil.doc.Doc;
import uk.ac.open.kmi.basil.sparql.Specification;
import uk.ac.open.kmi.basil.view.Views;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

	synchronized void write(String id, Serializable o, String ext) throws IOException {
		log.trace("writing {}.{}", id, ext);
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

	synchronized private boolean delete(String id, String ext) throws IOException {
		log.trace("deleting {}.{}", id, ext);
		try {
			File file = getFile(id, ext);
			return file.delete();
		} catch (IOException i) {
			throw i;
		}
	}


	synchronized Object read(String id, String ext) throws IOException,
			ClassNotFoundException {
		log.trace("reading {}.{}", id, ext);
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

	synchronized public Specification loadSpec(String id) throws IOException {
		try {
			return (Specification) read(id, "spec");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} catch (IOException e) {
			throw e;
		}
	}

	synchronized public void saveSpec(String id, Specification spec) throws IOException {
		write(id, spec, "spec");
	}

	synchronized public boolean existsSpec(String id) {
		try {
			return getFile(id, "spec").exists();
		} catch (IOException e) {
			log.error("cannot test file: {}", id);
			return false;
		}
	}

	synchronized public List<String> listSpecs() {
		List<String> specs = new ArrayList<String>();
		for (File f : org.apache.commons.io.FileUtils.listFiles(home,
				new String[] { "spec" }, false)) {
			specs.add(f.getName().substring(0, f.getName().lastIndexOf('.')));
		}
		return specs;
	}

	synchronized public Views loadViews(String id) throws IOException {
		try {
			try {
				return (Views) read(id, "views");
			} catch (FileNotFoundException e) {
				return new Views();
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	synchronized public Doc loadDoc(String id) throws IOException {
		try {
			try {
				return (Doc) read(id, "doc");
			} catch (FileNotFoundException e) {
				return new Doc();
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	synchronized public boolean deleteDoc(String id) throws IOException {
		return delete(id, "doc");
	}

	synchronized public boolean deleteSpec(String id) throws IOException {
		return delete(id, "spec") && delete(id, "views");
	}

	synchronized public void saveViews(String id, Views views) throws IOException {
		write(id, views, "views");
	}

	synchronized public void saveDoc(String id, Doc doc) throws IOException {
		write(id, doc, "doc");
	}
}
