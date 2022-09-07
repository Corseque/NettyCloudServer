package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Data;

import java.nio.file.Path;

@Data
public class CloudServer {
    private MySQLAuthService authService;
    private final Path rootDir = Path.of("C:/Users/Corse/IdeaProjects/NettyCloudServer/data");

    public CloudServer() {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new CloudServerHandler(CloudServer.this)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            authService = new MySQLAuthService();
            authService.start();
            // server started!
            future.channel().closeFuture().sync(); // block
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
//        super(
//                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
//                new ObjectEncoder(),
//                new CloudServerHandler(server)
//        );
    }

    public static void main(String[] args) {
        new CloudServer();
    }
}