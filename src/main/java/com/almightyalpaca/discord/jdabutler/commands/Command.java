package com.almightyalpaca.discord.jdabutler.commands;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public interface Command {
    void dispatch(String[] args, User sender, TextChannel channel, Message message, String content, GuildMessageReceivedEvent event);

    String getName();

    String getHelp();

    default String[] getAliases() {
        return new String[0];
    }
}