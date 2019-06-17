package com.example.karaoke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LyricView extends View {
    public final static String TAG = "LyricView";



    /**
     * 所有歌詞
     */
    private List<LyricRow> mLrcRows = new ArrayList<LyricRow>();

    /**Highlight歌詞行數     */
    private int mHighLightRow = 0;
    /**Highlight歌詞顏色     */
    private int mHighLightRowColor = Color.YELLOW;
    /**其他歌詞顏色     */
    private int mNormalRowColor = Color.WHITE;


    /**歌詞字體大小預設值*/
    private int mLrcFontSize = 50;

    /**Highlight歌詞字體大小預設值*/
    private int mHighLightFontSize = 65;

    /**歌詞間距     **/
    private int mPaddingY = 20;


    private Paint mPaint;
    private Context mContext;

    /**
     *  當前時間
     */
    long currentMillis;


    public LyricView(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mLrcFontSize);
        Typeface font = Typeface.createFromAsset(mContext.getAssets(),"fonts/cfont.ttf");
        mPaint.setTypeface(font);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int width = getWidth();

        int rowY = 0;
        final int rowX = width/2;
        int rowNum = 0;


        //Highlight歌詞
        int highlightRowY = height/2 - mLrcFontSize;
        drawKaraokeHighLightLrcRow(canvas, rowX, width, highlightRowY);


        mPaint.setColor(mNormalRowColor);
        mPaint.setTextSize(mLrcFontSize);
        mPaint.setTextAlign(Paint.Align.CENTER);

        //其餘歌詞
        rowNum = mHighLightRow - 1;
        rowY = highlightRowY - mPaddingY - mLrcFontSize;
        //顯示前一句歌詞
        if (rowY > -mLrcFontSize && rowNum >= 0) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
        }


        rowNum = mHighLightRow + 1;
        rowY = highlightRowY + mPaddingY + mLrcFontSize;


        //顯示後面的歌詞
        while (rowY < height && rowNum < mLrcRows.size()) {
            String text = mLrcRows.get(rowNum).content;
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY += (mPaddingY + mLrcFontSize);
            rowNum++;
        }

    }

    private void drawKaraokeHighLightLrcRow(Canvas canvas, int rowX, int width, int highlightRowY) {
        LyricRow highLrcRow = mLrcRows.get(mHighLightRow);
        String highlightText = highLrcRow.content;

        // 普通顏色
        mPaint.setColor(mNormalRowColor);
        mPaint.setTextSize(mHighLightFontSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

        // 逐字highlight
        int highLineWidth = (int) mPaint.measureText(highlightText);
        long start = highLrcRow.getStartTime();
        long end = highLrcRow.getEndTime();

        // highlight句子長度
        int highWidth = (int) ((currentMillis - start) * 2.0f / (end - start) * highLineWidth);
        if (highWidth > 0) {
            mPaint.setColor(mHighLightRowColor);
            mPaint.setTextSize(mHighLightFontSize);
            Bitmap textBitmap = Bitmap.createBitmap(highWidth, highlightRowY + mPaddingY, Bitmap.Config.ARGB_8888);
            Canvas textCanvas = new Canvas(textBitmap);
            textCanvas.drawText(highlightText, highLineWidth/2, highlightRowY, mPaint);
            canvas.drawBitmap(textBitmap, (width-highLineWidth)/2, 0, mPaint);
        }
    }



    public void seekLrc(int position) {
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()) {
            return;
        }
        mHighLightRow = position;
        invalidate();

    }


    public void setLrc(List<LyricRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    /**
     * 滾動歌詞
     */
    public void seekLrcToTime(long time) {

        currentMillis = time;

        for (int i = 0; i < mLrcRows.size(); i++) {
            LyricRow current = mLrcRows.get(i);
            LyricRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if ((time >= current.startTime && next != null && time < next.startTime)
                    || (time > current.startTime && next == null)) {
                seekLrc(i);
                return;
            }
        }
    }
}
