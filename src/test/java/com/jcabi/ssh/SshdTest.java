/**
 * Copyright (c) 2014-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.ssh;

import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 * Integration tests for ${@link Ssh}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SshdTest {

    /**
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Expected Exception.
     */
    @Rule
    public final transient ExpectedException thrown = ExpectedException.none();

    /**
     * Check that it's not Windows.
     */
    @Before
    public void notWindows() {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
    }

    /**
     * SSH can execute command on a real SSH server.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandOnServer() throws Exception {
        final Sshd sshd = new Sshd();
        try {
            MatcherAssert.assertThat(
                new Shell.Plain(
                    new Shell.Safe(sshd.connect())
                ).exec("echo one"),
                Matchers.startsWith("one")
            );
        } finally {
            sshd.close();
        }
    }

    /**
     * SSH can execute command on a real SSH server.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandOnServerWithManualConfig() throws Exception {
        final Sshd sshd = new Sshd(this.temp.newFolder());
        try {
            MatcherAssert.assertThat(
                new Shell.Plain(
                    new Shell.Safe(
                        new Ssh(
                            Sshd.host(), sshd.port(), Sshd.login(),
                            this.getClass().getResource("id_rsa")
                        )
                    )
                ).exec("echo 'how are you'"),
                Matchers.startsWith("how are")
            );
        } finally {
            sshd.close();
        }
    }

    /**
     * Execute command on a real SSH server before timeout.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandNoTimeout() throws Exception {
        final Sshd sshd = new Sshd();
        try {
            MatcherAssert.assertThat(
                new Shell.Plain(
                    new Shell.Timeouted(
                        new Shell.Safe(sshd.connect()),
                        Duration.ofSeconds(Tv.FIVE)
                    )
                ).exec("echo done"),
                Matchers.startsWith("done")
            );
        } finally {
            sshd.close();
        }
    }

    /**
     * Execute long command on a real SSH server and get timeout.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandTimeout() throws Exception {
        this.thrown.expect(IOException.class);
        this.thrown.expectCause(
            IsInstanceOf.instanceOf(TimeoutException.class)
        );
        final Sshd sshd = new Sshd();
        try {
            new Shell.Plain(
                new Shell.Timeouted(
                    new Shell.Safe(sshd.connect()),
                    Duration.ofMillis(Tv.HUNDRED)
                )
            ).exec("sleep 1");
        } finally {
            sshd.close();
        }
    }

}
