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

import de.kaiserpfalzedv.rpg.core.user.ImmutableUser;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.Guild;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.GuildStoreService;
import de.kaiserpfalzedv.rpg.integrations.discord.guilds.ImmutableGuild;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GuildRepository implements GuildStoreService, PanacheMongoRepository<MongoGuild> {
    private static final Logger LOG = LoggerFactory.getLogger(GuildRepository.class);


    public Optional<Guild> findByNameSpaceAndName(final String nameSpace, final String name) {
        LOG.trace("loading: type=user, nameSpace={}, name={}", nameSpace, name);

        Map<String, String> queryParams = new HashMap<>(2);
        queryParams.put("nameSpace", nameSpace);
        queryParams.put("name", name);
        PanacheQuery<MongoGuild> query = MongoGuild.find("nameSpace = :nameSpace and name = :name", Sort.descending("generation"), queryParams);

        MongoGuild result = query.firstResult();
        LOG.debug("Loaded: {}", result);
        return result != null ? Optional.ofNullable(ImmutableGuild.builder().from(result).build()) : Optional.empty();
    }

    @Override
    public Optional<Guild> findByUid(final UUID uid) {
        LOG.trace("loading: type=user, uid={}", uid);

        PanacheQuery<MongoGuild> query = MongoGuild.findById(uid);

        MongoGuild result = query.firstResult();

        LOG.debug("Loaded: {}", result);
        return Optional.ofNullable(ImmutableGuild.builder().from(result).build());
    }

    @Override
    public Guild persist(final Guild object) {
        LOG.trace("persisting: user={}", object);

        persistOrUpdate(new MongoGuild(object));

        return object;
    }

    @Override
    public void delete(final Guild object) {
        delete(new MongoGuild(object));
    }

    @Override
    public void delete(String nameSpace, String name) {
        Optional<Guild> object = findByNameSpaceAndName(nameSpace, name);

        object.ifPresent(this::delete);
    }

    @Override
    public void delete(final UUID uid) {
        Optional<Guild> object = findByUid(uid);

        object.ifPresent(this::delete);
    }
}
