webapp 应用目录：

./conf     基本配置，数据库配置
   base.properties    配置Controller所在目录，如"app.path=f:/dev3/classes/"

./css      基本版本（本目录下）及其他版本（2/3/...）的css文件; jQuery UI的skin
./img      图形图标资源
./js       基本版本（本目录下）及其他版本（2/3/...）的js文件
           main.js         所有公用的及部分具体应用的js函数都在此
           FusionMaps.js   Flash地图功能
           SyntaxHighlighterCore.js
           SyntaxHighlighterClojure.js   wr3/clj/cdoc.clj 用来高亮Clojure源码的语法
           jquery.draw.js  绘图，例如分栏报表的分隔斜线
./WEB-INF  web.xml配置及连接进来的classes/wr3|domain, lib

index.html 主页登录