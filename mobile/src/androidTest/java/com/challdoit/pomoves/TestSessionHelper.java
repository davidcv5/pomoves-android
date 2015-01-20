package com.challdoit.pomoves;

import android.test.AndroidTestCase;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;

import java.util.Date;

/**
 * Created by admin on 12/19/14.
 */
public class TestSessionHelper extends AndroidTestCase {

    public void testSessionHelper() {
        Session session = new Session();

        assertTrue(session.getId() == -1);

        SessionHelper.insert(getContext(), session);

        assertTrue(session.getId() > 0);

        final Date now = new Date();
        final String testStats = "some stats";

        session.setDate(now);
        session.getStats().info = testStats;

        SessionHelper.update(getContext(), session);

        Session loadedSession = SessionHelper.load(getContext(), session.getId());

        assertNotNull(loadedSession);

        assertEquals(
                PomovesContract.getDbDateString(now),
                PomovesContract.getDbDateString(loadedSession.getDate()));
        assertEquals(testStats, loadedSession.getStats().info);
    }
}
