package io.quarkiverse.roq.plugin.prism.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkiverse.roq.frontmatter.runtime.RoqHeadContributor;
import io.quarkiverse.roq.frontmatter.runtime.model.Page;
import io.quarkus.qute.RawString;

/**
 * Injects the Prism CSS and JS bundle into the {@code <head>} of any page whose
 * front matter contains {@code highlight: prism}.
 */
@ApplicationScoped
public class PrismHeadContributor implements RoqHeadContributor {

    static final String HIGHLIGHT_KEY = "highlight";
    static final String HIGHLIGHT_VALUE = "prism";

    static final String JS_PATH = "/static/bundle/prism.js";
    static final String CSS_PATH = "/static/bundle/prism.css";

    @Inject
    @ConfigProperty(name = "quarkus.uuid")
    String uuid;

    @Override
    public boolean appliesTo(Page page) {
        return HIGHLIGHT_VALUE.equals(page.data().getString(HIGHLIGHT_KEY));
    }

    @Override
    public RawString contribute(Page page) {
        String jsUrl = page.site().url().fromRoot(JS_PATH) + "?id=" + uuid;
        String cssUrl = page.site().url().fromRoot(CSS_PATH) + "?id=" + uuid;
        return new RawString(
                "<script defer src=\"" + jsUrl + "\"></script>\n" +
                        "<link rel=\"stylesheet\" href=\"" + cssUrl + "\">");
    }
}
