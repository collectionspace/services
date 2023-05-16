This directory contains the built-in batch jobs that are distributed with CollectionSpace.

To add a batch job:

1. Add the batch job's Java implementation to this directory. This file must have the extension `.java`.
2. Add the batch job metadata record XML file to the `src/main/resources` directory in this module. This file must have the same path and name as the Java file, with the extension `.xml` instead of `.java`.
3. Add the batch job to the appropriate tenant bindings configuration files, so that it will automatically be installed or updated using the metadata record XML file when CollectionSpace starts. To install a batch job in all tenants, add it to tenant-bindings-proto-unified.xml, as in these examples: https://github.com/collectionspace/services/blob/d03baa371657d3e83af17cb8cd5b16f322c6ba29/services/common/src/main/cspace/config/services/tenants/tenant-bindings-proto-unified.xml#L309-L320. To install a report only in specific tenants, add it to the delta file for that tenant. There are currently no examples of this for batch jobs, but this example shows a similar configuration for reports added to a single tenant: https://github.com/collectionspace/services/blob/676faa9cf37ee4d99816d3392eb1984d247cfb0b/services/common/src/main/cspace/config/services/tenants/publicart/publicart-tenant-bindings.delta.xml#L13-L20
