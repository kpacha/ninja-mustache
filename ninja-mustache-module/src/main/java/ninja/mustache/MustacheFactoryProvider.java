package ninja.mustache;

import java.io.File;

import ninja.utils.NinjaProperties;

import org.slf4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DeferringMustacheFactory;
import com.github.mustachejava.FallbackMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Simple mustache factory provider.
 * 
 * In Test mode, inject a
 * {@link com.github.mustachejava.FallbackMustacheFactory}
 * 
 * @author kpacha
 * 
 */
@Singleton
public class MustacheFactoryProvider implements Provider<MustacheFactory> {

    private final Logger logger;
    private final NinjaProperties ninjaProperties;
    private MustacheFactory cacheEnabledFactory = null;

    @Inject
    public MustacheFactoryProvider(Logger logger,
	    NinjaProperties ninjaProperties) {
	this.logger = logger;
	this.ninjaProperties = ninjaProperties;
    }

    @Override
    public MustacheFactory get() {
	MustacheFactory factory = null;
	if (ninjaProperties.isProd()) {
	    factory = getProductionFactory();
	} else if (ninjaProperties.isTest()) {
	    factory = getTestFactory();
	} else {
	    factory = new DefaultMustacheFactory();
	}
	return factory;
    }

    /**
     * return the cacheEnabledFactory. if it is null, instantiate a deferring
     * factory
     * 
     * @return
     */
    private MustacheFactory getProductionFactory() {
	if (cacheEnabledFactory == null) {
	    cacheEnabledFactory = new DeferringMustacheFactory();
	}
	return cacheEnabledFactory;
    }

    /**
     * return the cacheEnabledFactory. if it is null, instantiate a fallback
     * factory for testing
     * 
     * @return
     */
    private MustacheFactory getTestFactory() {
	MustacheFactory factory;
	if (cacheEnabledFactory == null) {
	    logger.debug("linking to the source folder as a fallback dir for testing");
	    String srcDir = System.getProperty("user.dir") + File.separator
		    + "src" + File.separator + "main" + File.separator + "java";
	    cacheEnabledFactory = new FallbackMustacheFactory(null, new File(
		    srcDir));
	}
	factory = cacheEnabledFactory;
	return factory;
    }

}
