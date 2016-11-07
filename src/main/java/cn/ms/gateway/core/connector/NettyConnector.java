package cn.ms.gateway.core.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.ms.gateway.base.ICallback;
import cn.ms.gateway.base.connector.IConnector;
import cn.ms.gateway.common.Conf;
import cn.ms.gateway.common.thread.NamedThreadFactory;
import cn.ms.gateway.entity.GatewayREQ;
import cn.ms.gateway.entity.GatewayRES;

/**
 * 基于Netty实现的连接器
 * 
 * @author lry
 */
@SuppressWarnings("deprecation")
public class NettyConnector implements IConnector<GatewayRES, GatewayRES, HttpResponse> {

	//$NON-NLS-通道回调函数绑定KEY$
	public static final AttributeKey<ICallback<GatewayRES, GatewayRES, HttpResponse>> CHANNEL_CALLBACK_KEY = AttributeKey.valueOf("gateway_connector_callback");
	
	private Bootstrap bootstrap = null;
	private ConcurrentHashMap<String, ChannelFuture> channelFutureMap = new ConcurrentHashMap<String, ChannelFuture>();
	
	@Override
	public void init() throws Exception {
		EventLoopGroup workerGroup = new NioEventLoopGroup(Conf.CONF.getConnectorWorkerThreadNum(), new NamedThreadFactory("NettyConnectorWorker"));
		
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void connect(GatewayREQ req, ICallback<GatewayRES, GatewayRES, HttpResponse> callback, Object... args) throws Throwable {
		URI tempURI = new URI(req.getRemoteURI());
		String address = tempURI.getHost() + ":" + (tempURI.getPort() <= 0 ? 80 : tempURI.getPort());

		//$NON-NLS-处理器和通道回收利用,一次创建N次使用$
		ChannelFuture channelFuture = channelFutureMap.get(address);
		if (channelFuture == null) {
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					IdleStateHandler idleStateHandler=new IdleStateHandler(
							Conf.CONF.getReaderIdleTimeSeconds(), Conf.CONF.getWriterIdleTimeSeconds(), Conf.CONF.getAllIdleTimeSeconds());
					
					ch.pipeline().addLast("idleStateHandler", idleStateHandler);
					ch.pipeline().addLast(new HttpResponseDecoder());
					ch.pipeline().addLast(new HttpRequestEncoder());
					
					ch.pipeline().addLast(new ConnectorHandler());
				}
			});

			channelFuture = bootstrap.connect(tempURI.getHost(), tempURI.getPort()).sync();
			channelFutureMap.put(address, channelFuture);
		}

		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, 
				HttpMethod.GET, tempURI.toASCIIString(), Unpooled.wrappedBuffer(req.getContent().getBytes("UTF-8")));

		// 构建http请求
		request.headers().set(Names.HOST, tempURI.getHost());
		request.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
		request.headers().set(Names.CONTENT_LENGTH, request.content().readableBytes());

		// $NON-NLS-通过通道获取回调函数$
		channelFuture.channel().attr(CHANNEL_CALLBACK_KEY).set(callback);
		// 发送http请求
		channelFuture.channel().writeAndFlush(request);
	}

	@Override
	public void destory() throws Exception {
		for (Map.Entry<String, ChannelFuture> entry : channelFutureMap
				.entrySet()) {
			ChannelFuture channelFuture = entry.getValue();
			channelFuture.channel().flush();
			channelFuture.channel().closeFuture().sync();
		}
	}

}
