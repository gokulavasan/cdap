package com.continuuity.data2.transaction.stream;

import com.continuuity.common.queue.QueueName;
import com.continuuity.data2.dataset.api.DataSetManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public interface StreamAdmin extends DataSetManager {

  /**
   * Deletes all entries for all queues.
   */
  void dropAll() throws Exception;

  /**
   * Sets the number of consumer instances for the given consumer group in a queue.
   * @param streamName Name of the stream.
   * @param groupId The consumer group to alter.
   * @param instances Number of instances.
   */
  void configureInstances(QueueName streamName, long groupId, int instances) throws Exception;


  /**
   * Sets the consumer groups information for the given queue.
   * @param streamName Name of the stream.
   * @param groupInfo A map from groupId to number of instances of each group.
   */
  void configureGroups(QueueName streamName, Map<Long, Integer> groupInfo) throws Exception;

  /**
   * Performs upgrade action for all streams.
   */
  void upgrade() throws Exception;

  /**
   * @param accountId User account id.
   * @return Configurations of all streams.
   */
  Collection<StreamConfig> getAll(String accountId);

  /**
   * Returns the configuration of the given stream.
   * @param accountId User account id.
   * @param streamName Name of the stream.
   * @return A {@link StreamConfig} instance.
   * @throws IOException If the stream doesn't exists.
   */
  StreamConfig getConfig(String accountId, String streamName) throws IOException;
}
