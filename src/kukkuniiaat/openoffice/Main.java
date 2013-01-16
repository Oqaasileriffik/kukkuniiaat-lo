package kukkuniiaat.openoffice;

import java.util.*;

import javax.swing.JOptionPane;

import kukkuniiaat.client.*;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceDisplayName;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.linguistic2.XSpellAlternatives;
import com.sun.star.linguistic2.XHyphenator;
import com.sun.star.linguistic2.XHyphenatedWord;
import com.sun.star.linguistic2.XPossibleHyphens;

public class Main extends WeakBase implements XJobExecutor, XServiceDisplayName, XServiceInfo, XSpellChecker, XHyphenator, XLinguServiceEventBroadcaster, ChangeListener {
	private Set<String> disabledRules = new HashSet<String>();
	private List<XLinguServiceEventListener> xEventListeners;

	/**
	 * Service name required by the OOo API && our own name.
	 */
	private static final String[] SERVICE_NAMES = { "com.sun.star.linguistic2.SpellChecker", "com.sun.star.linguistic2.Hyphenator", "kukkuniiaat.openoffice.Main" };

	private XComponentContext xContext;
	private Client kukkuniiaatClient = null;

	public Main(final XComponentContext xCompContext) {
		try {
			changeContext(xCompContext);
			disabledRules = new HashSet<String>();
			xEventListeners = new ArrayList<XLinguServiceEventListener>();
			kukkuniiaatClient = new Client();
			Configuration.getConfiguration().addChangeListener(this);
		} 
		catch (final Throwable t) {
			showError(t);
		}
	}

	public final void changeContext(final XComponentContext xCompContext) {
		xContext = xCompContext;
	}

	private XComponent getxComponent() {
		try {
			final XMultiComponentFactory xMCF = xContext.getServiceManager();
			final Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
			final XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
			final XComponent xComponent = xDesktop.getCurrentComponent();
			return xComponent;
		} 
		catch (final Throwable t) {
			showError(t);
			return null;
		}
	}

    public boolean isValid(final String aWord, final Locale aLocale, final PropertyValue[] aProperties) {
        return kukkuniiaatClient.isValid(aWord);
    }

    public XSpellAlternatives spell(final String aWord, final Locale aLocale, final PropertyValue[] aProperties) {
        XSpellAlternatives rv = null;
        try {
            rv = new SpellAlternatives(aWord, aLocale);
        }
		catch (final Throwable t) {
			showError(t);
		}
        return rv;
    }

    public XHyphenatedWord hyphenate(final String aWord, final Locale aLocale, final short nMaxLeading, final PropertyValue[] aProperties) {
        XHyphenatedWord rv = null;
        try {
            rv = Hyphenator.hyphenate(aWord, aLocale, nMaxLeading);
        }
		catch (final Throwable t) {
			showError(t);
		}
        return rv;
    }

    public XHyphenatedWord queryAlternativeSpelling(final String aWord, final Locale aLocale, final short nIndex, final PropertyValue[] aProperties) {
        XHyphenatedWord rv = null;
//        showMessage("XHyphenatedWord queryAlternativeSpelling()");
        return rv;
    }

    public XPossibleHyphens createPossibleHyphens(final String aWord, final Locale aLocale, final PropertyValue[] aProperties) {
//        showMessage("XPossibleHyphens createPossibleHyphens()");
        return new PossibleHyphens(aWord, aLocale);
    }

	public boolean hasLocale(Locale locale) {
		return "kl".equals(locale.Language);
	}

	public final Locale[] getLocales() {
		return new Locale[] {
			new Locale("kl", "GL", "kl_GL")
		};
	}

	public final boolean isSpellChecker() {
		return true;
	}

	public final boolean hasOptionsDialog() {
		return false;
	}

	public final boolean addLinguServiceEventListener(final XLinguServiceEventListener xLinEvLis) {
		if (xLinEvLis == null) {
			return false;
		}
		xEventListeners.add(xLinEvLis);
		return true;
	}

