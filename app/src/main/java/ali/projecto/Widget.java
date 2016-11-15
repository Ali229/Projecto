package ali.projecto;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
    //======================================= OnUpdate Method ======================================
 @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		views.setOnClickPendingIntent(R.id.rButton, buildButtonPendingIntent(context));
		pushWidgetUpdate(context, views);
        FirstUpdate(context);
	}
    public void FirstUpdate(Context context)
    {
        Intent it = new Intent();
        it.setAction("update");
        context.sendBroadcast(it);
    }
    public static PendingIntent buildButtonPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction("update");
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static void pushWidgetUpdate(Context context, RemoteViews views) {
        ComponentName myWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, views);
    }
}
