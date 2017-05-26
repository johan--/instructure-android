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
package instructure.rceditor;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class RCEUtils {

    //<span style="color: rgb(139, 150, 158);">Gray</span>
    private static final String RBG_REGEX = "rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\);";
    //<span style=";"></span>
    private static final String SPAN_REGEX = "<span\\s*style=\"\\s*;\">\\s*</span>";
    //<span style="color: #1482c8;"></span>
    private static final String HEX_COMMA_REGEX = "#([A-Fa-f0-9]{3,8});";


    @Nullable
    private static String workaround_RBG_2_HEX(String html) {
        try {
            Pattern pattern = Pattern.compile(RBG_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                String cleanValue = action.group().replace("rgb(", "");//removes prefix
                cleanValue = cleanValue.replace(");", "");//removes suffix
                cleanValue = cleanValue.replaceAll("\\s", "");//Clean up whitespace
                List<String> values = Arrays.asList(cleanValue.split(","));

                String hex = String.format("#%02x%02x%02x",
                        Integer.valueOf(values.get(0)),
                        Integer.valueOf(values.get(1)),
                        Integer.valueOf(values.get(2)));

                action.appendReplacement(sb, Matcher.quoteReplacement(hex));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String workaroundInvalidSpan(String html) {
        try {
            Pattern pattern = Pattern.compile(SPAN_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                action.appendReplacement(sb, Matcher.quoteReplacement(""));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String workaroundInvalidHex(String html) {
        try {
            Pattern pattern = Pattern.compile(HEX_COMMA_REGEX);
            Matcher action = pattern.matcher(html);
            StringBuffer sb = new StringBuffer(html.length());
            while (action.find()) {
                String cleanValue = action.group().replace(";", "");
                action.appendReplacement(sb, Matcher.quoteReplacement(cleanValue));
            }
            action.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * When we do a PUT on the HTML there is often small things that'll cause the HTML to be invalid by the server and replaced
     * by <p>&nbsp;</p>. In order to avoid content getting nuked we use these workarounds. It is possible that the actual error
     * is due to improper encoding on our end.
     * @param html A valid HTML string
     * @return A String or null value that has been sanitized.
     */
    @Nullable
    static String sanitizeHTML(String html) {
//        Log.d("HTML", "VALIDATE(NONE): " + html);
        String validated = workaround_RBG_2_HEX(html);
//        Log.d("HTML", "VALIDATE(RGB): " + validated);
        if(validated == null) return null;
        validated = workaroundInvalidSpan(validated);
//        Log.d("HTML", "VALIDATE(SPAN;): " + validated);
        if(validated == null) return null;
        validated = workaroundInvalidHex(validated);
//        Log.d("HTML", "VALIDATE(HEX;): " + validated);
        return validated;
    }
}
