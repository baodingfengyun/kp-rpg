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

package de.kaiserpfalzedv.rpg.integrations.datastore.guilds;

import de.kaiserpfalzedv.rpg.core.resources.ImmutableResourceHistory;
import de.kaiserpfalzedv.rpg.core.resources.ImmutableResourceMetadata;
import de.kaiserpfalzedv.rpg.core.resources.ImmutableResourceStatus;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.Guild;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.ImmutableGuild;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.ImmutableGuildData;
import de.kaiserpfalzedv.rpg.test.mongodb.MongoDBResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestGuildRepository -- tests the basic methods for storing, manipulating and deleting users.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 1.2.0 2021-01-30
 */
@QuarkusTest
@QuarkusTestResource(MongoDBResource.class)
public class TestGuildRepository {
    private static final Logger LOG = LoggerFactory.getLogger(TestGuildRepository.class);

    private static final String NAMESPACE = "discord";
    private static final String NAME = "Paladins Inn";
    private static final UUID UID = UUID.randomUUID();
    private static final OffsetDateTime CREATED = OffsetDateTime.now(ZoneOffset.UTC);
    private static final String PREFIX = "test!";

    /**
     * Default data created during setup of tests.
     */
    private static final Guild data = ImmutableGuild.builder()
            .metadata(
                    ImmutableResourceMetadata.builder()
                            .kind(Guild.KIND)
                            .apiVersion(Guild.API_VERSION)

                            .namespace(NAMESPACE)
                            .name(NAME)
                            .uid(UID)

                            .created(CREATED)

                            .putAnnotations("test", "true")

                            .build()
            )
            .spec(
                    ImmutableGuildData.builder()
                            .prefix(PREFIX)
                            .properties(new HashMap<>())
                            .build()
            )
            .status(
                    ImmutableResourceStatus.builder()
                            .observedGeneration(0L)
                            .addHistory(
                                    ImmutableResourceHistory.builder()
                                            .status("created")
                                            .timeStamp(CREATED)
                                            .build()
                            )
                            .build()
            )
            .build();

    @Inject
    MongoGuildRepository sut;

    @BeforeAll
    static void setUp() {
        MDC.put("test-class", TestGuildRepository.class.getSimpleName());

        MongoGuildRepository repo = new MongoGuildRepository();
        repo.save(data);

        LOG.info("Saved: data={}, count={}, stored={}", data, repo.count(), repo.findByUid(UID));
    }

    @AfterAll
    static void tearDown() {
        new MongoGuildRepository().remove(data);
        MDC.clear();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenThereIsNoGuild() {
        MDC.put("test", "not-present-without-guild");

        Optional<Guild> result = sut.findByNameSpaceAndName("no-namespace", "no-guild");
        LOG.trace("result={}", result);

        assertFalse(result.isPresent(), "There should be no guild no-namespace/no-guild");
    }

    @AfterEach
    void tearDownEach() {
        MDC.remove("test");
    }

    @Test
    public void shouldReturnGuildByNamespaceAndNameWhenThereIsAnGuild() {
        MDC.put("test", "load-guild-by-namespace-and-name");

        Optional<Guild> result = sut.findByNameSpaceAndName(NAMESPACE, NAME);
        LOG.trace("result={}", result);

        assertTrue(result.isPresent(), "There should be an user " + NAMESPACE + "/" + NAME);
    }

    @Test
    public void shouldReturnGuildByUidWhenThereIsAnGuild() {
        MDC.put("test", "load-user-by-uid");

        Optional<Guild> result = sut.findByUid(UID);
        LOG.trace("result={}", result);

        assertTrue(result.isPresent(), "There should be a guild with UID " + UID.toString());
    }
}
