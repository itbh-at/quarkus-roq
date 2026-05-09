package io.quarkiverse.roq.frontmatter.runtime;

import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import io.quarkiverse.roq.frontmatter.runtime.model.Page;
import io.quarkus.qute.RawString;

/**
 * Collects all {@link RoqHeadContributor} CDI beans and exposes their filtered output
 * for a given page. Consumed by {@code roq-base/default.html} via {@code cdi:roqHeadContributions}.
 */
@ApplicationScoped
public class RoqHeadContributions {

    @Inject
    Instance<RoqHeadContributor> contributors;

    /**
     * Returns the raw HTML fragments from all contributors that apply to {@code page}.
     * The list is empty when no contributors match, so the template loop is a no-op.
     */
    public List<RawString> forPage(Page page) {
        return contributors.stream()
                .filter(c -> c.appliesTo(page))
                .sorted(Comparator.comparingInt(RoqHeadContributor::priority))
                .map(c -> c.contribute(page))
                .toList();
    }
}
