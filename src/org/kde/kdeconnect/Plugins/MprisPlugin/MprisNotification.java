/*
 * Copyright 2014 Albert Vaca Cintora <albertvaka@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License or (at your option) version 3 or any later version
 * accepted by the membership of KDE e.V. (or its successor approved
 * by the membership of KDE e.V.), which shall act as a proxy
 * defined in Section 14 of version 3 of the license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

package org.kde.kdeconnect.Plugins.MprisPlugin;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.widget.RemoteViews;
import android.util.Log;

import org.kde.kdeconnect.Backends.BaseLink;
import org.kde.kdeconnect.BackgroundService;
import org.kde.kdeconnect.Device;
import org.kde.kdeconnect.Backends.BaseLinkProvider;
import org.kde.kdeconnect.NetworkPackage;
import org.kde.kdeconnect_tp.R;

import java.util.ArrayList;

public class MprisNotification extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 0x91b70463; // echo MprisNotification | md5sum | head -c 8
    private static Notification ntf = null;

    public static void showNotification(Context context, final boolean show, final String deviceId) {
        BackgroundService.RunCommand(context, new BackgroundService.InstanceCallback() {
            @Override
            public void onServiceStart(BackgroundService ctx) {
                if (show) {

                    //ntf = new Notification(R.drawable.icon, ctx.getString(R.string.app_title), System.currentTimeMillis());
                    ntf = new Notification();
                    ntf.icon = R.drawable.icon;
                    ntf.flags = Notification.FLAG_NO_CLEAR;
                    ntf.sound = null;
                    ntf.contentView = new RemoteViews(ctx.getPackageName(), R.layout.mpris_control_notification);

                    ntf.contentView.setOnClickPendingIntent(R.id.mpris_open_dialog,
                        PendingIntent.getActivity(ctx, 0,
                        new Intent(Intent.ACTION_MAIN, null, ctx, MprisActivity.class).putExtra("deviceId", deviceId),
                        PendingIntent.FLAG_CANCEL_CURRENT));

                    ntf.contentView.setOnClickPendingIntent(R.id.prev_button,
                        PendingIntent.getBroadcast(ctx, 0,
                                new Intent(ctx.getString(R.string.mpris_broadcast_previous),
                                        null, ctx, MprisNotification.class).putExtra("deviceId", deviceId), 0));

                    ntf.contentView.setOnClickPendingIntent(R.id.play_button,
                        PendingIntent.getBroadcast(ctx, 0,
                        new Intent(ctx.getString(R.string.mpris_broadcast_play),
                        null, ctx, MprisNotification.class).putExtra("deviceId", deviceId), 0));

                    ntf.contentView.setOnClickPendingIntent(R.id.next_button,
                        PendingIntent.getBroadcast(ctx, 0,
                        new Intent(ctx.getString(R.string.mpris_broadcast_next),
                        null, ctx, MprisNotification.class).putExtra("deviceId", deviceId), 0));

                    ntf.contentView.setCharSequence(R.id.now_playing_textview, "setText", ctx.getString(R.string.app_title));

                    ctx.startForeground(NOTIFICATION_ID, ntf);

                } else {

                    ntf = null;
                    ctx.stopForeground(true);

                }
            }
        });
    }

    public static void updateNotification(Context context, final String text, boolean isPlaying) {
        if (ntf == null) return;

        ntf.contentView.setCharSequence(R.id.now_playing_textview, "setText", text);
        ntf.contentView.setImageViewResource(R.id.play_button,
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        NotificationManager ntfMgr = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
        ntfMgr.notify(NOTIFICATION_ID, ntf);
    }

    @Override
    public void onReceive(final Context ctx, final Intent intent) {
        if (intent == null || intent.getAction() == null || intent.getStringExtra("deviceId") == null) {
            return;
        }
        BackgroundService.RunCommand(ctx, new BackgroundService.InstanceCallback() {
            @Override
            public void onServiceStart(BackgroundService service) {
                Device device = service.getDevice(intent.getStringExtra("deviceId"));
                if (device == null) return;
                MprisPlugin mpris = device.getPlugin(MprisPlugin.class);
                if (mpris == null) return;
                if (intent.getAction().equals(service.getString(R.string.mpris_broadcast_previous))) {
                    mpris.sendAction("Previous");
                }
                if (intent.getAction().equals(service.getString(R.string.mpris_broadcast_next))) {
                    mpris.sendAction("Next");
                }
                if (intent.getAction().equals(service.getString(R.string.mpris_broadcast_play))) {
                    mpris.sendAction("PlayPause");
                }
            }
        });
    }
}
