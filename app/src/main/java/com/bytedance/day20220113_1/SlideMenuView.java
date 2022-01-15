package com.bytedance.day20220113_1;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * 仿QQ消息的侧滑菜单
 * 左滑可置顶消息、删除消息、已读消息
 */
public class SlideMenuView extends ViewGroup {


    /**
     * 第一个View：消息内容
     */
    private View contentView;

    /**
     * 第二个View：侧滑内容
     */
    private View editView;

    /**
     * 事件监听接口
     */
    private OnEditClickListener onEditClickListener;

    /**
     * 已读按钮
     */
    private TextView buttonRead;

    /**
     * 置顶按钮
     */
    private TextView buttonTop;

    /**
     * 删除按钮
     */
    private TextView buttonDelete;

    /**
     * 处理时down事件的x坐标
     */
    private float downX;

    /**
     * 处理时down事件的y坐标
     */
    private float downY;

    /**
     * 处理时move事件的x坐标
     */
    private float moveX;

    /**
     * 处理时move事件的y坐标
     */
    private float moveY;

    /**
     * 处理时up事件的x坐标
     */
    private float upX;

    /**
     * 处理时up事件的y坐标
     */
    private float upY;

    /**
     * Scroller工具类对象
     */
    private Scroller scroller;

    /**
     * 动画（打开、关闭）持续时间
     */
    private int duration = 500;

    /**
     * 侧滑菜单状态（是否打开）
     */
    private boolean isOpen = false;

    /**
     * 侧滑方向
     */
    private Direction direction = Direction.NONE;

    /**
     * 拦截时down事件的x坐标
     */
    private float interceptDownX;

    /**
     * 拦截时down事件的y坐标
     */
    private float interceptDownY;

    /**
     * 拦截时move事件的x坐标
     */
    private float interceptMoveX;

    /**
     * 拦截时move事件的y坐标
     */
    private float interceptMoveY;

    /**
     * 侧滑方向枚举类
     */
    enum Direction {
        LEFT, RIGHT, NONE,
    }

    /**
     * 构造方法1
     *
     * @param context
     */
    public SlideMenuView(Context context) {
        this(context, null);
    }

