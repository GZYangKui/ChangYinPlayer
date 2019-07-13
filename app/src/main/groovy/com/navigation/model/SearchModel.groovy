package com.navigation.model

import com.navigation.base.BaseModel

class SearchModel extends BaseModel {
    String types = "search"
    //搜索源
    String source
    //页码
    int page = 1
    //搜索关键字
    String name
    //每页数据
    long count = 20
}
