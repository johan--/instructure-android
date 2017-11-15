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
package com.instructure.teacher.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.ContextCompat
import android.util.Base64
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import java.io.*
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.forEach

/**
 * Loader for creating discussions HTML.
 */
class DiscussionsHtmlLoader(context: Context,
                            val canvasContext: CanvasContext,
                            val discussionTopicHeader: DiscussionTopicHeader,
                            val discussionEntries: List<DiscussionEntry>,
                            val isAnnouncements: Boolean,
                            val isReadOnly: Boolean,
                            val isTablet: Boolean,
                            val startEntryId: Long) : AsyncTaskLoader<DiscussionsHtmlObject>(context) {

    override fun loadInBackground(): DiscussionsHtmlObject {
        val builder = StringBuilder()
        val brandColor = ThemePrefs.brandColor
        val likeColor = ContextCompat.getColor(context, R.color.discussion_liking)
        val converter = DiscussionEntryHtmlConverter()
        val template = getAssetsFile(context, "discussion_html_template_item.html")
        val likeImage = makeBitmapForWebView(brandColor, getBitmapFromAssets(context, "discussion_liked.png"))
        val replyButtonWidth = if (isTablet && context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "260px" else "220px"

        //Append Header - Don't do this in the loop to avoid String.replace() more than necessary
        builder.append(getAssetsFile(context, "discussion_html_header_item.html"))

        if (startEntryId == 0L) {
            loadDiscussionTopic(discussionEntries, builder, template, converter, brandColor, likeColor, likeImage, replyButtonWidth)
        } else {
            //We are looking for a subentry of discussions to display. This finds a subentry, and uses that as the initial entry for display.
            val discussionSubEntries = findSubentryForDisplay(startEntryId, discussionEntries)
            loadDiscussionTopic(discussionSubEntries, builder, template, converter, brandColor, likeColor, likeImage, replyButtonWidth)
        }

        //Append Footer - Don't do this in the loop to avoid String.replace() more than necessary
        builder.append(getAssetsFile(context, "discussion_html_footer_item.html"))
        val html = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(builder.toString())

        return DiscussionsHtmlObject(html, HashMap<String, String>())
    }

    private fun findSubentryForDisplay(startEntryId: Long, discussionEntries: List<DiscussionEntry>): List<DiscussionEntry> {
        discussionEntries.forEach {
            val foundEntries = recursiveFind(startEntryId, it.replies)
            if (foundEntries != null) {
                return foundEntries
            }
        }
        return discussionEntries
    }

    private fun recursiveFind(startEntryId: Long, replies: List<DiscussionEntry>): List<DiscussionEntry>? {
        replies.forEach {
            if (it.id == startEntryId) {
                //Creates a list of replies based on the entry the user clicked on. This will not show siblings of the parent.
                val formalReplies = ArrayList<DiscussionEntry>(1)
                formalReplies.add(it)
                return formalReplies
            } else {
                val items = recursiveFind(startEntryId, it.replies)
                if (items != null) {
                    return items
                }
            }
        }
        return null
    }

    private fun loadDiscussionTopic(
            discussionEntries: List<DiscussionEntry>,
            builder: StringBuilder,
            template: String,
            converter: DiscussionEntryHtmlConverter,
            brandColor: Int,
            likeColor: Int,
            likeImage: String,
            replyButtonWidth: String) {

        //This loops through each of the direct replies and for each child up to 3 or 5 based on if tablet or phone.
        //General rule of thumb is to pass in any values that need calculation so we don't repeat those within the loop.
        //We also filter out any deleted discussions and by nature their children so they don't get displayed.
        discussionEntries.forEach { discussionEntry ->
            builder.append(build(discussionEntry, converter, template,
                    makeAvatarForWebView(context, discussionEntry), 0, false,
                    brandColor, likeColor, likeImage, replyButtonWidth))

            discussionEntry.replies.forEach { discussionEntry ->
                builder.append(build(discussionEntry, converter, template,
                        makeAvatarForWebView(context, discussionEntry), 1, false,
                        brandColor, likeColor, likeImage, replyButtonWidth))

                if (isTablet) {
                    discussionEntry.replies.forEach { discussionEntry ->
                        builder.append(build(discussionEntry, converter, template,
                                makeAvatarForWebView(context, discussionEntry), 2, false,
                                brandColor, likeColor, likeImage, replyButtonWidth))

                        discussionEntry.replies.forEach { discussionEntry ->
                            builder.append(build(discussionEntry, converter, template,
                                    makeAvatarForWebView(context, discussionEntry), 3, false,
                                    brandColor, likeColor, likeImage, replyButtonWidth))

                            discussionEntry.replies.forEach { discussionEntry ->
                                builder.append(build(discussionEntry, converter, template,
                                        makeAvatarForWebView(context, discussionEntry), 4, false,
                                        brandColor, likeColor, likeImage, replyButtonWidth))

                                discussionEntry.replies.forEach { discussionEntry ->
                                    val reachedViewableEnd = (discussionEntry.totalChildren > 0)
                                    builder.append(build(discussionEntry, converter, template,
                                            makeAvatarForWebView(context, discussionEntry), 5, reachedViewableEnd,
                                            brandColor, likeColor, likeImage, replyButtonWidth))
                                }
                            }
                        }
                    }
                } else {
                    discussionEntry.replies.forEach { discussionEntry ->
                        val reachedViewableEnd = (discussionEntry.totalChildren > 0)
                        builder.append(build(discussionEntry, converter, template,
                                makeAvatarForWebView(context, discussionEntry), 2, reachedViewableEnd,
                                brandColor, likeColor, likeImage, replyButtonWidth))
                    }
                }
            }
        }
    }

    private fun build(
            discussionEntry: DiscussionEntry,
            converter: DiscussionEntryHtmlConverter,
            template: String,
            avatarImage: String,
            indent: Int,
            reachedViewableEnd: Boolean,
            brandColor: Int,
            likeColor: Int,
            likeImage: String,
            replyButtonWidth: String): String {

        return converter.buildHtml(
                context,
                brandColor,
                likeColor,
                discussionEntry,
                template,
                avatarImage,
                allowReplies(canvasContext, discussionTopicHeader),
                allowEditing(canvasContext, discussionTopicHeader),
                allowLiking(canvasContext, discussionTopicHeader),
                allowDeleting(canvasContext, discussionTopicHeader),
                reachedViewableEnd,
                indent,
                likeImage,
                replyButtonWidth,
                formatDeletedInfoText(context, discussionEntry))
    }

    private fun allowReplies(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        /*
            There are three related scenarios in which we don't want users to be able to reply.
               so we check that none of these conditions exist
            1.) The discussion is locked for an unknown reason.
            2.) It's locked due to a module/etc.
            3.) User is an Observer in a course.
            4.) IF it's a teacher we bag the entire rule book and let them reply.
        */

        if (canvasContext?.type == CanvasContext.Type.COURSE && (canvasContext as Course).isTeacher) return true

        val isLocked = header.isLocked
        val lockInfoEmpty = header.lockInfo == null || header.lockInfo.isEmpty
        val isCourse = canvasContext?.type == CanvasContext.Type.COURSE
        val isObserver = isCourse && (canvasContext as Course).isObserver
        val hasPermission = header.permissions.canReply()

        //If we are not locked, do not have lock info, have permission, is a course, and not an observer...
        // - I suspect this can all be replaced with hasPermission, need to verify.
        return !isLocked && lockInfoEmpty && hasPermission && isCourse && !isObserver
    }

    private fun allowEditing(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        //TODO add permissions in for student
        return true
    }

    private fun allowLiking(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        if (header.isAllowRating) {
            if (header.isOnlyGradersCanRate) {
                if (canvasContext?.type == CanvasContext.Type.COURSE) {
                    return (canvasContext as Course).isTeacher || canvasContext.isTA
                }
            } else {
                return true
            }
        }
        return false
    }

    private fun allowDeleting(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        //TODO: add permissions in for student
        return true
    }

    companion object {

        fun getAssetsFile(context: Context, fileName: String): String {
            try {
                var file = ""
                val reader = BufferedReader(
                        InputStreamReader(context.assets.open(fileName)))
                var line: String? = ""
                while (line != null) {
                    file += line
                    line = reader.readLine()
                }
                reader.close()
                return file
            } catch (e: Exception) {
                return ""
            }
        }

        fun getBitmapFromAssets(context: Context, filePath: String): Bitmap? {
            val assetManager = context.assets
            val inputStream: InputStream
            var bitmap: Bitmap? = null
            try {
                inputStream = assetManager.open(filePath)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                return null
            }
            return bitmap
        }

        fun makeBitmapForWebView(color: Int, bitmap: Bitmap?): String {
            if (bitmap == null) return ""
            val coloredBitmap = colorIt(color, bitmap)
            val outputStream = ByteArrayOutputStream()
            coloredBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            coloredBitmap.recycle()
            return "data:image/png;base64," + imageBase64
        }

        fun colorIt(color: Int, map: Bitmap): Bitmap {
            val mutableBitmap = map.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            val paint = Paint()
            paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
            return mutableBitmap
        }

        fun getRGBColorString(color: Int): String {
            val r = color shr 16 and 0xFF
            val g = color shr 8 and 0xFF
            val b = color shr 0 and 0xFF
            return "rgb($r,$g,$b)"
        }

        fun getHexColorString(color: Int): String {
            return String.format ("#%06X", (0xFFFFFF and color))
        }

        fun formatDeletedInfoText(context: Context, discussionEntry: DiscussionEntry): String {
            if(discussionEntry.isDeleted) {
                val atSeparator = context.getString(R.string.at)
                val deletedText = String.format(context.getString(R.string.discussions_deleted),
                        DateHelper.getMonthDayAtTime(context, discussionEntry.updatedAt, atSeparator))
                return String.format("<div class=\"deleted_info\">%s</div>", deletedText)
            } else {
                return ""
            }
        }

        /**
         * If the avatar is valid then returns an empty string. Otherwise...
         * Returns an avatar bitmap converted into a base64 string for webviews.
         */
        fun makeAvatarForWebView(context: Context, discussionEntry: DiscussionEntry): String {
            if(discussionEntry.author != null && ProfileUtils.shouldLoadAltAvatarImage(discussionEntry.author.avatarImageUrl)) {
                val avatarBitmap = ProfileUtils.getInitialsAvatarBitMap(
                        context, discussionEntry.author.displayName,
                        Color.TRANSPARENT,
                        ContextCompat.getColor(context, R.color.defaultTextDark),
                        ContextCompat.getColor(context, R.color.profileBorderColor))
                val outputStream = ByteArrayOutputStream()
                avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                avatarBitmap.recycle()
                return "data:image/png;base64," + imageBase64
            } else {
                if(discussionEntry.author == null || discussionEntry.author.avatarImageUrl.isNullOrBlank()) {
                    //Unknown author
                    val avatarBitmap = ProfileUtils.getInitialsAvatarBitMap(
                            context, "?",
                            Color.TRANSPARENT,
                            ContextCompat.getColor(context, R.color.defaultTextDark),
                            ContextCompat.getColor(context, R.color.profileBorderColor))
                    val outputStream = ByteArrayOutputStream()
                    avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    avatarBitmap.recycle()
                    return "data:image/png;base64," + imageBase64
                }
                return discussionEntry.author?.avatarImageUrl ?: ""
            }
        }
    }
}
