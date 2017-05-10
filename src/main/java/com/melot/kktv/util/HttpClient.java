/**
 * Copyright 2012-2014 the original author or authors.
 */
package com.melot.kktv.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * 网络工具类。
 */
public abstract class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class);

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static boolean ignoreSSLCheck; // 忽略SSL检查

    private static class VerisignTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            Exception exp = null;

            for (X509Certificate cert : chain) {
                cert.checkValidity(); // 验证证书有效期
                try {
                    exp = null;
                    break;
                } catch (Exception e) {
                    exp = e;
                }
            }
            if (exp != null) {
                throw new CertificateException(exp);
            }
        }
    }

    private static class TrustAllTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }

    private HttpClient() {
    }

    public static void setIgnoreSSLCheck(boolean ignoreSSLCheck) {
        HttpClient.ignoreSSLCheck = ignoreSSLCheck;
    }

    /**
     * 执行HTTP POST请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @return 响应字符串
     */
    public static String doPost(String url, Map<String, String> params,
            int connectTimeout, int readTimeout) throws IOException {
        return doPost(url, params, DEFAULT_CHARSET, connectTimeout, readTimeout);
    }

    /**
     * 执行HTTP POST请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @param charset 字符集，如UTF-8, GBK, GB2312
     * @param connectTimeout 连接超时,单位毫秒
     * @param readTimeout 响应超时.单位毫秒
     * @return 响应字符串
     */
    public static String doPost(String url, Map<String, String> params,
            String charset, int connectTimeout, int readTimeout)
            throws IOException {
        return doPost(url, params, charset, connectTimeout, readTimeout, null);
    }

    public static String doPost(String url, Map<String, String> params,
            String charset, int connectTimeout, int readTimeout,
            Map<String, String> headerMap) throws IOException {
        String ctype = "application/x-www-form-urlencoded;charset=" + charset;
        String query = buildQuery(params, charset);
        byte[] content = {};
        if (query != null) {
            content = query.getBytes(charset);
        }
        return _doPost(url, ctype, content, connectTimeout, readTimeout,
                headerMap);
    }

    /**
     * 执行HTTP POST请求。
     * 
     * @param url 请求地址
     * @param ctype 请求类型
     * @param content 请求字节数组
     * @return 响应字符串
     */
    public static String doPost(String url, String ctype, byte[] content,
            int connectTimeout, int readTimeout) throws IOException {
        return _doPost(url, ctype, content, connectTimeout, readTimeout, null);
    }

    private static String _doPost(String url, String ctype, byte[] content,
            int connectTimeout, int readTimeout, Map<String, String> headerMap)
            throws IOException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            try {
                conn = getConnection(new URL(url), METHOD_POST, ctype, headerMap);
                conn.setConnectTimeout(connectTimeout);
                conn.setReadTimeout(readTimeout);
            } catch (IOException e) {
                logger.error("_doPost Error", e);
                throw e;
            }
            try {
                out = conn.getOutputStream();
                out.write(content);
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                logger.error("_doPost Error", e);
                throw e;
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return rsp;
    }
    
    public static String doPostBody(String strURL, String params) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.append(params);

            out.flush();
            out.close();
            int length = connection.getContentLength();
            InputStream is = connection.getInputStream();
            if (length != -1) {
                byte[] data = new byte[length];
                byte[] temp = new byte[512];
                int readLen = 0;
                int destPos = 0;
                while ((readLen = is.read(temp)) > 0) {
                    System.arraycopy(temp, 0, data, destPos, readLen);
                    destPos += readLen;
                }
                String result = new String(data, "UTF-8");
                return result;
            }
        } catch (IOException e) {
            logger.error("postBody error,strUrl:" + strURL + ",params:" + params + "", e);
        }
        return "error";
    }

    
    
    public static String doGet(String url, Map<String, String> params) throws IOException {
        return doGet(url, params, DEFAULT_CHARSET, null);
    }
    
    /**
     * 执行HTTP GET请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @return 响应字符串
     */
    public static String doGet(String url, Map<String, String> params,Map<String, String> headerMap) throws IOException {
        return doGet(url, params, DEFAULT_CHARSET, headerMap);
    }

    /**
     * 执行HTTP GET请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @param charset  字符集，如UTF-8, GBK, GB2312
     * @return 响应字符串
     */
    public static String doGet(String url, Map<String, String> params, String charset, Map<String, String> headerMap) throws IOException {
        HttpURLConnection conn = null;
        String rsp = null;

        try {
            String ctype = "application/x-www-form-urlencoded;charset=" + charset;
            String query = buildQuery(params, charset);
            try {
                conn = getConnection(buildGetUrl(url, query), METHOD_GET, ctype, headerMap);
            } catch (IOException e) {
                logger.error("doGet Error", e);
                throw e;
            }
            try {
                rsp = getResponseAsString(conn);
            } catch (IOException e) {
                logger.error("_doPost Error", e);
                throw e;
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return rsp;
    }

    private static HttpURLConnection getConnection(URL url, String method,
            String ctype, Map<String, String> headerMap) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection connHttps = (HttpsURLConnection) conn;
            if (ignoreSSLCheck) {
                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init(null,  new TrustManager[] { new TrustAllTrustManager() },
                            new SecureRandom());
                    connHttps.setSSLSocketFactory(ctx.getSocketFactory());
                    connHttps.setHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname,
                                SSLSession session) {
                            return true;
                        }
                    });
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else {
                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init(null, new TrustManager[] { new VerisignTrustManager() },
                            new SecureRandom());
                    connHttps.setSSLSocketFactory(ctx.getSocketFactory());
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
            conn = connHttps;
        }

        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "text/xml,text/javascript");
        conn.setRequestProperty("User-Agent", "melot-recordServer-java");
        conn.setRequestProperty("Content-Type", ctype);
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return conn;
    }

    private static URL buildGetUrl(String strUrl, String query)
            throws IOException {
        URL url = new URL(strUrl);
        if (StringUtil.strIsNull(query)) {
            return url;
        }
        if (StringUtil.strIsNull(url.getQuery())) {
            if (strUrl.endsWith("?")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "?" + query;
            }
        } else {
            if (strUrl.endsWith("&")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "&" + query;
            }
        }

        return new URL(strUrl);
    }

    public static String buildQuery(Map<String, String> params, String charset)
            throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        Set<Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (!StringUtil.strIsNull(name) && !StringUtil.strIsNull(value)) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }
                query.append(name).append("=").append(URLEncoder.encode(value, charset));
            }
        }

        return query.toString();
    }

    protected static String getResponseAsString(HttpURLConnection conn)
            throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        InputStream es = conn.getErrorStream();
        if (es == null) {
            return getStreamAsString(conn.getInputStream(), charset);
        } else {
            String msg = getStreamAsString(es, charset);
            if (StringUtil.strIsNull(msg)) {
                throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
            } else {
                throw new IOException(msg);
            }
        }
    }

    private static String getStreamAsString(InputStream stream, String charset)
            throws IOException {
        try {
            Reader reader = new InputStreamReader(stream, charset);
            StringBuilder response = new StringBuilder();

            final char[] buff = new char[1024];
            int read = 0;
            while ((read = reader.read(buff)) > 0) {
                response.append(buff, 0, read);
            }
            return response.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static String getResponseCharset(String ctype) {
        String charset = DEFAULT_CHARSET;

        if (!StringUtil.strIsNull(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (!StringUtil.strIsNull(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    /**
     * 使用默认的UTF-8字符集反编码请求参数值。
     * 
     * @param value 参数值
     * @return 反编码后的参数值
     */
    public static String decode(String value) {
        return decode(value, DEFAULT_CHARSET);
    }

    /**
     * 使用默认的UTF-8字符集编码请求参数值。
     * 
     * @param value 参数值
     * @return 编码后的参数值
     */
    public static String encode(String value) {
        return encode(value, DEFAULT_CHARSET);
    }

    /**
     * 使用指定的字符集反编码请求参数值。
     * 
     * @param value 参数值
     * @param charset 字符集
     * @return 反编码后的参数值
     */
    public static String decode(String value, String charset) {
        String result = null;
        if (!StringUtil.strIsNull(value)) {
            try {
                result = URLDecoder.decode(value, charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 使用指定的字符集编码请求参数值。
     * 
     * @param value 参数值
     * @param charset 字符集
     * @return 编码后的参数值
     */
    public static String encode(String value, String charset) {
        String result = null;
        if (!StringUtil.strIsNull(value)) {
            try {
                result = URLEncoder.encode(value, charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 从URL中提取所有的参数。
     * 
     * @param query URL地址
     * @return 参数映射
     */
    public static Map<String, String> splitUrlQuery(String query) {
        Map<String, String> result = new HashMap<String, String>();

        String[] pairs = query.split("&");
        if (pairs != null && pairs.length > 0) {
            for (String pair : pairs) {
                String[] param = pair.split("=", 2);
                if (param != null && param.length == 2) {
                    result.put(param[0], param[1]);
                }
            }
        }
        return result;
    }

}
