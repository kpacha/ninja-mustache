package ninja.mustache;

import java.io.File;

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
	if (!ninjaProperties.isDev()) {
	    factory = getcacheEnabledFactory();
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
    private MustacheFactory getcacheEnabledFactory() {
	if (cacheEnabledFactory == null) {
	    cacheEnabledFactory = getNewFallbackFactory();
	}
	return cacheEnabledFactory;
    }

    /**
     * Instantiate a fallback factory in order to get templates from several
     * packages (like the ninja-core or the ninja-mustache)
     * 
     * @return
     */
    private MustacheFactory getNewFallbackFactory() {
	File srcRoot = getSrcViewsRootFile();
	Object[] roots = null;
	if (srcRoot.exists()) {
	    roots = new Object[] { srcRoot, "", "../../" };
	} else {
	    roots = new Object[] { "", "../../" };
	}
	return new FallbackMustacheFactory(roots);
    }

    /**
     * Init src folder for testing
     * 
     * @return
     */
    private File getSrcViewsRootFile() {
	String srcDir = System.getProperty("user.dir") + File.separator + "src"
		+ File.separator + "main" + File.separator + "java";
	return new File(srcDir);
    }

}
