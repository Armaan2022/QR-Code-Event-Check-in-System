package com.example.qrcheckin;

import org.junit.Test;
import static org.junit.Assert.*;
import android.os.Bundle;

public class EventUtilsTest {

    @Test
    public void testIsAttendee_WhenBundleIsNull() {
        assertFalse(EventUtils.isAttendee(null));
    }
}

