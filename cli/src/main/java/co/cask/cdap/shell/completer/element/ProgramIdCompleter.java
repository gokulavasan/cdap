/*
 * Copyright © 2012-2014 Cask Data, Inc.
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

package co.cask.cdap.shell.completer.element;

import co.cask.cdap.client.ApplicationClient;
import co.cask.cdap.client.exception.UnAuthorizedAccessTokenException;
import co.cask.cdap.proto.ProgramRecord;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.shell.completer.StringsCompleter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Completer for program IDs.
 */
public abstract class ProgramIdCompleter extends StringsCompleter {

  private final ApplicationClient appClient;

  public ProgramIdCompleter(final ApplicationClient appClient) {
    this.appClient = appClient;
  }

  @Override
  protected Supplier<Collection<String>> getStringsSupplier() {
    return Suppliers.memoizeWithExpiration(new Supplier<Collection<String>>() {
      @Override
      public Collection<String> get() {
        try {
          List<ProgramRecord> programs = appClient.listAllPrograms(getProgramType());
          List<String> programIds = Lists.newArrayList();
          for (ProgramRecord programRecord : programs) {
            programIds.add(programRecord.getApp() + "." + programRecord.getId());
          }
          return programIds;
        } catch (IOException e) {
          return Lists.newArrayList();
        } catch (UnAuthorizedAccessTokenException e) {
          return Lists.newArrayList();
        }
      }
    }, 3, TimeUnit.SECONDS);
  }

  public abstract ProgramType getProgramType();
}
