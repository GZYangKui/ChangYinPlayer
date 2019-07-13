package com.navigation.base

import io.vertx.core.json.JsonObject

class BaseModel {
    JsonObject toJson() {
        return JsonObject.mapFrom(this)
    }
}
