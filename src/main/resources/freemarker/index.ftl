<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Main page</title>
    <link rel="stylesheet" href="style.css"/>
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="../github.css"/>
    <script src="../highlight.pack.js"></script>
</head>
<body>
<div id="header"><h1><a href="/">${blogName}</a></h1></div>
<div id="nav">
    <ul>
        <li>Blog</li>
        <li>//</li>
        <li>About</li>
        <li>//</li>
        <li>Links</li>
    </ul>
</div>
<div id="content">
<#list posts as post>
    <h3><a href="/post/${post["permalink"]}">${post["title"]}</a></h3>
    <label>${post["dateTime"]}</label>
    <article class="article-preview">${post["articlePreview"]}</article>
    <ul class="post-info">
    <#if post.tags ??>
    <#list post.tags as tag>
    <li><a href="/category?c=${tag}">${tag}</a></li>
    </#list>
    </#if>
        <li class="more"><a href="/post/${post["permalink"]}">Read ...</a></li>
    </ul>
</#list>

</div>
<script>hljs.initHighlightingOnLoad();</script>

<div class="copyright">by <a href="https://github.com/biomaks/simple-blog">Simple Blog</a> 2013</div>
</body>
</html>