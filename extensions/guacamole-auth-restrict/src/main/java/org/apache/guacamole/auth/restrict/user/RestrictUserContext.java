/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.guacamole.auth.restrict.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.auth.restrict.connection.RestrictConnection;
import org.apache.guacamole.auth.restrict.connectiongroup.RestrictConnectionGroup;
import org.apache.guacamole.auth.restrict.usergroup.RestrictUserGroup;
import org.apache.guacamole.form.Form;
import org.apache.guacamole.net.auth.Connection;
import org.apache.guacamole.net.auth.ConnectionGroup;
import org.apache.guacamole.net.auth.DecoratingDirectory;
import org.apache.guacamole.net.auth.DelegatingUserContext;
import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.net.auth.UserGroup;

/**
 * A UserContext implementation for additional login and connection restrictions
 * which wraps the UserContext of some other extension.
 */
public class RestrictUserContext extends DelegatingUserContext {

    /**
     * The remote address from which this user logged in.
     */
    private final String remoteAddress;
    
    /**
     * Creates a new RestrictUserContext which wraps the given UserContext,
     * providing additional control for user logins and connections.
     *
     * @param userContext
     *     The UserContext to wrap.
     * 
     * @param remoteAddress
     *     The address the user is logging in from, if known.
     */
    public RestrictUserContext(UserContext userContext, String remoteAddress) {
        super(userContext);
        this.remoteAddress = remoteAddress;
    }
    
    @Override
    public Directory<Connection> getConnectionDirectory() throws GuacamoleException {
        return new DecoratingDirectory<Connection>(super.getConnectionDirectory()) {

            @Override
            protected Connection decorate(Connection object) {
                return new RestrictConnection(object, remoteAddress);
            }

            @Override
            protected Connection undecorate(Connection object) {
                assert(object instanceof RestrictConnection);
                return ((RestrictConnection) object).getUndecorated();
            }

        };
    }
    
    @Override
    public Collection<Form> getConnectionAttributes() {
        Collection<Form> connectionAttrs = new HashSet<>(super.getConnectionAttributes());
        connectionAttrs.add(RestrictConnection.RESTRICT_CONNECTION_FORM);
        return Collections.unmodifiableCollection(connectionAttrs);
    }
    
    @Override
    public Directory<ConnectionGroup> getConnectionGroupDirectory() throws GuacamoleException {
        return new DecoratingDirectory<ConnectionGroup>(super.getConnectionGroupDirectory()) {

            @Override
            protected ConnectionGroup decorate(ConnectionGroup object) {
                return new RestrictConnectionGroup(object, remoteAddress);
            }

            @Override
            protected ConnectionGroup undecorate(ConnectionGroup object) {
                assert(object instanceof RestrictConnectionGroup);
                return ((RestrictConnectionGroup) object).getUndecorated();
            }

        };
    }
    
    @Override
    public Collection<Form> getConnectionGroupAttributes() {
        Collection<Form> connectionGroupAttrs = new HashSet<>(super.getConnectionGroupAttributes());
        connectionGroupAttrs.add(RestrictConnectionGroup.RESTRICT_CONNECTIONGROUP_FORM);
        return Collections.unmodifiableCollection(connectionGroupAttrs);
    }
    
    @Override
    public Directory<User> getUserDirectory() throws GuacamoleException {
        return new DecoratingDirectory<User>(super.getUserDirectory()) {

            @Override
            protected User decorate(User object) {
                return new RestrictUser(object);
            }

            @Override
            protected User undecorate(User object) {
                assert(object instanceof RestrictUser);
                return ((RestrictUser) object).getUndecorated();
            }

        };
    }
    
    @Override
    public Collection<Form> getUserAttributes() {
        Collection<Form> userAttrs = new HashSet<>(super.getUserAttributes());
        userAttrs.add(RestrictUser.RESTRICT_LOGIN_FORM);
        return Collections.unmodifiableCollection(userAttrs);
    }
    
    @Override
    public Directory<UserGroup> getUserGroupDirectory() throws GuacamoleException {
        return new DecoratingDirectory<UserGroup>(super.getUserGroupDirectory()) {
            
            @Override
            protected UserGroup decorate(UserGroup object) {
                return new RestrictUserGroup(object);
            }
            
            @Override
            protected UserGroup undecorate(UserGroup object) {
                assert(object instanceof RestrictUserGroup);
                return ((RestrictUserGroup) object).getUndecorated();
            }
            
        };
    }
    
    @Override
    public Collection<Form> getUserGroupAttributes() {
        Collection<Form> userGroupAttrs = new HashSet<>(super.getUserGroupAttributes());
        userGroupAttrs.add(RestrictUserGroup.RESTRICT_LOGIN_FORM);
        return Collections.unmodifiableCollection(userGroupAttrs);
    }

}
