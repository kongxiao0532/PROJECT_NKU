package com.kongx.nkuassistant;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by kongx on 2016/12/7 0007.
 */

public class ListViewNoScroll extends ListView {
    public ListViewNoScroll(Context context) {
        super(context);
    }

    public ListViewNoScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
