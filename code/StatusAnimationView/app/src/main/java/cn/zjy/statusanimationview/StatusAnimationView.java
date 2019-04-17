package cn.zjy.statusanimationview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import static cn.zjy.statusanimationview.Utils.dpToPixel;

public class StatusAnimationView extends View {

    enum Status{
        SUCCESS,
        FAIL
    }

    Status status = Status.FAIL;

    //追踪Path的坐标
    private PathMeasure pathMeasure;
    private float circleRadius;
    private float circleWidth;
    private RectF arcRectF = new RectF();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path successPath = new Path();
    private Path failLeftPath = new Path();
    private Path failRightPath = new Path();
    private Path path = new Path();
    float mLength;
    float progress = 0;
    float centerX;
    float centerY;
    private float successValue;
    private float failLeftValue;
    private float failRightValue;
    private int successColor;
    private int failColor;
    private AnimatorSet animatorSet;
    private ObjectAnimator arcAnimator ;
    private ValueAnimator successAnimator;
    private ValueAnimator failLeftAnimator;
    private ValueAnimator failRightAnimator;

    public StatusAnimationView(Context context) {
        super(context);
    }

    public StatusAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.StatusAnimationView);
        int s = typedArray.getInteger(R.styleable.StatusAnimationView_sav_status,0);
        circleRadius =typedArray.getDimension(R.styleable.StatusAnimationView_sav_circle_radius,50);
        circleWidth = typedArray.getDimension(R.styleable.StatusAnimationView_sav_circle_width,2);

        successColor = typedArray.getColor(R.styleable.StatusAnimationView_sav_success_color,Color.parseColor("#03a44e"));
        failColor = typedArray.getColor(R.styleable.StatusAnimationView_sav_fail_color,Color.RED);
        typedArray.recycle();

        switch (s){
            case 0:
                status = Status.SUCCESS;
                break;
            case 1:
                status = Status.FAIL;
                break;
        }
        initPaint();
        initAnimation();
    }


    private void initPaint() {
        paint.setStrokeJoin(Paint.Join.ROUND);
        pathMeasure = new PathMeasure();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = (int)(circleRadius*2 + circleWidth*2 + getPaddingLeft() +getPaddingRight());
        int height = (int)(circleRadius*2 + circleWidth*2 + getPaddingTop() +getPaddingBottom());
        width = resolveSize(width,widthMeasureSpec);
        height = resolveSize(height,heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        if(status.equals(Status.SUCCESS)){
            paint.setColor(successColor);
        }else{
            paint.setColor(failColor);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(circleWidth);
        arcRectF.set(centerX - circleRadius, centerY - circleRadius, centerX + circleRadius, centerY + circleRadius);
        canvas.drawArc(arcRectF, 180, progress * 3.6f, false, paint);

        if(progress>=100) {
            if(status.equals(Status.SUCCESS)) {//画勾
                successPath.moveTo(centerX - circleRadius * 5 / 8, centerY);
                successPath.lineTo(centerX - circleRadius / 3, centerY + circleRadius / 2);
                successPath.lineTo(centerX + circleRadius * 2 / 3, centerY - circleRadius / 3);
                pathMeasure.nextContour();
                pathMeasure.setPath(successPath, false);
                mLength = pathMeasure.getLength();
                float starD = 0;
                float stopD = mLength * successValue;
                pathMeasure.getSegment(starD, stopD, path, true);
                canvas.drawPath(path, paint);
            }else{//画叉
                failLeftPath.moveTo(centerX - circleRadius / 2,centerY- circleRadius /2);
                failLeftPath.lineTo(centerX+ circleRadius /2,centerY+ circleRadius /2);
                pathMeasure.nextContour();
                pathMeasure.setPath(failLeftPath, false);
                mLength = pathMeasure.getLength();
                float starD = 0;
                float stopD = mLength * failLeftValue;
                pathMeasure.getSegment(starD, stopD, path, true);
                canvas.drawPath(path, paint);
                if(failLeftValue >= 1) {//左边画完了，开始画右边
                    failRightPath.moveTo(centerX + circleRadius / 2, centerY - circleRadius / 2);
                    failRightPath.lineTo(centerX - circleRadius / 2, centerY + circleRadius / 2);
                    pathMeasure.nextContour();
                    pathMeasure.setPath(failRightPath, false);
                    mLength = pathMeasure.getLength();
                    stopD = mLength * failRightValue;
                    pathMeasure.getSegment(starD, stopD, path, true);
                    canvas.drawPath(path, paint);
                }
            }
        }
    }

    void reset(){
        successValue = 0;
        failLeftValue = 0;
        failRightValue = 0;
        path.reset();
        successPath.reset();
        failLeftPath.reset();
        failRightPath.reset();
    }

    public void starAnim(){
        reset();
        animatorSet = new AnimatorSet();
        if(status.equals(Status.SUCCESS)) {
            animatorSet.play(successAnimator).after(arcAnimator);
        }else{
            animatorSet.playSequentially(arcAnimator,failLeftAnimator,failRightAnimator);
        }
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    private void initAnimation() {
        arcAnimator = ObjectAnimator.ofFloat(this,"progress",0f,100f);
        arcAnimator.setDuration(500);
        successAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        successAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                successValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        failLeftAnimator = ValueAnimator.ofFloat(0f,1.0f);
        failLeftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failLeftValue = (float)animation.getAnimatedValue();
                invalidate();
            }
        });
        failRightAnimator = ValueAnimator.ofFloat(0f,1.0f);
        failRightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                failRightValue = (float)animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCircleRadius(float circleRadius) {
        this.circleRadius = dpToPixel(circleRadius);
    }

    public void setCircleWidth(float circleWidth) {
        this.circleWidth = dpToPixel(circleWidth);
    }

    public void setSuccessColor(int successColor) {
        this.successColor = successColor;
    }

    public void setFailColor(int failColor) {
        this.failColor = failColor;
    }
}
