# 项目描述
- 这是一个基于Java + Springboot的maven多模块项目，用来Demo如何更好的记录Http请求和响应到日志中，包括inbound以及outbound的请求和响应

# 技术规范
- 使用Maven作为构建工具，JDK使用JDK17
- 使用最新的 Springbook3版本
- 项目使用maven多模块项目，模块1：zxf-springboot-trace-webmvc, 模块2：zxf-springboot-trace-webflux
- 通过Lombok的@Slf4j注解引入slf作为日志API
- 实际日志输出采用logback
- 模块1是基于WebMVC的Rest API项目
- 模块2是基于WebFlux的Rest API项目

# 业务需求
- 子模块的主类为TraceApplication
- 子模块中有关Log trace相关的功能在zxf.trace.support.trace包
- 子模块中的RestControl相关的功能在zxf.trace.app.control包中
- 子模块必须实现一个Rest endpoint /http/calling，在其中通过http 请求从http://localhost:8089/pa/a/json获取数据并返回
- 子模块必须以如下规则和格式记录所有inbound以及outbound的日志
- 规则1： 如果响应 http status code 不是200, 则响应日志的level为ERROR否则日志的Level为Debug
- 规则2： 请求日志的level需要和与之对应的响应的日志的Level一致
- 规则3： 对于请求和相应的Body，需要使用基于Json的敏感数据处理的Java库对其进行处理，要处理的Json敏感字段列表可以通过配置定义
- 日志格式1: Inbound Request
- 2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Request begin(Inbound)=================================================
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : URI             : /http/calling
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Methed          : POST
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Headers         : [Accept:"application/json, application/*+json", Content-Type:"application/json", Content-Length:"12"]
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Request Body    : {"task":200}
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Request end(Inbound)=================================================
- 日志格式2: Outbound Response
- 2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Response begin(Outbound)=================================================
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Status code     : 200
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Status text     : OK
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Headers         : [Content-Type:"application/json", Matched-Stub-Id:"bfed0512-c2c1-4812-8e49-5d7061f5129b", Transfer-Encoding:"chunked"]
  2025-04-27 07:57:06.801 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Response Body   : {"status":"Success","value":"1723600759971"}
  2025-04-27 07:57:06.801 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Response end(Outbound)=================================================
- 日志格式1: Outbound Request
- 2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Request begin(Outbound)=================================================
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : URI             : http://localhost:8089/pa/a/json
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Methed          : POST
  2025-04-27 07:57:06.799 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Headers         : [Accept:"application/json, application/*+json", Content-Type:"application/json", Content-Length:"12"]
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Request Body    : {"task":200}
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Request end(Outbound)=================================================
- 日志格式2: Inbound Response
- 2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Response begin(Inbound)=================================================
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Status code     : 200
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Status text     : OK
  2025-04-27 07:57:06.800 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Headers         : [Content-Type:"application/json", Matched-Stub-Id:"bfed0512-c2c1-4812-8e49-5d7061f5129b", Transfer-Encoding:"chunked"]
  2025-04-27 07:57:06.801 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : Response Body   : {"task":"PA.A-200","value":"1723600759971"}
  2025-04-27 07:57:06.801 DEBUG 74030 --- [           main] z.s.p.c.http.LoggingRequestInterceptor   : =================================================Response end(Inbound)=================================================