	public final boolean removeLinguServiceEventListener(final XLinguServiceEventListener xLinEvLis) {
		if (xLinEvLis == null) {
			return false;
		}
    
		if (xEventListeners.contains(xLinEvLis)) {
			xEventListeners.remove(xLinEvLis);
			return true;
		}
		return false;
	}

	public final void recheckDocument() {
		if (!xEventListeners.isEmpty()) {
			for (final XLinguServiceEventListener xEvLis : xEventListeners) {
				if (xEvLis != null) {
					final com.sun.star.linguistic2.LinguServiceEvent xEvent = new com.sun.star.linguistic2.LinguServiceEvent();
					xEvent.nEvent = com.sun.star.linguistic2.LinguServiceEventFlags.PROOFREAD_AGAIN;
					xEvLis.processLinguServiceEvent(xEvent);
 				}
			}
		}
	}

	public final void resetDocument() {
		disabledRules = new HashSet<String>();
		recheckDocument();
	}

	public String[] getSupportedServiceNames() {
		return getServiceNames();
	}

	public static String[] getServiceNames() {
		return SERVICE_NAMES;
	}

	public boolean supportsService(final String sServiceName) {
		for (final String sName : SERVICE_NAMES) {
			if (sServiceName.equals(sName)) {
				return true;
			}
		}
		return false;
	}

	public String getImplementationName() {
		return Main.class.getName();
	}

	public static XSingleComponentFactory __getComponentFactory(final String sImplName) {
		SingletonFactory xFactory = null;
		if (sImplName.equals(Main.class.getName())) {
			xFactory = new SingletonFactory();
		}
		return xFactory;
	}

	public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
		return Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
	}

	public void trigger(final String sEvent) {
		if (!javaVersionOkay()) {
			return;
		}

		try {
			if (sEvent.equals("reset")) {
				resetDocument();	
			}
			else {
				System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
			}
		} 
		catch (final Throwable e) {
			showError(e);
		}
	}

	public void settingsChanged() {
		resetDocument();
	}

	private boolean javaVersionOkay() {
		final String version = System.getProperty("java.version");

		if (version != null && (version.startsWith("1.0") || version.startsWith("1.1") || version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4") || version.startsWith("1.5"))) {
			final DialogThread dt = new DialogThread("Error: Kukkuniiaat Spell Checker requires Java 1.6 or later. Current version: " + version);
			dt.start();
			return false;
		}

		return true;
	}

	public static void showError(final Throwable e) {
        e.printStackTrace();

		try {
            String metaInfo = "OS: " + System.getProperty("os.name") + " on " + System.getProperty("os.arch") + ", Java version " + System.getProperty("java.vm.version") + " from " + System.getProperty("java.vm.vendor");
            String msg = "An error has occurred in Kukkuniiaat Spell Checker:\n" + e.toString() + "\nStacktrace:\n";
        
            final StackTraceElement[] elem = e.getStackTrace();
            for (final StackTraceElement element : elem) {
                msg += element.toString() + "\n";
            }
            msg += metaInfo;
            final DialogThread dt = new DialogThread(msg);
            dt.start();
		}
		catch (final Throwable t) {
            t.printStackTrace();
		}
	}

	public static void showMessage(String msg) {
		final DialogThread dt = new DialogThread(msg);
		dt.start();
	}

	public void ignoreRule(final String ruleId, final Locale locale) throws IllegalArgumentException {
		try {
			disabledRules.add(ruleId);
			recheckDocument();
		} 
		catch (final Throwable t) {
			showError(t);
		}
  	}

	public void resetIgnoreRules() {
		try {
			disabledRules = new HashSet<String>();
		} 
		catch (final Throwable t) {
			showError(t);
		}
	}
  
	public String getServiceDisplayName(Locale locale) {
		return "Kukkuniiaat Spell Checker";
	}
}

class DialogThread extends Thread {
	final private String text;

	DialogThread(final String text) {
		this.text = text;
	}

	@Override
	public void run() {
		JOptionPane.showMessageDialog(null, text);
	}
}
