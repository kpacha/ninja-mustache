package ninja.mustache.template;

import java.util.Map;
import java.util.Map.Entry;

import ninja.Context;
import ninja.Result;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.template.TemplateEngine;

import org.slf4j.Logger;

import com.google.common.base.CaseFormat;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Default implementation of the TemplateEngine interface
 * 
 * @author ra, kpacha
 */
public abstract class AbstractTemplateEngine implements TemplateEngine {

    protected Messages messages;

    protected Lang lang;

    protected Logger logger;

    /**
     * Just collect the properties and put them into a Map
     * 
     * @param context
     * @param result
     * @return
     */
    protected Map getTemplateProperties(Context context, Result result) {
	Map map = initializeTemplatePropertiesMap(result.getRenderable());
	insertContextPath(context, map);
	insertLanguageProperty(context, result, map);
	insertSessionProperties(context, map);
	insertI18nProperties(context, result, map);
	insertFlashProperties(context, result, map);
	return map;
    }

    /**
     * Get an initialized map with the received object
     * 
     * @param renderableResult
     * @return
     */
    protected Map initializeTemplatePropertiesMap(Object renderableResult) {
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

    /**
     * Put the context path into the map
     * 
     * @param context
     * @param map
     */
    protected void insertContextPath(Context context, Map map) {
	map.put("contextPath", context.getContextPath());
    }

    /**
     * set language from framework. You can access it in the templates as {lang}
     * 
     * @param context
     * @param result
     * @param map
     */
    protected void insertLanguageProperty(Context context, Result result,
	    Map map) {
	Optional<String> language = lang.getLanguage(context,
		Optional.of(result));
	if (language.isPresent()) {
	    map.put("lang", language.get());
	}
    }

    /**
     * Put all entries of the session cookie to the map. You can access the
     * values by their key in the cookie
     * 
     * @param context
     * @param map
     */
    protected void insertSessionProperties(Context context, Map map) {
	if (!context.getSession().isEmpty()) {
	    map.put("session", context.getSession().getData());
	}
    }

    /**
     * Add the required key-values or deltas for i18n
     * 
     * @param context
     * @param result
     * @param map
     */
    abstract protected void insertI18nProperties(Context context,
	    Result result, Map map);

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
    protected void insertFlashProperties(Context context, Result result, Map map) {
	Map<String, String> translatedFlashCookieMap = Maps.newHashMap();
	for (Entry<String, String> entry : context.getFlashScope()
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

}
