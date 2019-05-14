package proxy.server.v3;


import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import proxy.server.v3.intercept.HttpProxyInterceptInitializer;
import proxy.server.v3.intercept.HttpProxyInterceptPipeline;
import proxy.server.v3.intercept.common.CertDownIntercept;
import proxy.server.v3.intercept.common.FullRequestIntercept;
import proxy.server.v3.intercept.common.FullResponseIntercept;
import proxy.server.v3.server.HttpProxyServer;
import proxy.server.v3.server.HttpProxyServerConfig;
import proxy.server.v3.util.HttpUtil;

import java.io.IOException;

public class InterceptFullHttpProxyServer {

  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config = new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
            .serverConfig(config)
            .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
              @Override
              public void init(HttpProxyInterceptPipeline pipeline) {
                pipeline.addLast(new CertDownIntercept());

                pipeline.addLast(new FullRequestIntercept() {

                  @Override
                  public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                    //如果是json报文
                    if(HttpUtil.checkHeader(httpRequest.headers(), HttpHeaderNames.CONTENT_TYPE,"^(?i)application/json.*$")){
                      return true;
                    }
                    return false;
                  }
                });
                pipeline.addLast(new FullResponseIntercept() {

                  @Override
                  public boolean match(HttpRequest httpRequest, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) {
                    //请求体中包含user字符串
//                    if(httpRequest instanceof FullHttpRequest){
//                      FullHttpRequest fullHttpRequest = (FullHttpRequest) httpRequest;
//                      String content = fullHttpRequest.content().toString(Charset.defaultCharset());
//                      return content.matches("user");
//                    }
//                    return false;
                    return true;
                  }

                  @Override
                  public void handelResponse(HttpRequest httpRequest, FullHttpResponse httpResponse, HttpProxyInterceptPipeline pipeline)  {
                    System.out.println(httpRequest.toString());

//                    HttpMethod method = httpRequest.method();
//                    Map<String, String> parmMap = new HashMap<>();
//                    if (HttpMethod.GET == method) {
//                      // 是GET请求
//                      QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
//                      decoder.parameters().entrySet().forEach( entry -> {
//                        // entry.getValue()是一个List, 只取第一个元素
//                        parmMap.put(entry.getKey(), entry.getValue().get(0));
//                      });
//                    } else if (HttpMethod.POST == method) {
//                      // 是POST请求
//                      HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(httpRequest);
//
//                      List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
//
//                      for (InterfaceHttpData parm : parmList) {
//
//                        Attribute data = (Attribute) parm;
//                        parmMap.put(data.getName(), data.getValue());
//                      }
//
//                      System.out.println("************");
//                      System.out.println(parmMap);
//                    }



                    //打印原始响应信息
                    System.out.println(httpResponse.toString());
                    System.out.println(httpResponse.content().toString());
                    //修改响应头和响应体
                    httpResponse.headers().set("handel", "edit head");
                    /*int index = ByteUtil.findText(httpResponse.content(), "<head>");
                    ByteUtil.insertText(httpResponse.content(), index, "<script>alert(1)</script>");*/
//                    httpResponse.content().writeBytes("<script>alert('hello proxyee')</script>".getBytes());
                  }
                });

              }
            })
            .start(9999);
  }
}
