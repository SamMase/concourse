/*
 * Copyright (c) 2013-2015 Cinchapi Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cinchapi.concourse.cli;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.cinchapi.concourse.Concourse;
import org.cinchapi.concourse.config.ConcourseClientPreferences;
import org.slf4j.LoggerFactory;

import jline.console.ConsoleReader;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

/**
 * A {@link CommandLineInterface} is a console tool that interacts with
 * {@link Concourse} via the client interface. This class contains boilerplate
 * logic for grabbing authentication credentials and establishing a connection
 * to Concourse.
 * 
 * @author Jeff Nelson
 */
public abstract class CommandLineInterface {

    /**
     * Internal {@link Concourse} instance that should be used to do the work of
     * the CLI.
     */
    protected Concourse concourse = null;

    /**
     * Handler to the console for interactive I/O.
     */
    protected ConsoleReader console;

    /**
     * The CLI options.
     */
    protected Options options;

    /**
     * The parser that validates the CLI options.
     */
    protected JCommander parser;

    /**
     * The logger that should be used when printing information to the console
     * from the CLI.
     */
    protected Logger log = (Logger) LoggerFactory.getLogger(getClass());

    /**
     * Construct a new instance.
     * <p>
     * The subclass should call {@link #CommandLineInterface(Options, String[])}
     * from this constructor with an instance of of the appropriate subclass of
     * {@link Options} if necessary.
     * </p>
     * 
     * @param args
     */
    public CommandLineInterface(String[] args) {
        this(new Options(), args);
    }

    /**
     * Construct a new instance that is seeded with an object containing options
     * metadata. The {@code options} will be parsed by {@link JCommander} to
     * configure them appropriately.
     * <p>
     * The subclass should NOT override this constructor. If the subclass
     * defines a custom {@link Options} class, then it only needs to pass those
     * to this super constructor from {@link #CommandLineInterface(String...)}.
     * </p>
     * 
     * @param options
     * @param args - these usually come from the main method
     */
    protected CommandLineInterface(Options options, String... args) {
        try {
            this.parser = new JCommander(options, args);
            this.options = options;
            parser.setProgramName(CaseFormat.UPPER_CAMEL.to(
                    CaseFormat.LOWER_HYPHEN, this.getClass().getSimpleName()));
            this.console = new ConsoleReader();
            this.console.setExpandEvents(false);
            if(options.help) {
                parser.usage();
                System.exit(1);
            }
            if(!Strings.isNullOrEmpty(options.prefs)) {
                ConcourseClientPreferences prefs = ConcourseClientPreferences
                        .load(options.prefs);
                options.username = prefs.getUsername();
                options.password = new String(prefs.getPassword());
                options.host = prefs.getHost();
                options.port = prefs.getPort();
                options.environment = prefs.getEnvironment();
            }
            if(Strings.isNullOrEmpty(options.password)) {
                options.password = console.readLine("password for ["
                        + options.username + "]: ", '*');
            }
            int attemptsRemaining = 5;
            while (concourse == null && attemptsRemaining > 0) {
                try {
                    concourse = Concourse.connect(options.host, options.port,
                            options.username, options.password,
                            options.environment);
                }
                catch (Exception e) {
                    System.err.println("Error processing login. Please check "
                            + "username/password combination and try again.");
                    concourse = null;
                    options.password = console.readLine("password for ["
                            + options.username + "]: ", '*');
                    attemptsRemaining--;
                }
            }
            if(concourse == null) {
                System.err
                        .println("Unable to connect to Concourse. Is the server running?");
                System.exit(1);
            }
        }
        catch (ParameterException e) {
            System.exit(die(e.getMessage()));
        }
        catch (IOException e) {
            System.exit(die(e.getMessage()));
        }
    }

    /**
     * Run the CLI. This method should only be called from the main method.
     */
    public final int run() {
        try {
            log.setLevel(options.verbose ? Level.INFO : Level.ERROR);
            doTask();
            return 0;
        }
        catch (Exception e) {
            return die(e.getMessage());
        }
    }

    /**
     * Print {@code message} to stderr and exit with a non-zero status.
     * 
     * @param message
     */
    protected int die(String message) {
        if(!StringUtils.isBlank(message)) {
            System.err.println("ERROR: " + message);
        }
        return 2;
    }

    /**
     * Implement the task. This method is called by the main {@link #run()}
     * method, so the implementer should place all task logic here.
     * <p>
     * DO NOT call {@link System#exit(int)} with '0' from this method
     * </p>
     */
    protected abstract void doTask();

    /**
     * Return the original working directory from which the CLI was launched.
     * This information is sometimes necessary to properly resolve file paths.
     * 
     * @return the launch directory or {@code null} if the CLI is unable to
     *         determine its original working directory
     */
    @Nullable
    protected final String getLaunchDirectory() {
        return System.getProperty("user.dir.real"); // this is set by the .env
                                                    // script that is sourced by
                                                    // every server-side CLI
    }
}
