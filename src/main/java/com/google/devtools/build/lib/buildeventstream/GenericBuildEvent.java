// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.buildeventstream;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

/**
 * Class for a generic {@link BuildEvent}.
 *
 * This class implements a basic {@link BuildEvent}. Events of this class are mainly
 * used for infrastructural evnts (progress, aborted build, etc).
 */
public class GenericBuildEvent implements BuildEvent {
  BuildEventId id;
  Collection<BuildEventId> children;
  String eventText;

  @Override
  public BuildEventId getEventId() {
    return id;
  }

  @Override
  public Collection<BuildEventId> getChildrenEvents() {
    return children;
  }

  public static String textChaining(ChainableEvent event) {
    String text = event.getEventId().toString() + "\n";
    for (BuildEventId id : event.getChildrenEvents()) {
      text += "    " + id.toString() + "\n";
    }
    return text;
  }

  @Override
  public String getTextRepresentation() {
    return textChaining(this) + "  " + eventText + "\n";
  }

  public GenericBuildEvent(BuildEventId id, Collection<BuildEventId> children, String eventText) {
    this.id = id;
    this.children = children;
    this.eventText = eventText;
  }

  public static BuildEvent buildAborted(BuildEventId id) {
    return new GenericBuildEvent(id, ImmutableList.of(), "ABORTED");
  }
}

