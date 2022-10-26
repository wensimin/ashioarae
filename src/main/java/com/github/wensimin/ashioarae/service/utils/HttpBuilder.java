package com.github.wensimin.ashioarae.service.utils;

import com.github.wensimin.ashioarae.controller.exception.AshiException;
import com.github.wensimin.ashioarae.controller.exception.CookieExpireException;
import com.github.wensimin.ashioarae.entity.TarCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.util.List;

/**
 * http builder
 */
@Component
public class HttpBuilder {
    private static final Logger logger = LoggerFactory.getLogger(HttpBuilder.class);
    @Value("${system.proxy.host}")
    private String host;
    @Value("${system.proxy.port}")
    private int port;

    public Http builder() {
        return new Http();
    }

    /**
     * 生成proxy
     *
     * @return proxy
     */
    public Proxy createProxy() {
        return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
    }

    /**
     * cookie error 捕捉
     */
    private static class CookieErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CookieExpireException("cookie expire");
            } else {
                logger.error(StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
                throw new AshiException("error: " + response.getStatusCode());
            }
        }
    }

    public class Http {
        private final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        private final RestTemplate restTemplate;
        private String url;
        private final HttpHeaders headers = new HttpHeaders();
        private List<TarCookie> cookies;
        private HttpMethod method = HttpMethod.GET;
        private Object body;

        private Http() {
            headers.add("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            requestFactory.setConnectTimeout(3000);
            requestFactory.setReadTimeout(3000);
            restTemplate = new RestTemplate(requestFactory);
            CookieErrorHandler errorhandler = new CookieErrorHandler();
            restTemplate.setErrorHandler(errorhandler);
        }

        /**
         * 使用proxy
         *
         * @return http
         */
        public Http proxy() {
            Proxy proxy = HttpBuilder.this.createProxy();
            requestFactory.setProxy(proxy);
            requestFactory.setConnectTimeout(5000);
            requestFactory.setReadTimeout(5000);
            return this;
        }


        /**
         * set url
         *
         * @param url url
         * @return http
         */
        public Http url(String url) {
            this.url = url;
            return this;
        }

        /**
         * set headers
         *
         * @param headers headers
         * @return http
         */
        public Http headers(HttpHeaders headers) {
            this.headers.addAll(headers);
            return this;
        }

        /**
         * set header
         *
         * @param key   key
         * @param value value
         * @return http
         */
        public Http header(String key, String value) {
            this.headers.add(key, value);
            return this;
        }

        /**
         * set method
         * 默认为GET
         *
         * @param method method
         * @return http
         */
        public Http method(HttpMethod method) {
            this.method = method;
            return this;
        }

        /**
         * set body
         *
         * @param body body
         * @return http
         */
        public Http body(Object body) {
            if (method == HttpMethod.GET) {
                throw new RuntimeException("get not body");
            }
            this.body = body;
            return this;
        }

        /**
         * set 消息转换器
         *
         * @param converter 转换器
         * @return http
         */
        public Http converter(HttpMessageConverter<?> converter) {
            restTemplate.getMessageConverters().add(converter);
            return this;
        }

        /**
         * set cookies
         *
         * @param cookies cookies
         * @return http
         */
        public Http cookies(List<TarCookie> cookies) {
            this.cookies = cookies;
            return this;
        }

        /**
         * set 内容类型
         *
         * @param mediaType 内容类型
         * @return http
         */
        public Http contentType(MediaType mediaType) {
            this.headers.setContentType(mediaType);
            return this;
        }

        /**
         * 发起http请求
         *
         * @param type 返回值类型
         * @param <T>  返回值类型
         * @return 要求的返回值
         */
        public <T> T run(Class<T> type) {
            return runResponse(type).getBody();
        }

        /**
         * 获取完整的http返回
         *
         * @param type 返回值类型
         * @param <T>  返回值类型
         * @return 要求的返回值
         */
        public <T> ResponseEntity<T> runResponse(Class<T> type) {
            if (url == null) {
                throw new NullPointerException("url is null");
            }
            headers.add("cookie", HttpUtils.cookie2String(HttpUtils.getCookieByUrl(cookies, url)));
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), type);
        }


    }
}
