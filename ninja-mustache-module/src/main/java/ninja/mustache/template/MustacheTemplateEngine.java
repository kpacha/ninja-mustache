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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.mustache.exception.NinjaExceptionHandler;
import ninja.mustache.utils.MustacheConstant;
import ninja.template.TemplateEngineHelper;
import ninja.template.TemplateEngineManager;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

import org.slf4j.Logger;

import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Render Ninja with Mustache template engine (http://mustache.github.io/).
 * 
 * @author kpacha
 */
public class MustacheTemplateEngine extends AbstractTemplateEngine {

    private final TemplateEngineHelper templateEngineHelper;

    private final NinjaExceptionHandler exceptionHandler;

    private final MustacheFactory mustacheFactory;

    @Inject
    public MustacheTemplateEngine(Messages messages, Lang lang,
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
		context.getRoute(), result, MustacheConstant.DEFAULT_EXTENSION);

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
	} catch (IOException e) {
	    handleServerError(context, e);
	} catch (MustacheException e) {
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
	map.put("i18n", new MustacheTranslateBundleFunction(messages, context));
    }

    /**
     * handle the error: finalize the response headers, get the error template
     * and delegate the rendering to the exceptionHandler
     * 
     * @param context
     * @param e
     */
    private void handleServerError(Context context, Exception e) {
	ResponseStreams outStream = context.finalizeHeaders(Results
		.internalServerError());
	String errorTemplate = mustacheFactory
		.compile(
			MustacheConstant.LOCATION_VIEW_HTML_INTERNAL_SERVER_ERROR)
		.execute(new StringWriter(), Maps.newHashMap()).toString();
	exceptionHandler.handleException(e, errorTemplate, outStream);
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
	return MustacheConstant.DEFAULT_EXTENSION;
    }

    @Override
    public String getContentType() {
	return "text/html";
    }

}
