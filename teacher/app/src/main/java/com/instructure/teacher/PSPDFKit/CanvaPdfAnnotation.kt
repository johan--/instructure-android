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
import com.pspdfkit.annotations.*

class CanvaInkAnnotation(info: CanvaPdfAnnotation) : InkAnnotation(info.page), PSCanvaInterface by info

class CanvaHighlightAnnotation(info: CanvaPdfAnnotation) : HighlightAnnotation(info.page, info.rectList ?: emptyList()), PSCanvaInterface by info

class CanvaStrikeOutAnnotation(info: CanvaPdfAnnotation) : StrikeOutAnnotation(info.page, info.rectList), PSCanvaInterface by info

class CanvaSquareAnnotation(info: CanvaPdfAnnotation) : SquareAnnotation(info.page, info.rect ?: RectF()), PSCanvaInterface by info

class CanvaFreeTextAnnotation(info: CanvaPdfAnnotation, contents: String) : FreeTextAnnotation(info.page, info.rect!!, contents), PSCanvaInterface by info

class CanvaNoteAnnotation(info: CanvaPdfAnnotation, iconName: String, contents: String ) : NoteAnnotation(info.page, info.rect ?: RectF(), contents, iconName), PSCanvaInterface by info
