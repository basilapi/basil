/*
 * Copyright (c) 2021. Enrico Daga and Luca Panziera
 *
 * MLicensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.basilapi.basil.store.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.basilapi.basil.core.ApiInfo;
import io.github.basilapi.basil.doc.Doc;
import io.github.basilapi.basil.sparql.Specification;
import io.github.basilapi.basil.store.Store;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.basilapi.basil.view.Views;

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

	synchronized Object read(String id, String ext) throws IOException, ClassNotFoundException {
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
		for (File f : org.apache.commons.io.FileUtils.listFiles(home, new String[] { "spec" }, false)) {
			specs.add(f.getName().substring(0, f.getName().lastIndexOf('.')));
		}
		return specs;
	}

	synchronized public List<ApiInfo> list() {
		List<ApiInfo> specs = new ArrayList<ApiInfo>();
		for (File f : org.apache.commons.io.FileUtils.listFiles(home, new String[] { "spec" }, false)) {
			final String id = f.getName().substring(0, f.getName().lastIndexOf('.'));
			try {
				specs.add(info(id));
			} catch (IOException e) {
				log.error("", e);
			}
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

	/**
	 * File store implementation always return the last modified date
	 */
	@Override
	public Date created(String id) throws IOException {
		return new Date(getFile(id, "spec").lastModified());
	}

	@Override
	public Date modified(String id) throws IOException {
		return new Date(getFile(id, "spec").lastModified());
	}

	@Override
	public ApiInfo info(String id) throws IOException {
		return new ApiInfo() {
			@Override
			public Date created() {
				try {
					return FileStore.this.created(id);
				} catch (IOException e) {
					log.error("", e);
					return new Date(0);
				}
			}

			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getName() {
				try {
					return FileStore.this.loadDoc(id).get(Doc.Field.NAME);
				} catch (IOException e) {
					log.error("", e);
					return "";
				}
			}

			public Date modified() {
				try {
					return FileStore.this.modified(id);
				} catch (IOException e) {
					log.error("", e);
					return new Date(0);
				}
			};

			public Set<String> alias() {
				try {
					return FileStore.this.loadAlias(id);
				} catch (IOException e) {
					log.error("", e);
					return Collections.emptySet();
				}
			};
		};
	}

	@Override
	public void saveAlias(String id, Set<String> alias) throws IOException {
		write(id, StringUtils.join(alias.iterator(), "\n"), "alias");
	}

	@Override
	public Set<String> loadAlias(String id) throws IOException {
		String dat;
		try {
			dat = (String) read(id, "alias");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(StringUtils.split(dat, "\n"))));
	}

	@Override
	public String getIdByAlias(String alias) throws IOException {
		for (File f : org.apache.commons.io.FileUtils.listFiles(home, new String[] { "alias" }, false)) {
			final String id = f.getName().substring(0, f.getName().lastIndexOf('.'));
			try {
				List<String> a = IOUtils.readLines(new FileInputStream(f));
				if (a.contains(alias)) {
					return id;
				}
			} catch (IOException e) {
				log.error("", e);
				throw e;
			}
		}
		throw new IOException("Not found");
	}

	@Override
	public void saveCredentials(String id, String user, String password) throws IOException {
		write(id, StringUtils.join(new String[] { user, password }, "\n"), "auth");
	}

	@Override
	public String[] credentials(String id) throws IOException {
		String dat;
		try {
			dat = (String) read(id, "auth");
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}catch (FileNotFoundException e) {
			return null;
		}
		return StringUtils.split(dat, "\n");
	}
	
	@Override
	public void deleteCredentials(String id) throws IOException {
		delete(id, "auth");
	}
}
