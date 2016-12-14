package com.almightyalpaca.discord.jdabutler.commands.commands;

import com.almightyalpaca.discord.jdabutler.Bot;
import com.almightyalpaca.discord.jdabutler.EmbedUtil;
import com.almightyalpaca.discord.jdabutler.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.stream.Collectors;

public class HelpCommand implements Command {
    @Override
    public void dispatch(String[] args, User sender, TextChannel channel, Message message, String content, GuildMessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        EmbedUtil.setColor(builder);
        String help = Bot.dispatcher.getCommands().stream()
                .filter(c -> c.getHelp() != null)
                .map(c -> String.format("%s%s - %s", Bot.config.getString("prefix", ""), c.getName().toLowerCase(), c.getHelp()))
                .collect(Collectors.joining("\n"));
        builder.setAuthor(channel.getGuild().getMember(sender).getEffectiveName(), null, sender.getEffectiveAvatarUrl());
        builder.setDescription(help);
        channel.sendMessage(new MessageBuilder().setEmbed(builder.build()).build()).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Prints a list of commands";
    }
}