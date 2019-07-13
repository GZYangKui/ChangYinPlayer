package com.navigation.service


import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient

import java.util.Map.Entry

import static com.navigation.utils.Message.showError

/**
 *
 * HTTP请求服务
 *
 */
class HttpService {
    //发起post请求
    static Future<HttpResponse> post(Vertx vertx, String url, JsonObject param, JsonObject data, Future<HttpResponse> resultFuture) {
        def requestUrl = connectParam(param, url)
        def client = WebClient.create(vertx)
        Future<HttpResponse> future = Future.future()
        def httpRequest = client.postAbs(requestUrl)
        addHeader(httpRequest)
        httpRequest.sendBuffer(data.toBuffer(), future)
        return future
    }
    //发起get请求
    static Future<HttpResponse> get(Vertx vertx, String url, JsonObject param) {
        def requestUrl = connectParam(param, url)
        def client = WebClient.create(vertx)
        Future<HttpResponse> future = Future.future()
        def httpRequest = client.getAbs(requestUrl)
        addHeader(httpRequest)
        httpRequest.send(future)
        return future
    }
    /**
     * 获取音乐url
     *
     * @param id 歌曲id
     * @param source 歌曲来源
     * @return
     */
    static Future<String> getMusicUrl(Vertx vertx, String id, String source) {
        def requestUrl = "http://www.gequdaquan.net/gqss/api.php"
        def param = new JsonObject()
        param.put("types", "url")
        param.put("id", id)
        param.put("source", source == null ? "netease" : source)
        Future<String> future = Future.future()
        get(vertx, requestUrl, param).setHandler {
            if (it.succeeded()) {
                def url = it.result().bodyAsJsonObject().getString("url")
                if (source == "netease") {
                    if (url == "") {
                        url = "https://music.163.com/song/media/outer/url?id=" + id + ".mp3"
                    } else {
                        url = url.replace("m7c.music.", "m7.music.");
                        url = url.replace("m8c.music.", "m8.music.");
                    }
                    //解决百度防盗链
                } else if (source == "baidu") {
                    url = url.replace("http://zhangmenshiting.qianqian.com", "https://gss0.bdstatic.com/y0s1hSulBw92lNKgpU_Z2jR7b2w6buu")
                } else if (source == "tencent") {
                    url = url.replace("https://dl.stream.qqmusic.qq.com/M800", "https://dl.stream.qqmusic.qq.com/M500")
                }
                if (url == "") {
                    url = "err"
                }
                future.complete(url)
            } else {
                showError("获取歌曲url失败", it.cause())
            }
        }
        return future
    }
    /**
     *
     * 获取歌词
     *
     * @param request
     * @return
     */
    static Future<String> getMusicLyric(Vertx vertx, String id, String source) {
        final Future<String> future = Future.future()
        def url = "http://www.gequdaquan.net/gqss/api.php"
        def param = new JsonObject()
        param.put("types", "lyric")
        param.put("id", id)
        param.put("source", source == null ? "netease" : source)
        get(vertx, url, param).setHandler {
            if (it.succeeded()) {
                def lyric = it.result().bodyAsJsonObject().getString("lyric")
                future.complete(lyric)
            } else {
                showError("获取歌词失败", it.cause())
            }
        }
        return future
    }

    /**
     *
     *
     * 获取音乐分类对应的数据
     *
     * @param request
     * @return
     */
    static Future<JsonObject> getCategoryList(Vertx vertx, long id) {
        final def param = new JsonObject()
        final def url = "http://www.gequdaquan.net/gqss/api.php"
        final Future<JsonObject> future = Future.future()
        param.put("types", "playlist")
        param.put("id", id)
        get(vertx, url, param).setHandler {
            if (it.succeeded()) {
                future.complete(it.result().bodyAsJsonObject())
            } else {
                showError("获取分类失败,分类#${id}", it.cause())
            }
        }
        return future
    }
    //添加请求头
    static String addHeader(HttpRequest<Buffer> request) {
        request.putHeader("Host", "www.gequdaquan.net")
        request.putHeader("Origin", "http://www.gequdaquan.net")
        request.putHeader("Referer", "http://www.gequdaquan.net/gqss/")
        request.putHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
        request.putHeader("X-Requested-With", "XMLHttpRequest")
    }

    //拼接http请求参数
    static String connectParam(JsonObject param, String requestUrl) {
        if (param != null) {
            param.eachWithIndex { Entry<String, Object> entry, int i ->
                if (i == 0) {
                    requestUrl += "?${entry.key}=${entry.value}"
                } else {
                    requestUrl += "&${entry.key}=${entry.value}"
                }
            }
        }
        return requestUrl
    }
}
