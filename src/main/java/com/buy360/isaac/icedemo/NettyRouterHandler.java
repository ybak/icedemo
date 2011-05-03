package com.buy360.isaac.icedemo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class NettyRouterHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(NettyRouterHandler.class.getName());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        // TODO Auto-generated method stub
        super.messageReceived(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        e.getChannel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }

}
