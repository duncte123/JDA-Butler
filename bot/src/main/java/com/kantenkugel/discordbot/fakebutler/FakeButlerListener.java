package com.kantenkugel.discordbot.fakebutler;

import com.almightyalpaca.discord.jdabutler.Bot;
import com.almightyalpaca.discord.jdabutler.commands.commands.NotifyCommand;
import com.almightyalpaca.discord.jdabutler.commands.commands.StatsCommand;
import com.almightyalpaca.discord.jdabutler.util.DurationUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.managers.Presence;

public class FakeButlerListener implements EventListener
{
    private static final long JDA_SERVER_ID = 125227483518861312L;

    private final long mainBotId;

    private long onlineTime;
    private long offlineTime;
    private long latestStamp;
    private boolean online = false;

    public FakeButlerListener(long mainBotId) {
        this.mainBotId = mainBotId;
    }

    public String getStats(JDA jda)
    {
        long on = onlineTime, off = offlineTime;
        if(online)
            on += System.currentTimeMillis() - latestStamp;
        else
            off += System.currentTimeMillis() - latestStamp;
        long sum = on + off;

        return String.format("%s stats since start of %s:\nOnline: %s (%.1f%%)\nOffline: %s (%.1f%%)",
                jda.getUserById(mainBotId), jda.getSelfUser(),
                DurationUtils.formatDuration(on), on*100f/sum,
                DurationUtils.formatDuration(off), off*100f/sum);
    }

    @Override
    public void onEvent(Event event)
    {
        if (event instanceof UserUpdateOnlineStatusEvent)
        {
            UserUpdateOnlineStatusEvent e = (UserUpdateOnlineStatusEvent) event;
            Guild guild = e.getGuild();
            User user = e.getUser();
            if(user.getIdLong() == mainBotId && guild.getIdLong() == JDA_SERVER_ID)
                handleStatus(e.getJDA(), guild.getMember(user));
        }
        else if (event instanceof ReadyEvent)
        {
            if(event.getJDA().getSelfUser().getIdLong() == mainBotId)
            {
                event.getJDA().removeEventListener(this);
                return;
            }

            Bot.dispatcher.registerCommand(new StatsCommand(this));

            Guild jdaGuild = event.getJDA().getGuildById(JDA_SERVER_ID);
            if (jdaGuild == null)
            {
                handleStatus(event.getJDA(), null);
                return;
            }
            Member butler = jdaGuild.getMemberById(mainBotId);
            handleStatus(event.getJDA(), butler);
            latestStamp = System.currentTimeMillis();
            onlineTime = offlineTime = 0L;
        }
    }

    private void handleStatus(JDA jda, Member butler)
    {
        Presence presence = jda.getPresence();
        if (butler == null || butler.getOnlineStatus() == OnlineStatus.OFFLINE)
        {
            //Main Butler is offline
            if(presence.getStatus() == OnlineStatus.ONLINE)
                return;
            NotifyCommand.reloadBlacklist(null);
            online = false;
            onlineTime += (System.currentTimeMillis() - latestStamp);
            presence.setStatus(OnlineStatus.ONLINE);
        }
        else
        {
            //Main Butler is online
            if(presence.getStatus() == OnlineStatus.INVISIBLE)
                return;
            online = true;
            offlineTime += (System.currentTimeMillis() - latestStamp);
            presence.setStatus(OnlineStatus.INVISIBLE);
        }
        //Only called if change occurred (or if main butler is online on startup)
        latestStamp = System.currentTimeMillis();

        Bot.isStealth = online;
    }
}
