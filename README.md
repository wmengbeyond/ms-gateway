# 微服务网关(ms-gateway)

**目录**
[TOC]

**QQ交流群：191958521(微服务基础设施)**

**微服务网关开源地址：**

+ **GitHub：**https://github.com/yu120/ms-gateway
+ **码云：**http://git.oschina.net/yu120/ms-gateway

**微服务基础设施开源项目：**

+ **微服务神经元(neural)：**http://git.oschina.net/yu120/neural
+ **微服务通讯网关(ms-gateway)：**http://git.oschina.net/yu120/ms-gateway
+ **黑科技序列(sequence)：**http://git.oschina.net/yu120/sequence


##一、背景/功能介绍
###1.1 背景介绍

###1.2 功能介绍
网关是一个所有流量的入口，给人太多的想象空间，因此我将网关的所有功能分解为：核心功能与扩展功能。这样分解的目的是为了把握核心，其余的都统一作为扩展功能进行逐一完善。

####1.2.1 核心功能
+ **安全：**主要包括HTTPS、IP黑白名单
+ **流量控制(流量整形)：**并发控制、流量速率控制
+ **请求代理：**HTTP请求代理
+ **静/动态路由：**动态路由、静态路由

####1.2.2 扩展功能
+ **超时控制：**整体处理超时控制

##二、核心设计
###2.1 反向代理
####2.1.1 概念
根据客户端的请求，从后端的服务器上获取资源，然后再将这些资源返回给客户端。

####2.1.2 反向代理的主要作用
+ 加密和SSL加速
+ 负载均衡
+ 缓存静态内容
+ 压缩
+ 减速上传
+ 安全
+ 外网发布

###2.2 安全
####2.2.1 HTTP/HTTPS
提供了HTTP或HTTPS的方式接入微服务网关，通过配置文件参数配置进行切换模式。

####2.2.2 IP黑/白名单
提供了基于IP黑/白名单的接入者控制。该过滤器用于过滤非指定列表中的IP不能访问指定功能权限,支持IP段*通配符配置,如：

+ 所有IP: 192.168.*
+ 所有192.168.开头的IP: 192.168.0.*
+ 所有0网段的IP: 10.195.13.0-10.195.13.255
+ 也支持这种格式的IP段指定: 192.168.1.100
+ 单独IP指定: 192.168.1*.50
+ 所有以192.168.1开头.50结尾的IP,即:192.168.10.50-192.168.19.50;192.168.100.50-192.168.199.50

####2.2.3 OAuth2.0
> [暂不考虑]

####2.2.4 账号/密码认证
> [暂不考虑]

###2.3 流量控制
####2.3.1 并发控制 
+ **资源层控制：**基于Netty线程大小进行并发数的控制
+ **流控层控制：**基于JDK型号量Semphore来实现并发流量的控制。该参数只能小于等于资源层的控制参数（Netty线程数）

####2.3.2 流速控制
基于Google开源Guava项目总的RateLimiter进行流速的控制。

###2.4 请求代理
通过Netty的方式实现向后端转发HTTP请求。

###2.5 静/动态路由
静态路由与动态路由二选一的模式进行使用。

#### 2.5.1 静态路由
通过配置文件配置的方式实现路由表（路由清单）的配置。

#### 2.5.2 动态路由
通过注册中心的方式动态化获取路由表的配置信息。

###其他功能
+ 支持配置化参数校验
+ 支持

##三、如何使用
### 3.1 引入MAVEN依赖
> [待定]

### 3.2 修改配置文件
> [待定]

### 3.3 启动微服务网关
> [待定]

### 3.4 打开浏览器测试
>  [待定]

##四、发布痕迹
| 发布日期       | 版本号    | 变更信息                |
| :--------- | :----- | :------------------ |
| 2016-10-28 | V1.0.0 | ①描述文档撰写②项目的规划③框架的搭建 |

##五、性能报告
+ 压测机器：
+ 压测工具：AB工具
+ 压测时间：2016-10-28
+ 最大并发数：
+ 平均TPS/响应耗时：

###5.1 并发量(CV)

###5.2 交易量(TPS)

###5.3 响应耗时(RT)

