This directory contains the built-in reports that are distributed with CollectionSpace.

To add a report:

1. Add the report JRXML file to this directory. This file must have the extension `.jrxml`.
2. Add the report metadata record XML file to this directory. This file must have the same name as the JRXML file, with the extension `.xml` instead of `.jrxml`.
3. Add the report to the appropriate tenant bindings configuration files, so that it will automatically be installed or updated using the metadata record XML file when CollectionSpace starts. To install a report in all tenants, add it to tenant-bindings-proto-unified.xml, as in these examples: https://github.com/collectionspace/services/blob/676faa9cf37ee4d99816d3392eb1984d247cfb0b/services/common/src/main/cspace/config/services/tenants/tenant-bindings-proto-unified.xml#L586-L681. To install a report only in specific tenants, add it to the delta file for that tenant, as in this example: https://github.com/collectionspace/services/blob/676faa9cf37ee4d99816d3392eb1984d247cfb0b/services/common/src/main/cspace/config/services/tenants/publicart/publicart-tenant-bindings.delta.xml#L13-L20
