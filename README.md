# dropwizard-markdown-assets-bundle

TODO Intro

## Usage

### Customizing template and stylesheet

A default page template and CSS stylesheet are included to serve as a default baseline for use. These defaults can be found inside this module at `/default-dropwizard-markdown-template.ftl` and `/default-dropwizard-markdown.css` respectively.

The page template is defined with Freemarker, and defines the structure that rendered markdown content will be placed into.

To replace either/both with your own, simply place a file named `template.ftl` or `dropwizard-markdown.css` in your `resourcePath` location (the root folder for markdown assets).