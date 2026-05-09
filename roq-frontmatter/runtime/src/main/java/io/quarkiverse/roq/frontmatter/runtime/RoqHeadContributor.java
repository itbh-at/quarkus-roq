package io.quarkiverse.roq.frontmatter.runtime;

import io.quarkiverse.roq.frontmatter.runtime.model.Page;
import io.quarkus.qute.RawString;

/**
 * CDI interface for plugins to inject raw HTML into the {@code <head>} of every rendered page.
 * <p>
 * <b>This is the standard extensibility mechanism for Roq plugins that need to contribute to
 * {@code <head>} (CSS, JavaScript, meta tags, etc.). Implement this interface instead of
 * modifying or overriding any template.</b>
 * <p>
 * Implement this interface and make the class a CDI bean (e.g. {@code @ApplicationScoped}).
 * {@link RoqHeadContributions} collects all instances and the base layout iterates over them,
 * so no theme or user-template changes are required.
 */
public interface RoqHeadContributor {

    /**
     * Determines the position of this contributor relative to others.
     * Lower values appear earlier in {@code <head>}. Defaults to {@code 0}.
     */
    default int priority() {
        return 0;
    }

    /**
     * Returns {@code true} if this contributor should emit markup for the given page.
     * Called once per page render; keep it cheap (e.g. a front-matter key check).
     */
    boolean appliesTo(Page page);

    /**
     * Returns the raw HTML fragment to inject (e.g. {@code <link>} / {@code <script>} tags).
     * Only called when {@link #appliesTo(Page)} returns {@code true}.
     */
    RawString contribute(Page page);
}
