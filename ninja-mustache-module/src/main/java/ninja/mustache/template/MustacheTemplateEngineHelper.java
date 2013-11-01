package ninja.mustache.template;

import ninja.Result;
import ninja.Route;
import ninja.mustache.utils.MustacheConstant;
import ninja.template.TemplateEngineHelper;

import com.google.inject.Singleton;

@Singleton
public class MustacheTemplateEngineHelper extends TemplateEngineHelper {

    public String getTemplateForResult(Route route, Result result, String suffix) {
	return super.getTemplateForResult(route, result, suffix).replace(
		".ftl.html", MustacheConstant.DEFAULT_EXTENSION);
	// return super.getTemplateForResult(route, result, suffix);
    }

}
