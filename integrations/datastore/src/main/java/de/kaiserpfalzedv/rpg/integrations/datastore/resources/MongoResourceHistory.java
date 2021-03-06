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

package de.kaiserpfalzedv.rpg.integrations.datastore.resources;

import de.kaiserpfalzedv.rpg.core.resources.ImmutableResourceHistory;
import de.kaiserpfalzedv.rpg.core.resources.ResourceHistory;

import java.util.StringJoiner;


/**
 * A single history entry. Basic data is the timestamp, the status and the message.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 1.2.0  2021-01-31
 */
public class MongoResourceHistory {
    public String status;
    public MongoOffsetDateTime timeStamp;
    public String message;

    public MongoResourceHistory() {}

    public MongoResourceHistory(final ResourceHistory orig) {
        status = orig.getStatus();
        timeStamp = new MongoOffsetDateTime(orig.getTimeStamp());

        if (orig.getMessage().isPresent()) {
            message = orig.getMessage().get();
        }
    }

    public ResourceHistory history() {
        ImmutableResourceHistory.Builder result = ImmutableResourceHistory.builder()
                .status(status)
                .timeStamp(timeStamp.timeStamp());

        if (message != null)            result.message(message);

        return result.build();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MongoResourceHistory.class.getSimpleName() + "[", "]")
                .add("status='" + status + "'")
                .add("timeStamp=" + timeStamp)
                .add("message='" + message + "'")
                .toString();
    }
}
