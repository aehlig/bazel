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

package com.google.devtools.build.lib.actions;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.buildeventstream.BuildEvent;
import com.google.devtools.build.lib.buildeventstream.BuildEventId;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.buildeventstream.GenericBuildEvent;

import java.util.Collection;

/**
 * This event is fired during the build, when an action is executed. It contains information about
 * the action: the Action itself, and the output file names its stdout and stderr are recorded in.
 */
public class ActionExecutedEvent implements BuildEvent {
  private final Action action;
  private final ActionExecutionException exception;
  private final String stdout;
  private final String stderr;

  public ActionExecutedEvent(Action action,
      ActionExecutionException exception, String stdout, String stderr) {
    this.action = action;
    this.exception = exception;
    this.stdout = stdout;
    this.stderr = stderr;
  }

  public Action getAction() {
    return action;
  }

  // null if action succeeded
  public ActionExecutionException getException() {
    return exception;
  }

  public String getStdout() {
    return stdout;
  }

  public String getStderr() {
    return stderr;
  }

  @Override
  public BuildEventId getEventId() {
    return BuildEventId.actionCompleted(getAction().getOwner().getLabel());
  }

  @Override
  public Collection<BuildEventId> getChildrenEvents() {
    return ImmutableList.<BuildEventId>of();
  }

  @Override
  public BuildEventStreamProtos.BuildEvent asStreamProto() {
    BuildEventStreamProtos.ActionExecuted.Builder actionBuilder =
        BuildEventStreamProtos.ActionExecuted.newBuilder()
        .setSuccess(getException() == null);
    if (stdout != null) {
      actionBuilder.setStdout(stdout);
    }
    if (stderr != null) {
      actionBuilder.setStdout(stderr);
    }
    return GenericBuildEvent.protoChaining(this).setAction(actionBuilder.build()).build();
  }
}
