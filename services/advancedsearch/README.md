# Adding custom fields and schemas to the advancedsearch service

The `advancedsearch` service (new in 8.3) is the endpoint cspace-ui calls to
list collection objects: on the search result page, and in other collection
object lists such as the search panels on record pages and the
search-to-relate dialog. It replaces the plain `/collectionobjects` list in
those places. The two work differently:

| | `/collectionobjects` list (before 8.3) | `/advancedsearch` (8.3 and later) |
|---|---|---|
| Which fields are returned | Any field marked `mini="...list..."` in the application config, via the tenant bindings `ListResultField` entries | A fixed set of fields defined in Java and an XSD |
| How to add a field | Edit the application XML, redeploy the tenant | Change the services source, rebuild and redeploy services |
| Tenant awareness | Per tenant | None. Every tenant gets the same list item shape |

Because of this, a search result column that reads a field from a
tenant-specific schema, or a field that was only exposed through
`mini="list"`, shows up empty from 8.3 on until the service is taught to
return it. This guide explains how to do that.

This is a code change, not configuration. An institution that needs it has
to build and deploy its own copy of the services layer, and carry the change
forward when upgrading.

The only configuration alternative is to opt out of the new search in the
UI. cspace-ui uses `/advancedsearch` when
`recordTypes.collectionobject.serviceConfig.features.updatedSearch` is
`true`, which is the default. A plugin that sets it to `false` goes back to
the old result page and the `/collectionobjects` list, and gives up the new
grid and detail views. Test this on your own tenant before relying on it.

Sorting is not affected. The sort field is passed through to the normal
collection object query, so existing sort keys keep working. The UI looks
the field up in the record type's `sort` config first, then in the column's
`sortBy`.

## How the service builds a result row

All code lives under `services/advancedsearch/`.

1. `AdvancedSearch.getList()` runs the normal collection object list query and
   gets back the full record payload for each hit.
2. `CollectionObjectMapper.asListItem()` picks the parts it knows about out
   of that payload by part label and unmarshals each one with JAXB:
   - `collectionspace_core`
   - `collectionobjects_common`
   - `collectionobjects_nagpra`
   - `collectionobjects_naturalhistory_extension`

   The part labels are the `*_PART_NAME` constants at the top of
   `CollectionObjectMapper`, plus `COLLECTIONSPACE_CORE_SCHEMA` for the core
   part.
3. It copies values from those JAXB objects onto an `AdvancedsearchListItem`.
   The item's fields are declared in
   `jaxb/src/main/resources/advanced-search_common.xsd`. The class is
   generated from that XSD at build time.
4. The UI table reads each column by name: a column whose key is `taxon`
   reads the `<taxon>` element of the list item. If the column sets a
   `dataKey`, that is used instead of the key. So the `dataKey`, or the
   column key when there is no `dataKey`, in a profile plugin's `columns.js`
   must match an element name in the XSD.

Parts that are not in step 2 are ignored, even if the field is marked
`mini="list"` in the application config.

## Case A: the field is in a schema the service already unmarshals

Example: an institution using the natural history extension wants a
"Hybrid name" column with the key `taxonomicIdentHybridName`. The field lives
in `collectionobjects_naturalhistory_extension` at
`taxonomicIdentGroupList/taxonomicIdentGroup/taxonomicIdentHybridName`.

### 1. Add the field to the part's JAXB schema

The JAXB schemas for the collection object parts live in
`services/collectionobject/jaxb/src/main/resources/`. They only need to
declare the fields that Java code reads. Unknown elements in the payload are
ignored when unmarshalling, which is why these XSDs can be much smaller than
the Nuxeo schema. If the field is already declared there, skip this step.

In `collectionobjects_naturalhistory.xsd`, add the element to the
`taxonomicIdentGroup` type:

```xml
<xs:complexType name="taxonomicIdentGroup">
  <xs:sequence>
    <xs:element name="taxon" type="xs:string"/>
    ...
    <xs:element name="notes" type="xs:string"/>
    <xs:element name="taxonomicIdentHybridName" type="xs:string"/>
  </xs:sequence>
</xs:complexType>
```

This generates a `getTaxonomicIdentHybridName()` on `TaxonomicIdentGroup`, in
the package `org.collectionspace.services.collectionobject.domain.naturalhistory_extension`.

Do not change the `targetNamespace` of an existing schema. The Java package
of the generated classes is derived from it, and it has to match the
namespace of the stored records.

### 2. Add the field to the list item schema

In `services/advancedsearch/jaxb/src/main/resources/advanced-search_common.xsd`,
add an element to the `advancedsearch-list-item` sequence. Use the name the
UI column reads (its `dataKey`, or its key when it has no `dataKey`):

```xml
<xs:element name="taxonomicIdentHybridName" type="xs:string" nillable="true"/>
```

Append new elements at the end of the sequence. Order does not matter to the
UI, and keeping additions together makes merging future releases easier.

### 3. Copy the value in the mapper

Add a helper next to the existing ones in
`services/advancedsearch/jaxb/src/main/java/org/collectionspace/services/advancedsearch/model/`.
Follow the pattern in `TaxonModel.taxon()`: null-check every level, and take
the first entry of a repeating group.

```java
public static String hybridName(final CollectionobjectsNaturalhistory naturalHistory) {
    if (naturalHistory == null || naturalHistory.getTaxonomicIdentGroupList() == null) {
        return null;
    }
    List<TaxonomicIdentGroup> groups =
            naturalHistory.getTaxonomicIdentGroupList().getTaxonomicIdentGroup();
    return groups.isEmpty() ? null : groups.get(0).getTaxonomicIdentHybridName();
}
```

