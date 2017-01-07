package ch.vorburger.osgi.gradle.internal.tests;

import static com.google.common.truth.Truth.assertThat;
import ch.vorburger.osgi.gradle.internal.LoggingOutputStream;
import ch.vorburger.osgi.gradle.internal.LoggingOutputStream.Level;
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
        assertThat(queue).isEmpty();
        loggingOutputStream.append('h');
        assertThat(queue).isEmpty();
        loggingOutputStream.write('e');
        assertThat(queue).isEmpty();
        loggingOutputStream.print("llo, ");
        assertThat(queue).isEmpty();
        loggingOutputStream.println("world");
        assertThat(queue).isNotEmpty();
        assertThat(queue.remove().getMessage()).isEqualTo("hello, world");
        assertThat(queue).isEmpty();
        loggingOutputStream.println("another message");
        assertThat(queue.remove().getMessage()).isEqualTo("another message");
        loggingOutputStream.close();
    }

}
