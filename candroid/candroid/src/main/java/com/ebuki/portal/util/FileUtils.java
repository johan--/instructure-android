/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.ebuki.portal.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ebuki.portal.activity.CandroidPSPDFActivity;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.annotations.AnnotationType;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.activity.ThumbnailBarMode;
import com.pspdfkit.configuration.page.PageFitMode;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.preferences.PSPDFKitPreferences;
import com.pspdfkit.ui.PdfActivityIntentBuilder;
import com.pspdfkit.ui.special_mode.controller.AnnotationTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

	public static void showPdfDocument(Uri uri, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia, final Context context) {
		if (!PSPDFKitPreferences.get(context).isAnnotationCreatorSet()) {
			User cachedUser = ApiPrefs.getUser();
			String userName = "";
			if (cachedUser != null) {
				userName = ApiPrefs.getUser().getShortName();
			}
			PSPDFKitPreferences.get(context).setAnnotationCreator(userName);
		}

		final PdfActivityConfiguration pspdfActivityConfiguration;

		if(loadedMedia.isSubmission()) {
			// We don't want to allow users to edit for submission viewing
			pspdfActivityConfiguration = new PdfActivityConfiguration.Builder(context)
					.scrollDirection(PageScrollDirection.HORIZONTAL)
					.showThumbnailGrid()
					.setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_DEFAULT)
					.disableAnnotationEditing()
					.disableAnnotationList()
					.disableDocumentEditor()
					.fitMode(PageFitMode.FIT_TO_WIDTH)
					.build();
		} else {
			// Standard behavior
			pspdfActivityConfiguration = new PdfActivityConfiguration.Builder(context)
					.scrollDirection(PageScrollDirection.HORIZONTAL)
					.showThumbnailGrid()
					.setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_DEFAULT)
					.enableDocumentEditor()
					.enabledAnnotationTools(setupAnnotationCreationList())
					.editableAnnotationTypes(setupAnnotationEditList())
					.fitMode(PageFitMode.FIT_TO_WIDTH)
					.build();
		}



		if (PSPDFKit.isOpenableUri(context, uri)) {
			Intent intent = PdfActivityIntentBuilder
					.fromUri(context, uri)
					.configuration(pspdfActivityConfiguration)
					.activityClass(CandroidPSPDFActivity.class)
					.build();
			context.startActivity(intent);
		} else {
			//If we still can't open this PDF, we will then attempt to pass it off to the user's pdfviewer
			context.startActivity(loadedMedia.getIntent());
		}

	}

	public static List<AnnotationTool> setupAnnotationCreationList() {
		return new ArrayList<AnnotationTool>(Arrays.asList(AnnotationTool.INK, AnnotationTool.HIGHLIGHT, AnnotationTool.STRIKEOUT, AnnotationTool.SQUARE, AnnotationTool.NOTE, AnnotationTool.FREETEXT));
	}
	public static List<AnnotationType> setupAnnotationEditList() {
		return new ArrayList<AnnotationType>(Arrays.asList(AnnotationType.INK, AnnotationType.HIGHLIGHT, AnnotationType.STRIKEOUT, AnnotationType.SQUARE, AnnotationType.NOTE, AnnotationType.FREETEXT));
	}
}
