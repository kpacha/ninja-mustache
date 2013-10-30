Mustache template module for Ninja framework.
=====================
Mustache (http://mustache.github.io/) can be used for HTML, config files, source code - anything. It works by expanding tags in a template using values provided in a hash or object.

This is an easly plugable module for Ninja web framework to write templates using [mustache.java](https://github.com/spullara/mustache.java) engine. based on the [ninja-rythm](https://github.com/ninjaframework/ninja-rythm) module.

Getting started
---------------

Setup
-----

1) Add the ninja-mustache dependency to your pom.xml:

    <dependency>
        <groupId>org.ninjaframework</groupId>
        <artifactId>ninja-mustache-module</artifactId>
        <version>0.0.1</version>
    </dependency>

2) Install the module in your conf.Module:

    @Override
    protected void configure() {

        ...

        install(new NinjaMustacheModule());

        ...

    }
    
3) All set. Start writing template in 'views' folder of your application.


4) Or check out <code>ninja-mustache-demo</code> (in progress). Run any one of the below commands under demo:

    mvn jetty:run
    OR 
    mvn tomcat7:run


Modify code/template -- Save -- Refresh browser. Enjoy!


TODO
-----

 - Improve error/exception handler
 - Fix the demo module tests

