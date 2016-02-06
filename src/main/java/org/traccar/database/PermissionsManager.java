/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.database;

import org.traccar.database.mongo.SessionRepository;
import org.traccar.model.Permission;
import org.traccar.model.Server;
import org.traccar.model.User;

import java.util.*;

public class PermissionsManager {

    private Server server;

    private final Map<Long, User> users = new HashMap<>();

    private final Map<Long, Set<Long>> permissions = new HashMap<>();

    private Set<Long> getNotNull(long userId) {
        permissions.putIfAbsent(userId, new HashSet<>());

        return permissions.get(userId);
    }

    public PermissionsManager() {
        refresh();
    }

    public final void refresh() {
        users.clear();
        permissions.clear();
        SessionRepository sessionRepository = new SessionRepository();
        server = sessionRepository.getServer();
        sessionRepository.getUsers().forEach(u -> users.put(u.getId(), u));

        for (Permission permission : sessionRepository.getPermissions()) {
            getNotNull(permission.getUserId()).add(permission.getDeviceId());
        }
    }

    public boolean isAdmin(long userId) {
        return users.containsKey(userId) && users.get(userId).getAdmin();
    }

    public void checkAdmin(long userId) throws SecurityException {
        if (!isAdmin(userId)) {
            throw new SecurityException("Admin access required");
        }
    }

    public void checkUser(long userId, long otherUserId) throws SecurityException {
        if (userId != otherUserId) {
            checkAdmin(userId);
        }
    }

    public Collection<Long> allowedDevices(long userId) {
        return getNotNull(userId);
    }

    public void checkDevice(long userId, long deviceId) throws SecurityException {
        if (!getNotNull(userId).contains(deviceId)) {
            throw new SecurityException("Device access denied");
        }
    }

    public void checkRegistration(long userId) {
        if (!server.getRegistration() && !isAdmin(userId)) {
            throw new SecurityException("Registration disabled");
        }
    }

    public void checkReadonly(long userId) {
        if (server.getReadonly() && !isAdmin(userId)) {
            throw new SecurityException("Readonly user");
        }
    }

}
