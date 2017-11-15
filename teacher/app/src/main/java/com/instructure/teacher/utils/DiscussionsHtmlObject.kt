package com.instructure.teacher.utils

import java.util.*

class DiscussionsHtmlObject(html: String, vimeoIds: HashMap<String, String>) {
    var html = ""
    var vimeoIds = HashMap<String, String>()

    init {
        this.html = html
        this.vimeoIds = vimeoIds
    }
}
