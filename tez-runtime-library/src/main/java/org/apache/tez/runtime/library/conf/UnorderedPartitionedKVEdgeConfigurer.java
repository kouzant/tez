/*
 * *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.tez.runtime.library.conf;

import java.util.Map;

import com.google.common.base.Preconditions;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.tez.dag.api.EdgeManagerDescriptor;
import org.apache.tez.dag.api.EdgeProperty;
import org.apache.tez.dag.api.InputDescriptor;
import org.apache.tez.dag.api.OutputDescriptor;
import org.apache.tez.runtime.library.input.ShuffledUnorderedKVInput;
import org.apache.tez.runtime.library.output.OnFileUnorderedPartitionedKVOutput;

/**
 * Configure payloads for the OnFileUnorderedPartitionedKVOutput and ShuffledUnorderedKVInput pair
 */
@InterfaceAudience.Public
@InterfaceStability.Evolving
public class UnorderedPartitionedKVEdgeConfigurer extends HadoopKeyValuesBasedBaseEdgeConfigurer {

  private final OnFileUnorderedPartitionedKVOutputConfiguration outputConf;
  private final ShuffledUnorderedKVInputConfiguration inputConf;

  private UnorderedPartitionedKVEdgeConfigurer(
      OnFileUnorderedPartitionedKVOutputConfiguration outputConfiguration,
      ShuffledUnorderedKVInputConfiguration inputConfiguration) {
    this.outputConf = outputConfiguration;
    this.inputConf = inputConfiguration;

  }

  /**
   * Create a builder to configure the relevant Input and Output
   * @param keyClassName the key class name
   * @param valueClassName the value class name
   * @param partitionerClassName the partitioner class name
   * @param  partitionerConf the partitioner configuration. Can be null
   * @return a builder to configure the edge
   */
  public static Builder newBuilder(String keyClassName, String valueClassName,
                                   String partitionerClassName, Configuration partitionerConf) {
    return new Builder(keyClassName, valueClassName, partitionerClassName, partitionerConf);
  }

  @Override
  public byte[] getOutputPayload() {
    return outputConf.toByteArray();
  }

  @Override
  public String getOutputClassName() {
    return OnFileUnorderedPartitionedKVOutput.class.getName();
  }

  @Override
  public byte[] getInputPayload() {
    return inputConf.toByteArray();
  }

  @Override
  public String getInputClassName() {
    return ShuffledUnorderedKVInput.class.getName();
  }

  /**
   * This is a convenience method for the typical usage of this edge, and creates an instance of
   * {@link org.apache.tez.dag.api.EdgeProperty} which is likely to be used. </p>
   * If custom edge properties are required, the methods to get the relevant payloads should be
   * used. </p>
   * * In this case - DataMovementType.SCATTER_GATHER, EdgeProperty.DataSourceType.PERSISTED,
   * EdgeProperty.SchedulingType.SEQUENTIAL
   *
   * @return an {@link org.apache.tez.dag.api.EdgeProperty} instance
   */
  public EdgeProperty createDefaultEdgeProperty() {
    EdgeProperty edgeProperty = new EdgeProperty(EdgeProperty.DataMovementType.SCATTER_GATHER,
        EdgeProperty.DataSourceType.PERSISTED, EdgeProperty.SchedulingType.SEQUENTIAL,
        new OutputDescriptor(
            getOutputClassName()).setUserPayload(getOutputPayload()),
        new InputDescriptor(
            getInputClassName()).setUserPayload(getInputPayload()));
    return edgeProperty;
  }

  /**
   * This is a convenience method for creating an Edge descriptor based on the specified
   * EdgeManagerDescriptor.
   *
   * @param edgeManagerDescriptor the custom edge specification
   * @return an {@link org.apache.tez.dag.api.EdgeProperty} instance
   */
  public EdgeProperty createDefaultCustomEdgeProperty(EdgeManagerDescriptor edgeManagerDescriptor) {
    Preconditions.checkNotNull(edgeManagerDescriptor, "EdgeManagerDescriptor cannot be null");
    EdgeProperty edgeProperty =
        new EdgeProperty(edgeManagerDescriptor, EdgeProperty.DataSourceType.PERSISTED,
            EdgeProperty.SchedulingType.SEQUENTIAL,
            new OutputDescriptor(getOutputClassName()).setUserPayload(getOutputPayload()),
            new InputDescriptor(getInputClassName()).setUserPayload(getInputPayload()));
    return edgeProperty;
  }

