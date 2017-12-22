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
package ch.vorburger.osgi.builder.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.slf4j.Logger;

/**
 * {@link OutputStream} which logs to slf4j.
 *
 * @author Michael Vorburger
 */
public class LoggingOutputStream extends PrintStream {

    public static enum Level { ERROR, WARN, INFO, DEBUG, TRACE }

    public LoggingOutputStream(Logger logger, Level level) {
        super(new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                super.flush();
                switch (level) {
                case ERROR:
                    logger.error(toString());
                    break;
                case WARN:
                    logger.warn(toString());
                    break;
                case INFO:
                    logger.info(toString());
                    break;
                case DEBUG:
                    logger.debug(toString());
                    break;
                case TRACE:
                    logger.trace(toString());
                    break;
                default:
                    break;
                }
            }

            @Override
            public synchronized String toString() {
                String string;
                if (buf[count - 1] == '\n') {
                    string = new String(buf, 0, count - 1);
                } else {
                    string = super.toString();
                }
                reset();
                return string;
            }
        }, true);
    }

    @Override
    public void write(byte buf[], int off, int len) {
        try {
            synchronized (this) {
                // ensureOpen();
                out.write(buf, off, len);
                // DO NOT if (autoFlush) { out.flush(); }
            }
        } catch (InterruptedIOException x) {
            Thread.currentThread().interrupt();
        } catch (IOException x) {
            // trouble = true;
        }
    }

}
