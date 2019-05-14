package proxy.server.v3;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import proxy.server.v3.intercept.HttpProxyInterceptInitializer;
import proxy.server.v3.intercept.HttpProxyInterceptPipeline;
import proxy.server.v3.intercept.common.CertDownIntercept;
import proxy.server.v3.intercept.common.FullRequestIntercept;
import proxy.server.v3.server.HttpProxyServer;
import proxy.server.v3.server.HttpProxyServerConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InterceptFullRequestProxyServer {

  /*
    curl -x 127.0.0.1:9999 \
    -X POST \
    http://www.baidu.com \
    -H 'Content-Type: application/json' \
    -d '{"name":"admin","pwd":"123456"}'

    echo '{"name":"admin","pwd":"123456"}' | gzip | \
        curl -x 127.0.0.1:9999 \
        http://www.baidu.com \
        -H "Content-Encoding: gzip" \
        -H "Content-Type: application/json" \
        --data-binary @-
   */
  public static void main(String[] args) throws Exception {
    HttpProxyServerConfig config =  new HttpProxyServerConfig();
    config.setHandleSsl(true);
    new HttpProxyServer()
        .serverConfig(config)
        .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
          @Override
          public void init(HttpProxyInterceptPipeline pipeline) {
            pipeline.addLast(new CertDownIntercept());
            pipeline.addLast(new FullRequestIntercept() {

              public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
                //如果是json报文
//                if(HttpUtil.checkHeader(httpRequest.headers(), HttpHeaderNames.HOST,"www.baidu.com")){
//                  System.out.println("进入请求");
//                  return true;
//                }

                return true;
              }

              public void handelRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws IOException {
//                System.out.println("执行");
                ByteBuf content = httpRequest.content();
                //打印请求信息

//                httpRequest.setUri("/s?ie=utf-8&csq=1&pstg=20&mod=2&isbd=1&cqid=c35ded500028ca09&istc=681&ver=RdpqS2ZXPePajeuj5PPU639W1LVrXyGVCoO&chk=5cdabbeb&isid=cba3270c00030395&ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=111&oq=111&rsv_pq=cba3270c00030395&rsv_t=4d627TN20d2%2B0GZIu7BEtoqUrIGvpvmptQsNG0e3LxMjr3BYWl7gS191vy0&rqlang=cn&rsv_enter=0&bs=111&f4s=1&_ck=1013.1.94.27.16.588.27&isnop=0&rsv_stat=-2&rsv_bp=1 HTTP/1.1\n");
                HttpMethod method = httpRequest.method();

                Map<String, String> parmMap = new HashMap<>();

                if (HttpMethod.GET == method) {
                  // 是GET请求
                  QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
                  decoder.parameters().entrySet().forEach( entry -> {
                    // entry.getValue()是一个List, 只取第一个元素
                    parmMap.put(entry.getKey(), entry.getValue().get(0));
                  });
                } else if (HttpMethod.POST == method) {
                  // 是POST请求
                  HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(httpRequest);
                  decoder.offer(httpRequest);

                  List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

                  for (InterfaceHttpData parm : parmList) {

                    Attribute data = (Attribute) parm;
                    parmMap.put(data.getName(), data.getValue());
                  }

                }
//                System.out.println(parmMap);

                //修改请求体
                String body = "{\"username\":\"15218891412\",\"password\":\"Admin123\"}";
                content.clear();

//                System.out.println(new String(body.getBytes()));
//                content.writeBytes(body.getBytes());

                System.out.println(httpRequest.toString());
                System.out.println(content.toString(Charset.defaultCharset()));
              }

            });
          }
        })
        .start(9999);
  }
}
