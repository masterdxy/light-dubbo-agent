package com.github.masterdxy.light.dubbo.agent.connector.tcp;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.connector.AbstractNetworkServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.ThreadRenamingRunnable;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.masterdxy.light.dubbo.agent.common.Constants.MAX_FRAME_LENGTH;


/**
 * @author tomoyo
 */
public final class LengthBasePBServer extends AbstractNetworkServer {

    private static final Logger LOG = LoggerFactory.getLogger(LengthBasePBServer.class);

    private static final int CLOSE_CHANNEL_TIMEOUT = 5;


    /**
     * Initialize LengthBasePBServer.
     *
     * @param serviceName              name of this service. Threads created for this service will be prefixed with the given name.
     * @param bindAddress              Address for the service to bind to.
     * @param bossThreadPoolSize       Size of the boss thread pool.
     * @param workerThreadPoolSize     Size of the worker thread pool.
     * @param execThreadPoolSize       Size of the thread pool for the executor.
     * @param execThreadKeepAliveSecs  maximum time that excess idle threads will wait for new tasks before terminating.
     * @param channelConfigs           Configurations for the server socket channel.
     * @param rejectedExecutionHandler rejection policy for executor.
     * @param pipelineModifier         Function used to modify the pipeline.
     */
    private LengthBasePBServer(String serviceName,
                               InetSocketAddress bindAddress, int bossThreadPoolSize, int workerThreadPoolSize,
                               int execThreadPoolSize, long execThreadKeepAliveSecs,
                               Map<String, Object> channelConfigs,
                               OneToOneEncoder encoder,
                               OneToOneDecoder decoder,
                               RejectedExecutionHandler rejectedExecutionHandler,
                               Function<ChannelPipeline, ChannelPipeline> pipelineModifier,
                               Map<String, ChannelHandler> channelHandlerMap) {
        this.serviceName = serviceName;
        this.bindAddress = bindAddress;
        this.bossThreadPoolSize = bossThreadPoolSize;
        this.workerThreadPoolSize = workerThreadPoolSize;
        this.execThreadPoolSize = execThreadPoolSize;
        this.execThreadKeepAliveSecs = execThreadKeepAliveSecs;
        this.channelConfigs = ImmutableMap.copyOf(channelConfigs);
        this.encoder = encoder;
        this.decoder = decoder;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.channelGroup = new DefaultChannelGroup();
        this.pipelineModifier = pipelineModifier;
        this.channelHandlerMap = channelHandlerMap;
    }

