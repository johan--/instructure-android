/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class UsageRights(
        @SerializedName("legal_copyright")
        var legalCopyright: String? = "",
        @SerializedName("use_justification")
        var useJustification: FileUsageRightsJustification = FileUsageRightsJustification.OWN_COPYRIGHT,
        var license: String? = "",
        @SerializedName("license_name")
        var licenseName: String? = "",
        var message: String? = "",
        @SerializedName("file_ids")
        var fileIds: ArrayList<Long> = ArrayList()
) : PaperParcelable {
    companion object {
        @Suppress("unresolved_reference")
        @JvmField
        val CREATOR = PaperParcelUsageRights.CREATOR
    }
}

enum class FileUsageRightsJustification {
    @SerializedName("own_copyright")
    OWN_COPYRIGHT,
    @SerializedName("used_by_permission")
    USED_BY_PERMISSION,
    @SerializedName("public_domain")
    PUBLIC_DOMAIN,
    @SerializedName("fair_use")
    FAIR_USE,
    @SerializedName("creative_commons")
    CREATIVE_COMMONS
}

/**
 * Example of a License:
 * {
 *   "id": "cc_by_sa",
 *   "name": "CC Attribution Share Alike",
 *   "url": "http://creativecommons.org/licenses/by-sa/4.0"
 *  }
 */
@PaperParcel
data class License(
        val id: String,
        val name: String,
        val url: String
) : PaperParcelable {
    companion object {
        @Suppress("unresolved_reference")
        @JvmField
        val CREATOR = PaperParcelLicense.CREATOR
    }
}