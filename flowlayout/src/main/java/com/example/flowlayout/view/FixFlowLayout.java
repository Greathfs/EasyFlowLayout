package com.example.flowlayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FixFlowLayout extends FlowLayout {
    private static final String TAG = "FlowLayout";
    private static final int[] LL = new int[]{android.R.attr.maxLines};

    protected List<List<View>> mAllViews = new ArrayList<>();
    protected List<Integer> mLineHeights = new ArrayList<>();

    private int mMaxLines;

    public FixFlowLayout(Context context) {
        super(context);
    }

    public FixFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, LL);
        mMaxLines = a.getInt(0, Integer.MAX_VALUE);
        a.recycle();
    }

    /**
     * 1.flowlayout
     * 宽度：一定确定的
     * 高度：wrapcontent，exactly，unspe
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mAllViews.clear();
        mLineHeights.clear();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //widthMeasureSpec
        // 建议宽度 + mode
        // 1. 300dp +exactly 确定
        // 2. parent width ,at_most
        // 3. 0dp,parent with + unspecified 一般在滚动view中 scrollview

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //当前行宽
        int lineWidth = 0;
        //当前行高
        int lineHeight = 0;
        int height = 0;


        int cCount = getChildCount();
        //未换行一行显示多少个View
        List<View> lineViews = new ArrayList<>();

        //拿到当前所有 child 需要占据的高度，设置给我们的容器
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;

            }

            // child 也要确定宽高
            // child mode + size
            // 1. xml 里面写10dp，match_parent,wrap_content
            // 2. 父控件当前的 mode
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            //child宽
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //child高
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //换行处理
            if (lineWidth + childWidth > sizeWidth - (getPaddingLeft() + getPaddingRight())) {
                //假设第二个固定不换行
                if (i == 1) {
                    lp.width = sizeWidth - lp.leftMargin - lp.rightMargin - child.getPaddingLeft() - child.getPaddingRight() - lineWidth;
                    child.setLayoutParams(lp);
                    lineWidth = sizeWidth;
                    lineHeight = Math.max(lineHeight, childHeight);
                    lineViews.add(child);
                    continue;
                }

                height += lineHeight;

                mLineHeights.add(lineHeight);

                mAllViews.add(lineViews);
                //重新初始化
                lineViews = new ArrayList<>();
                //添加当前view
                lineViews.add(child);

                //重置
                lineWidth = childWidth;
                lineHeight = childHeight;

            } else {
                //未换行
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);

                lineViews.add(child);

            }

            //最后一行
            if (i == cCount - 1) {
                height += lineHeight;
                mLineHeights.add(lineHeight);
                mAllViews.add(lineViews);
            }
        }

        //maxLines校正
        if (mMaxLines < mLineHeights.size()) {
            height = getMaxLinesHeight();
        }

        //可以前移优化
        if (modeHeight == MeasureSpec.EXACTLY) {
            height = sizeHeight;
        } else if (modeHeight == MeasureSpec.AT_MOST) {
            height = Math.min(sizeHeight, height);
            height = height + getPaddingTop() + getPaddingBottom();
        } else if (modeHeight == MeasureSpec.UNSPECIFIED) {
            height = height + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(sizeWidth, height);

    }

    protected int getMaxLinesHeight() {

        int height = 0;

        for (int i = 0; i < mMaxLines; i++) {
            height += mLineHeights.get(i);
        }

        return height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //总宽度
        int totalWith = getWidth();
        // 摆放 view
        int left = getPaddingLeft();
        int top = getPaddingTop();
        //总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            //当前行显示的view集合
            List<View> lineViews = mAllViews.get(i);
            //当前行的高度
            int lineHeight = mLineHeights.get(i);

            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                // left top right bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                if (i == 0 && j == 1) {
                    if (rc > totalWith - lp.rightMargin) {
                        rc = totalWith - lp.rightMargin;
                    }
                }
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);
                left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;

            }
            left = getPaddingLeft();
            top += lineHeight;
        }

    }

    //child 没有设置 layoutparams
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    //inflater 布局
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    //addView
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    //addView
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }
}
