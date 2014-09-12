/*
 * Copyright 2014 Cask Data, Inc.
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

package co.cask.cdap.client;

import co.cask.cdap.client.config.ClientConfig;
import co.cask.cdap.client.exception.ApplicationNotFoundException;
import co.cask.cdap.client.exception.UnAuthorizedAccessTokenException;
import co.cask.cdap.client.util.RESTClient;
import co.cask.cdap.common.http.HttpMethod;
import co.cask.cdap.common.http.HttpRequest;
import co.cask.cdap.common.http.HttpResponse;
import co.cask.cdap.common.http.ObjectResponse;
import co.cask.cdap.proto.ApplicationRecord;
import co.cask.cdap.proto.ProgramRecord;
import co.cask.cdap.proto.ProgramType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Provides ways to interact with CDAP applications.
 */
public class ApplicationClient {

  private final RESTClient restClient;
  private final ClientConfig config;

  @Inject
  public ApplicationClient(ClientConfig config) {
    this.config = config;
    this.restClient = RESTClient.create(config);
  }

  /**
   * Lists all applications currently deployed.
   *
   * @return list of {@link ApplicationRecord}s.
   * @throws IOException
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public List<ApplicationRecord> list() throws IOException, UnAuthorizedAccessTokenException {
    HttpResponse response = restClient.execute(HttpMethod.GET, config.resolveURL("apps"), config.getAccessToken());
    return ObjectResponse.fromJsonBody(response, new TypeToken<List<ApplicationRecord>>() { }).getResponseObject();
  }

  /**
   * Deletes an application.
   *
   * @param appId ID of the application to delete
   * @throws ApplicationNotFoundException if the application with the given ID was not found
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public void delete(String appId) throws ApplicationNotFoundException, IOException, UnAuthorizedAccessTokenException {
    HttpResponse response = restClient.execute(HttpMethod.DELETE, config.resolveURL("apps/" + appId),
                                               config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ApplicationNotFoundException(appId);
    }
  }

  /**
   * Deploys an application.
   *
   * @param jarFile jar file of the application to deploy
   * @throws IOException if a network error occurred
   */
  public void deploy(File jarFile) throws IOException {
    URL url = config.resolveURL("apps");
    Map<String, String> headers = ImmutableMap.of("X-Archive-Name", jarFile.getName());

    HttpRequest request = HttpRequest.post(url).addHeaders(headers).withBody(jarFile).build();
    restClient.upload(request, config.getAccessToken());
  }

  /**
   * Promotes an application to another environment.
   *
   * @param appId ID of the application to promote
   * @throws ApplicationNotFoundException if the application with the given ID was not found
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public void promote(String appId) throws ApplicationNotFoundException, IOException,
    UnAuthorizedAccessTokenException {
    URL url = config.resolveURL("apps/" + appId);

    HttpResponse response = restClient.execute(HttpMethod.POST, url, config.getAccessToken(),
                                               HttpURLConnection.HTTP_NOT_FOUND);
    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ApplicationNotFoundException(appId);
    }
  }

  /**
   * Lists all programs of some type.
   *
   * @param programType type of the programs to list
   * @return list of {@link ProgramRecord}s
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public List<ProgramRecord> listAllPrograms(ProgramType programType) throws IOException,
    UnAuthorizedAccessTokenException {

    Preconditions.checkArgument(programType.isListable());

    URL url = config.resolveURL(programType.getCategoryName());
    HttpRequest request = HttpRequest.get(url).build();

    ObjectResponse<List<ProgramRecord>> response = ObjectResponse.fromJsonBody(
      restClient.execute(request, config.getAccessToken()), new TypeToken<List<ProgramRecord>>() { });

    return response.getResponseObject();
  }

  /**
   * Lists all programs.
   *
   * @return list of {@link ProgramRecord}s
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public Map<ProgramType, List<ProgramRecord>> listAllPrograms() throws IOException, UnAuthorizedAccessTokenException {

    ImmutableMap.Builder<ProgramType, List<ProgramRecord>> allPrograms = ImmutableMap.builder();
    for (ProgramType programType : ProgramType.values()) {
      if (programType.isListable()) {
        List<ProgramRecord> programRecords = Lists.newArrayList();
        programRecords.addAll(listAllPrograms(programType));
        allPrograms.put(programType, programRecords);
      }
    }
    return allPrograms.build();
  }


  /**
   * Lists programs of some type belonging to an application.
   *
   * @param appId ID of the application
   * @param programType type of the programs to list
   * @return list of {@link ProgramRecord}s
   * @throws ApplicationNotFoundException if the application with the given ID was not found
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public List<ProgramRecord> listPrograms(String appId, ProgramType programType)
    throws ApplicationNotFoundException, IOException, UnAuthorizedAccessTokenException {
    Preconditions.checkArgument(programType.isListable());

    URL url = config.resolveURL(String.format("apps/%s/%s", appId, programType.getCategoryName()));
    HttpRequest request = HttpRequest.get(url).build();

    ObjectResponse<List<ProgramRecord>> response = ObjectResponse.fromJsonBody(
      restClient.execute(request, config.getAccessToken(), HttpURLConnection.HTTP_NOT_FOUND),
      new TypeToken<List<ProgramRecord>>() { });

    if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new ApplicationNotFoundException(appId);
    }

    return response.getResponseObject();
  }

  /**
   * Lists programs belonging to an application.
   *
   * @param appId ID of the application
   * @return Map of {@link ProgramType} to list of {@link ProgramRecord}s
   * @throws ApplicationNotFoundException if the application with the given ID was not found
   * @throws IOException if a network error occurred
   * @throws UnAuthorizedAccessTokenException if the request is not authorized successfully in the gateway server
   */
  public Map<ProgramType, List<ProgramRecord>> listPrograms(String appId)
    throws ApplicationNotFoundException, IOException, UnAuthorizedAccessTokenException {

    ImmutableMap.Builder<ProgramType, List<ProgramRecord>> allPrograms = ImmutableMap.builder();
    for (ProgramType programType : ProgramType.values()) {
      if (programType.isListable()) {
        List<ProgramRecord> programRecords = Lists.newArrayList();
        programRecords.addAll(listPrograms(appId, programType));
        allPrograms.put(programType, programRecords);
      }
    }
    return allPrograms.build();
  }
}
