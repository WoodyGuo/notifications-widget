package com.roymam.android.nilsplus.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roymam.android.common.BitmapUtils;
import com.roymam.android.notificationswidget.NotificationData;
import com.roymam.android.notificationswidget.NotificationsService;
import com.roymam.android.notificationswidget.R;
import com.roymam.android.notificationswidget.SettingsManager;
import com.roymam.android.nilsplus.ui.theme.Theme;
import com.roymam.android.nilsplus.ui.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends BaseAdapter
{
    private Theme mTheme = null;
    private Context context;

    public NotificationAdapter(Context context)
    {
        this.context = context;
        mTheme = ThemeManager.getInstance(context).getCurrentTheme();
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        return (position % 2);
    }

    @Override
    public int getCount()
    {
        if (NotificationsService.getSharedInstance() != null)
            return NotificationsService.getSharedInstance().getNotifications().size();
        else
            return 0;
    }

    @Override
    public NotificationData getItem(int position)
    {
        if (NotificationsService.getSharedInstance() != null)
            return NotificationsService.getSharedInstance().getNotifications().get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position)
    {
        List<NotificationData> data = new ArrayList<NotificationData>();
        if (NotificationsService.getSharedInstance() != null)
            data = NotificationsService.getSharedInstance().getNotifications();

        if (position < data.size())
            return data.get(position).getUid();
        else
            return -1;
    }

    public static void applySettingsToView(Context context, View notificationView, NotificationData item, int position, Theme theme, boolean preview)
    {
        boolean even = position % 2 == 0;

        ViewHolder holder;
        if (notificationView.getTag() == null)
        {
            holder = new ViewHolder();
            holder.notificationView = notificationView.findViewById(R.id.front);
            holder.ivImage = (ImageView) notificationView.findViewById(R.id.notification_image);
            holder.tvTitle = (TextView) notificationView.findViewById(R.id.notification_title);
            holder.tvDescription = (TextView) notificationView.findViewById(R.id.notification_text);
            holder.tvTime = (TextView) notificationView.findViewById(R.id.notification_time);
            holder.vNotificationBG = notificationView.findViewById(R.id.front);
            holder.vTextBG = notificationView.findViewById(R.id.notification_text_container);
            holder.vIconBG = notificationView.findViewById(R.id.notification_bg);
            holder.vIconBgImage = (ImageView) notificationView.findViewById(R.id.icon_bg);
            holder.vIconFgImage = (ImageView) notificationView.findViewById(R.id.icon_fg);

            notificationView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) notificationView.getTag();
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int primaryTextColor = prefs.getInt(SettingsManager.PRIMARY_TEXT_COLOR, SettingsManager.DEFAULT_PRIMARY_TEXT_COLOR);
        int secondaryTextColor = prefs.getInt(SettingsManager.SECONDARY_TEXT_COLOR, SettingsManager.DEFAULT_SECONDARY_TEXT_COLOR);
        int notificationBGColor = prefs.getInt(SettingsManager.MAIN_BG_COLOR, SettingsManager.DEFAULT_MAIN_BG_COLOR);
        int iconBGColor = prefs.getInt(SettingsManager.ICON_BG_COLOR, SettingsManager.DEFAULT_ICON_BG_COLOR);
        int altNotificationBGColor = prefs.getInt(SettingsManager.ALT_MAIN_BG_COLOR, SettingsManager.DEFAULT_ALT_MAIN_BG_COLOR);
        int altIconBGColor = prefs.getInt(SettingsManager.ALT_ICON_BG_COLOR, SettingsManager.DEFAULT_ALT_ICON_BG_COLOR);

        Bitmap icon = NotificationAdapter.createThemedIcon(item.getIcon(), theme, BitmapUtils.dpToPx(prefs.getInt(SettingsManager.ICON_SIZE, SettingsManager.DEFAULT_ICON_SIZE)));
        holder.ivImage.setImageDrawable(new BitmapDrawable(icon));
        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle().toString() : null);
        holder.tvTitle.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Medium);
        holder.tvTitle.setTextSize(prefs.getInt(SettingsManager.TITLE_FONT_SIZE, SettingsManager.DEFAULT_TITLE_FONT_SIZE));
        holder.tvTime.setText(item.getTimeText(context));
        holder.tvTime.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
        holder.tvTime.setTextSize(prefs.getInt(SettingsManager.TEXT_FONT_SIZE, SettingsManager.DEFAULT_TEXT_FONT_SIZE));
        holder.tvTime.setVisibility(prefs.getBoolean(SettingsManager.SHOW_TIME, SettingsManager.DEFAULT_SHOW_TIME) ? View.VISIBLE : View.GONE);
        holder.tvDescription.setText(item.getText() != null ? item.getText().toString() : null);
        holder.tvDescription.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
        int textSizeSp = prefs.getInt(SettingsManager.TEXT_FONT_SIZE, SettingsManager.DEFAULT_TEXT_FONT_SIZE);
        holder.tvDescription.setTextSize(textSizeSp);
        int maxLines = Integer.parseInt(prefs.getString(SettingsManager.MAX_TEXT_LINES, String.valueOf(SettingsManager.DEFAULT_MAX_TEXT_LINES)));
        holder.tvTitle.setLines(1);
        boolean fitToText = prefs.getBoolean(SettingsManager.FIT_HEIGHT_TO_CONTENT, SettingsManager.DEFAULT_FIT_HEIGHT_TO_CONTENT);
        if (!fitToText && maxLines > -1)
            holder.tvDescription.setLines(maxLines);
        else if (maxLines > -1)
            holder.tvDescription.setMaxLines(maxLines);
        else {
            holder.tvDescription.setMaxLines(Integer.MAX_VALUE);
        }

        // set colors
        holder.tvTitle.setTextColor(primaryTextColor);
        holder.tvDescription.setTextColor(secondaryTextColor);
        holder.tvTime.setTextColor(secondaryTextColor);
        holder.vNotificationBG.setBackgroundColor(even?altNotificationBGColor:notificationBGColor);
        holder.vIconBG.setBackgroundColor(even?altIconBGColor:iconBGColor);

        // handle single line
        boolean singleLine = item.getText() == null || item.getText().equals("") || prefs.getBoolean(SettingsManager.SINGLE_LINE, SettingsManager.DEFAULT_SINGLE_LINE);
        if (singleLine)
        {
            holder.tvDescription.setVisibility(View.GONE);
            if (!fitToText)
                holder.tvTitle.setLines(maxLines);
            else
                holder.tvTitle.setMaxLines(maxLines);

            if (item.getText() != null && Color.alpha(secondaryTextColor) > 0)
            {
                // create a combined title + text
                CharSequence text = TextUtils.concat(item.getTitle(), " ", item.getText());
                SpannableStringBuilder ssb = new SpannableStringBuilder(text);
                CharacterStyle titleStyle = new ForegroundColorSpan(primaryTextColor);
                CharacterStyle textColor = new ForegroundColorSpan(secondaryTextColor);
                AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(BitmapUtils.spToPx(textSizeSp));
                ssb.setSpan(titleStyle, 0, item.getTitle().length(),0);
                ssb.setSpan(textSize, item.getTitle().length()+1, item.getTitle().length()+1+item.getText().length(),0);
                ssb.setSpan(textColor, item.getTitle().length()+1, item.getTitle().length()+1+item.getText().length(),0);
                holder.tvTitle.setText(ssb);
                if (!fitToText)
                    holder.tvTitle.setLines(maxLines+1);
                else
                    holder.tvTitle.setMaxLines(maxLines+1);
            }
        }
        else
        {
            holder.tvDescription.setVisibility(View.VISIBLE);
        }

        // apply theme (if needed)
        if (theme != null)
        {
            // apply font style and size if available
            if (theme.timeFontSize != -1) holder.tvTime.setTextSize(theme.timeFontSize);
            if (theme.titleTypeface != null) holder.tvTitle.setTypeface(theme.titleTypeface);
            if (theme.textTypeface != null) holder.tvDescription.setTypeface(theme.titleTypeface);
            if (theme.timeTypeface != null) holder.tvTime.setTypeface(theme.titleTypeface);

            // if "fit to content" is enabled, we need to reload background drawables every item again and again because the height can be different
            if (prefs.getBoolean(SettingsManager.FIT_HEIGHT_TO_CONTENT, SettingsManager.DEFAULT_FIT_HEIGHT_TO_CONTENT))
                ThemeManager.getInstance(context).reloadDrawables();
            if (theme.background != null)
            {
                Drawable background = even?theme.altBackground:theme.background;
                    background.setAlpha(255 * prefs.getInt(SettingsManager.MAIN_BG_OPACITY, SettingsManager.DEFAULT_MAIN_BG_OPACITY) / 100);
                //noinspection deprecation
                holder.vNotificationBG.setBackgroundDrawable(background);
            }

            Drawable textBG = even ? theme.altTextBG : theme.textBG;
            if (textBG != null) textBG.setAlpha(255 * prefs.getInt(SettingsManager.MAIN_BG_OPACITY, SettingsManager.DEFAULT_MAIN_BG_OPACITY) / 100);
            holder.vTextBG.setBackgroundDrawable(textBG);
            Drawable iconBgImage = even?theme.altIconBg:theme.iconBg;
            if (iconBgImage != null)
                iconBgImage.setAlpha(255 * prefs.getInt(SettingsManager.MAIN_BG_OPACITY, SettingsManager.DEFAULT_MAIN_BG_OPACITY) / 100);
            holder.vIconBgImage.setImageDrawable(iconBgImage);
            holder.vIconFgImage.setImageDrawable(theme.iconFg);
        }

        ViewGroup.LayoutParams vparams = holder.vIconBG.getLayoutParams();
        vparams.width = BitmapUtils.dpToPx(prefs.getInt(SettingsManager.ICON_SIZE, SettingsManager.DEFAULT_ICON_SIZE));
        vparams.height = BitmapUtils.dpToPx(prefs.getInt(SettingsManager.ICON_SIZE, SettingsManager.DEFAULT_ICON_SIZE));
        holder.vIconBG.setLayoutParams(vparams);

        // set width
        int leftMargin = 0;
        int rightMargin = 0;

        if (!preview)
        {
            Point size = NPViewManager.getDisplaySize(context);

            leftMargin = prefs.getInt(NPViewManager.getRotationMode(context) + "left_margin", (int) (size.x * 0.05f));
            rightMargin = prefs.getInt(NPViewManager.getRotationMode(context) + "right_margin", (int) (size.x * 0.05f));
        }

        View front = notificationView.findViewById(R.id.front);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) front.getLayoutParams();
        params.leftMargin = leftMargin;
        params.rightMargin = rightMargin;

        front.setLayoutParams(params);

        // force relayout of background
        holder.vNotificationBG.requestLayout();
        holder.vTextBG.requestLayout();
    }

    public static int getTextAppearance(String size)
    {
        if (size.equals("small"))
            return android.R.style.TextAppearance_DeviceDefault_Small;
        else if (size.equals("medium"))
            return android.R.style.TextAppearance_DeviceDefault_Medium;
        else
            return android.R.style.TextAppearance_DeviceDefault_Large;
    }

    public static Bitmap createThemedIcon(Bitmap icon, Theme theme, int iconSize)
    {
        if (theme != null && theme.iconMask != null)
        {
            if (theme.iconMask instanceof BitmapDrawable)
            {
                // scale mask to the bitmap size
                Bitmap mask = ((BitmapDrawable) theme.iconMask).getBitmap();
                Bitmap iconBitmap = icon;
                Bitmap maskBitmap = mask;

                // scale mask to icon container size
                int w = iconSize;
                int h = iconSize;
                maskBitmap = Bitmap.createScaledBitmap(maskBitmap, w, h, false);

                // center icon bitmap in the icon container
                iconBitmap = BitmapUtils.createCenteredBitmap(iconBitmap, w, h);

                // crop the bitmap using the mask
                Bitmap maskedBitmap = BitmapUtils.drawBitmapOnMask(iconBitmap, maskBitmap, 0,0);

                // set the new app icon
                icon = maskedBitmap;
            }
        }
        else
        {
            // center icon bitmap in the icon container
            icon = BitmapUtils.createCenteredBitmap(icon, iconSize, iconSize);
        }
        return icon;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        final NotificationData item = getItem(position);

        if (convertView == null)
        {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.notification_row, parent, false);
        }

        applySettingsToView(context, convertView, item, position, mTheme, false);

        // make sure that the view is visible (might have been hidden previously)
        convertView.setAlpha(1);
        convertView.setTranslationY(0);
        convertView.setTranslationX(0);

        return convertView;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    public void remove(int position)
    {
        if (NotificationsService.getSharedInstance() != null)
            NotificationsService.getSharedInstance().clearNotification(NotificationsService.getSharedInstance().getNotifications().get(position).getUid());

        notifyDataSetChanged();
    }

    static class ViewHolder
    {
        ImageView ivImage;
        TextView tvTitle;
        TextView tvDescription;
        public View vNotificationBG;
        public View vIconBG;
        public View notificationView;
        public ImageView vIconBgImage;
        public ImageView vIconFgImage;
        public View vTextBG;
        public TextView tvTime;
    }
}