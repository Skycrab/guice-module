## guice

在阅读[Apache Druid](https://github.com/apache/incubator-druid)代码时，喜欢上了guice这个短小精悍的DI框架，
所以有建这个库的想法，一期主要从Druid中拆出一些优秀的guice扩展。


## guice-module

 * guice-lifecycle 实现生命周期托管
 
 * guice-jsonconfig Properties配置文件bean自动装配
 
 * guice-jersey-jetty 内嵌jetty的jersey Restful
 
 * guice-common
 
    * PropertiesModule 加载properties配置
 
 * guice-example 示例代码