package com.roymam.android.nilsplus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.InsetDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.roymam.android.notificationswidget.NotificationData;
import com.roymam.android.notificationswidget.NotificationsService;
import com.roymam.android.notificationswidget.R;
import com.roymam.android.notificationswidget.SettingsManager;
import com.roymam.android.nilsplus.ui.theme.Theme;
import com.roymam.android.nilsplus.ui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NPListView extends RelativeLayout implements ViewTreeObserver.OnPreDrawListener {
    private Callbacks callbacks;
    private View mPullDownView;
    private View mPullDownText;
    private View mReleaseText;
    private ViewGroup mView;
    private LayoutParams mViewParams;
    private Context mContext;
    private ViewGroup listViewContainer;
    private DotsSwipeView mDotsView;
    private Theme mTheme;
    private LayoutParams mlistViewContainerParams;
    private NotificationAdapter adapter;
    private ListView listView;
    private long mAnimationTime;
    private LayoutParams mlistViewParams;
    private Point mMaxPos;
    private Point mMaxSize;
    private ViewTreeObserver.OnPreDrawListener mPreDrawListener = null;
    private boolean mNotificationsStateSaved = false;
    private boolean mOnAnimation = false;

    public void updateSizeAndPosition(Point pos, Point size)
    {
        Point displaySize = NPViewManager.getDisplaySize(mContext);

        // set vertical alignment
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String yAlignment = prefs.getString(SettingsManager.VERTICAL_ALIGNMENT, SettingsManager.DEFAULT_VERTICAL_ALIGNMENT);

        mlistViewParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                size.y);

        // set & animate vertical position changes
        int itemsHeight = getItemsHeight();
        int deltay = size.y - itemsHeight;
        if (deltay < 0) deltay = 0;

        if (yAlignment.equals("center"))
        {
            deltay/=2;
        }
        else if (yAlignment.equals("bottom"))
        {
            // do nothing - deltay stays the same size
        }
        else
        {
            deltay = 0;
        }

        listView.animate().translationY(deltay).setDuration(mAnimationTime).setListener(null);
        mDotsView.updateSizeAndPosition(pos, size);

        listView.setLayoutParams(mlistViewParams);

        int leftMargin = pos.x;
        int  rightMargin = displaySize.x - size.x - pos.x;

        // update width for current list items
        for(int i=0; i<listView.getChildCount();i++)
        {
            View v = listView.getChildAt(i);
            View front = v.findViewById(R.id.front);
            LayoutParams params = (LayoutParams) front.getLayoutParams();
            params.rightMargin = rightMargin;
            params.leftMargin = leftMargin;
            front.setLayoutParams(params);
        }

        mlistViewContainerParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                size.y);

        mlistViewContainerParams.topMargin = pos.y;
        listViewContainer.setLayoutParams(mlistViewContainerParams);

        mMaxPos = pos;
        mMaxSize = size;

        // re-set divider
        if (mTheme.divider != null)
        {
            InsetDrawable divider = new InsetDrawable(mTheme.divider, leftMargin, 0, rightMargin, 0);
            listView.setDivider(divider);
        }

        // animation handling
        listView.getViewTreeObserver().addOnPreDrawListener(this);
    }

    public int getItemsHeight()
    {
        int itemsHeight = 0;
        for(int i=0; i<listView.getChildCount(); i++)
        {
            itemsHeight+=listView.getChildAt(i).getHeight();
        }
        // add spacing height
        if (listView.getChildCount() > 0)
            itemsHeight+= mTheme.notificationSpacing * (listView.getChildCount() - 1);

        return itemsHeight;
    }

    public void reloadAppearance()
    {
        // set the new theme
        mTheme = ThemeManager.getInstance(mContext).getCurrentTheme();

        // re-create notification adapter
        adapter = new NotificationAdapter(mContext);
        listView.setAdapter(adapter);

        // re-set divider
        if (mTheme.divider == null)
        {
            //noinspection ResourceType
            listView.setDivider(Resources.getSystem().getDrawable(android.R.color.transparent));
        }
        listView.setDividerHeight((int) mTheme.notificationSpacing);

        // re-position everything
        updateSizeAndPosition(mMaxPos,mMaxSize);
    }

    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
