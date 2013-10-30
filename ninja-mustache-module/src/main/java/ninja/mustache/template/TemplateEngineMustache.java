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

import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.mustache.exception.NinjaExceptionHandler;
import ninja.template.TemplateEngineHelper;
import ninja.template.TemplateEngineManager;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

import org.slf4j.Logger;

import com.github.mustachejava.MustacheFactory;
import com.google.inject.Inject;

/**
 * 
 * Render Ninja with Mustache template engine (http://mustache.github.io/).
 * 
 * @author sojin, kpacha
 * 
 */

public class TemplateEngineMustache extends AbstractTemplateEngine {

    private final String FILE_SUFFIX = ".mustache";

    private final TemplateEngineHelper templateEngineHelper;

    private final NinjaExceptionHandler exceptionHandler;

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
     * A method that renders i18n messages and can also render messages with
     * placeholders directly in your template:
     * 
     * {#i18n}mykey{/i18n}
     * 
     * @param context
     * @param map
     */
    protected void insertI18nProperties(Context context, Map map) {
	map.put("i18n", new NinjaMustacheTranslateBundleFunction(messages,
		context));
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
