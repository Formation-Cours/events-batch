package com.formation.events_batch.batch.users;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class UserItemPartitioner implements Partitioner {

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> partitions = new HashMap<>();

    for (int i = 0; i < gridSize; i++) {
      ExecutionContext context = new ExecutionContext();
      context.put("partitionNumber", i);
      context.put("totalPartitions", gridSize);
      partitions.put("partition", context);
    }

    return partitions;
  }

}
