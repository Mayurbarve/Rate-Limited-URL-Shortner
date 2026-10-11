package com.shortly.code;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62CounterGeneratorTest {
    @Test void base62RepresentsValuesAcrossDigits() {
        assertEquals("0",Base62.encode(0)); assertEquals("z",Base62.encode(61)); assertEquals("10",Base62.encode(62));
    }
}
