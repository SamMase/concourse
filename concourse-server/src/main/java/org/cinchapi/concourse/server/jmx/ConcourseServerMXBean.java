/*
 * Copyright (c) 2013-2015 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cinchapi.concourse.server.jmx;

import javax.management.MXBean;

import org.cinchapi.concourse.server.GlobalState;

/**
 * An MXBean that defines managed operations for {@link ConcourseServer}.
 * 
 * @author Jeff Nelson
 */
@MXBean
public interface ConcourseServerMXBean {

    /**
     * The url used for connecting to the server's JMX service.
     */
    public static final String JMX_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:"
            + GlobalState.JMX_PORT + "/jmxrmi";

    /**
     * Return a string that contains the dumps for all the storage units (i.e.
     * buffer, primary, secondary, search) identified by {@code id}.
     * 
     * @param id
     * @return the dump string
     * @deprecated As of version 0.4.0. Replaced by
     *             {@link #dump(String, String)}
     */
    @ManagedOperation
    @Deprecated
    public String dump(String id);

    /**
     * Return a string that contains the dumps for all the storage units (i.e.
     * buffer, primary, secondary, search) in {@code environment} identified by
     * {@code id}.
     * 
     * @param id
     * @param environment
     * @return the dump string
     */
    @ManagedOperation
    public String dump(String id, String environment);

    /**
     * Return a string that contains a list of the ids for all the blocks that
     * can be dumped using {@link #dump(String)}.
     * 
     * @return the dump list
     * @deprecated As of version 0.4.0. Replaced by {@link #getDumpList(String)}
     */
    @ManagedOperation
    @Deprecated
    public String getDumpList();

    /**
     * Return a string that contains a list of the ids in the
     * {@code environment} for all the blocks that can be dumped using
     * {@link #dump(String, String)}.
     * 
     * @param environment
     * @return the dump list
     */
    @ManagedOperation
    public String getDumpList(String environment);

    /**
     * Return the release version of the server.
     * 
     * @return the server Version string
     */
    @ManagedOperation
    public String getServerVersion();

    /**
     * Grant access to the user identified by the combination of
     * {@code username} and {@code password}.
     * 
     * @param username
     * @param password
     */
    public void grant(byte[] username, byte[] password);

    /**
     * Return {@code true} if the server can be accessed
     * by a user identified by {@code username}.
     * 
     * @param username
     * @return true/false
     */
    @ManagedOperation
    public boolean hasUser(byte[] username);

    /**
     * Return the names of all the environments that exist within Concourse
     * Server. An environment is said to exist if at least one user has
     * established a connection to that environment.
     * 
     * @return a string containing all of the environments
     */
    @ManagedOperation
    public String listAllEnvironments();

    /**
     * Return a description of all the currently active user sessions within
     * Concourse Server.
     * 
     * @return a string containing all the user sessions
     */
    @ManagedOperation
    public String listAllUserSessions();

    /**
     * Return {@code true} if {@code username} and {@code password} is a valid
     * combination to login to the server for the purpose of performing a
     * managed operation. This method should only be used to authenticate a user
     * for the purpose of performing a single operation.
     * 
     * @param username
     * @param password
     * @return {@code true} if the credentials are valid
     */
    @ManagedOperation
    public boolean login(byte[] username, byte[] password);

    /**
     * Remove the user identified by {@code username}.
     * 
     * @param username
     */
    public void revoke(byte[] username);

}
