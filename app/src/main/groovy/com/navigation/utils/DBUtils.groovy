package com.navigation.utils

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection

class DBUtils {
    private static JDBCClient client

    DBUtils(Vertx vertx, JsonObject config) {
        client = JDBCClient.create(vertx, config)
    }
    //执行查询
    static Future<List<JsonObject>> executeQuery(String sql, JDBCClient client) {
        final Future<List<JsonObject>> future = Future.future()
        getConnection().setHandler {
            if (it.failed()) {
                future.fail(it.cause())
                return
            }
            it.result().query(sql) { _tt ->
                if (_tt.failed()) {
                    future.fail(_tt.cause())
                    return
                }
                def temp = _tt.result()
                def result = []
                while (temp.next) {
                    result.add(temp.toJson())
                }
                future.complete(result)
            }
        }
        return future
    }
    //执行查询并指定参数
    static Future<List<JsonObject>> executeQueryWithParam(String sql, JsonArray param) {
        final Future<List<JsonObject>> future = Future.future()
        getConnection().setHandler {
            if (it.failed()) {
                future.fail(it.cause())
                return
            }
            it.result().queryWithParams(sql, param) { _tt ->
                if (_tt.failed()) {
                    future.fail(_tt.cause())
                    return
                }
                def temp = _tt.result()
                def result = []
                while (temp.next) {
                    result.add(temp.toJson())
                }
                future.complete(result)
            }
        }
        return future

    }
    //执行更新
    static Future<Integer> executeUpdate(String sql) {
        final Future<Integer> future = Future.future()
        getConnection().setHandler {
            if (it.failed()) {
                future.fail(it.cause())
                return
            }
            it.result().update(sql) { _tt ->
                if (_tt.failed()) {
                    future.fail(_tt.cause())
                    return
                }
                future.complete(_tt.result().updated)
            }
        }
        return future
    }
    //执行更新并指定参数
    static Future<Integer> executeUpdate(String sql, JsonArray param) {
        final Future<Integer> future = Future.future()
        getConnection().setHandler {
            if (it.failed()) {
                future.fail(it.cause())
                return
            }
            it.result().updateWithParams(sql, param) { _tt ->
                if (_tt.failed()) {
                    future.fail(_tt.cause())
                    return
                }
                future.complete(_tt.result().updated)
            }
        }
        return future
    }

    //获取数据库连接对象
    static Future getConnection() {
        final Future<SQLConnection> future = Future.future()
        client.getConnection {
            if (it.succeeded()) {
                future.complete(it.result())
            } else {
                future.fail(it.cause())
            }
        }
        return future
    }

}


