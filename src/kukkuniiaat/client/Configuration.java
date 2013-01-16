package kukkuniiaat.client;

import java.util.*;
import java.io.*;
import java.lang.ref.*;

/* loads Kukkuniiaat config file */
public class Configuration {
	protected Properties config;
	protected File file;
	protected Set<String> categories;
	protected Set<String> phrases;
	protected List<WeakReference<ChangeListener>> listeners = new LinkedList<WeakReference<ChangeListener>>();

	protected static Configuration singleton = null;

	public void fireChange() {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			WeakReference ref = (WeakReference)i.next();
			Object o = ref.get();
			if (o == null)
				i.remove();
			else
				((ChangeListener)o).settingsChanged();
		}
	}

	public void addChangeListener(ChangeListener l) {
		listeners.add(new WeakReference<ChangeListener>(l));
	}

	public synchronized static Configuration getConfiguration() {
		if (singleton == null) {
			singleton = new Configuration(new File(System.getProperty("user.home"), ".Kukkuniiaat-OpenOffice.org"));
			singleton.load();
		}
		return singleton;
	}

	public Configuration(File _file) {
		config = new Properties();
		file   = _file;
	}

	public void load() {
		try {
			config.load(new FileInputStream(file));
			phrases = getIgnoredPhrases();
			categories = getCategories();
		}
		catch (Exception ex) {
			phrases = new HashSet<String>();
			categories = new HashSet<String>();
		}
	}

	private Set<String> createSet(String[] strings) {
		Set<String> temp = new HashSet<String>();
		for (int x = 0; x < strings.length; x++) {
			temp.add(strings[x]);
		}
		return temp;
	}

	public synchronized boolean isIgnored(String phrase) {
		return phrases.contains(phrase);
	}

	public synchronized boolean isEnabled(String category) {
		return categories.contains(category);
	}

	public synchronized void ignorePhrase(String phrase) {
		phrases.add(phrase);
		config.setProperty("ignoredPhrases", createString(phrases));
	}

	public synchronized void removePhrase(String phrase) {
		phrases.remove(phrase);
		config.setProperty("ignoredPhrases", createString(phrases));
	}

	public synchronized void showCategory(String category) {
		categories.add(category);
		config.setProperty("categories", createString(categories));
	}

	public synchronized void hideCategory(String category) {
		categories.remove(category);
		config.setProperty("categories", createString(categories));
	}

	private String createString(Set<String> strings) {
		StringBuffer temp = new StringBuffer();
		Iterator<String> i = strings.iterator();
		while (i.hasNext()) {
			String value = i.next();
			temp.append(value);

			if (i.hasNext())
				temp.append(", ");
		}

		return temp.toString();
	}

	public synchronized Set<String> getIgnoredPhrases() {
		return createSet(config.getProperty("ignoredPhrases", "").split(",\\s+"));
	}

	public synchronized Set<String> getCategories() {
		return createSet(config.getProperty("categories", "").split(",\\s+"));
	}

	public synchronized String getServiceHost() {
	    return "http://alpha.visl.sdu.dk:80/tools/office/";
	}

	public synchronized void setServiceHost(String name) {
		config.setProperty("host", name);
	}

	public synchronized String getLogin() {
		return config.getProperty("login", "").trim();
	}

	public synchronized void setLogin(String name) {
		config.setProperty("login", name);
	}

	public synchronized String getPassword() {
		return config.getProperty("password", "").trim();
	}

	public synchronized void setPassword(String name) {
		config.setProperty("password", name);
	}

	public void save() {
		try {
			config.store(new FileOutputStream(file), "Kukkuniiaat-OpenOffice Properties");
			fireChange();
		}
		catch (Exception ex) {
			throw new RuntimeException("Could not save properties\nLocation:" + file + "\n" + ex.getMessage());
		}
	}
}
