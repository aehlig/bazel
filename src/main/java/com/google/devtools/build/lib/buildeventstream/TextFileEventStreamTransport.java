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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A simple {@link BuildEventTransport} that writes the text representation of all
 * events to a text file.
 */
public class TextFileEventStreamTransport implements BuildEventTransport {
  private FileOutputStream out;

  public TextFileEventStreamTransport(String path) {
    try {
      this.out = new FileOutputStream(new File(path));
    } catch (IOException e) {
      this.out = null;
    }
  }

  @Override
  public synchronized void sendBuildEvent(BuildEvent event) {
    try {
      if (out != null) {
        out.write(event.getTextRepresentation().getBytes(StandardCharsets.UTF_8));
        out.flush();
      }
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public void close() {
    try {
      if (out != null) {
        out.close();
      }
    } catch (IOException e) {
      // ignore
    }
  }
}
