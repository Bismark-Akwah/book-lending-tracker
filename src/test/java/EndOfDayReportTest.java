import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Runs the demo program and compares its output with the approved result. */
class EndOfDayReportTest {

    @Test
    void demoOutputMatchesApprovedResult() throws Exception {
        String expected = normalise(Files.readString(
                Path.of("src/test/resources/golden-rebuilt.txt"), StandardCharsets.UTF_8));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            LendingTracker.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(expected, normalise(captured.toString(StandardCharsets.UTF_8)));
    }

    private static String normalise(String text) {
        return text.replace("\r\n", "\n").strip();
    }
}