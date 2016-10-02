# dropwizard-markdown-assets-bundle

[![CircleCI](https://circleci.com/gh/rnorth/dropwizard-markdown-assets-bundle.svg?style=svg)](https://circleci.com/gh/rnorth/dropwizard-markdown-assets-bundle)

An assets bundle (like [io.dropwizard.assets.AssetsBundle](http://www.dropwizard.io/1.0.0/dropwizard-assets/apidocs/io/dropwizard/assets/AssetsBundle.html)) that allows a dropwizard app to render and serve Markdown documents from the classpath. The goal is to provide a way to quickly and easily serve up static documentation for a service, taking advantage of Markdown's ease of editing and reusability. For example:

* Presenting markdown-based API documentation alongside a microservice
* Hosting usage or design documentation (text, graphs and sequence diagrams)
* Presenting basic usage notes for a public facing API, without requiring extra effort to construct fully-fledged HTML documentation

### A basic demo 'application' (consisting of documentation examples and nothing more) can be seen [here](https://dropwizard-markdown-demo.herokuapp.com/docs/) (see [here](https://github.com/rnorth/dropwizard-markdown-assets-bundle-demo) for source code).

In the 'out of the box' configuration (with default template and stylesheet) the following additional features are supported:

* Fast, capable server-side **markdown rendering** using [flexmark-java](https://github.com/vsch/flexmark-java) (including Github Flavored Markdown):
    * [Anchor links](https://github.com/vsch/flexmark-java/wiki/Extensions#anchorlink)
    * [Autolink](https://github.com/vsch/flexmark-java/wiki/Extensions#autolink)
    * [Footnotes](https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes)
    * [GFM Strikethrough](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-strikethrough)
    * [GFM Task lists](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-tasklist)
    * [GFM Tables](https://github.com/vsch/flexmark-java/wiki/Extensions#tables)
* [Generated tables of contents](https://github.com/vsch/flexmark-java/wiki/Extensions#table-of-contents-1)
* Optional rendering of **diagrams** using [Mermaid](https://knsv.github.io/mermaid/) in conjunction with fenced code blocks for graceful degradation:
    * Graphs and flowcharts
    * Sequence diagrams
    * Gantt charts
* Optional code **syntax highlighting** using [highlight.js](https://highlightjs.org/)
* Optional analytics integration using Google Analytics
* A simple and (subjectively!) nice default stylesheet
* Ability to serve non-markdown static assets of any type, as well

## Usage

### Basic setup

* Add the `dropwizard-markdown-assets-bundle` JAR to your Dropwizard application dependencies:
```xml
<dependency>
    <groupId>org.rnorth.dropwizard</groupId>
    <artifactId>dropwizard-markdown-assets-bundle</artifactId>
    <version>1.0.4</version>
</dependency>

```
* Modify your configuration class to implement `MarkdownBundleConfiguration`. Implementing this will entail adding a `MarkdownAssetsConfiguration` getter to your configuration class, as in the example below.

```java
public static class TestConfiguration extends Configuration implements MarkdownBundleConfiguration {

    public MarkdownAssetsConfiguration assets = new MarkdownAssetsConfiguration();

    @Override
    public MarkdownAssetsConfiguration getMarkdownAssetsConfiguration() {
        return assets;
    }
}
```

* Modify your Application `bootstrap` method to register a `MarkdownAssetsBundle`:

```java
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(new MarkdownAssetsBundle());
        }
```

* (For starters) place a markdown file named `index.md` in the `/assets` folder of your application classpath.
* Launch the application and see that the markdown file is available at `http://localhost:8080/assets/`, rendered as HTML.

### Customising behaviour

The following fluent setter methods on `MarkdownAssetsBundle` allow its behaviour to be customized:

* `withResourcePath`: set the resource path (in the classpath) of the markdown and static asset files (default: `/assets`)
* `withUriPath`: set the uri path for the markdown and static asset files (default: `/assets`)
* `withIndexFile`: set the name of the index file to use (default: `index.md`)
* `withAssetsName`: set the name of servlet mapping used for this assets bundle
* `withCacheBuilderSpec`: set the spec for the cache builder
* `withFlexMarkExtensions`: set a list of flexmark-java extensions that should be used for markdown parsing/rendering
* `withFlexMarkOptions`: set collection of flexmark-java options that should be used for markdown parsing/rendering

Through configuration, on a per-environment basis the following may also be set:

* Google Analytics tracking ID
* Whether or not to enable Mermaid rendering
* Whether or not to enable highlight.js highlighting
* Page footer content (e.g. copyright notice)

### Customizing template and stylesheet

A default page template and CSS stylesheet are included to serve as a default baseline for use. These defaults can be found inside this module at `/default-dropwizard-markdown-template.ftl` and `/default-dropwizard-markdown.css` respectively.

The page template is defined with Freemarker, and defines the structure that rendered markdown content will be placed into.

To replace either/both with your own, simply place a file named `template.ftl` or `dropwizard-markdown.css` in your `resourcePath` location (the root folder for markdown assets).

### Licence

See [LICENSE](LICENSE)

### Copyright

(c) Richard North 2016

### Acknowledgements

This library takes inspiration from or utilizes:

* https://github.com/bazaarvoice/dropwizard-configurable-assets-bundle
* https://github.com/vsch/flexmark-java
* https://knsv.github.io/mermaid/
* https://highlightjs.org/
* https://necolas.github.io/normalize.css/

### Changelog

#### [1.0.4](https://github.com/rnorth/dropwizard-markdown-assets-bundle/releases/tag/1.0.4)

* Responsive layout fixes

#### [1.0.3](https://github.com/rnorth/dropwizard-markdown-assets-bundle/releases/tag/1.0.3)

* Fix additional index document rendering issues for directory URLs

#### [1.0.2](https://github.com/rnorth/dropwizard-markdown-assets-bundle/releases/tag/1.0.2)

* Fix additional index document rendering issue

#### [1.0.1](https://github.com/rnorth/dropwizard-markdown-assets-bundle/releases/tag/1.0.1)

* Fix index document rendering issue
* Aesthetic improvements to default CSS stylesheet

#### [1.0.0](https://github.com/rnorth/dropwizard-markdown-assets-bundle/releases/tag/1.0.0)

Initial release