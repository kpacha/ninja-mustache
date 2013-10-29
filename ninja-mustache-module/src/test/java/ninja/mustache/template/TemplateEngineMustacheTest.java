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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

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

    TemplateEngineMustache mustacheTemplate;

    @Before
    public void setUp() throws Exception {
	mockProperties();
	mockContext();
	mockTemplateHelper();
	mockLanguage();
	mockCookies();
	mockFlashCookies();

	mustacheTemplate = new TemplateEngineMustache(messages, lang,
		ninjaLogger, exceptionHandler, templateHelper,
		templateEngineManager, ninjaProperties, engine);
    }

    @Test
    public void testInvoke() throws Exception {
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	Writer writer = new PrintWriter(byteArrayOutputStream);
	when(responseStreams.getWriter()).thenReturn(writer);

	when(mustache.execute(Mockito.eq(writer), Mockito.any(HashMap.class)))
		.then(new Answer<Writer>() {
		    public Writer answer(InvocationOnMock invocation)
			    throws Throwable {
			Map<String, Object> parameters = (Map<String, Object>) invocation
				.getArguments()[1];
			assertNotNull(parameters.get("flash"));
			assertNotNull(parameters.get("i18n"));
			assertEquals("en", parameters.get("lang"));
			assertEquals("/", parameters.get("contextPath"));

			Writer writer = (Writer) invocation.getArguments()[0];
			writer.write("Hellow world from Mustache");
			return writer;
		    }
		});

	when(engine.compile(Mockito.eq("TemplateName"))).thenReturn(mustache);

	mustacheTemplate.invoke(contextRenerable, result);

	assertEquals("Hellow world from Mustache",
		byteArrayOutputStream.toString());
    }

    @Test
    public void testInvokeWithMapAsRenderable() throws Exception {
	when(result.getRenderable()).thenReturn(Maps.newHashMap());
	testInvoke();
    }

    @Test
    public void testInvokeWithObjectAsRenderable() throws Exception {
	when(result.getRenderable()).thenReturn(new Object());
	testInvoke();
    }

    private void mockContext() {
	when(contextRenerable.finalizeHeaders(Mockito.eq(result))).thenReturn(
		responseStreams);
	when(contextRenerable.getRoute()).thenReturn(route);
	when(contextRenerable.getContextPath()).thenReturn("/");
    }

    private void mockProperties() {
	Properties p = new Properties();
	p.setProperty("key", "value");
	when(ninjaProperties.getAllCurrentNinjaProperties()).thenReturn(p);
    }

    private void mockFlashCookies() {
	when(flashCookie.getCurrentFlashCookieData()).thenReturn(
		new HashMap<String, String>());
	when(contextRenerable.getFlashCookie()).thenReturn(flashCookie);
    }

    private void mockCookies() {
	Map<String, String> cookieData = Maps.newHashMap();
	cookieData.put("sessionKey", "sessionData");
	when(cookie.isEmpty()).thenReturn(false);
	when(cookie.getData()).thenReturn(cookieData);
	when(contextRenerable.getSessionCookie()).thenReturn(cookie);
    }

    private void mockTemplateHelper() {
	when(
		templateHelper.getTemplateForResult(Mockito.eq(route),
			Mockito.eq(result), Mockito.eq(".html"))).thenReturn(
		"TemplateName");
    }

    private void mockLanguage() {
	when(contextRenerable.getAcceptLanguage()).thenReturn("en");
	Optional<String> language = Optional.of("en");
	when(
		lang.getLanguage(Mockito.eq(contextRenerable),
			Mockito.eq(Optional.of(result)))).thenReturn(language);
    }
}
