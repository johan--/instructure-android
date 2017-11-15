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

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.COMMENT_REPLY_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.FREE_TEXT_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.HIGHLIGHT_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.INK_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.SQUARE_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.STRIKEOUT_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocAnnotation.Companion.TEXT_SUBJECT
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocCoordinate
import com.instructure.canvasapi2.models.CanvaDocs.CanvaDocInkList
import com.instructure.canvasapi2.utils.ApiPrefs.user
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat
import com.pspdfkit.annotations.*
import com.pspdfkit.annotations.Annotation
import java.util.*

fun CanvaDocAnnotation.convertCanvaDocAnnotationToPDF(context: Context) : Annotation? {
    return when(annotationType) {
        CanvaDocAnnotation.AnnotationType.INK -> convertInkType(this, context)
        CanvaDocAnnotation.AnnotationType.HIGHLIGHT -> convertHighlightType(this, context)
        CanvaDocAnnotation.AnnotationType.STRIKEOUT -> convertStrikeoutType(this, context)
        CanvaDocAnnotation.AnnotationType.SQUARE -> convertSquareType(this, context)
        CanvaDocAnnotation.AnnotationType.FREE_TEXT -> convertFreeTextType(this, context)
        CanvaDocAnnotation.AnnotationType.TEXT -> convertTextType(this, context)
        else -> null
    }
}

fun Annotation.convertPDFAnnotationToCanvaDoc(canvaDocId: String) : CanvaDocAnnotation? {
    return when(type) {
        AnnotationType.INK -> (this as InkAnnotation).toCanvaDocAnnotation(canvaDocId)
        AnnotationType.HIGHLIGHT -> (this as HighlightAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.STRIKEOUT -> (this as StrikeOutAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.SQUARE -> (this as SquareAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.NOTE -> (this as NoteAnnotation).convertToCanvaDoc(canvaDocId)
        AnnotationType.FREETEXT -> (this as FreeTextAnnotation).convertToCanvaDoc(canvaDocId)
        else -> null
    }
}

//region canvaDoc to PDF
private fun convertInkType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): InkAnnotation {
    val inkAnnotation = CanvaInkAnnotation(
            CanvaPdfAnnotation(
                    page = canvaDocAnnotation.page
            )
    )

    inkAnnotation.lineWidth = canvaDocAnnotation.width?.toFloat() ?: 0f
    inkAnnotation.boundingBox = canvaDocAnnotation.rect?.let {RectF(it[0][0], it[0][1], it[1][0], it[1][1])} ?: RectF()
    inkAnnotation.lines = canvaDocAnnotation.inklist?.gestures?.map{ it.map { PointF(it.x, it.y) } }
    inkAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.canvas_default_button))
    inkAnnotation.contents = canvaDocAnnotation.contents
    inkAnnotation.name = canvaDocAnnotation.id

    return inkAnnotation
}

private fun convertHighlightType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): HighlightAnnotation {
    val rectList = coordsToListOfRectfs(canvaDocAnnotation.coords)

    val highLightAnnotation = CanvaHighlightAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rectList = rectList
    ))
    highLightAnnotation.contents = canvaDocAnnotation.contents
    highLightAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.canvas_default_button))
    highLightAnnotation.name = canvaDocAnnotation.id

    return highLightAnnotation
}

private fun convertStrikeoutType(canvaDocAnnotation: CanvaDocAnnotation, context: Context): StrikeOutAnnotation {
    val rectList = coordsToListOfRectfs(canvaDocAnnotation.coords)

    val strikeOutAnnotation = CanvaStrikeOutAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rectList = rectList
    ))
    strikeOutAnnotation.contents = canvaDocAnnotation.contents
    strikeOutAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.canvas_default_button))
    strikeOutAnnotation.name = canvaDocAnnotation.id

    return strikeOutAnnotation
}

private fun convertSquareType(canvaDocAnnotation: CanvaDocAnnotation, context: Context) : SquareAnnotation {
    val rect = canvaDocAnnotation.rect?.let {RectF(it[0][0], it[0][1], it[1][0], it[1][1])}
    val squareAnnotation = CanvaSquareAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect
    ))
    squareAnnotation.contents = canvaDocAnnotation.contents
    squareAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.canvas_default_button))
    squareAnnotation.borderWidth = canvaDocAnnotation.width?.toFloat() ?: 2f //default width of 2
    squareAnnotation.name = canvaDocAnnotation.id

    return squareAnnotation
}

private fun convertFreeTextType(canvaDocAnnotation: CanvaDocAnnotation, context: Context) : FreeTextAnnotation {
    val rect = canvaDocAnnotation.rect?.let {RectF(it[0][0], it[1][1], it[1][0], it[0][1])}
    val freeTextAnnotation = CanvaFreeTextAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect
    ), contents = canvaDocAnnotation.contents ?: "")

    freeTextAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.black))
    freeTextAnnotation.name = canvaDocAnnotation.id
    freeTextAnnotation.textSize = 12f
    freeTextAnnotation.fillColor = context.getColorCompat(R.color.white)

    return freeTextAnnotation
}

