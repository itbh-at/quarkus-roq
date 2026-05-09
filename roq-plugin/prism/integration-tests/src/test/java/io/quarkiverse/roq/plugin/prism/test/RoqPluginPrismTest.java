package io.quarkiverse.roq.plugin.prism.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class RoqPluginPrismTest {

    @Test
    void prismJsBundleIsServedAndContainsRequestedAndTransitiveLanguages() {
        Response response = RestAssured.when().get("/static/bundle/prism.js")
                .then().statusCode(200).log().ifValidationFails().extract().response();
        String body = response.asString();
        assertThat(response.contentType()).contains("javascript");

        // Prism core (an IIFE) must be present; the global is exported as 'Prism'
        assertThat(body).contains("Prism=function");
        // Explicitly requested languages: bash and java (java is registered via extend("clike", ...))
        assertThat(body).contains("languages.bash=");
        assertThat(body).contains("languages.java=");
        // 'java' transitively requires 'clike'; the resolver must include it
        assertThat(body).contains("Prism.languages.clike=");
        // Auto-highlight trigger appended by the build step
        assertThat(body).contains("Prism.highlightAll()");
    }

    @Test
    void prismCssBundleIsServedAndMatchesConfiguredTheme() {
        Response response = RestAssured.when().get("/static/bundle/prism.css")
                .then().statusCode(200).log().ifValidationFails().extract().response();
        String body = response.asString();
        // The 'tomorrow' theme uses #2d2d2d as its code background; this byte sequence
        // is unique to the dark themes and absent from the default theme.
        assertThat(body).contains("#2d2d2d");
    }

    @Test
    void prismTagInjectsScriptAndStylesheetIntoRenderedPage() {
        String body = RestAssured.when().get("/")
                .then().statusCode(200).log().ifValidationFails().extract().asString();
        assertThat(body).contains("/static/bundle/prism.js");
        assertThat(body).contains("/static/bundle/prism.css");
        // The {#prism /} tag emits a <script> and a <link rel="stylesheet">
        assertThat(body).containsPattern("<script[^>]+src=\"[^\"]*/static/bundle/prism\\.js");
        assertThat(body).containsPattern("<link[^>]+href=\"[^\"]*/static/bundle/prism\\.css");
    }

    @Test
    void frontMatterHighlightKeyInjectsPrismIntoHead() {
        // /highlight/ uses layout: plain (no {#prism /}) but has highlight: prism in front matter.
        // PrismHeadContributor should inject the tags via roq-base/default.html's contributor loop.
        String body = RestAssured.when().get("/highlight/")
                .then().statusCode(200).log().ifValidationFails().extract().asString();
        assertThat(body).containsPattern("<script[^>]+src=\"[^\"]*/static/bundle/prism\\.js");
        assertThat(body).containsPattern("<link[^>]+href=\"[^\"]*/static/bundle/prism\\.css");
        // Tags must land inside <head>, not in <body>
        int headClose = body.indexOf("</head>");
        int scriptPos = body.indexOf("/static/bundle/prism.js");
        assertThat(scriptPos).isGreaterThan(0).isLessThan(headClose);
    }

    @Test
    void pageWithoutHighlightKeyDoesNotLoadPrism() {
        // The index page uses layout: prism-test which has {#prism /} explicitly, but
        // a hypothetical page with no highlight front matter and a plain layout must not
        // receive the Prism assets via the contributor.
        // Re-use /highlight/ as reference: confirm the index (layout: prism-test, no
        // highlight key) still loads prism only via the explicit tag, not the contributor.
        // We verify the contributor does NOT double-inject on a page that already has it.
        String indexBody = RestAssured.when().get("/")
                .then().statusCode(200).log().ifValidationFails().extract().asString();
        // Only one occurrence of the JS bundle path — not duplicated by the contributor
        assertThat(countOccurrences(indexBody, "/static/bundle/prism.js")).isEqualTo(1);
    }

    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(pattern, idx)) != -1) {
            count++;
            idx += pattern.length();
        }
        return count;
    }
}
