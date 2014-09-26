/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.deploy;

import co.cask.cdap.app.deploy.ConfigResponse;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.annotation.Nullable;

/**
 * This is implementation of {@link ConfigResponse}.
 * <p>
 * Immutable class that hold exit code and stream of input.
 * </p>
 */
public final class DefaultConfigResponse implements ConfigResponse {
  private final int exit;
  private final InputSupplier<? extends Reader> input;

  /**
   * Constructor.
   *
   * @param exit  code returned from processing a command.
   * @param input that streams in data generated by running command.
   */
  public DefaultConfigResponse(int exit, InputSupplier<? extends Reader> input) {
    this.exit = exit;
    this.input = input;
  }

  /**
   * @return A handle to {@link InputStream} if execution was successfully; else null.
   */
  @Override
  @Nullable
  public Reader get() throws IOException {
    if (input != null) {
      return input.getInput();
    }
    return null;
  }

  /**
   * @return Exit code of command that was executed.
   */
  @Override
  public int getExitCode() {
    return exit;
  }
}