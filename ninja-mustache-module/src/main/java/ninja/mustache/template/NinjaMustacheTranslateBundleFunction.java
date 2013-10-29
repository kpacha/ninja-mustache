package ninja.mustache.template;

import ninja.Context;
import ninja.i18n.Messages;

import com.github.mustachejava.TemplateFunction;
import com.google.common.base.Optional;

public class NinjaMustacheTranslateBundleFunction implements TemplateFunction {

    private final Messages messages;
    private final Context context;

    public NinjaMustacheTranslateBundleFunction(Messages messages,
	    Context context) {
	this.messages = messages;
	this.context = context;
    }

    @Override
    public String apply(String input) {
	Optional<String> language = Optional.of(context.getAcceptLanguage());
	return messages.get(input, language).or(input);
    }

}
