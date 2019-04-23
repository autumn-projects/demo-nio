package proxy.server.v1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        for (; ; ) {
            new SocketHandle(serverSocket.accept()).start();
        }
    }

    static class SocketHandle extends Thread {

        private Socket socket;

        public SocketHandle(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            OutputStream clientOutput = null;
            InputStream clientInput = null;
            Socket proxySocket = null;
            InputStream proxyInput = null;
            OutputStream proxyOutput = null;

            try {
                clientInput = socket.getInputStream();
                clientOutput = socket.getOutputStream();

                InputStreamReader isr = new InputStreamReader(clientInput);
                BufferedReader br = new BufferedReader(isr);
                String line;
                String host = "";
                StringBuilder head = new StringBuilder();
                while (null != (line = br.readLine())) {
                    System.out.println(line);
                    head.append(line + "\r\n");

                    if (line.length() == 0) {
//                        break;
                    } else {
                        String[] temp = line.split(" ");
                        if (temp[0].contains("Host")) {
                            host = temp[1];
                        }
                    }
                }
                String type = head.substring(0, head.indexOf(" "));
                // 根据host头解析出目标服务器的host和port
                String[] hostTemp = host.split(":");
                host = hostTemp[0];
                int port = 80;
                if (hostTemp.length > 1) {
                    port = Integer.valueOf(hostTemp[1]);
                }

                // 连接到目标服务器
                proxySocket = new Socket(host, port);
                proxyInput = proxySocket.getInputStream();
                proxyOutput = proxySocket.getOutputStream();
                //根据HTTP method来判断是https还是http请求
                if ("CONNECT".equalsIgnoreCase(type)) {//https先建立隧道
                    clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    clientOutput.flush();
                } else {//http直接将请求头转发
                    proxyOutput.write(head.toString().getBytes());
                }

                //新开线程转发客户端请求至目标服务器
                new ProxyHandleThread(clientInput, proxyOutput).start();

                while (true) {
                    clientOutput.write(proxyInput.read());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (proxyInput != null) {
                    try {
                        proxyOutput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (proxyOutput != null) {
                    try {
                        proxyOutput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (proxySocket != null) {
                    try {
                        proxySocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clientInput != null) {
                    try {
                        clientInput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clientOutput != null) {
                    try {
                        clientOutput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class ProxyHandleThread extends Thread {

        private InputStream input;
        private OutputStream output;

        public ProxyHandleThread(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    output.write(input.read());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
