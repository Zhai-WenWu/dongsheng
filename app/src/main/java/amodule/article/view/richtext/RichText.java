/*
 * Copyright (C) 2015 Matthew Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package amodule.article.view.richtext;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.content.Context.CLIPBOARD_SERVICE;

public class RichText extends EditText implements TextWatcher {
    public static final int FORMAT_BOLD = 0x01;
    public static final int FORMAT_ITALIC = 0x02;
    public static final int FORMAT_UNDERLINED = 0x03;
    public static final int FORMAT_STRIKETHROUGH = 0x04;
    public static final int FORMAT_BULLET = 0x05;
    public static final int FORMAT_QUOTE = 0x06;
    public static final int FORMAT_LINK = 0x07;
    public static final int FORMAT_CENTER= 0x08;

    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    private Context mContext;

    private int bulletColor = 0;
    private int bulletRadius = 0;
    private int bulletGapWidth = 0;
    private boolean historyEnable = true;
    private int historySize = 100;
    private int linkColor = 0;
    private boolean linkUnderline = true;
    private int quoteColor = 0;
    private int quoteStripeWidth = 0;
    private int quoteGapWidth = 0;

    private List<Editable> historyList = new LinkedList<>();
    private boolean historyWorking = false;
    private int historyCursor = 0;

    private SpannableStringBuilder inputBefore;
    private Editable inputLast;

    private List<Map<String, String>> linkMapArray = new ArrayList<>();

    public RichText(Context context) {
        super(context);
        this.mContext = context;
        init(null);
    }

    public RichText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs);
    }

    public RichText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    @SuppressWarnings("NewApi")
    public RichText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RichText);
        bulletColor = array.getColor(R.styleable.RichText_bulletColor, 0);
        bulletRadius = array.getDimensionPixelSize(R.styleable.RichText_bulletRadius, 0);
        bulletGapWidth = array.getDimensionPixelSize(R.styleable.RichText_bulletGapWidth, 0);
        historyEnable = array.getBoolean(R.styleable.RichText_historyEnable, true);
        historySize = array.getInt(R.styleable.RichText_historySize, 100);
        linkColor = array.getColor(R.styleable.RichText_linkColor, 0);
        linkUnderline = array.getBoolean(R.styleable.RichText_linkUnderline, true);
        quoteColor = array.getColor(R.styleable.RichText_quoteColor, 0);
        quoteStripeWidth = array.getDimensionPixelSize(R.styleable.RichText_quoteStripeWidth, 0);
        quoteGapWidth = array.getDimensionPixelSize(R.styleable.RichText_quoteCapWidth, 0);
        array.recycle();

        if (historyEnable && historySize <= 0) {
            throw new IllegalArgumentException("historySize must > 0");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        addTextChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
    }

    // StyleSpan ===================================================================================

    public void bold(boolean valid) {
        if (valid) {
            styleValid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        } else {
            styleInvalid(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
        }
    }

    public void italic(boolean valid) {
        if (valid) {
            styleValid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        } else {
            styleInvalid(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
        }
    }

    protected void styleValid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return;
        }

        if (start >= end) {
            return;
        }

        getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void styleInvalid(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return;
        }

        if (start >= end) {
            return;
        }

        StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
        List<RichPart> list = new ArrayList<>();

        for (StyleSpan span : spans) {
            if (span.getStyle() == style) {
                list.add(new RichPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
                getEditableText().removeSpan(span);
            }
        }

        for (RichPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    styleValid(style, part.getStart(), start);
                }

                if (part.getEnd() > end) {
                    styleValid(style, end, part.getEnd());
                }
            }
        }
    }

    protected boolean containStyle(int style, int start, int end) {
        switch (style) {
            case Typeface.NORMAL:
            case Typeface.BOLD:
            case Typeface.ITALIC:
            case Typeface.BOLD_ITALIC:
                break;
            default:
                return false;
        }

        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                StyleSpan[] before = getEditableText().getSpans(start - 1, start, StyleSpan.class);
                StyleSpan[] after = getEditableText().getSpans(start, start + 1, StyleSpan.class);
                return before.length > 0 && after.length > 0 && before[0].getStyle() == style && after[0].getStyle() == style;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            // Make sure no duplicate characters be added
            for (int i = start; i < end; i++) {
                StyleSpan[] spans = getEditableText().getSpans(i, i + 1, StyleSpan.class);
                for (StyleSpan span : spans) {
                    if (span.getStyle() == style) {
                        builder.append(getEditableText().subSequence(i, i + 1).toString());
                        break;
                    }
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }



    // UnderlineSpan ===============================================================================

    public void underline(boolean valid) {
        if (valid) {
            underlineValid(getSelectionStart(), getSelectionEnd());
        } else {
            underlineInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void underlineValid(int start, int end) {
        if (start >= end) {
            return;
        }

        getEditableText().setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void underlineInvalid(int start, int end) {
        if (start >= end) {
            return;
        }

        UnderlineSpan[] spans = getEditableText().getSpans(start, end, UnderlineSpan.class);
        List<RichPart> list = new ArrayList<>();

        for (UnderlineSpan span : spans) {
            list.add(new RichPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (RichPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    underlineValid(part.getStart(), start);
                }

                if (part.getEnd() > end) {
                    underlineValid(end, part.getEnd());
                }
            }
        }
    }

    protected boolean containUnderline(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                UnderlineSpan[] before = getEditableText().getSpans(start - 1, start, UnderlineSpan.class);
                UnderlineSpan[] after = getEditableText().getSpans(start, start + 1, UnderlineSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, UnderlineSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    // StrikethroughSpan ===========================================================================

    public void strikethrough(boolean valid) {
        if (valid) {
            strikethroughValid(getSelectionStart(), getSelectionEnd());
        } else {
            strikethroughInvalid(getSelectionStart(), getSelectionEnd());
        }
    }

    protected void strikethroughValid(int start, int end) {
        if (start >= end) {
            return;
        }

        getEditableText().setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    protected void strikethroughInvalid(int start, int end) {
        if (start >= end) {
            return;
        }

        StrikethroughSpan[] spans = getEditableText().getSpans(start, end, StrikethroughSpan.class);
        List<RichPart> list = new ArrayList<>();

        for (StrikethroughSpan span : spans) {
            list.add(new RichPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (RichPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    strikethroughValid(part.getStart(), start);
                }

                if (part.getEnd() > end) {
                    strikethroughValid(end, part.getEnd());
                }
            }
        }
    }

    protected boolean containStrikethrough(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                StrikethroughSpan[] before = getEditableText().getSpans(start - 1, start, StrikethroughSpan.class);
                StrikethroughSpan[] after = getEditableText().getSpans(start, start + 1, StrikethroughSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, StrikethroughSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    // AlignmentSpan ===============================================================================

    public void center(boolean valid){
        final int centerSelectionStart = getCenterSelectionStart();
        final int centerSelectionEnd = getCenterSelectionEnd();

        if(valid){
            centerValid(centerSelectionStart,centerSelectionEnd);
        }else{
            centerInvalid(centerSelectionStart,centerSelectionEnd);
        }
    }

    private void centerValid(int start, int end) {
        if (start >= end) {
            return;
        }

        getEditableText().setSpan(new RichCenterAlignmentSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void centerInvalid(int start, int end) {
        if (start >= end) {
            return;
        }

        RichCenterAlignmentSpan[] spans = getEditableText().getSpans(start, end, RichCenterAlignmentSpan.class);
        List<RichPart> list = new ArrayList<>();

        for (AlignmentSpan span : spans) {
            list.add(new RichPart(getEditableText().getSpanStart(span), getEditableText().getSpanEnd(span)));
            getEditableText().removeSpan(span);
        }

        for (RichPart part : list) {
            if (part.isValid()) {
                if (part.getStart() < start) {
                    centerValid(part.getStart(), start);
                }

                if (part.getEnd() > end) {
                    centerValid(end, part.getEnd());
                }
            }
        }

    }

    private int getCenterSelectionStart() {
        final int cursorStart = getSelectionStart();

        int firstLineBreak = cursorStart;
        String allStr = getText().toString();
        if(firstLineBreak == 0)
            return 0;
        else if(firstLineBreak == allStr.length())
            firstLineBreak--;
        boolean isFind = false;
        while (!isFind && firstLineBreak > 0 && firstLineBreak < allStr.length()){
            char c = allStr.charAt(firstLineBreak);
            isFind = '\n' == c;
            if(isFind) break;
            firstLineBreak--;
        }
        return firstLineBreak;
    }

    private int getCenterSelectionEnd() {
        final int cursorEnd = getSelectionEnd();

        int lastLineBreak = cursorEnd;
        String allStr = getText().toString();
        if(lastLineBreak == allStr.length())
            return allStr.length();
        boolean isFind = false;
        while (!isFind && lastLineBreak >= 0 && lastLineBreak < allStr.length()){
            char c = allStr.charAt(lastLineBreak);
            isFind = '\n' == c;
            if(isFind) break;
            lastLineBreak++;
        }
        return lastLineBreak;
    }

    protected boolean containCenter(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                RichCenterAlignmentSpan[] before = getEditableText().getSpans(start - 1, start, RichCenterAlignmentSpan.class);
                RichCenterAlignmentSpan[] after = getEditableText().getSpans(start, start + 1, RichCenterAlignmentSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, RichCenterAlignmentSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    public void centerFormat(boolean valid){
        if (valid) {
            centerFormatValid();
        } else {
            centerFormatInvalid();
        }
    }

    protected void centerFormatValid(){
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (containCenterFormat(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1; // \n
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            // Find selection area inside
            int centerStart = 0;
            int centerEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                centerStart = lineStart;
                centerEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                centerStart = lineStart;
                centerEnd = lineEnd;
            }

            if (centerStart < centerEnd) {
                getEditableText().setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), centerStart, centerEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void centerFormatInvalid(){
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (!containCenterFormat(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int centerStart = 0;
            int centerEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                centerStart = lineStart;
                centerEnd = lineEnd;
                if (centerStart <= centerEnd) {
                    AlignmentSpan[] spans = getEditableText().getSpans(centerStart, centerEnd, AlignmentSpan.class);
                    for (AlignmentSpan span : spans) {
                        getEditableText().removeSpan(span);
                    }
                }
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                centerStart = lineStart;
                centerEnd = lineEnd;
                if (centerStart <= centerEnd) {
                    AlignmentSpan[] spans = getEditableText().getSpans(centerStart, centerEnd, AlignmentSpan.class);
                    for (AlignmentSpan span : spans) {
                        getEditableText().removeSpan(span);
                    }
                }
            }

        }
    }

    public boolean previousLineContainCenter(){
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                list.add(i-1);
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                list.add(i-1);
            }
        }

        for (Integer i : list) {
            if (!containCenterFormat(i)) {
                return false;
            }
        }

        return true;
    }

    protected boolean containCenterFormat(){
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                list.add(i);
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                list.add(i);
            }
        }

        for (Integer i : list) {
            if (!containCenterFormat(i)) {
                return false;
            }
        }

        return true;
    }

    protected boolean containCenterFormat(int index){
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        if (index < 0 || index >= lines.length) {
            return false;
        }

        int start = 0;
        for (int i = 0; i < index; i++) {
            start = start + lines[i].length() + 1;
        }

        int end = start + lines[index].length();
        if (start >= end) {
            return false;
        }

        AlignmentSpan[] spans = getEditableText().getSpans(start, end, AlignmentSpan.class);
        return spans.length > 0;
    }

    // BulletSpan ==================================================================================

    public void bullet(boolean valid) {
        if (valid) {
            bulletValid();
        } else {
            bulletInvalid();
        }
    }

    protected void bulletValid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (containBullet(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1; // \n
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            // Find selection area inside
            int bulletStart = 0;
            int bulletEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            }

            if (bulletStart < bulletEnd) {
                getEditableText().setSpan(new RichBulletSpan(bulletColor, bulletRadius, bulletGapWidth), bulletStart, bulletEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void bulletInvalid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (!containBullet(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int bulletStart = 0;
            int bulletEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                bulletStart = lineStart;
                bulletEnd = lineEnd;
            }

            if (bulletStart < bulletEnd) {
                BulletSpan[] spans = getEditableText().getSpans(bulletStart, bulletEnd, BulletSpan.class);
                for (BulletSpan span : spans) {
                    getEditableText().removeSpan(span);
                }
            }
        }
    }

    protected boolean containBullet() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                list.add(i);
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                list.add(i);
            }
        }

        for (Integer i : list) {
            if (!containBullet(i)) {
                return false;
            }
        }

        return true;
    }

    protected boolean containBullet(int index) {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        if (index < 0 || index >= lines.length) {
            return false;
        }

        int start = 0;
        for (int i = 0; i < index; i++) {
            start = start + lines[i].length() + 1;
        }

        int end = start + lines[index].length();
        if (start >= end) {
            return false;
        }

        BulletSpan[] spans = getEditableText().getSpans(start, end, BulletSpan.class);
        return spans.length > 0;
    }

    // QuoteSpan ===================================================================================

    public void quote(boolean valid) {
        if (valid) {
            quoteValid();
        } else {
            quoteInvalid();
        }
    }

    protected void quoteValid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (containQuote(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1; // \n
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int quoteStart = 0;
            int quoteEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                quoteStart = lineStart;
                quoteEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                quoteStart = lineStart;
                quoteEnd = lineEnd;
            }

            if (quoteStart < quoteEnd) {
                getEditableText().setSpan(new RichQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    protected void quoteInvalid() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (!containQuote(i)) {
                continue;
            }

            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            int quoteStart = 0;
            int quoteEnd = 0;
            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                quoteStart = lineStart;
                quoteEnd = lineEnd;
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                quoteStart = lineStart;
                quoteEnd = lineEnd;
            }

            if (quoteStart < quoteEnd) {
                QuoteSpan[] spans = getEditableText().getSpans(quoteStart, quoteEnd, QuoteSpan.class);
                for (QuoteSpan span : spans) {
                    getEditableText().removeSpan(span);
                }
            }
        }
    }

    protected boolean containQuote() {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;
            for (int j = 0; j < i; j++) {
                lineStart = lineStart + lines[j].length() + 1;
            }

            int lineEnd = lineStart + lines[i].length();
            if (lineStart >= lineEnd) {
                continue;
            }

            if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
                list.add(i);
            } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
                list.add(i);
            }
        }

        for (Integer i : list) {
            if (!containQuote(i)) {
                return false;
            }
        }

        return true;
    }

    protected boolean containQuote(int index) {
        String[] lines = TextUtils.split(getEditableText().toString(), "\n");
        if (index < 0 || index >= lines.length) {
            return false;
        }

        int start = 0;
        for (int i = 0; i < index; i++) {
            start = start + lines[i].length() + 1;
        }

        int end = start + lines[index].length();
        if (start >= end) {
            return false;
        }

        QuoteSpan[] spans = getEditableText().getSpans(start, end, QuoteSpan.class);
        return spans.length > 0;
    }

    // URLSpan =====================================================================================

    public void link(String link) {
        link(link, getSelectionStart(), getSelectionEnd());
    }

    // When KnifeText lose focus, use this method
    public void link(String link, int start, int end) {
        if (link != null && !TextUtils.isEmpty(link.trim())) {
            linkValid(link, start, end);
        } else {
            linkInvalid(start, end);
        }
    }

    protected void linkValid(final String link, final int start, final int end) {
        if (start >= end) {
            return;
        }
        //此处确定需要添加link操作
        addLinkMapToArray(link, getText().subSequence(start, end).toString());

        linkInvalid(start, end);
        getEditableText().setSpan(new RichURLSpan(link, linkColor, linkUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        setSelection(end);
    }

    /**
     * @param link
     * @param desc
     */
    public void addLinkMapToArray(@NonNull String link, @NonNull String desc) {
        for(Map<String,String> map:linkMapArray){
            if(map.get(KEY_URL).equals(link) && map.get(KEY_TITLE).equals(desc)){
                return;
            }
        }
        Map<String,String> linkMap = new HashMap();
        linkMap.put(KEY_URL, link);
        linkMap.put(KEY_TITLE, desc);
        linkMapArray.add(linkMap);
    }

    // Remove all span in selection, not like the boldInvalid()
    protected void linkInvalid(int start, int end) {
        if (start >= end) {
            return;
        }

        URLSpan[] spans = getEditableText().getSpans(start, end, URLSpan.class);
        for (URLSpan span : spans) {
            getEditableText().removeSpan(span);
        }
        styleInvalid(Typeface.BOLD, start, end);
        underlineInvalid(start, end);
    }

    public boolean containLink() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                URLSpan[] before = getEditableText().getSpans(start - 1, start, URLSpan.class);
                URLSpan[] after = getEditableText().getSpans(start, start + 1, URLSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, URLSpan.class).length > 0) {
                    return true;
                }
            }
            return false;
        }
    }

    protected boolean containLink(int start, int end) {
        if (start > end) {
            return false;
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > getEditableText().length()) {
                return false;
            } else {
                URLSpan[] before = getEditableText().getSpans(start - 1, start, URLSpan.class);
                URLSpan[] after = getEditableText().getSpans(start, start + 1, URLSpan.class);
                return before.length > 0 && after.length > 0;
            }
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (getEditableText().getSpans(i, i + 1, URLSpan.class).length > 0) {
                    builder.append(getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return getEditableText().subSequence(start, end).toString().equals(builder.toString());
        }
    }

    // Redo/Undo ===================================================================================

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        if (!historyEnable || historyWorking) {
            return;
        }

        inputBefore = new SpannableStringBuilder(text);

        if (count > 0 && count != after) {
            int seletionIndex = getSelectionStart();
            String textStr = getText().toString();
            //遍历link的array
            for (int index = 0; index < linkMapArray.size(); index++) {
                Map<String, String> linkMap = linkMapArray.get(index);
                String desc = linkMap.get(KEY_TITLE);
                int startIndex = textStr.indexOf(linkMap.get(KEY_TITLE));
                //找不到则remove
                if (startIndex < 0) {
                    linkMapArray.remove(linkMap);
                    index--;
                    continue;
                }
                int endIdex = startIndex + desc.length();
                //判断当前光标位置
                if (seletionIndex > startIndex && seletionIndex <= endIdex) {
                    CharacterStyle[] spans = getText().getSpans(startIndex, endIdex, CharacterStyle.class);
                    for (CharacterStyle span : spans) {
                        if (span instanceof RichURLSpan) {
                            URLSpan[] urlSpens = getEditableText().getSpans(startIndex, endIdex, URLSpan.class);
                            for (URLSpan urlSpan : urlSpens) {
                                getEditableText().removeSpan(urlSpan);
                            }
                            setText(text);
                            setSelection(seletionIndex);
                            linkMapArray.remove(linkMap);
                            index--;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable text) {
        if (!historyEnable || historyWorking) {
            return;
        }

        inputLast = new SpannableStringBuilder(text);
        if (text != null && text.toString().equals(inputBefore.toString())) {
            return;
        }

        if (historyList.size() >= historySize) {
            historyList.remove(0);
        }

        historyList.add(inputBefore);
        historyCursor = historyList.size();
    }

    public void redo() {
        if (!redoValid()) {
            return;
        }

        historyWorking = true;

        if (historyCursor >= historyList.size() - 1) {
            historyCursor = historyList.size();
            setText(inputLast);
        } else {
            historyCursor++;
            setText(historyList.get(historyCursor));
        }

        setSelection(getEditableText().length());
        historyWorking = false;
    }

    public void undo() {
        if (!undoValid()) {
            return;
        }

        historyWorking = true;

        historyCursor--;
        setText(historyList.get(historyCursor));
        setSelection(getEditableText().length());

        historyWorking = false;
    }

    public boolean redoValid() {
        if (!historyEnable || historySize <= 0 || historyList.size() <= 0 || historyWorking) {
            return false;
        }

        return historyCursor < historyList.size() - 1 || historyCursor >= historyList.size() - 1 && inputLast != null;
    }

    public boolean undoValid() {
        if (!historyEnable || historySize <= 0 || historyWorking) {
            return false;
        }

        if (historyList.size() <= 0 || historyCursor <= 0) {
            return false;
        }

        return true;
    }

    public void clearHistory() {
        if (historyList != null) {
            historyList.clear();
        }
    }

    // Helper ======================================================================================

    public boolean contains(int format) {
        switch (format) {
            case FORMAT_BOLD:
                return containStyle(Typeface.BOLD, getSelectionStart(), getSelectionEnd());
            case FORMAT_ITALIC:
                return containStyle(Typeface.ITALIC, getSelectionStart(), getSelectionEnd());
            case FORMAT_UNDERLINED:
                return containUnderline(getSelectionStart(), getSelectionEnd());
            case FORMAT_STRIKETHROUGH:
                return containStrikethrough(getSelectionStart(), getSelectionEnd());
            case FORMAT_BULLET:
                return containBullet();
            case FORMAT_QUOTE:
                return containQuote();
            case FORMAT_LINK:
                return containLink(getSelectionStart(), getSelectionEnd());
            case FORMAT_CENTER:
//                return containCenter(getCenterSelectionStart(),getCenterSelectionEnd());
                return containCenterFormat();
            default:
                return false;
        }
    }

    public void clearFormats() {
        setText(getEditableText().toString());
        setSelection(getEditableText().length());
    }

    public void hideSoftInput() {
        clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void showSoftInput() {
        requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void fromHtml(String source) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        //------------
        builder.append(RichParser.fromHtml(source));
        switchToKnifeStyle(builder, 0, builder.length());
        //------------
        setText(builder);
    }

    public String toHtml() {
        return RichParser.toHtml(getEditableText());
    }

    protected void switchToKnifeStyle(Editable editable, int start, int end) {
        AlignmentSpan[] alignSpans = editable.getSpans(start, end, AlignmentSpan.class);
        for(int index = 0 ; index < alignSpans.length ; alignSpans = editable.getSpans(start, end, AlignmentSpan.class),index ++){
            AlignmentSpan span = alignSpans[index];
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            if(0 <= spanStart - 1 && spanStart < editable.length() && editable.charAt(spanStart -1) == '\n'){
                editable.delete(spanStart - 1,spanStart);
                spanStart--;
                spanEnd--;
                end--;
            }
            if(0 < spanEnd && spanEnd + 1 <= editable.length() && editable.charAt(spanEnd) == '\n'){
                editable.delete(spanEnd,spanEnd + 1);
                end--;
            }else if(0 < spanEnd && spanEnd - 1 < editable.length() && editable.charAt(spanEnd - 1) == '\n'){
                editable.delete(spanEnd - 1,spanEnd);
                spanEnd--;
                end--;
            }
            editable.removeSpan(span);
            editable.setSpan(new RichCenterAlignmentSpan(), spanStart, spanEnd,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        BulletSpan[] bulletSpans = editable.getSpans(start, end, BulletSpan.class);
        for (BulletSpan span : bulletSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            spanEnd = 0 < spanEnd && spanEnd < editable.length() && editable.charAt(spanEnd) == '\n' ? spanEnd - 1 : spanEnd;
            editable.removeSpan(span);
            editable.setSpan(new RichBulletSpan(bulletColor, bulletRadius, bulletGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        QuoteSpan[] quoteSpans = editable.getSpans(start, end, QuoteSpan.class);
        for (QuoteSpan span : quoteSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            spanEnd = 0 < spanEnd && spanEnd < editable.length() && editable.charAt(spanEnd) == '\n' ? spanEnd - 1 : spanEnd;
            editable.removeSpan(span);
            editable.setSpan(new RichQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        URLSpan[] urlSpans = editable.getSpans(start, end, URLSpan.class);
        for (URLSpan span : urlSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);
            editable.removeSpan(span);
            editable.setSpan(new RichURLSpan(span.getURL(), linkColor, linkUnderline), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        final String text = getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (onSelectContainsType != null)
            onSelectContainsType.onSelectBold(contains(FORMAT_BOLD));
        if (onSelectContainsType != null)
            onSelectContainsType.onSelectUnderline(contains(FORMAT_UNDERLINED));
        if (onSelectContainsType != null)
            onSelectContainsType.onSelecrCenter(contains(FORMAT_CENTER));
        int textLength = text.length();

        if (selStart >= 0 && selStart <= textLength) {
            //遍历link的array
            for (int index = 0; index < linkMapArray.size(); index++) {
                Map<String, String> linkMap = linkMapArray.get(index);
                String desc = linkMap.get(KEY_TITLE);
                String url = linkMap.get(KEY_URL);
                String textTemp = text;
                int defatultStart = 0;
                while (textTemp.indexOf(desc) >= 0 && defatultStart < text.length()){
                    int startIndex = textTemp.indexOf(desc);
                    int endIndesc = startIndex + desc.length();
//                    Log.i("tzy", "desc = " + desc);
//                    Log.i("tzy", "selStart = " + selStart + " ; selEnd = " + selEnd);

                    int realStartIndex = startIndex + defatultStart;
                    int realEndIndex = endIndesc + defatultStart;
//                    Log.i("tzy", "realStartIndex = " + realStartIndex);
//                    Log.i("tzy", "realEndIndex = " + realEndIndex);
                    //判断当前光标位置
                    if (selStart == selEnd){
                        if(selStart > realStartIndex && selStart < realEndIndex
                                && selEnd > realStartIndex && selEnd < realEndIndex) {
                            if(containsSpan(realStartIndex,realEndIndex,textLength,desc,url))
                                return;
                        }
                    } else if(selStart != selEnd){
                        if(selStart >= realStartIndex && selStart <= realEndIndex
                                && selEnd >= realStartIndex && selEnd <= realEndIndex) {
                            if(containsSpan(realStartIndex,realEndIndex,textLength,desc,url))
                                return;
                        }
                    }
                    textTemp = textTemp.substring(startIndex + desc.length() , textTemp.length());
                    defatultStart += startIndex + desc.length();
                }
            }
        }
    }

    private boolean containsSpan(int startIndex,int endIndex,int textLength,String desc,String url){
        CharacterStyle[] spans = getText().getSpans(startIndex, endIndex, CharacterStyle.class);
        for (CharacterStyle span : spans) {
            if (span instanceof RichURLSpan) {
                if (((RichURLSpan)span).getURL().equals(url) && onSelectContainsType != null) {
                    onSelectContainsType.onSelectLink(url, desc);
                    this.setSelection(endIndex + 1 <= textLength ? endIndex + 1 : textLength );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean showContextMenu() {
        boolean flag = super.showContextMenu();
//        Log.i("tzy","showContextMenu");
//        onSelectionChanged(getSelectionStart(),getSelectionEnd());
        return flag;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        switch (id) {
            case android.R.id.cut:
                //剪切
                break;
            case android.R.id.copy:
                //复制
                break;
            case android.R.id.paste:
                //粘帖
                final ClipboardManager manager = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);
                String pasteText = getClipFirstText(manager);
//                Log.i("tzy", "copied text: " + pasteText);
                pasteText = TextUtils.isEmpty(pasteText) ? "" : pasteText;
                ClipData clip = ClipData.newPlainText("simple text copy", pasteText);
                manager.setPrimaryClip(clip);
                break;
        }

        return super.onTextContextMenuItem(id);
    }

    private String getClipFirstText(ClipboardManager manager) {
        String addedText = "";
        if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
            addedText = manager.getPrimaryClip().getItemAt(0).coerceToText(getContext()) + "";
        }
        return addedText;
    }

    public List<Map<String, String>> getLinkMapArray() {
        return linkMapArray;
    }

    public void putLinkMapArray(List<Map<String, String>> urls){
        this.linkMapArray.addAll(urls);
        setSelection(0);
    }

    private OnSelectContainsType onSelectContainsType;

    public void setOnSelectTypeCallback(OnSelectContainsType onSelectContainsType) {
        this.onSelectContainsType = onSelectContainsType;
    }

    public interface OnSelectContainsType {
        public void onSelectBold(boolean isSelected);

        public void onSelectUnderline(boolean isSelected);

        public void onSelectLink(String url, String desc);

        public void onSelecrCenter(boolean isSelected);
    }
}
