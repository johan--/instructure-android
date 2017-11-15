/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.PSPDFKit

import android.graphics.RectF
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.document.PdfDocument

//right now this is expecting a url in the format of:
//"/1/sessions/eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjIjoxNDk1NzM1ODM2MDU2LCJkIjoicV9vdlBnTnpxVVh0MTY3UVJjUDFyYVhwMlJydEpPIiwiZSI6MTQ5NTczOTQzNiwiciI6InBkZmpzIiwiYSI6eyJjIjoiZGVmYXVsdCIsInAiOiJyZWFkd3JpdGUiLCJ1IjoiMTAwMDAwMDU4MTQ3ODkiLCJuIjoiVHJldm9yIiwiciI6IiJ9LCJpYXQiOjE0OTU3MzU4MzZ9.IKn4kV-mrseE4INa8niX8A6rMxWS9f798bFeWtUkFIA/file/file.pdf"
fun extractSessionId(url: String) = url.substringAfter("sessions/", "").substringBefore('/', "")

fun extractDocId(url: String) = url.substringAfter("documents/", "").substringBefore('/', "")

fun extractCanvaDocsDomain(url: String) = url.substringBefore("/1/sessions")

/**
 * If is CanvaAnnotation type, return id
 * Otherwise return ""
 */
fun Annotation.getId() = (this as? PSCanvaInterface)?.id ?: ""


/**
 * If is CanvaAnnotation type, return userId
 * Otherwise return ""
 */
fun Annotation.getUserId() = (this as? PSCanvaInterface)?.userId ?: ""

/**
 * If is CanvaAnnotation type, return ctxId
 * Otherwise return ""
 */
fun Annotation.getCtxId() = (this as? PSCanvaInterface)?.ctxId ?: ""

/**
 * If is CanvaAnnotation type, return ctxId
 * Otherwise return ""
 */
fun Annotation.getCreatedAt() = (this as? PSCanvaInterface)?.createdAt ?: ""

fun Annotation.isNotFound() = (this as? PSCanvaInterface)?.isNotFound ?: false

fun Annotation.setIsNotFound(isNotFound: Boolean) {
    (this as? PSCanvaInterface)?.isNotFound = isNotFound
}

fun Annotation.getContext() = (this as? PSCanvaInterface)?.context ?: ""

/**
 * Finds an annotation based on it's id
 */
fun PdfDocument.findAnnotationById(id: String, pageIndex: Int): Annotation? {
    val annotations = this.annotationProvider.getAnnotations(pageIndex)
    return annotations.find { it.getId() == id }
}

fun PdfDocument.removeAnnotation(id: String, pageIndex: Int) {
    val annotationToRemove = findAnnotationById(id, pageIndex) ?: return
    annotationProvider.removeAnnotationFromPage(annotationToRemove)
}

fun PdfDocument.addAnnotation(annotation: Annotation) {
    annotationProvider.addAnnotationToPage(annotation)
}


// Delegate
interface PSCanvaInterface {
    val id: String?
    val userId: String?
    val context: String?
    var page: Int
    var rect: RectF?
    val rectList: MutableList<RectF>?
    var isNotFound: Boolean
    val ctxId: String?
    val createdAt: String?
}

data class CanvaPdfAnnotation(
        override val id: String? = null,
        override val userId: String? = null,
        override val context: String? = null,
        override var page: Int = 0,
        override var rect: RectF? = null,
        override var isNotFound: Boolean = false,
        override val ctxId: String? = null,
        override val createdAt: String? = null,
        override val rectList: MutableList<RectF>? = null
) : PSCanvaInterface
