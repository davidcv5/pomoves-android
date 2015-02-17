package com.challdoit.pomoves;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.challdoit.pomoves.data.PomovesContract;
import com.challdoit.pomoves.model.Event;
import com.challdoit.pomoves.model.EventHelper;
import com.challdoit.pomoves.model.Session;
import com.challdoit.pomoves.model.SessionHelper;

import java.util.Date;

public class TestEventHelper extends AndroidTestCase {

    public void testDeleteAllRecords() {
        mContext.getContentResolver().delete(
                PomovesContract.EventEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(
                PomovesContract.SessionEntry.CONTENT_URI, null, null);

        Cursor cursor = mContext.getContentResolver().query(
                PomovesContract.SessionEntry.CONTENT_URI, null, null, null, null);
        assertTrue(cursor.getCount() == 0);

        cursor.close();

        cursor = mContext.getContentResolver().query(
                PomovesContract.EventEntry.CONTENT_URI, null, null, null, null);
        assertTrue(cursor.getCount() == 0);
    }

    public void testEventHelper() {
        Session session = new Session();
        SessionHelper.insert(getContext(), session);

        Event event = new Event(session.getId(), Event.POMODORO);
        EventHelper.insert(getContext(), event);

        assertTrue(event.getId() > 0);

        Event loadedEvent = EventHelper.load(getContext(), event.getId());

        assertEquals(Event.POMODORO, loadedEvent.getEventType());

        Date startDate = new Date(event.getStartDate().getTime() + 5000);

        loadedEvent.setStartDate(startDate);

        EventHelper.update(getContext(), loadedEvent);

        Event updatedEvent = EventHelper.load(getContext(), loadedEvent.getId());

        assertTrue(event.getStartDate().before(updatedEvent.getStartDate()));

        EventHelper.delete(getContext(), event.getId());

        Event deleted = EventHelper.load(getContext(), event.getId());

        assertNull(deleted);

    }
}