    /**
     * Create Execution handlers with threadPoolExecutor.
     *
     * @param threadPoolSize      size of threadPool
     * @param threadKeepAliveSecs maximum time that excess idle threads will wait for new tasks before terminating.
     * @return instance of {@code ExecutionHandler}.
     */
    private ExecutionHandler createExecutionHandler(int threadPoolSize, long threadKeepAliveSecs) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final ThreadGroup threadGroup = new ThreadGroup(serviceName + "-executor-thread");
            private final AtomicLong count = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(threadGroup, r, String.format("%s-executor-%d", serviceName, count.getAndIncrement()));
                t.setDaemon(true);
                return t;
            }
        };

        //Create ExecutionHandler
        ThreadPoolExecutor threadPoolExecutor =
                new OrderedMemoryAwareThreadPoolExecutor(threadPoolSize, 0, 0,
                        threadKeepAliveSecs, TimeUnit.SECONDS, threadFactory);
        threadPoolExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
        return new ExecutionHandler(threadPoolExecutor, false, true);
    }

    /**
     * Bootstrap the pipeline.
     * <ul>
     * <li>Setup the netty pipeline</li>
     * </ul>
     *
     * @param threadPoolSize      Size of threadpool in threadpoolExecutor
     * @param threadKeepAliveSecs maximum time that excess idle threads will wait for new tasks before terminating.
     */
    private void bootStrap(int threadPoolSize, long threadKeepAliveSecs) throws Exception {
        // Make Netty uses the current name (i.e. the thread name as created by the executor) to name the threads.
        ThreadRenamingRunnable.setThreadNameDeterminer(ThreadNameDeterminer.CURRENT);

        executionHandler = (threadPoolSize) > 0 ? createExecutionHandler(threadPoolSize, threadKeepAliveSecs) : null;

        Executor bossExecutor = Executors.newFixedThreadPool(bossThreadPoolSize,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(serviceName + "-boss-thread-%d")
                        .build());

        Executor workerExecutor = Executors.newFixedThreadPool(workerThreadPoolSize,
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat(serviceName + "-worker-thread-%d")
                        .build());

        //Server bootstrap with default worker threads (2 * number of cores)
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        bossExecutor,
                        bossThreadPoolSize,
                        workerExecutor,
                        workerThreadPoolSize
                ));
        bootstrap.setOptions(channelConfigs);


        bootstrap.setPipelineFactory(() -> {
            ChannelPipeline pipeline = Channels.pipeline();
            //Encoder 执行顺序和addLast相反 : custom encoder --> Zlib --> LineDelimiterEncoder
            //换行分割
            pipeline.addLast("length_prepender", new LengthFieldPrepender(4));
//            pipeline.addLast("protobuf_prepender",new ProtobufVarint32LengthFieldPrepender());
            pipeline.addLast("protobuf_encoder",new ProtobufEncoder());
            //压缩
            //pipeline.addLast("deflater", new com.github.masterdxy.light.dubbo.agent.connector.tcp.codec.ZlibEncoder());
            //编码
            //pipeline.addLast("encoder", encoder);

            //Decoder 执行顺序和addLast顺序一致 ：Zlib --> custom decoder
            pipeline.addLast("length_decoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4,0,4));//超过max_frame_length 会抛出TooLongFrameException
            pipeline.addLast("protobuf_decoder",new ProtobufDecoder(AgentRequestOuterClass.AgentRequest.getDefaultInstance()));
            //解压缩
            //pipeline.addLast("inflater", new com.github.masterdxy.light.dubbo.agent.connector.tcp.codec.ZlibDecoder());
            //解码
            //pipeline.addLast("decoder", decoder);
//            pipeline.addLast("decoder",new JSONDecoder());
//            pipeline.addLast("process",new RequestDispatcher());
            pipeline.addLast("logger", new LoggingHandler(InternalLogLevel.INFO,true));
            //use hystrix thread pool instead of netty executor
            //pipeline.addLast("executor",createExecutionHandler(60,60L));
            channelHandlerMap.forEach(pipeline::addLast);
            return pipeline;
        });
        //for debug
        //DefaultChannelFuture.setUseDeadLockChecker(false);
    }

    /**
     * Creates a {@link Builder} for creating new instance of {@link LengthBasePBServer}.
     *
     * @param serviceName name of the http service. The name will be used to name threads created for the service.
     */
    public static Builder builder(String serviceName) {
        return new Builder(serviceName);
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting {} netty server on address {}...", serviceName, bindAddress);
        bootStrap(execThreadPoolSize, execThreadKeepAliveSecs);
        Channel channel = bootstrap.bind(bindAddress);
        channelGroup.add(channel);
        bindAddress = ((InetSocketAddress) channel.getLocalAddress());
        LOG.info("Started {} netty server on address {}", serviceName, bindAddress);
    }

    /**
     * @return port where the service is running.
     */
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    @Override
    protected void shutDown() throws Exception {
        LOG.info("Stopping {} netty server on address {}...", serviceName, bindAddress);
        try {
            bootstrap.shutdown();
            if (!channelGroup.close().await(CLOSE_CHANNEL_TIMEOUT, TimeUnit.SECONDS)) {
                LOG.warn("Timeout when closing all channels.");
            }
        } finally {
            bootstrap.releaseExternalResources();
            if (executionHandler != null) {
                executionHandler.releaseExternalResources();
                ExecutorUtil.terminate(executionHandler.getExecutor());
            }
        }
        LOG.info("Stopped {} netty server on address {}", serviceName, bindAddress);
    }

    /**
     * Builder to help create the LengthBasePBServer.
     */
    public static class Builder {

        private static final int DEFAULT_BOSS_THREAD_POOL_SIZE = 1;
        private static final int DEFAULT_WORKER_THREAD_POOL_SIZE = 10;
        private static final int DEFAULT_CONNECTION_BACKLOG = 1000;
        private static final int DEFAULT_EXEC_HANDLER_THREAD_POOL_SIZE = 60;
        private static final long DEFAULT_EXEC_HANDLER_THREAD_KEEP_ALIVE_TIME_SECS = 60L;
        private static final RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER =
                new ThreadPoolExecutor.CallerRunsPolicy();
        private static final OneToOneDecoder DEFAULT_DECODER = new StringDecoder();
        private static final OneToOneEncoder DEFAULT_ENCODER = new StringEncoder();

        private final String serviceName;
        private final Map<String, Object> channelConfigs;

        private int bossThreadPoolSize;
        private int workerThreadPoolSize;
        private int execThreadPoolSize;
        private long execThreadKeepAliveSecs;
        private String host;
        private int port;
        private RejectedExecutionHandler rejectedExecutionHandler;
        private OneToOneEncoder encoder;
        private OneToOneDecoder decoder;
        private Function<ChannelPipeline, ChannelPipeline> pipelineModifier;
        private Map<String, ChannelHandler> channelHandlerMap;

        // Protected constructor to prevent instantiating Builder instance directly.
        protected Builder(String serviceName) {
            this.serviceName = serviceName;
            bossThreadPoolSize = DEFAULT_BOSS_THREAD_POOL_SIZE;
            workerThreadPoolSize = DEFAULT_WORKER_THREAD_POOL_SIZE;
            execThreadPoolSize = DEFAULT_EXEC_HANDLER_THREAD_POOL_SIZE;
            rejectedExecutionHandler = DEFAULT_REJECTED_EXECUTION_HANDLER;
            execThreadKeepAliveSecs = DEFAULT_EXEC_HANDLER_THREAD_KEEP_ALIVE_TIME_SECS;
            encoder = DEFAULT_ENCODER;
            decoder = DEFAULT_DECODER;
            port = 0;
            channelConfigs = Maps.newHashMap();
            channelConfigs.put("backlog", DEFAULT_CONNECTION_BACKLOG);
            channelConfigs.put("tcpNoDelay", true);
            channelConfigs.put("reuseAddress", true);

        }

        /**
         * Returns the simple class name of the first caller that is different than the {@link LengthBasePBServer} class.
         * This method is for backward compatibility.
         */
        private static String getCallerClassName() {
            // Get the stacktrace and determine the caller class. We skip the first one because it's always
            // Thread.getStackTrace().
            for (StackTraceElement element : Iterables.skip(Arrays.asList(Thread.currentThread().getStackTrace()), 1)) {
                if (!element.getClassName().startsWith(LengthBasePBServer.class.getName())) {
                    String className = element.getClassName();
                    int idx = className.lastIndexOf('.');
                    return idx > 0 ? className.substring(idx + 1) : className;
                }
            }
            return "netty-server";
        }

        /**
         * Modify the pipeline upon build by applying the function.
         *
         * @param function Function that modifies and returns a pipeline.
         * @return builder
         */
        public Builder modifyChannelPipeline(Function<ChannelPipeline, ChannelPipeline> function) {
            this.pipelineModifier = function;
            return this;
        }

        /**
         * Set size of bossThreadPool in netty default value is 1 if it is not set.
         *
         * @param bossThreadPoolSize size of bossThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setBossThreadPoolSize(int bossThreadPoolSize) {
            this.bossThreadPoolSize = bossThreadPoolSize;
            return this;
        }

        public Builder setHandlers(Map<String, ChannelHandler> handlerMap) {
            this.channelHandlerMap = handlerMap;
            return this;
        }

        public Builder setEncoder(OneToOneEncoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder setDecoder(OneToOneDecoder decoder) {
            this.decoder = decoder;
            return this;
        }

        /**
         * Set size of workerThreadPool in netty default value is 10 if it is not set.
         *
         * @param workerThreadPoolSize size of workerThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setWorkerThreadPoolSize(int workerThreadPoolSize) {
            this.workerThreadPoolSize = workerThreadPoolSize;
            return this;
        }

        /**
         * Set size of backlog in netty service - size of accept queue of the TCP stack.
         *
         * @param connectionBacklog backlog in netty server. Default value is 1000.
         * @return an instance of {@code Builder}.
         */
        public Builder setConnectionBacklog(int connectionBacklog) {
            channelConfigs.put("backlog", connectionBacklog);
            return this;
        }


        public Builder setExecThreadKeepAliveSecs(int execThreadKeepAliveSecs) {
            this.execThreadKeepAliveSecs = execThreadKeepAliveSecs;
            return this;
        }


        /**
         * Sets channel configuration for the the netty service.
         *
         * @return an instance of {@code Builder}.
         * @see ChannelConfig
         * @see org.jboss.netty.channel.socket.ServerSocketChannelConfig
         */
        public Builder setChannelConfig(Map<String, Object> config) {
            channelConfigs.putAll(config);
            return this;
        }

        /**
         * Set size of executorThreadPool in netty default value is 60 if it is not set.
         * If the size is {@code 0}, then no executor will be used, hence calls to biz code would be made from
         * worker threads directly.
         *
         * @param execThreadPoolSize size of workerThreadPool.
         * @return an instance of {@code Builder}.
         */
        public Builder setExecThreadPoolSize(int execThreadPoolSize) {
            this.execThreadPoolSize = execThreadPoolSize;
            return this;
        }


        /**
         * Set RejectedExecutionHandler - rejection policy for executor.
         *
         * @param rejectedExecutionHandler rejectionExecutionHandler.
         * @return an instance of {@code Builder}.
         */
        public Builder setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        /**
         * Set the port on which the service should listen to.
         * By default the service will run on a random port.
         *
         * @param port port on which the service should listen to.
         * @return instance of {@code Builder}.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Set the bindAddress for the service. Default value is localhost.
         *
         * @param host bindAddress for the service.
         * @return instance of {@code Builder}.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }


        /**
         * @return instance of {@code LengthBasePBServer}
         */
        public LengthBasePBServer build() {
            InetSocketAddress bindAddress;
            if (host == null) {
                bindAddress = new InetSocketAddress("localhost", port);
            } else {
                bindAddress = new InetSocketAddress(host, port);
            }

            return new LengthBasePBServer(serviceName, bindAddress, bossThreadPoolSize, workerThreadPoolSize,
                    execThreadPoolSize, execThreadKeepAliveSecs, channelConfigs, encoder, decoder, rejectedExecutionHandler, pipelineModifier, channelHandlerMap);
        }
    }
}
