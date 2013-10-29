/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.mustache.template;

import java.io.File;
import java.util.Properties;

import ninja.i18n.Messages;
import ninja.template.TemplateEngineManager;
import ninja.utils.NinjaProperties;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * To Configure Mustache in Ninja style.
 * 
 * @author sojin, kpacha
 * 
 */
@Singleton
public class TemplateMustacheConfiguration {

    private final Messages messages;

    private final NinjaProperties ninjaProperties;

    private final Logger logger;

    private final Properties properties = new Properties();

    @Inject
    public TemplateMustacheConfiguration(Messages messages, Logger ninjaLogger,
	    TemplateEngineManager templateEngineManager,
	    NinjaProperties ninjaProperties) {

	this.messages = messages;
	this.logger = ninjaLogger;
	this.ninjaProperties = ninjaProperties;

	init();
    }

    private void init() {

	final boolean isProd = ninjaProperties.isProd();

	// properties.put("mustache.engine.mode", isProd ? Rythm.Mode.prod
	// : Rythm.Mode.dev);
	// properties.put("mustache.engine.plugin.version", "");
	//
	// properties.put("mustache.log.factory", new ILoggerFactory() {
	// @Override
	// public ILogger getLogger(Class<?> clazz) {
	// return new TemplateEngineRythmLogger(logger);
	// }
	// });

	String classLocation = TemplateMustacheConfiguration.class
		.getProtectionDomain().getCodeSource().getLocation().getPath();

	Optional<String> templateDir, tmpDir = Optional.absent();
	// TODO revisit here once issue
	// https://github.com/greenlaw110/Rythm/issues/173 solved.
	if (classLocation.contains("WEB-INF")
		&& classLocation.contains("ninja-mustache-module") /*
								    * dirty hack
								    * until then
								    */) {
	    String webInf = classLocation.substring(0,
		    classLocation.indexOf("WEB-INF") + 7)
		    + File.separator;
	    templateDir = Optional.of(webInf + "classes" + File.separator
		    + "views");
	    properties.put("mustache.engine.file_write", false);
	    // properties.put("mustache.engine.mode", Rythm.Mode.prod);
	    logger.warn("Mustache seems to be running in a servlet container, set the mode to prod.");
	} else {
	    String tmp = System.getProperty("user.dir") + File.separator;
	    templateDir = Optional.of(tmp + "src" + File.separator + "main"
		    + File.separator + "java" + File.separator + "views");
	    tmpDir = Optional.of(tmp + "target" + File.separator + "tmp"
		    + File.separator + "__mustache");
	}

	if (templateDir.isPresent()) {
	    properties.put("mustache.home.template",
		    new File(templateDir.get()));
	    logger.info("mustache template root set to: {}",
		    properties.get("mustache.home.template"));
	} else {
	    logger.error("Unable to set mustache.home.template");
	}

	if (isProd
		|| (ninjaProperties
			.getBooleanWithDefault("mustache.gae", false))) {
	    properties.put("mustache.engine.file_write", false);
	    logger.info("In Prod/GAE mode, Mustache engine writing to file system disabled.");
	} else if (tmpDir.isPresent()) {
	    File temp = new File(tmpDir.get());
	    if (!temp.exists()) {
		temp.mkdirs();
	    }
	    properties.put("mustache.home.tmp", temp);
	    logger.info("mustache tmp dir set to {}",
		    properties.get("mustache.home.tmp"));
	} else {
	    logger.info("Didn't set mustache.home.tmp.");
	}

	properties.put("mustache.resource.autoScan", false);

	// properties.put("rythm.codegen.source_code_enhancer",
	// new ISourceCodeEnhancer() {
	//
	// @Override
	// public Map<String, ?> getRenderArgDescriptions() {
	// Map<String, Object> m = new HashMap<String, Object>();
	// m.put("flash", "java.util.Map<String, String>");
	// m.put("lang", "java.lang.String");
	// m.put("contextPath", "java.lang.String");
	// m.put("session", "java.util.Map<String, String>");
	// return m;
	// }
	//
	// @Override
	// public void setRenderArgs(ITemplate template) {
	//
	// }
	//
	// @Override
	// public List<String> imports() {
	// return new ArrayList<String>();
	// }
	//
	// @Override
	// public String sourceCode() {
	// return null;
	// }
	// });

	// properties.put("mustache.i18n.message.resolver",
	// new TemplateEngineRythmI18nMessageResolver(messages));

	// Set Mustache properties coming from Ninja application.conf
	// allows users to override any mustache properties.
	for (String key : ninjaProperties.getAllCurrentNinjaProperties()
		.stringPropertyNames()) {
	    if (key.startsWith("mustache.")) {
		properties.setProperty(key, ninjaProperties.get(key));
	    }
	}
    }

    public Properties getConfiguration() {
	return properties;
    }
}
