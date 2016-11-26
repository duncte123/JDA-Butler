package com.almightyalpaca.discord.jdabutler;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class FormattingUtil {

	public static String formatTimestap(final long timestap) {
		return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.ENGLISH).format(Date.from(Instant.ofEpochMilli(timestap)));
	}

	public static String getChangelog(final JSONArray changeSets) {
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i < changeSets.length(); i++) {
			final JSONObject item = changeSets.getJSONObject(i);

			final String id = item.getString("id");

			final String comment = item.getString("comment");
			final String[] lines = comment.split("\n");
			for (int j = 0; j < lines.length; j++) {

				builder.append("[`").append(j == 0 ? id.substring(0, 6) : "`......`").append("`](https://github.com/DV8FromTheWorld/JDA/commit/" + id + ")").append(" ").append(lines[j]).append("\n");
			}
		}

		return builder.toString();

	}

	public static void setFooter(final EmbedBuilder eb, final JSONArray culprits, final String timestamp) {

		if (culprits.length() == 1) {
			try {
				final JSONObject author = Unirest.get(culprits.getJSONObject(0).getString("absoluteUrl") + "/api/json").asJson().getBody().getObject();

				if (!author.isNull("description")) {

					final String description = author.getString("description");

					User user = null;

					for (final String line : description.split("\n")) {
						if (line.startsWith("discord: ")) {
							user = Bot.jda.getUserById(line.substring(7));
							break;
						}
					}

					if (user != null) {
						eb.setFooter(user.getName() + "   |    " + timestamp, user.getAvatarUrl());
					} else {
						eb.setFooter("unknown user   |   " + timestamp, null);
					}
				} else {
					eb.setFooter("unknown user   |   " + timestamp, null);
				}

			} catch (final Exception e) {
				eb.setFooter("unknown user   |   " + timestamp, null);
				Bot.LOG.log(e);
			}
		}
		if (culprits.length() > 1) {
			eb.setFooter("multiple users   |   " + timestamp, null);
		} else {
			eb.setFooter(timestamp, null);
		}

	}
}
