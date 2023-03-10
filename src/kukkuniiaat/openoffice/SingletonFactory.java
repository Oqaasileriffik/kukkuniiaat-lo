package kukkuniiaat.openoffice;

import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.uno.XComponentContext;

/**
 * This class is a factory that creates only a single instance,
 * or a singleton, of the Main class. Used for performance 
 * reasons and to allow various parts of code to interact.
 *
 * @author Marcin Miłkowski
 */
public class SingletonFactory implements XServiceInfo, XSingleComponentFactory {

  private transient kukkuniiaat.openoffice.Main instance;

  public final Object createInstanceWithArgumentsAndContext(final Object[] arguments, 
      final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    return createInstanceWithContext(xContext);
  }

  public final Object createInstanceWithContext(final XComponentContext xContext) throws com.sun.star.uno.Exception {    
    if (instance == null) {     
      instance = new kukkuniiaat.openoffice.Main(xContext);
    } else {  
      instance.changeContext(xContext);      
    }
    return instance;
  }

  public final String getImplementationName() {
    return Main.class.getName();
  }

  public final boolean supportsService(String serviceName) {
    for (String s : getSupportedServiceNames()) {
      if (s.equals(serviceName)) {
        return true;
      }
    }
    return false;
  }

  public final String[] getSupportedServiceNames() {
    return Main.getServiceNames();
  }
}
