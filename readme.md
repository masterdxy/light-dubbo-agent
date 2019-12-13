### Light-Dubbo-Agent

#### Feature

- 调用方与Agent长连接，Agent与Provider长连接，最大限度降低转发的性能损耗。
- ProtoBuf自定义消息格式，调用方无需服务元信息，同时降低网络传输体积，灵活拓展。
- 方法级限流熔断降级支持，内部处理全异步，并发量和Idle线程可配置，最大程度防止激增流量带来的抖动。
- 实现基于OpenTracing规范的全链路监控，并发数、RT、OPS、熔断等监控数据实时上报
- 支持热点服务调用结果缓存，支持基于参数选择的自定义逻辑插入(TODO)。
- 支持优雅停机。