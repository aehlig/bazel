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

/**
 * Interface for objects that can be posted on the public event stream.
 *
 * Objects posted on the build-event stream will implement this interface. This allows
 * pass-through of events, as well as proper chaining of events.
 */
public interface BuildEvent extends ChainableEvent {
  /**
   * Provide a text presentation of the event.
   *
   * Provide a presentation of the event containing the most important information, including
   * chaining. The presentation is supposed to start with a non-whitespace character and end with a
   * newline character. If more than one line is used, all but the first line have to start with a
   * whitespace character to indicate continuation lines.
   *
   * While this representation is mainly used for debugging, still care should be taken that
   * it can be read properly.
   */
  String getTextRepresentation();
}

