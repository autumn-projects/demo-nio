package proxy.server.v3;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import proxy.server.v3.exception.HttpProxyExceptionHandle;
import proxy.server.v3.intercept.HttpProxyIntercept;
import proxy.server.v3.intercept.HttpProxyInterceptInitializer;
import proxy.server.v3.intercept.HttpProxyInterceptPipeline;
import proxy.server.v3.server.HttpProxyServer;
import proxy.server.v3.server.HttpProxyServerConfig;
import proxy.server.v3.util.HttpUtil;

/**
 * @Author: LiWei
 * @Description 匹配到百度首页时重定向到指定url
 * @Date: 2019/3/4 16:23
 */
public class InterceptRedirectHttpProxyServer {
  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
            .serverConfig(config)
            .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
              @Override
              public void init(HttpProxyInterceptPipeline pipeline) {
                pipeline.addLast(new HttpProxyIntercept() {
                  @Override
                  public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                            HttpProxyInterceptPipeline pipeline) throws Exception {
                    //匹配到百度首页跳转到淘宝
                    if (HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")) {
                      HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                      hookResponse.setStatus(HttpResponseStatus.FOUND);
                      hookResponse.headers().set(HttpHeaderNames.LOCATION, "http://www.taobao.com");
                      clientChannel.writeAndFlush(hookResponse);
                      HttpContent lastContent = new DefaultLastHttpContent();
                      clientChannel.writeAndFlush(lastContent);
                      return;
                    }
                    pipeline.beforeRequest(clientChannel, httpRequest);
                  }
                });
              }
            })
            .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
              @Override
              public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                cause.printStackTrace();
              }

              @Override
              public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
                      throws Exception {
                cause.printStackTrace();
              }
            })
            .start(9999);
  }
}