private fun convertTextType(canvaDocAnnotation: CanvaDocAnnotation, context: Context) : NoteAnnotation{
    val rect = canvaDocAnnotation.rect?.let {RectF(it[0][0], it[0][1], it[1][0], it[1][1])}
    val noteAnnotation = CanvaNoteAnnotation(CanvaPdfAnnotation(
            page = canvaDocAnnotation.page,
            rect = rect
    ), NoteAnnotation.CIRCLE, canvaDocAnnotation.contents ?: "")

    noteAnnotation.color = canvaDocAnnotation.getColorInt(context.getColorCompat(R.color.canvas_default_button))
    noteAnnotation.name = canvaDocAnnotation.id

    return noteAnnotation
}
//endregion


//region PDF to canvadoc
fun InkAnnotation.toCanvaDocAnnotation(canvaDocId: String): CanvaDocAnnotation {
    // inkList is a list of lists; Kotlin adds wildcards to generic lists and Paperparcel can't handle that
    @Suppress("UNCHECKED_CAST")
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = INK_SUBJECT,
            page = this.pageIndex,
            width = this.lineWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.INK,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            inklist = CanvaDocInkList(convertListOfPointFToCanvaDocCoordinates(this.lines)),
            isEditable = true
    )

}

fun HighlightAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = HIGHLIGHT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.HIGHLIGHT,
            rect = listOfRectsToListOfListOfFloats(this.rects),
            coords = convertListOfRectsToListOfListOfListOfFloats(this.rects),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}



fun StrikeOutAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = STRIKEOUT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.STRIKEOUT,
            rect = listOfRectsToListOfListOfFloats(this.rects),
            coords = convertListOfRectsToListOfListOfListOfFloats(this.rects),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun SquareAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = SQUARE_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.SQUARE,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun NoteAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = TEXT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.TEXT,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            icon = "Comment",
            color = this.colorToHexString(),
            contents = this.contents,
            iconColor= this.colorToHexString(),
            isEditable = true
    )
}

fun FreeTextAnnotation.convertToCanvaDoc(canvaDocId: String): CanvaDocAnnotation {
    return CanvaDocAnnotation(
            id = this.name ?: "",
            userName = user?.shortName,
            documentId = canvaDocId,
            subject = FREE_TEXT_SUBJECT,
            page = this.pageIndex,
            context = this.getContext(),
            width = this.borderWidth,
            annotationType = CanvaDocAnnotation.AnnotationType.FREE_TEXT,
            rect = listOfRectsToListOfListOfFloats(listOf(this.boundingBox)),
            color = this.colorToHexString(),
            contents = this.contents,
            isEditable = true
    )
}

fun createCommentReplyAnnotation(contents: String, inReplyTo: String, canvaDocId: String, userId: String, page: Int): CanvaDocAnnotation{
    return CanvaDocAnnotation(
            id = "",
            ctxId = "",
            userId = userId,
            userName = "",
            createdAt = "",
            documentId = canvaDocId,
            subject = COMMENT_REPLY_SUBJECT,
            page = page,
            context = "",
            annotationType = CanvaDocAnnotation.AnnotationType.COMMENT_REPLY,
            contents = contents,
            inReplyTo = inReplyTo,
            isEditable = true)
}


fun listOfRectsToListOfListOfFloats(rects: List<RectF>?): ArrayList<ArrayList<Float>>? {
    if (rects == null || rects.isEmpty())
        return null

    val listOfLists = ArrayList<ArrayList<Float>>()
    listOfLists.add(arrayListOf(rects.minBy { it.left }?.left ?: 0f, rects.minBy { it.bottom }?.bottom ?: 0f))
    listOfLists.add(arrayListOf(rects.maxBy { it.right }?.right ?: 0f, rects.maxBy { it.top }?.top ?: 0f))

    return listOfLists
}

fun coordsToListOfRectfs(coords: List<List<List<Float>>>?) : MutableList<RectF> {
    val rectList = mutableListOf<RectF>()
    coords?.let {
        it.forEach {
            val tempRect = RectF(it[0][0], it[0][1], it[3][0], it[3][1])
            rectList.add(tempRect)
        }
    }

    return rectList
}

fun convertListOfRectsToListOfListOfListOfFloats(rects: MutableList<RectF>?): ArrayList<ArrayList<ArrayList<Float>>>? {
    if (rects == null || rects.isEmpty()) {
        return null
    }
    // The distance between the top of the line and the bottom
    val rectList: ArrayList<ArrayList<ArrayList<Float>>> = ArrayList()
    rects.forEach {
        val posList = arrayListOf<ArrayList<Float>>()

        val bottomLineLeftTop = arrayListOf(it.left, it.bottom)
        val bottomLineRightBottom = arrayListOf(it.right, it.bottom)
        posList.add(bottomLineLeftTop)
        posList.add(bottomLineRightBottom)

        val topLineLeftTop = arrayListOf(it.left, it.top)
        val topLineRightBottom = arrayListOf(it.right, it.top)
        posList.add(topLineLeftTop)
        posList.add(topLineRightBottom)

        rectList.add(posList)
    }
    return rectList
}

fun convertListOfPointFToCanvaDocCoordinates(linesList: List<List<PointF>>): ArrayList<ArrayList<CanvaDocCoordinate>> {
    val newList = ArrayList<ArrayList<CanvaDocCoordinate>>()
    for((position, list) in linesList.withIndex()) {
        newList.add(ArrayList<CanvaDocCoordinate>())
        for(point in list) {
            newList[position].add(CanvaDocCoordinate(point.x, point.y))
        }
    }

    return newList
}

fun Annotation.colorToHexString() = String.format("#%06X", 0xFFFFFF and this.color)
//endregion