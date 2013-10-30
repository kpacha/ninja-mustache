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

import ninja.Context;
import ninja.Result;
import ninja.i18n.Messages;

import com.github.mustachejava.TemplateFunction;
import com.google.common.base.Optional;

/**
 * A Mustache TemplateFunction useful for i18n
 * 
 * @author kpacha
 */
public class MustacheTranslateBundleFunction implements TemplateFunction {

    private final Messages messages;
    private final Context context;
    private final Optional<Result> result;

    /**
     * @param messages
     * @param context
     * @param result
     */
    public MustacheTranslateBundleFunction(Messages messages, Context context,
	    Result result) {
	this.messages = messages;
	this.context = context;
	this.result = Optional.of(result);
    }

    @Override
    public String apply(String input) {
	return messages.get(input, context, result).or(input);
    }

}
