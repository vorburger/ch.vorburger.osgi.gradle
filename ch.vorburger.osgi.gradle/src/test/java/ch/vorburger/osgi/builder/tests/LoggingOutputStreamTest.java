/**
 * ch.vorburger.osgi.gradle
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
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
package ch.vorburger.osgi.builder.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.vorburger.osgi.builder.internal.LoggingOutputStream;
import ch.vorburger.osgi.builder.internal.LoggingOutputStream.Level;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.Test;
import org.slf4j.event.EventRecodingLogger;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

/**
 * LoggingOutputStream unit test.
 *
 * @author Michael Vorburger
 */
public class LoggingOutputStreamTest {

    @Test
    public void testLoggingOutputStream() {
        Queue<SubstituteLoggingEvent> queue = new LinkedList<>();
        LoggingOutputStream loggingOutputStream = new LoggingOutputStream(
                new EventRecodingLogger(
                        new SubstituteLogger("noop", null, true),
                        queue),
                Level.ERROR);
        assertTrue(queue.isEmpty());
        loggingOutputStream.append('h');
        assertTrue(queue.isEmpty());
        loggingOutputStream.write('e');
        assertTrue(queue.isEmpty());
        loggingOutputStream.print("llo, ");
        assertTrue(queue.isEmpty());
        loggingOutputStream.println("world");
        assertFalse(queue.isEmpty());
        assertEquals("hello, world", queue.remove().getMessage());
        assertTrue(queue.isEmpty());
        loggingOutputStream.println("another message");
        assertEquals("another message", queue.remove().getMessage());
        loggingOutputStream.close();
    }

}