/*
    public void saveNotificationsState()
    {
        if (!mNotificationsStateSaved && !mOnAnimation) {
            // save position of notifications before making a change in the data
            final NotificationAdapter mAdapter = (NotificationAdapter) listView.getAdapter();

            mItemIdTopMap = new HashMap<Long, Integer>();
            ;

            int firstVisiblePosition = listView.getFirstVisiblePosition();
            Log.d("NiLS", "saveNotificationsState()");
            for (int i = 0; i < listView.getChildCount(); ++i) {
                View child = listView.getChildAt(i);
                int position = firstVisiblePosition + i;
                long itemId = mAdapter.getItemId(position);
                if (itemId != -1) {
                    Log.d("NiLS", "position:" + position + " id:" + itemId + " top:" + child.getTop());
                    mItemIdTopMap.put(itemId, child.getTop());
                }
            }

            mNotificationsStateSaved = true;
        }
        else
        {
            // ignore this call because previous animation hasn't ended yet
        }
    }

    public void animateNotificationsChange() {
        // animating moving of notifications in the list from their previous position to the current position
        final NotificationAdapter mAdapter = (NotificationAdapter) listView.getAdapter();
        final ViewTreeObserver observer = listView.getViewTreeObserver();

        // if a previous call hasn't been ended yet then do not call this again
        if (mPreDrawListener != null) return; //observer.removeOnPreDrawListener(mPreDrawListener);

        Log.d("NiLS", "animateNotificationsChange");

        // create pre draw listener
        if (mNotificationsStateSaved)
        {
            mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    try {
                        ViewTreeObserver observer = listView.getViewTreeObserver();
                        observer.removeOnPreDrawListener(mPreDrawListener);
                        mPreDrawListener = null;
                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                    ;

                    boolean firstAnimation = true;
                    int firstVisiblePosition = listView.getFirstVisiblePosition();
                    for (int i = 0; i < listView.getChildCount(); ++i) {
                        final View child = listView.getChildAt(i);
                        int position = firstVisiblePosition + i;
                        long itemId = mAdapter.getItemId(position);
                        Integer startTop = mItemIdTopMap.get(itemId);
                        int top = child.getTop();
                        if (startTop != null) {
                            Log.d("NiLS", "position:" + position + " id:" + itemId + " top:" + child.getTop() + " dest:" + startTop);
                            if (startTop != top) {
                                int delta = startTop - top;
                                child.setTranslationY(delta);
                                child.animate().setDuration(mAnimationTime).translationY(0).setListener(null);
                                if (firstAnimation) {
                                    mOnAnimation = true;
                                    child.animate().setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            listView.setEnabled(true);
                                            mOnAnimation = false;
                                        }
                                    });
                                    firstAnimation = false;
                                }
                            }
                        } else {
                            // Animate new views along with the others. The catch is that they did not
                            // exist in the start state, so we must calculate their starting position
                            // based on neighboring views.
                            Log.d("NiLS", "position:" + position + " id:" + itemId + " top: unknown");
                            int childHeight = child.getHeight() + listView.getDividerHeight();
                            startTop = top + (i > 0 ? childHeight : -childHeight);
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(mAnimationTime).translationY(0).setListener(null);
                            if (firstAnimation) {
                                mOnAnimation = true;
                                child.animate().setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        listView.setEnabled(true);
                                        mOnAnimation = false;
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    }
                    mItemIdTopMap.clear();
                    mNotificationsStateSaved = false;
                    return true;
                }
            };
            observer.addOnPreDrawListener(mPreDrawListener);
        }
    }
*/
    public void show()
    {
        listView.setAlpha(1);
        listView.setVisibility(View.VISIBLE);
        setVisibility(View.VISIBLE);

        // reset all list item appearance
        for(int i=0; i<listView.getChildCount(); i++)
        {
            View v = listView.getChildAt(i);
            v.setTranslationX(0);
            v.setAlpha(1);
        }
    }

    @Override
    public boolean onPreDraw()
    {
        final NotificationAdapter adapter = (NotificationAdapter) listView.getAdapter();

        boolean firstAnimation = true;
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        for (int i = 0; i < listView.getChildCount(); ++i) {
            final View child = listView.getChildAt(i);
            int position = firstVisiblePosition + i;
            final long itemId = adapter.getItemId(position);
            Integer startTop = mItemIdTopMap.get(itemId);
            int top = child.getTop();
            if (startTop != null) {
                if (startTop != top) {
                    int delta = startTop - top;
                    child.setTranslationY(delta);
                    child.animate().setDuration(mAnimationTime).translationY(0).setListener(null);
                    if (firstAnimation) {
                        child.animate().setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                listView.setEnabled(true);
                                mItemIdTopMap.put(itemId, child.getTop());
                            }
                        });
                        firstAnimation = false;
                    }
                }
            } else {
                // Animate new views along with the others. The catch is that they did not
                // exist in the start state, so we must calculate their starting position
                // based on neighboring views.
                int childHeight = child.getHeight() + listView.getDividerHeight();
                startTop = top + (i > 0 ? childHeight : -childHeight);
                int delta = startTop - top;
                child.setTranslationY(delta);
                child.animate().setDuration(mAnimationTime).translationY(0).setListener(null);
                if (firstAnimation) {
                    child.animate().setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            listView.setEnabled(true);
                            // store new position
                            mItemIdTopMap.put(itemId, child.getTop());
                        }
                    });
                    firstAnimation = false;
                }
            }
        }
        return true;
    }

    public interface Callbacks
    {
        public void notificationCleared(NotificationData ni);
        public void notificationOpen(NotificationData ni);
        public void notificationClicked(NotificationData ni, int position, boolean notificationClicked);
        public void notificationRunAction(NotificationData ni, int i);
        public void onTouchAndHold();
        public void onDrag(float x, float y);
        public void onTouchRelease();
    }

    public ListView getListView()
    {
        return listView;
    }

    public NPListView(final Context context, Point size, Point pos, Callbacks callbacks)
    {
        super(context);
        mContext = context;
        mTheme = ThemeManager.getInstance(context).getCurrentTheme();

        this.callbacks = callbacks;

        LayoutInflater inflater = LayoutInflater.from(context);
        mView = (ViewGroup) inflater.inflate(R.layout.notifications_list, null);

        listViewContainer = (ViewGroup) mView.findViewById(R.id.notifications_listview_container);

        // create the list view
        listView = new ListView(mContext);
        if (mTheme != null && mTheme.divider != null) {
            listView.setDivider(mTheme.divider);
        }
        else {
            //noinspection ResourceType
            listView.setDivider(Resources.getSystem().getDrawable(android.R.color.transparent));
        }

        listView.setDividerHeight((int) mTheme.notificationSpacing);
        listViewContainer.addView(listView, ViewGroup.LayoutParams.MATCH_PARENT, size.y);

        mPullDownView = mView.findViewById(R.id.pull_to_dismiss);
        mPullDownText = mView.findViewById(R.id.pull_to_dismiss_view);
        mReleaseText = mView.findViewById(R.id.release_to_dismiss_view);

        adapter = new NotificationAdapter(context);
        mAnimationTime = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);

        prepareListView();

        mViewParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mViewParams.leftMargin = 0;
        mViewParams.topMargin = 0;

        mDotsView = new DotsSwipeView(mContext, pos, size);
        mDotsView.setAlpha(0);
        addView(mDotsView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        updateSizeAndPosition(pos, size);
        addView(mView, mViewParams);

    }

    private void prepareListView()
    {
        final SwipeIconTouchListener swipeDismissTouchListener =
                new SwipeIconTouchListener(
                        listView,
                        new SwipeIconTouchListener.DismissCallbacks()
                        {
                            public boolean canDismiss(int position)
                            {
                                List<NotificationData> data = new ArrayList<NotificationData>();
                                if (NotificationsService.getSharedInstance() != null)
                                    data = NotificationsService.getSharedInstance().getNotifications();

                                return position < data.size();
                            }

                            public void onDismiss(ListView listView, int[] reverseSortedPositions, boolean dismissRight)
                            {
                                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                                List<NotificationData> data = new ArrayList<NotificationData>();
                                if (NotificationsService.getSharedInstance() != null)
                                    data = NotificationsService.getSharedInstance().getNotifications();

                                boolean isSwipeToOpenEnabled = prefs.getBoolean(SettingsManager.SWIPE_TO_OPEN, SettingsManager.DEFAULT_SWIPE_TO_OPEN);
                                //saveNotificationsState();
                                for (int position : reverseSortedPositions)
                                {
                                    if (position < data.size())
                                    {
                                        NotificationData ni = data.get(position);
                                        callNotificationCleared(ni);
                                        if (dismissRight && isSwipeToOpenEnabled)
                                            callNotificationOpen(ni);
                                    }
                                }
                                //animateNotificationsChange();
                            }

                            @Override
                            public void onIconDown(int position, int x, int y, MotionEvent touchEvent)
                            {
                                List<NotificationData> data = new ArrayList<NotificationData>();
                                if (NotificationsService.getSharedInstance() != null)
                                    data = NotificationsService.getSharedInstance().getNotifications();

                                // show dots view
                                mDotsView.animate().alpha(1).setListener(null);
                                View v = listView.getChildAt(position-listView.getFirstVisiblePosition());
                                v.animate().alpha(0).setListener(null);

                                // set area for the action buttons to show up
                                Rect r = new Rect();
                                v.getHitRect(r);

                                int[] listPos = new int[2];
                                listView.getLocationInWindow(listPos);
                                int dy = listPos[1]-mMaxPos.y;
                                mDotsView.updateSizeAndPosition(new Point(mMaxPos.x, mMaxPos.y+dy), mMaxSize);

                                NotificationData ni = data.get(position);
                                mDotsView.setIcons(r, ni.getAppIcon(),
                                                   ni.getActions().length > 0?ni.getActions()[0].drawable:null,
                                                   ni.getActions().length > 1?ni.getActions()[1].drawable:null);
                                mDotsView.dispatchTouchEvent(touchEvent);
                            }

                            @Override
                            public void onIconDrag(int position, int dx, int dy, MotionEvent touchEvent)
                            {
                                mDotsView.dispatchTouchEvent(touchEvent);
                            }

                            @Override
                            public void onIconUp(int position, MotionEvent touchEvent)
                            {
                                List<NotificationData> data = new ArrayList<NotificationData>();
                                if (NotificationsService.getSharedInstance() != null)
                                    data = NotificationsService.getSharedInstance().getNotifications();

                                mDotsView.dispatchTouchEvent(touchEvent);
                                mDotsView.animate().alpha(0).setListener(null);

                                View v = listView.getChildAt(position-listView.getFirstVisiblePosition());
                                v.animate().alpha(1).setListener(null);

                                int selected = mDotsView.getSelected();
                                if (selected == 0)
                                {
                                    callNotificationOpen(data.get(position));
                                }
                                else if (selected >= 1)
                                {
                                    callNotificationRunAction(data.get(position), selected - 1);
                                }
                            }

                            @Override
                            public void onClick(int position)
                            {
                                List<NotificationData> data = new ArrayList<NotificationData>();
                                if (NotificationsService.getSharedInstance() != null)
                                    data = NotificationsService.getSharedInstance().getNotifications();

                                if (position < data.size())
                                    callNotificationClicked(data.get(position), position, false);
                            }
                        }, R.id.notification_bg);

        listView.setOnScrollListener(swipeDismissTouchListener.makeScrollListener());
        listView.setOnTouchListener(new PullToClearTouchListener(mContext, mPullDownView,
                mPullDownText, mReleaseText, new PullToClearTouchListener.Callbacks()
        {
            public Point mDisplaySize;

            @Override
            public void onRelease()
            {
                if (NotificationsService.getSharedInstance() != null)
                    NotificationsService.getSharedInstance().clearAllNotifications();

                // reset list
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onTouchAndHold()
            {
                mDisplaySize = NPViewManager.getDisplaySize(mContext);

                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                listViewContainer.animate().scaleY(1.05f).scaleX(1.05f).setDuration(mAnimationTime).setListener(null);
                callOnTouchAndHold();
            }

            @Override
            public void onDrag(float x, float y)
            {
                if (mMaxPos.y + y < 0) y = -mMaxPos.y;
                if (mMaxPos.y + y + mMaxSize.y > mDisplaySize.y) y = mDisplaySize.y - mMaxSize.y - mMaxPos.y;
                listViewContainer.setTranslationY(y);
                callOnDrag(x, y);
            }

            @Override
            public void onTouchRelease()
            {
                int y = (int) listViewContainer.getTranslationY();
                listViewContainer.setTranslationY(0);
                mlistViewParams.topMargin = mlistViewParams.topMargin + y;
                listView.setLayoutParams(mlistViewParams);
                listViewContainer.animate().scaleY(1.0f).scaleX(1.0f).setDuration(mAnimationTime).setListener(null);
                callOnTouchReleased();
            }
        }, swipeDismissTouchListener));

        listView.setItemsCanFocus(true);
        listView.setAdapter(adapter);
    }

    private void callOnTouchReleased()
    {
        if (callbacks != null)
            callbacks.onTouchRelease();
    }

    private void callOnDrag(float x, float y)
    {
        if (callbacks != null)
            callbacks.onDrag(x, y);
    }

    private void callOnTouchAndHold()
    {
        if (callbacks != null)
            callbacks.onTouchAndHold();
    }


    private void callNotificationOpen(NotificationData ni)
    {
        if (callbacks != null)
            callbacks.notificationOpen(ni);
    }

    private void callNotificationCleared(NotificationData ni)
    {
        if (callbacks != null)
            callbacks.notificationCleared(ni);
    }

    private void callNotificationClicked(NotificationData ni, int position, boolean clicked)
    {
        if (callbacks != null)
            callbacks.notificationClicked(ni, position, clicked);
    }

    private void callNotificationRunAction(NotificationData ni, int i)
    {
        if (callbacks != null)
            callbacks.notificationRunAction(ni, i);
    }

    public void notifyDataChanged()
    {
        adapter.notifyDataSetChanged();
    }

    public void cleanup()
    {
        listView.setOnScrollListener(null);
        listView.setOnTouchListener(null);
        callbacks = null;
    }
}
