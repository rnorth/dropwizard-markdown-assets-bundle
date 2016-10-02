<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>

    <#if useHlJs>
        <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.7.0/styles/default.min.css">
    </#if>
    <link rel="stylesheet" href="${uriPath}dropwizard-markdown.css"/>
</head>
<body>
    ${html}

    <#if useMermaid>
        <script src="//cdn.rawgit.com/knsv/mermaid/6.0.0/dist/mermaid.min.js"></script>
        <script>mermaid.init({startOnLoad: true}, ".language-mermaid");</script>
    </#if>

    <#if useHlJs>
        <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.7.0/highlight.min.js"></script>
        <script>hljs.initHighlightingOnLoad();</script>
    </#if>

    <#if useGoogleAnalytics>
        <script>
          (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
          (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
          m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
          })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

          ga('create', '${googleAnalyticsTrackingId}', 'auto');
          ga('send', 'pageview');

        </script>
    </#if>
</body>
<footer style="padding-top: 100px;">
    <hr />
    ${copyrightFooter}
</footer>
</html>