  @InterfaceAudience.Public
  @InterfaceStability.Evolving
  public static class Builder extends HadoopKeyValuesBasedBaseEdgeConfigurer.Builder<Builder> {

    private final OnFileUnorderedPartitionedKVOutputConfiguration.Builder outputBuilder =
        new OnFileUnorderedPartitionedKVOutputConfiguration.Builder();
    private final OnFileUnorderedPartitionedKVOutputConfiguration.SpecificBuilder<UnorderedPartitionedKVEdgeConfigurer.Builder>
        specificOutputBuilder =
        new OnFileUnorderedPartitionedKVOutputConfiguration.SpecificBuilder<UnorderedPartitionedKVEdgeConfigurer.Builder>(
            this, outputBuilder);

    private final ShuffledUnorderedKVInputConfiguration.Builder inputBuilder =
        new ShuffledUnorderedKVInputConfiguration.Builder();
    private final ShuffledUnorderedKVInputConfiguration.SpecificBuilder<UnorderedPartitionedKVEdgeConfigurer.Builder>
        specificInputBuilder =
        new ShuffledUnorderedKVInputConfiguration.SpecificBuilder<UnorderedPartitionedKVEdgeConfigurer.Builder>(
            this, inputBuilder);

    @InterfaceAudience.Private
    Builder(String keyClassName, String valueClassName, String partitionerClassName,
            Configuration partitionerConf) {
      outputBuilder.setKeyClassName(keyClassName);
      outputBuilder.setValueClassName(valueClassName);
      outputBuilder.setPartitioner(partitionerClassName, partitionerConf);
      inputBuilder.setKeyClassName(keyClassName);
      inputBuilder.setValueClassName(valueClassName);
    }

    @Override
    public Builder enableCompression(String compressionCodec) {
      outputBuilder.enableCompression(compressionCodec);
      inputBuilder.enableCompression(compressionCodec);
      return this;
    }

    @Override
    public Builder setAdditionalConfiguration(String key, String value) {
      outputBuilder.setAdditionalConfiguration(key, value);
      inputBuilder.setAdditionalConfiguration(key, value);
      return this;
    }

    @Override
    public Builder setAdditionalConfiguration(Map<String, String> confMap) {
      outputBuilder.setAdditionalConfiguration(confMap);
      inputBuilder.setAdditionalConfiguration(confMap);
      return this;
    }

    @Override
    public Builder setFromConfiguration(Configuration conf) {
      outputBuilder.setFromConfiguration(conf);
      inputBuilder.setFromConfiguration(conf);
      return this;
    }

    /**
     * Set serialization class responsible for providing serializer/deserializer for keys.
     *
     * @param serializationClassName
     * @return
     */
    public Builder setKeySerializationClass(String serializationClassName) {
      outputBuilder.setKeySerializationClass(serializationClassName);
      inputBuilder.setKeySerializationClass(serializationClassName);
      return this;
    }

    /**
     * Set serialization class responsible for providing serializer/deserializer for values.
     *
     * @param serializationClassName
     * @return
     */
    public Builder setValueSerializationClass(String serializationClassName) {
      outputBuilder.setValueSerializationClass(serializationClassName);
      inputBuilder.setValueSerializationClass(serializationClassName);
      return this;
    }

    /**
     * Configure the specific output
     *
     * @return a builder to configure the output
     */
    public OnFileUnorderedPartitionedKVOutputConfiguration.SpecificBuilder<Builder> configureOutput() {
      return specificOutputBuilder;
    }

    /**
     * Configure the specific input
     * @return a builder to configure the input
     */
    public ShuffledUnorderedKVInputConfiguration.SpecificBuilder<Builder> configureInput() {
      return specificInputBuilder;
    }

    /**
     * Build and return an instance of the configuration
     * @return an instance of the acatual configuration
     */
    public UnorderedPartitionedKVEdgeConfigurer build() {
      return new UnorderedPartitionedKVEdgeConfigurer(outputBuilder.build(), inputBuilder.build());
    }

  }
}
