// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.lib.buildeventstream.BuildEvent;
import com.google.devtools.build.lib.buildeventstream.BuildEventId;
import com.google.devtools.build.lib.buildeventstream.BuildEventTransport;
import com.google.devtools.build.lib.buildeventstream.GenericBuildEvent;
import com.google.devtools.build.lib.buildeventstream.ProgressEvent;
import com.google.devtools.build.lib.buildtool.buildevent.BuildCompleteEvent;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests {@link BuildEventStreamer}.
 */
@RunWith(JUnit4.class)
public class BuildEventStreamerTest {

  private class RecordingBuildEventTransport implements BuildEventTransport {
    private List<BuildEvent> events;

    RecordingBuildEventTransport() {
      events = new ArrayList<>();
    }

    @Override
    public void sendBuildEvent(BuildEvent event) {
      events.add(event);
    }

    @Override
    public void close() {}

    List<BuildEvent> getEvents() {
      return events;
    }
  }


  @Test
  public void testSimpleStream() {
    // Verify that a well-formed event is passed through and that completion of the
    // build clears the pending progress-update event.

    RecordingBuildEventTransport transport = new RecordingBuildEventTransport();
    BuildEventStreamer streamer = new BuildEventStreamer(ImmutableSet.of(transport));

    BuildEvent startEvent = new GenericBuildEvent(
        new BuildEventId("Initial"),
        ImmutableSet.of(ProgressEvent.INITIAL_PROGRESS_UPDATE),
        "initial event");

    streamer.buildEvent(startEvent);

    List<BuildEvent> afterFirstEvent = transport.getEvents();
    assertEquals(1, afterFirstEvent.size());
    assertEquals(startEvent, afterFirstEvent.get(0));

    streamer.buildComplete(new BuildCompleteEvent(null));

    List<BuildEvent> finalStream = transport.getEvents();
    assertEquals(2, finalStream.size());
    assertEquals(ProgressEvent.INITIAL_PROGRESS_UPDATE, finalStream.get(1).getEventId());
  }

  @Test
  public void testChaining() {
    // Verify that unannounced events are linked in with progress update events, assuming
    // a correctly formed initial event.

    RecordingBuildEventTransport transport = new RecordingBuildEventTransport();
    BuildEventStreamer streamer = new BuildEventStreamer(ImmutableSet.of(transport));

    BuildEvent startEvent = new GenericBuildEvent(
        new BuildEventId("Initial"),
        ImmutableSet.of(ProgressEvent.INITIAL_PROGRESS_UPDATE),
        "initial event");
    BuildEvent unexpectedEvent = new GenericBuildEvent(
        new BuildEventId("unexpected"),
        ImmutableSet.of(),
        "unexpected event");

    streamer.buildEvent(startEvent);
    streamer.buildEvent(unexpectedEvent);

    List<BuildEvent> eventsSeen = transport.getEvents();
    assertEquals(3, eventsSeen.size());
    assertEquals(startEvent, eventsSeen.get(0));
    assertEquals(unexpectedEvent, eventsSeen.get(2));
    BuildEvent linkEvent = eventsSeen.get(1);
    assertEquals(ProgressEvent.INITIAL_PROGRESS_UPDATE, linkEvent.getEventId());
    assertTrue(
        "Unexpected events should be linked",
        linkEvent.getChildrenEvents().contains(unexpectedEvent.getEventId()));
  }

  @Test
  public void testBadInitialEvent() {
    // Verify that, if the initial event does not announce the initial progress update event,
    // the initial progress event is used instead to chain that event; in this way, new
    // progress updates can always be chained in.

    RecordingBuildEventTransport transport = new RecordingBuildEventTransport();
    BuildEventStreamer streamer = new BuildEventStreamer(ImmutableSet.of(transport));

    BuildEvent unexpectedStartEvent = new GenericBuildEvent(
        new BuildEventId("unexpected start"),
        ImmutableSet.of(),
        "unexpected event at the beginning");

    streamer.buildEvent(unexpectedStartEvent);

    List<BuildEvent> eventsSeen = transport.getEvents();
    assertEquals(2, eventsSeen.size());
    assertEquals(unexpectedStartEvent, eventsSeen.get(1));
    BuildEvent initial = eventsSeen.get(0);
    assertEquals(ProgressEvent.INITIAL_PROGRESS_UPDATE, initial.getEventId());
    assertTrue(
        "Event should be linked",
        initial.getChildrenEvents().contains(unexpectedStartEvent.getEventId()));

    // The initial event should also announce a new progress event; we test this
    // by streaming another unannounced event.

    BuildEvent unexpectedEvent = new GenericBuildEvent(
        new BuildEventId("unexpected"),
        ImmutableSet.of(),
        "unexpected event");

    streamer.buildEvent(unexpectedEvent);
    List<BuildEvent> allEventsSeen = transport.getEvents();
    assertEquals(4, allEventsSeen.size());
    assertEquals(unexpectedEvent, allEventsSeen.get(3));
    BuildEvent secondLinkEvent = allEventsSeen.get(2);
    assertTrue(
        "Progress should have been announced",
        initial.getChildrenEvents().contains(secondLinkEvent.getEventId()));
    assertTrue(
        "Second event should be linked",
        secondLinkEvent.getChildrenEvents().contains(unexpectedEvent.getEventId()));
  }
}
