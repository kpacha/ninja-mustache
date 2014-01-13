package ninja.mustache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Singleton;
import ninja.utils.NinjaProperties;

import org.slf4j.Logger;

import com.github.mustachejava.FallbackMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Simple mustache factory provider.
 * 
 * Just return a {@link com.github.mustachejava.FallbackMustacheFactory}. In dev
 * mode, always instantiate a new factory
 * 
 * @author kpacha
 */
@Singleton
public class MustacheFactoryProvider implements Provider<MustacheFactory> {

    private final Logger logger;
    private final NinjaProperties ninjaProperties;
    private MustacheFactory cachedFactory = null;
    private List<Object> resourceRoots = new ArrayList<Object>();

    @Inject
    public MustacheFactoryProvider(Logger logger,
	    NinjaProperties ninjaProperties) {
	this.logger = logger;
	this.ninjaProperties = ninjaProperties;
	initResourceRoots();
    }

    /**
     * register the roots to be passed to the fallback factory
     */
    private void initResourceRoots() {
	if (!ninjaProperties.isProd()) {
	    File srcRoot = getSrcViewsRootFile();
	    if (srcRoot.exists()) {
		resourceRoots.add(srcRoot);
	    }
	}
	resourceRoots.add("");
	resourceRoots.add("../../");
	logger.debug(resourceRoots.size()
		+ " resource roots have been registered!");
    }

    @Override
    public MustacheFactory get() {
	MustacheFactory factory = null;
	if (!ninjaProperties.isDev()) {
	    factory = getCachedFactory();
	} else {
	    factory = getNewFallbackFactory();
	}
	return factory;
    }

    /**
     * return the cacheEnabledFactory. if it is null, instantiate a fallback
     * factory
     * 
     * @return
     */
    private MustacheFactory getCachedFactory() {
	if (cachedFactory == null) {
	    cachedFactory = getNewFallbackFactory();
	}
	return cachedFactory;
    }

    /**
     * Instantiate a fallback factory in order to get templates from several
     * packages (like the ninja-core or the ninja-mustache)
     * 
     * @return
     */
    private MustacheFactory getNewFallbackFactory() {
	logger.debug("Instantiating a new Mustache Factory with "
		+ resourceRoots.size() + " resource roots");
	return new FallbackMustacheFactory(resourceRoots.toArray());
    }

    /**
     * Init src folder
     * 
     * @return
     */
    private File getSrcViewsRootFile() {
	String srcDir = System.getProperty("user.dir") + File.separator + "src"
		+ File.separator + "main" + File.separator + "java";
	return new File(srcDir);
    }

}
