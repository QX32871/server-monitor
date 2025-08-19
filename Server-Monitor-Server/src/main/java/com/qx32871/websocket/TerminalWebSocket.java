package com.qx32871.websocket;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.qx32871.entity.dto.ClientDetailDTO;
import com.qx32871.entity.dto.ClientSshDTO;
import com.qx32871.mapper.ClientDetailMapper;
import com.qx32871.mapper.ClientSshMapper;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@ServerEndpoint("/terminal/{clientId}")
public class TerminalWebSocket {
    //由于TerminalWebSocket这个类不是单例，是每一个连接都会创建一个新实例，因此注入的两个Mapper要用类层级的static变量来声明
    //注入static方法要使用成员变量的set方法来注入才能注入成功

    private static ClientDetailMapper detailMapper;

    @Resource
    public void setDetailMapper(ClientDetailMapper detailMapper) {
        TerminalWebSocket.detailMapper = detailMapper;
    }

    private static ClientSshMapper sshMapper;

    @Resource
    public void setSshMapper(ClientSshMapper sshMapper) {
        TerminalWebSocket.sshMapper = sshMapper;
    }

    private static final Map<Session, Shell> sessionMap = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    @OnOpen
    public void onOpen(Session session,
                       @PathParam(value = "clientId") String clientId) throws IOException {
        ClientDetailDTO detail = detailMapper.selectById(clientId);
        ClientSshDTO ssh = sshMapper.selectById(clientId);
        if (detail == null || ssh == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无法识别实例主机"));
            return;
        }
        if (this.createSshConnection(session, ssh, detail.getIp())) {
            log.info("实例主机 {} 的SSH连接已创建", detail.getIp());
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        Shell shell = sessionMap.get(session);
        if (shell != null) {
            shell.close();
            sessionMap.remove(session);
            log.info("实例主机 {} 的SSH已断开", shell.jschSession.getHost());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("用户的WebSocket连接出现错误", error);
        session.close();
    }

    @OnMessage
    public void onMessage(Session session, String messages) throws IOException {
        Shell shell = sessionMap.get(session);
        OutputStream output = shell.output;
        output.write(messages.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }


    private boolean createSshConnection(Session session, ClientSshDTO ssh, String ip) throws IOException {
        try {
            JSch jSch = new JSch();
            com.jcraft.jsch.Session jSchSession = jSch.getSession(ssh.getUsername(), ip, ssh.getPort());
            jSchSession.setPassword(ssh.getPassword());
            jSchSession.setConfig("StrictHostKeyChecking", "no");
            jSchSession.setTimeout(3000);
            jSchSession.connect();
            ChannelShell channelShell = (ChannelShell) jSchSession.openChannel("shell");
            channelShell.setPtyType("xterm");
            channelShell.connect(3000);
            sessionMap.put(session, new Shell(session, jSchSession, channelShell));
            return true;
        } catch (JSchException e) {
            String message = e.getMessage();
            if (message.equals("Auth fail")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "登陆SSH失败，用户名或密码错误"));
                log.error("连接SSH失败，用户名或密码错误,登陆失败");
            } else if (message.contains("Connection refused")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "连接被拒绝，可能是没有启动SSH服务或端口未开放"));
                log.error("连接SSH失败，连接被拒绝，可能是没有启动SSH服务或端口未开放");
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, message));
                log.error("连接SSH时出现错误", e);
            }
        }
        return false;
    }

    private class Shell {
        private final Session session;
        private final com.jcraft.jsch.Session jschSession;
        private final ChannelShell channelShell;
        private final InputStream input;
        private final OutputStream output;

        public Shell(Session session,
                     com.jcraft.jsch.Session jschSession,
                     ChannelShell channelShell) throws IOException {
            this.jschSession = jschSession;
            this.session = session;
            this.channelShell = channelShell;
            this.input = channelShell.getInputStream();
            this.output = channelShell.getOutputStream();
            executorService.submit(this::read);
        }

        private void read() {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int i;
                while ((i = input.read(buffer)) != -1) {
                    String text = new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8);
                    session.getBasicRemote().sendText(text);
                }
            } catch (Exception e) {
                log.error("读取SSH输入流时出现问题", e);
            }
        }

        private void close() throws IOException {
            input.close();
            output.close();
            channelShell.disconnect();
            jschSession.disconnect();
            executorService.shutdown();
        }
    }
}
