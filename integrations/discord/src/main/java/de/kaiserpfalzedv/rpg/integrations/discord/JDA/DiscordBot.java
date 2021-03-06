/*
 * Copyright (c) 2021 Kaiserpfalz EDV-Service, Roland T. Lichti.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.kaiserpfalzedv.rpg.integrations.discord.JDA;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.util.StringJoiner;

/**
 * DiscordBot -- The discord connection bot.
 *
 * @since 1.0.0
 */
@ApplicationScoped
public class DiscordBot {
    public static final String SERVICE_NAME = "Discord Connector";

    private static final Logger LOG = LoggerFactory.getLogger(DiscordBot.class);
    public static final long MAX_OK_PING = 500L;

    /**
     * The JDA bot.
     */
    private JDA bot;

    /**
     * Our discord authentication token.
     */
    @ConfigProperty(name = "discord.token", defaultValue = "<please configure discord.token>")
    String discordToken;

    /**
     * The dispather for events generated by discord.
     */
    @Inject
    DiscordDispatcher dispatcher;

    void startup(@Observes StartupEvent event) {
        registerToDiscord();
    }

    void onStop(@Observes ShutdownEvent event) {
        unregisterFromDiscord();
    }

    void reconnect() {
        unregisterFromDiscord();
        registerToDiscord();
    }

    private void registerToDiscord() {
        if (dontRegisterToDiscord()) {
            LOG.info("Discord connection will not be created.");
            return;
        }

        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(discordToken);
        builder.addEventListeners(dispatcher);

        try {
            bot = builder.build();
        } catch (LoginException e) {
            LOG.error("Could not login to Discord: " + e.getMessage(), e);

            throw new IllegalStateException("Login to Discord failed: " + e.getMessage());
        }

        LOG.info("Created Discord bot: {}", bot.getInviteUrl(
                Permission.MANAGE_ROLES,
                Permission.MANAGE_CHANNEL,
                Permission.MANAGE_EMOTES,
                Permission.MANAGE_WEBHOOKS,

                Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_TTS,
                Permission.MESSAGE_ATTACH_FILES,
                Permission.MESSAGE_EMBED_LINKS,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_EXT_EMOJI,

                Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK
        ));
    }

    private void unregisterFromDiscord() {
        if (dontRegisterToDiscord()) {
            LOG.info("No Discord disconnect on shutdown since it has not been registered in the first place.");
            return;
        }

        bot.shutdownNow();
        LOG.info("Discord bot shut down.");
    }

    private boolean dontRegisterToDiscord() {
        String profile = ProfileManager.getActiveProfile();
        return "dev".equals(profile) || "test".equals(profile);
    }

    /**
     * @return The ping to discord in ms.
     */
    public long discordPing() {
        return bot.getGatewayPing();
    }

    /**
     * Checks if the bot is auto-reconnecting, initialized and the ping is below {@link #MAX_OK_PING}
     * ({@value #MAX_OK_PING}).
     *
     * If the status is not ok, then it tries to reconnect to discord.
     *
     * @return true, if everything is ok.
     */
    public boolean discordOK() {
        JDA.Status status = bot.getStatus();

        if (bot.isAutoReconnect() && status.isInit() && discordPing() <= MAX_OK_PING) {
            return true;
        } else {
            reconnect();

            return false;
        }
    }

    /**
     * The JDA implementation. For usage by the discord integration.
     *
     * @deprecated Please don't use it. It's for internal use only.
     * @return the JDA implementation.
     */
    public JDA getJDA() {
        return bot;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "@" + System.identityHashCode(this) + "[", "]")
                .add("dispatcher=" + dispatcher)
                .toString();
    }
}
