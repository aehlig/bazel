// Copyright 2014 The Bazel Authors. All rights reserved.
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

package com.google.devtools.build.lib.buildtool.buildevent;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.buildtool.BuildRequest;
import com.google.devtools.build.lib.buildeventstream.BuildEvent;
import com.google.devtools.build.lib.buildeventstream.BuildEventId;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.buildeventstream.GenericBuildEvent;
import com.google.devtools.build.lib.buildeventstream.ProgressEvent;

import java.util.Collection;

/**
 * This event is fired from BuildTool#startRequest().
 * At this point, the set of target patters are known, but have
 * yet to be parsed.
 */
public class BuildStartingEvent implements BuildEvent {
  private final String outputFileSystem;
  private final BuildRequest request;

  /**
   * Construct the BuildStartingEvent.
   * @param request the build request.
   */
  public BuildStartingEvent(String outputFileSystem, BuildRequest request) {
    this.outputFileSystem = outputFileSystem;
    this.request = request;
  }

  /**
   * @return the output file system.
   */
  public String getOutputFileSystem() {
    return outputFileSystem;
  }

  /**
   * @return the active BuildRequest.
   */
  public BuildRequest getRequest() {
    return request;
  }

  @Override
  public BuildEventId getEventId() {
    return new BuildEventId("START");
  }

  @Override
  public Collection<BuildEventId> getChildrenEvents() {
    return ImmutableList.of(
        ProgressEvent.INITIAL_PROGRESS_UPDATE,
        BuildEventId.targetPatternExpanded(request.getTargets()));
  }

  @Override
  public BuildEventStreamProtos.BuildEvent asStreamProto() {
    BuildEventStreamProtos.BuildStarted started =
        BuildEventStreamProtos.BuildStarted.newBuilder()
        .setUuid(request.getId().toString())
        .setStartTime(request.getStartTime())
        .setOptionsDescription(request.getOptionsDescription())
        .build();
    return GenericBuildEvent.protoChaining(this).setStarted(started).build();
  }
}
