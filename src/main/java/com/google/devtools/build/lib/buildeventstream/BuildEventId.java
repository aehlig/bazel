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

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.cmdline.TargetPattern;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Class of identifiers for publically posted events.
 *
 * Since event identifiers need to be created before the actual event, the event IDs are highly
 * structured so that equal identifiers can easily be generated. The main way of pregenerating event
 * identifiers that do not accidentally coincide is by providing a target or a target pattern;
 * therefore, those (if provided) are made specially visible.
 */
@Immutable
public final class BuildEventId implements Serializable {
  private final BuildEventStreamProtos.BuildEventType eventType;
  private final String opaque;
  private final int opaqueCount;
  @Nullable private final List<String> targetPattern;
  @Nullable private final Label target;

  @Override
  public int hashCode() {
    return Objects.hash(eventType, targetPattern, target, opaque, opaqueCount);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !other.getClass().equals(getClass())) {
      return false;
    }
    BuildEventId that = (BuildEventId) other;
    return Objects.equals(this.eventType, that.eventType)
        && Objects.equals(this.targetPattern, that.targetPattern)
        && Objects.equals(this.target, that.target)
        && Objects.equals(this.opaque, that.opaque)
        && Objects.equals(this.opaqueCount, that.opaqueCount);
  }

  @Override
  public String toString() {
    String id = "[ID|" + eventType + "|";
    if (target != null) {
      id += target.toString();
    }
    id += "|";
    if (targetPattern != null) {
      for (String s : targetPattern) {
        id += "'" + s + "'";
      }
    }
    id += "|" + opaque + " - " + opaqueCount + "]";
    return id;
  }

  public BuildEventStreamProtos.BuildEventId asStreamProto() {
    BuildEventStreamProtos.BuildEventId.Builder builder =
        BuildEventStreamProtos.BuildEventId
        .newBuilder()
        .setEventType(eventType)
        .setOpaque(opaque)
        .setOpaqueCount(opaqueCount);
    if (target != null) {
      builder.setTarget(target.toString());
    }
    if (targetPattern != null) {
      for (String s : targetPattern) {
        builder.addTargetPattern(s);
      }
    }
    return builder.build();
  }

  public BuildEventId(BuildEventStreamProtos.BuildEventType eventType, String opaque,
      int opaqueCount) {
    this.eventType = eventType;
    this.targetPattern = null;
    this.target = null;
    this.opaque = opaque;
    this.opaqueCount = opaqueCount;
  }

  public BuildEventId(BuildEventStreamProtos.BuildEventType eventType, Label target,
      String opaque, int opaqueCount) {
    this.eventType = eventType;
    this.targetPattern = null;
    this.target = target;
    this.opaque = opaque;
    this.opaqueCount = opaqueCount;
  }

  public BuildEventId(BuildEventStreamProtos.BuildEventType eventType,
      List<String> targetPattern, String opaque, int opaqueCount) {
    this.eventType = eventType;
    this.targetPattern = targetPattern;
    this.target = null;
    this.opaque = opaque;
    this.opaqueCount = opaqueCount;
  }

  public Label getTarget() {
    return target;
  }

  public List<String> getTargetPattern() {
    return targetPattern;
  }

  public static BuildEventId targetPatternExpanded(List<String> targetPattern) {
    return new BuildEventId(
        BuildEventStreamProtos.BuildEventType.PATTERN_EXPANDED, targetPattern, "", 0);
  }
}

