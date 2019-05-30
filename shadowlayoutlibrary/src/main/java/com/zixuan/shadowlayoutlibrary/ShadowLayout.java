package com.zixuan.shadowlayoutlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.graphics.Path;

import androidx.annotation.ColorInt;

import static java.lang.reflect.Array.getInt;

public class ShadowLayout extends FrameLayout {
    static final boolean debug = false;

    final static private int FLAG_SIDES_TOP = 1;
    final static private int FLAG_SIDES_RIGHT = 2;
    final static private int FLAG_SIDES_BOTTOM = 4;
    final static private int FLAG_SIDES_LEFT = 8;
    final static private int FLAG_SIDES_ALL = 15;

    @ColorInt public static int default_shadowColor = Color.BLACK;
    final static float default_shadowRadius = 0f;
    final static float default_dx = 0f;
    final static float default_dy = 0f;
    final static float default_cornerRadius = 0f;
    @ColorInt final static int default_borderColor = Color.RED;
    final static float default_borderWidth = 0f;
    final static int default_shadowSides = FLAG_SIDES_ALL;
    RectF  rectF ;
    private RectF mContentRF = null;

    public ShadowLayout(@androidx.annotation.NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        initDrawAttributes();
        processPadding();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public ShadowLayout(@androidx.annotation.NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        initDrawAttributes();
        processPadding();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public ShadowLayout(@androidx.annotation.NonNull Context context, @androidx.annotation.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttributes(context, attrs);
        initDrawAttributes();
        processPadding();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        try {
            if (a != null) {
                mShadowColor = a.getColor(R.styleable.ShadowLayout_sl_shadowColor, default_shadowColor);
                mShadowRadius =
                        a.getDimension(R.styleable.ShadowLayout_sl_shadowRadius, default_shadowRadius);
                mDx = a.getDimension(R.styleable.ShadowLayout_sl_dx, default_dx);
                mDy = a.getDimension(R.styleable.ShadowLayout_sl_dy, default_dy);

                mCornerRadius =
                        a.getDimension(R.styleable.ShadowLayout_sl_cornerRadius, default_cornerRadius);
                mBorderColor = a.getColor(R.styleable.ShadowLayout_sl_borderColor, default_borderColor);
                mBorderWidth =
                        a.getDimension(R.styleable.ShadowLayout_sl_borderWidth, default_borderWidth);

                mShadowSides = a.getInt(R.styleable.ShadowLayout_sl_shadowSides, default_shadowSides);
                Log.d("MyTag", "initAttributes: ");
            }
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }finally {
           if(a != null){
               a.recycle();
           }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mContentRF = new RectF(
                (float)getPaddingLeft(),
                (float)getPaddingTop(),
                (float)(w - getPaddingRight()),
                (float)(h - getPaddingBottom())
        );

        //以边框宽度的三分之一，微调边框绘制位置，以在边框较宽时得到更好的视觉效果
        final float bw = mBorderWidth / 3;
        if (bw > 0) {
            mBorderRF = new RectF(mContentRF.left + bw, mContentRF.top + bw, mContentRF.right - bw,
                    mContentRF.bottom - bw);
    }
    }

    /**
     * 阴影颜色
     */
    @ColorInt
    private int mShadowColor = 0;
    /**
     * 阴影发散距离 blur
     */
    private float mShadowRadius= 0f;
    /**
     * x轴偏移距离
     */
    private float mDx= 0f;
    /**
     * y轴偏移距离
     */
    private float mDy= 0f;
    /**
     * 圆角半径
     */
    private float mCornerRadius = 0f;
    /**
     * 边框颜色
     */
    @ColorInt
    private int mBorderColor = 0;
    /**
     * 边框宽度
     */
    private float mBorderWidth = 0f;
    /**
     * 控制四边是否显示阴影
     */
    private int mShadowSides = default_shadowSides;

    //********************************
    //* 绘制使用的属性部分
    //********************************

    /**
     * 全局画笔
     */
    private Paint mPaint = new Paint(Color.WHITE);
    private Paint mHelpPaint = new Paint(Color.RED);
    private Path mPath = new Path();
    private PorterDuffXfermode mXfermode =  null;
    private RectF mBorderRF = null;

    public ShadowLayout(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (canvas == null) {
            return;
        }
//        canvas.helpGreenCurtain(debug);

        //绘制阴影
        drawShadow(canvas);

        //绘制子View
        drawChild(canvas);



        //绘制边框
        drawBorder(canvas);
    }
    private void drawBorder(Canvas canvas) {
        if (mBorderRF != null) {
            canvas.save();

            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mBorderColor);
            canvas.drawRoundRect(mBorderRF, mCornerRadius, mCornerRadius, mPaint);
            utilReset(mPaint);
//            mPaint.reset();

//            mPaint.utilReset();

            canvas.restore();
        }
    }
    private void utilReset(Paint paint){
        paint.reset();

        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setAntiAlias(true);
//        paint.isAntiAlias = true
        paint.setStyle(Paint.Style.FILL);
//        paint.setStyle = Paint.Style.FILL;
        paint.setStrokeWidth(0f);
    }
    public void drawShadow(Canvas canvas){
        canvas.save();


        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setShadowLayer(10,10,10,Color.BLUE);
        mPaint.setShadowLayer(mShadowRadius,mDx,mDy,mShadowColor);
//        if(mContentRF != null){
        canvas.drawRoundRect(mContentRF,mCornerRadius,mCornerRadius,mPaint);
        utilReset(mPaint);
//        mPaint.reset();

        canvas.restore();
//        }
    }
    private void drawChild(Canvas canvas){
        canvas.saveLayer(0f, 0f, canvas.getWidth(), canvas.getHeight(), mPaint, Canvas.ALL_SAVE_FLAG);

        //先绘制子控件
        super.dispatchDraw(canvas);

        //使用path构建四个圆角

            mPath.addRect(
                    mContentRF,
                    Path.Direction.CW
            );
            mPath.addRoundRect(
                    mContentRF,
                    mCornerRadius,
                    mCornerRadius,
                    Path.Direction.CW
            );
            mPath.setFillType(Path.FillType.EVEN_ODD);


        //使用xfermode在图层上进行合成，处理圆角
        mPaint.setXfermode (mXfermode);
        canvas.drawPath(mPath, mPaint);
//        mPaint.utilReset();
        utilReset(mPaint);
//        mPaint.reset();
        mPath.reset();

        canvas.restore();

    }
    private void initDrawAttributes() {
        //使用xfermode在图层上进行合成，处理圆角
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }
    /**
     * 处理View的Padding为阴影留出空间
     */
    private void processPadding() {
        int xPadding = (int)(mShadowRadius + getAbs(mDx));
        int yPadding = (int)(mShadowRadius + getAbs(mDy));

        setPadding(getPaddingOrNot(containsFlag(mShadowSides,FLAG_SIDES_LEFT),yPadding),
        getPaddingOrNot(containsFlag(mShadowSides,FLAG_SIDES_TOP),yPadding),
        getPaddingOrNot(containsFlag(mShadowSides,FLAG_SIDES_RIGHT),yPadding),
        getPaddingOrNot(containsFlag(mShadowSides,FLAG_SIDES_BOTTOM),yPadding)
        );
    }
    private boolean containsFlag(int a,int b){
        return (a|b) == a;
    }
    private int getPaddingOrNot(Boolean flag,int Padding){
        if(flag){
            return Padding;
        }
        else {
            return 0;
        }
    }
    private float getAbs(float fl){
        if(fl < 0){
            return -fl;
        }
        else {
            return fl;
        }
    }
}
