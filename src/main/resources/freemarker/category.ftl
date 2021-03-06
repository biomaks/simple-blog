<!DOCTYPE html>
<html>
<head>
    <title>Main page</title>
    <link rel="stylesheet" href="style.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

</head>
<body>
<div id="header"><h1><a href="/">${blogName}</a></h1></div>
<div id="content">
<div class="category"><h2>Category: ${category}</h2></div>
<#list posts as post>
    <h3><a href="/post/${post["permalink"]}">${post["title"]} ${post["dateTime"]}</a></h3>
    <article class="article-preview">${post["articlePreview"]}</article>
    <ul class="post-info">
        <#if post.tags ??>
            <#list post.tags as tag>
                <li><a href="/category?c=${tag["name"]}">${tag["name"]}</a></li>
            </#list>
        </#if>
    </ul>
</#list>

</div>


<div class="copyright">by <a href="https://github.com/biomaks/simple-java.blog">Simple Blog</a> 2013</div>
</body>
</html>