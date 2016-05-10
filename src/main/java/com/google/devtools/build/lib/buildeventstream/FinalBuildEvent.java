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
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;

import java.util.Collection;

/**
 * Wrapper to make a build event the final for a build.
 */
public class FinalBuildEvent implements BuildEvent{
  final BuildEvent event;

  public FinalBuildEvent(BuildEvent event) {
    this.event = event;
  }

  @Override
  public BuildEventId getEventId() {
    return event.getEventId();
  }

  @Override
  public Collection<BuildEventId> getChildrenEvents() {
    return ImmutableList.of();
  }


  @Override
  public BuildEventStreamProtos.BuildEvent asStreamProto() {
    return event.asStreamProto().toBuilder().setFinalEvent(true).build();
  }
}

