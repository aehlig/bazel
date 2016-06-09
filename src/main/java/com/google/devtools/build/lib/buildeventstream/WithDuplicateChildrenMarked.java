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

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Wrapper around a {@link BuildEvent} setting the already_announced bit.
 *
 * Given a {@link BuildEvent} and a set of already known {@link BuildEventId}s,
 * Create a new one with the already_announced bit in the children events set
 * correctly. The provided set is only looked at in the constructor, so it is
 * safe to modify it afterwards.
 */
public class WithDuplicateChildrenMarked implements BuildEvent {
  private final BuildEventId id;
  private final Collection<BuildEventId> children;
  private final BuildEventStreamProtos.BuildEvent streamProto;

  public WithDuplicateChildrenMarked(BuildEvent event, Set<BuildEventId> knownChildren) {
    this.id = event.getEventId();
    this.children = event.getChildrenEvents();
    BuildEventStreamProtos.BuildEvent.Builder streamProtoBuilder =
        event.asStreamProto().toBuilder().clearChildren();
    for (BuildEventId childId : this.children) {
      streamProtoBuilder.addChildren(
          BuildEventStreamProtos.ChildEventId.newBuilder()
          .setId(childId.asStreamProto())
          .setAlreadyAnnounced(knownChildren.contains(childId))
          .build());
    }
    this.streamProto = streamProtoBuilder.build();
  }

  @Override
  public BuildEventId getEventId() {
    return id;
  }

  @Override
  public Collection<BuildEventId> getChildrenEvents() {
    return children;
  }

  @Override
  public BuildEventStreamProtos.BuildEvent asStreamProto() {
    return streamProto;
  }
}

