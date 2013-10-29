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

import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.mustache.exception.NinjaExceptionHandler;
import ninja.template.TemplateEngine;
import ninja.template.TemplateEngineHelper;
import ninja.template.TemplateEngineManager;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

import org.slf4j.Logger;

import com.github.mustachejava.MustacheFactory;
import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * 
 * Render Ninja with Mustache template engine (http://mustache.github.io/).
 * 
 * @author sojin, kpacha
 * 
 */

public class TemplateEngineMustache implements TemplateEngine {

    private final String FILE_SUFFIX = ".html";

    private final Messages messages;

    private final Lang lang;

    private final TemplateEngineHelper templateEngineHelper;

    private final NinjaExceptionHandler exceptionHandler;

    private final Logger logger;

    private final MustacheFactory mustacheFactory;

    @Inject
    public TemplateEngineMustache(Messages messages, Lang lang,
	    Logger ninjaLogger, NinjaExceptionHandler exceptionHandler,
	    TemplateEngineHelper templateEngineHelper,
	    TemplateEngineManager templateEngineManager,
	    NinjaProperties ninjaProperties, MustacheFactory mustacheFactory)
	    throws Exception {

	this.messages = messages;
	this.lang = lang;
	this.logger = ninjaLogger;
	this.templateEngineHelper = templateEngineHelper;
	this.exceptionHandler = exceptionHandler;
	this.mustacheFactory = mustacheFactory;
    }

    @Override
    public void invoke(Context context, Result result) {
	ResponseStreams responseStreams = context.finalizeHeaders(result);
	String templateName = templateEngineHelper.getTemplateForResult(
		context.getRoute(), result, FILE_SUFFIX);

	render(context, responseStreams,
		getTemplateProperties(context, result), templateName);
    }

    private void render(Context context, ResponseStreams responseStreams,
	    Map templateProperties, String templateName) {
	try {
	    Writer writer = mustacheFactory.compile(templateName).execute(
		    responseStreams.getWriter(), templateProperties);
	    writer.flush();
	    writer.close();
	} catch (Exception e) {
	    handleServerError(context, e);
	}
    }

    /**
     * Just collect the properties and put them into a Map
     * 
     * @param context
     * @param result
     * @return
     */
    private Map getTemplateProperties(Context context, Result result) {
	Map map = initializeTemplatePropertiesMap(result.getRenderable());
	insertLanguageProperty(context, result, map);
	insertSessionProperties(context, map);
	insertI18nProperties(context, map);
	insertFlashProperties(context, result, map);
	return map;
    }

    /**
     * Convenience method to translate possible flash scope keys.
     * 
     * If you want to set messages with placeholders please do that in your
     * controller. We only can set simple messages. Eg. A message like
     * "errorMessage=my name is: {0}" => translate in controller and pass
     * directly. A message like " errorMessage=An error occurred" => use that as
     * errorMessage.
     * 
     * get keys via {flash.KEYNAME}
     * 
     * @param context
     * @param result
     * @param map
     */
    private void insertFlashProperties(Context context, Result result, Map map) {
	Map<String, String> translatedFlashCookieMap = Maps.newHashMap();
	for (Entry<String, String> entry : context.getFlashCookie()
		.getCurrentFlashCookieData().entrySet()) {

	    String messageValue = null;

	    Optional<String> messageValueOptional = messages.get(
		    entry.getValue(), context, Optional.of(result));

	    if (!messageValueOptional.isPresent()) {
		messageValue = entry.getValue();
	    } else {
		messageValue = messageValueOptional.get();
	    }
	    translatedFlashCookieMap.put(entry.getKey(), messageValue);
	}

	map.put("flash", translatedFlashCookieMap);
    }

    /**
     * A method that renders i18n messages and can also render messages with
     * placeholders directly in your template:
     * 
     * {#i18n}mykey{/i18n}
     * 
     * @param context
     * @param map
     */
    private void insertI18nProperties(Context context, Map map) {
	map.put("i18n", new NinjaMustacheTranslateBundleFunction(messages,
		context));
    }

    /**
     * Put all entries of the session cookie to the map. You can access the
     * values by their key in the cookie
     * 
     * @param context
     * @param map
     */
    private void insertSessionProperties(Context context, Map map) {
	if (!context.getSessionCookie().isEmpty()) {
	    map.put("session", context.getSessionCookie().getData());
	}

	map.put("contextPath", context.getContextPath());
    }

    /**
     * set language from framework. You can access it in the templates as {lang}
     * 
     * @param context
     * @param result
     * @param map
     */
    private void insertLanguageProperty(Context context, Result result, Map map) {
	Optional<String> language = lang.getLanguage(context,
		Optional.of(result));
	if (language.isPresent()) {
	    map.put("lang", language.get());
	}
    }

    private Map initializeTemplatePropertiesMap(Object renderableResult) {
	Map map;
	// if the object is null we simply render an empty map...
	if (renderableResult == null) {
	    map = Maps.newHashMap();

	} else if (renderableResult instanceof Map) {
	    map = (Map) renderableResult;

	} else {
	    map = createTemplatePropertiesMapAndInsert(renderableResult);

	}
	return map;
    }

    /**
     * Getting an arbitrary Object, put that into the root of the template
     * properties map
     * 
     * If you are rendering something like Results.ok().render(new MyObject())
     * Assume MyObject has a public String name field.
     * 
     * You can then access the fields in the template like that:
     * {myObject.publicField}
     * 
     * @param renderableResult
     * @return
     */
    private Map createTemplatePropertiesMapAndInsert(Object renderableResult) {
	String realClassNameLowerCamelCase = CaseFormat.UPPER_CAMEL.to(
		CaseFormat.LOWER_CAMEL, renderableResult.getClass()
			.getSimpleName());

	Map map = Maps.newHashMap();
	map.put(realClassNameLowerCamelCase, renderableResult);
	return map;
    }

    // FIXME!!!
    private void handleServerError(Context context, Exception e) {
	ResponseStreams outStream = context.finalizeHeaders(Results
		.internalServerError());
	// String response = engine.getTemplate(
	// MustacheConstant.LOCATION_VIEW_HTML_INTERNAL_SERVER_ERROR)
	// .render();
	// exceptionHandler.handleException(e, response, outStream);
	exceptionHandler.handleException(e, null, outStream);
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
	return FILE_SUFFIX;
    }

    @Override
    public String getContentType() {
	return "text/html";
    }

}