    /**
     * 构造方法2
     *
     * @param context
     * @param attrs
     */
    public SlideMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法3
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SlideMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
    }

    /**
     * 当View和他的所有子控件被XML布局文件填充完成时被调用，是在setContentView()方法加载解析布局文件完毕之后执行这个方法。
     * 如果是一个ViewGroup，只有它和它的子View完全被加载实例化了之后才会回调该ViewGroup的这个方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 获取第一个子View（消息内容）
        contentView = getChildAt(0);
        // 根据属性，添加子view（侧滑内容）
        editView = LayoutInflater.from(getContext()).inflate(R.layout.item_slide_action, this, false);
        addView(editView);
        // 初始化View
        initView();
        // 初始化事件
        initAction();
    }

    /**
     * 初始化View
     */
    private void initView() {
        buttonRead = editView.findViewById(R.id.button_read);
        buttonTop = editView.findViewById(R.id.button_top);
        buttonDelete = editView.findViewById(R.id.button_delete);
    }

    /**
     * 初始化事件
     */
    private void initAction() {
        buttonRead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditClickListener.onReadClick();
                close();
            }
        });
        buttonTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditClickListener.onTopClick();
                close();
            }
        });
        buttonDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditClickListener.onDeleteClick();
                close();
            }
        });
    }

    /**
     * 测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 测量第一个子View：消息内容
        // 宽度与父控件一只，高度由子View决定（指定值、match_parent、wrap_parent）
        LayoutParams layoutParams = contentView.getLayoutParams();
        int contentHeight = layoutParams.height;
        int contentHeightMeasureSpec;
        if (contentHeight == layoutParams.MATCH_PARENT) {
            contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
        } else if (contentHeight == layoutParams.WRAP_CONTENT) {
            contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST);
        } else {
            contentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
        }
        contentView.measure(widthMeasureSpec, contentHeightMeasureSpec);
        int contentMeasureHeight = contentView.getMeasuredHeight();

        // 测量第二个子View：侧滑内容
        // 宽度占父控件的3/4，高度与第一个子View（消息内容）高度一致
        int editWidth = parentWidth * 3 / 4;
        editView.measure(MeasureSpec.makeMeasureSpec(editWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(contentMeasureHeight, MeasureSpec.EXACTLY));

        // 测量自己
        // 宽度为第一个子View与第二个子View之和，高度与第一个子View（消息内容）高度一致
        setMeasuredDimension(parentWidth + editWidth, contentMeasureHeight);

    }

    /**
     * 布局
     *
     * @param b
     * @param i
     * @param i1
     * @param i2
     * @param i3
     */
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        //摆放第一个子View（消息内容）
        int contentViewLeft = 0;
        int contentViewTop = 0;
        int contentViewRight = contentViewLeft + contentView.getMeasuredWidth();
        int contentViewBottom = contentViewTop + contentView.getMeasuredHeight();
        contentView.layout(contentViewLeft, contentViewTop, contentViewRight, contentViewBottom);

        //摆放第二个子View（侧滑内容）
        int editViewLeft = contentViewRight;
        int editViewTop = contentViewTop;
        int editViewRight = editViewLeft + editView.getMeasuredWidth();
        int editViewBottom = editViewTop + editView.getMeasuredHeight();
        editView.layout(editViewLeft, editViewTop, editViewRight, editViewBottom);
    }

    /**
     * 事件拦截
     * 如果横向滑动，则拦截事件，由当前ViewGroup消费事件。否则不拦截事件，分发给子View消费
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                interceptDownX = ev.getX();
                interceptDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                interceptMoveX = ev.getX();
                interceptMoveY = ev.getY();
                // 横向滑动，则当前ViewGroup消费事件
                if (Math.abs(interceptMoveX - interceptDownX) > Math.abs(interceptMoveY - interceptDownY)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 处理触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX();
                moveY = event.getY();
                // 移动的差值
                int dx = (int) (moveX - downX);
                if (dx > 0) {
                    direction = Direction.RIGHT;
                } else if (dx < 0) {
                    direction = Direction.LEFT;
                }
                // 更新坐标，以便后续move事件计算移动的差值
                downX = moveX;
                downY = moveY;
                // 边界处理（在移动前处理）
                int scrollX = getScrollX();
                int resultScrollX = scrollX - dx;
                if (resultScrollX <= 0) { //过度右滑
                    scrollTo(0, 0);
                } else if (resultScrollX > editView.getMeasuredWidth()) { //过度左滑
                    scrollTo(editView.getMeasuredWidth(), 0);
                } else {
                    // 使用差值，移动屏幕（相当于向反方向移动子View）
                    scrollBy(-dx, 0);
                }
                // 重新摆放子View
                // contentViewLeft+=dx;
                // 请求重新布局（调用onLayout方法）
                // requestLayout();
                break;
            case MotionEvent.ACTION_UP:
                upX = event.getX();
                upY = event.getY();
                // 实现回弹动效
                // 关闭状态下：
                //     左滑：如果滑动值超过侧滑菜单宽度的1/4，则打开。否则回弹关闭
                //     右滑：关闭
                // 打开状态下：
                //     左滑：打开
                //     右滑：如果滑动值小于侧滑菜单宽度的3/4，则关闭。否则回弹打开
                int hasBeenScrolledX = getScrollX();
                if (isOpen) {
                    if (direction == Direction.RIGHT) {
                        if (hasBeenScrolledX <= editView.getMeasuredWidth() * 3 / 4) {
                            close();
                        } else {
                            open();
                        }
                    } else if (direction == Direction.LEFT) {
                        open();
                    }
                } else {
                    if (direction == Direction.LEFT) {
                        if (hasBeenScrolledX > editView.getMeasuredWidth() / 4) {
                            open();
                        } else {
                            close();
                        }

                    } else if (direction == Direction.RIGHT) {
                        close();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 使用Scroller工具类，重写computeScroll方法
     */
    @Override
    public void computeScroll() {
        // 判断滑动过程是否结束 && 更新当前应该滑动到的坐标点
        if (scroller.computeScrollOffset()) {
            // 滑动到某个位置
            scrollTo(scroller.getCurrX(), 0);
            // 请求重绘（调用draw方法--->draw中调用computeScroll方法）
            invalidate();
        }
    }

    /**
     * 打开侧滑菜单
     */
    private void open() {
        //scrollTo(editView.getMeasuredWidth(), 0);
        scroller.startScroll(getScrollX(), 0, editView.getMeasuredWidth() - getScrollX(), 0, duration);
        isOpen = true;
        invalidate();
    }

    /**
     * 关闭侧滑菜单
     */
    private void close() {
        //scrollTo(0, 0);
        scroller.startScroll(getScrollX(), 0, -getScrollX(), 0, duration);
        isOpen = false;
        invalidate();
    }

    /**
     * 设置事件监听接口
     *
     * @param onEditClickListener
     */
    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    /**
     * 事件监听
     */
    public interface OnEditClickListener {
        // 已读按钮监听
        void onReadClick();

        // 置顶按钮监听
        void onTopClick();

        // 删除按钮监听
        void onDeleteClick();
    }
}
