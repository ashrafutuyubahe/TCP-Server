package com.mina;


import com.hub900.HubManager;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InitHub900 {

    private int port = 9000;
    private NioSocketAcceptor acceptor = null;
    private static InitHub900 initHub900 = null;
    private static final Handler900 handler = new Handler900();

    private InitHub900() {
    }

    public static InitHub900 getInstance() {
        if (initHub900 == null) {
            initHub900 = new InitHub900();
        }
        return initHub900;
    }

    public void startServer(HubManager manager, int port) {
        this.port = port;
        this.acceptor = new NioSocketAcceptor();
        this.acceptor.getFilterChain().addLast("ProtocolCodecFilter", new ProtocolCodecFilter(new ProtocolCodecFactory900(manager)));
        this.acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(new ThreadPoolExecutor(100, 2000, 10, TimeUnit.SECONDS, new LinkedBlockingQueue())));
        this.acceptor.setHandler(handler);
        this.acceptor.getSessionConfig().setReuseAddress(false);
        // Set receive buffer size
        this.acceptor.getSessionConfig().setReceiveBufferSize(1024 * 1024);
        // Set the send buffer size
        this.acceptor.getSessionConfig().setReadBufferSize(1024 * 1024);
        // The read and write channel enters the idle state without any operation within 60 seconds
        this.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 600);

        try {
            this.acceptor.bind(new InetSocketAddress(this.port));
            System.out.println("[hub server] startup success ,   port：" + this.port);
        } catch (IOException e) {
            System.out.println("[hub server] startup failure ,   port：" + this.port);
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (acceptor != null) {
            this.acceptor.dispose(true);
        }
    }

    private static class Handler900 extends IoHandlerAdapter {

        private static Logger logger = LoggerFactory.getLogger(Handler900.class);

        public Handler900() {
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            logger.error("[ERROR]exceptionCaught:" + cause.getMessage(), cause);
            session.closeNow();
        }

        @Override
        public void messageReceived(IoSession session, Object packet) throws Exception {
            byte[] bytes = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(packet);
                oos.flush();
                bytes = bos.toByteArray();
                oos.close();
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (bytes != null && bytes.length > 0) {
                callback(session, bytes);
            }
        }

        private static void callback(IoSession session, byte[] callbackMessage) {
            IoBuffer buf = IoBuffer.wrap(callbackMessage);
            WriteFuture future = session.write(buf);
            future.awaitUninterruptibly(100L);
            if (!future.isWritten())
                logger.warn("response client failure");
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            if (session.getRemoteAddress() != null) {
                System.out.println("[hub server] " + session.getRemoteAddress().toString() + " sessionClosed!");
            } else {
                System.out.println("[hub server] Unknown session closed (remote address is null)");
            }
        }
        

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            System.out.println("[hub server] sessionCreated");
            super.sessionCreated(session);
            SocketSessionConfig cfg = (SocketSessionConfig) session.getConfig();
            cfg.setReceiveBufferSize(2 * 1024 * 1024);
            cfg.setReadBufferSize(2 * 1024 * 1024);
            cfg.setKeepAlive(true);
            cfg.setSoLinger(70000); //The solution may take a lot of time wait
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
//        Date day = new Date();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            super.sessionOpened(session);
            System.out.println("Session Opened!!!");
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            System.out.println(df.format(day) + "[hub server] Close empty session" + session.getRemoteAddress());
            session.closeNow();
        }

    }
}
