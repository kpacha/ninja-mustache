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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;

import ninja.Context;
import ninja.Result;
import ninja.Route;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.mustache.exception.NinjaExceptionHandler;
import ninja.session.FlashCookie;
import ninja.session.SessionCookie;
import ninja.template.TemplateEngineHelper;
import ninja.template.TemplateEngineManager;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class TemplateEngineMustacheTest {

    @Mock
    Context contextRenerable;

    @Mock
    ResponseStreams responseStreams;

    @Mock
    NinjaProperties ninjaProperties;

    @Mock
    Messages messages;

    @Mock
    Lang lang;

    @Mock
    Logger ninjaLogger;

    @Mock
    NinjaExceptionHandler exceptionHandler;

    @Mock
    TemplateEngineManager templateEngineManager;

    @Mock
    TemplateEngineHelper templateHelper;

    @Mock
    Result result;

    @Mock
    Route route;

    @Mock
    MustacheFactory engine;

    @Mock
    SessionCookie cookie;

    @Mock
    FlashCookie flashCookie;

    @Mock
    Mustache mustache;

    @Test
    public void testInvoke() throws Exception {
	Properties p = new Properties();
	p.setProperty("key", "value");
	when(ninjaProperties.getAllCurrentNinjaProperties()).thenReturn(p);

	TemplateEngineMustache mustacheTemplate = new TemplateEngineMustache(
		messages, lang, ninjaLogger, exceptionHandler, templateHelper,
		templateEngineManager, ninjaProperties, engine);

	when(contextRenerable.finalizeHeaders(Mockito.eq(result))).thenReturn(
		responseStreams);

	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	when(responseStreams.getOutputStream()).thenReturn(
		byteArrayOutputStream);

	Writer writer = new PrintWriter(byteArrayOutputStream);
	when(responseStreams.getWriter()).thenReturn(writer);

	when(cookie.isEmpty()).thenReturn(true);
	when(contextRenerable.getSessionCookie()).thenReturn(cookie);

	when(flashCookie.getCurrentFlashCookieData()).thenReturn(
		new HashMap<String, String>());
	when(contextRenerable.getFlashCookie()).thenReturn(flashCookie);
	when(contextRenerable.getRoute()).thenReturn(route);

	when(
		templateHelper.getTemplateForResult(Mockito.eq(route),
			Mockito.eq(result), Mockito.eq(".html"))).thenReturn(
		"TemplateName");

	Optional<String> language = Optional.absent();
	when(
		lang.getLanguage(Mockito.eq(contextRenerable),
			Mockito.eq(Optional.of(result)))).thenReturn(language);

	when(engine.compile(Mockito.eq("TemplateName"))).thenReturn(mustache);

	when(
		mustache.execute(Mockito.any(Writer.class),
			Mockito.eq(new HashMap<String, Object>()))).thenReturn(
		appendTemplateContent(writer));

	mustacheTemplate.invoke(contextRenerable, result);

	assertEquals("Hellow world from Mustache" + SystemUtils.LINE_SEPARATOR,
		byteArrayOutputStream.toString());
    }

    private Writer appendTemplateContent(Writer writer) throws IOException {
	writer.write("Hellow world from Mustache");
	return writer;
    }
}