Then call it from `CollectionObjectMapper.asListItem()` inside the existing
`if (naturalHistory != null)` block:

```java
item.setTaxonomicIdentHybridName(TaxonModel.hybridName(naturalHistory));
```

## Case B: the field is in a tenant-specific schema

Example: a tenant named `mymuseum` stores local fields in its own part,
`collectionobjects_mymuseum`, and its profile plugin shows two of them as
search result columns:

- `localNumber`, a simple field
- `localTitle`, the first value of the repeating group
  `localTitleGroupList/localTitleGroup/localTitle`

The service has never seen this part, so there are two extra steps: create a
JAXB schema for the part and register the generated class with the JAXB
context.

### 1. Create a JAXB schema for the tenant part

Add a new XSD in `services/collectionobject/jaxb/src/main/resources/`, for
example `collectionobjects_mymuseum.xsd`. Use `collectionobjects_nagpra.xsd`
as the template. The important pieces:

- `targetNamespace` must be the namespace the services layer uses for the
  part. It is declared in the `services-record-path` of the tenant's
  collection object config in the application layer, for example
  `collectionobjects_mymuseum:http://collectionspace.org/services/collectionobject/local/mymuseum,collectionobjects_mymuseum`.
  The generated Java package follows from it, here
  `org.collectionspace.services.collectionobject.local.mymuseum`.
  At runtime the service takes the part label and namespace from the Nuxeo
  schema of the record, which is generated from this config.
- The root element name must be the part label, here
  `collectionobjects_mymuseum`.
- Declare only the fields the mapper will read. Everything else in the stored
  record is ignored.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:jaxb="https://jakarta.ee/xml/ns/jaxb"
           jaxb:version="3.0"
           xmlns:mymuseum="http://collectionspace.org/services/collectionobject/local/mymuseum"
           targetNamespace="http://collectionspace.org/services/collectionobject/local/mymuseum"
           version="0.1">

  <xs:element name="collectionobjects_mymuseum">
    <xs:annotation>
      <xs:appinfo>
        <jaxb:class name="CollectionObjectsMyMuseum"/>
      </xs:appinfo>
    </xs:annotation>
    <xs:complexType>
      <xs:sequence>
        <xs:element name="localNumber" type="xs:string"/>
        <xs:element name="localTitleGroupList" type="mymuseum:localTitleGroupList"/>
      </xs:sequence>
    </xs:complexType>
  </xs:element>

  <xs:complexType name="localTitleGroupList">
    <xs:sequence>
      <xs:element name="localTitleGroup" type="mymuseum:localTitleGroup"
                  minOccurs="0" maxOccurs="unbounded"/>
    </xs:sequence>
  </xs:complexType>

  <xs:complexType name="localTitleGroup">
    <xs:sequence>
      <xs:element name="localTitle" type="xs:string"/>
    </xs:sequence>
  </xs:complexType>
</xs:schema>
```

The build picks up every `*.xsd` in that directory automatically. That is
the default of the `jaxb-maven-plugin` configured in `services/pom.xml`, so
no POM change is needed.

### 2. Register the class in the JAXB context

In `services/advancedsearch/service/src/main/java/org/collectionspace/services/advancedsearch/AdvancedSearchJAXBContext.java`,
add the generated class to the existing `JAXBContext.newInstance(...)` call:

```java
jaxbContext = JAXBContext.newInstance(
        CollectionSpaceCore.class, CollectionobjectsCommon.class,
        CollectionobjectsNaturalhistory.class, CollectionObjectsNAGPRA.class,
        CollectionObjectsMyMuseum.class);
```

Without this, `unmarshal()` throws for the new root element.

### 3. Unmarshal the part in the mapper

In `CollectionObjectMapper`, add a part-name constant and an unmarshal call,
following the existing ones:

```java
private static final String MYMUSEUM_PART_NAME =
        CollectionObjectClient.SERVICE_NAME + PART_LABEL_SEPARATOR + "mymuseum";
...
final CollectionObjectsMyMuseum myMuseum =
        unmarshall(CollectionObjectsMyMuseum.class, MYMUSEUM_PART_NAME, outputPayload, unmarshaller);
```

`unmarshall()` returns `null` when the payload has no such part, which is
what happens for every other tenant on the same server. Keep the
field-copying inside a null check.

### 4. Add the list item elements and copy the values

Same as Case A, steps 2 and 3:

```xml
<xs:element name="localNumber" type="xs:string" nillable="true"/>
<xs:element name="localTitle" type="xs:string" nillable="true"/>
```

```java
if (myMuseum != null) {
    item.setLocalNumber(myMuseum.getLocalNumber());
    item.setLocalTitle(MyMuseumModel.title(myMuseum)); // first localTitleGroup
}
```

## The UI side

The table view usually needs no change in the profile plugin, because the
plugin already defines the column. The table reads the list item by the
column's `dataKey` if it has one, otherwise by the column key, so check that
this name is exactly the new element name.

The grid and detail views do not use `columns`. They are driven by the
`grid` and `detailList` configs of the collection object record type, whose
formatters receive the same list item. A new field shows up in those views
only if a formatter is changed to read it, for example with
`data.get('taxonomicIdentHybridName')`.

Computed fields such as `computedCurrentLocation` are stored in the record
like any other field, so the stored value is what the service returns.
