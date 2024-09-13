package org.collectionspace.services.common.test;

import org.testng.annotations.Test;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.testng.Assert;

public class RefNameServiceUtilsTest {

  @Test
  public void buildWhereForAuthItemByName() {
    String commonSchemaName = "common";
    String shortId = "shortId";
    String parentCsid = "1234";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByName(commonSchemaName, shortId, parentCsid),
      "common:shortIdentifier='shortId' AND common:inAuthority='1234'");
  }

  @Test
  public void buildWhereForAuthItemByNameWithNullParent() {
    String commonSchemaName = "common";
    String shortId = "shortId";
    String parentCsid = null;

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByName(commonSchemaName, shortId, parentCsid),
      "common:shortIdentifier='shortId'"
    );
  }

  @Test
  public void buildWhereForAuthItemByDisplayName() {
    String displayNameField = "common:displayName";
    String displayName = "foo";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE 'foo'");
  }

  @Test
  public void buildWhereForAuthItemByDisplayNameEscapesDisplayNameQuote() {
    String displayNameField = "common:displayName";
    String displayName = "who's there";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE 'who\\'s there'");
  }

  @Test
  public void buildWhereForAuthItemByDisplayNameEscapesDisplayNamePercent() {
    String displayNameField = "common:displayName";
    String displayName = "100%";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE '100\\%'");
  }

  @Test
  public void buildWhereForAuthItemByDisplayNameEscapesDisplayNameUnderscore() {
    String displayNameField = "common:displayName";
    String displayName = "foo_bar";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE 'foo\\_bar'");
  }

  @Test
  public void buildWhereForAuthItemByDisplayNameEscapesDisplayNameBackslash() {
    String displayNameField = "common:displayName";
    String displayName = "foo\\bar";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE 'foo\\\\bar'");
  }

  @Test
  public void buildWhereForAuthItemByDisplayNameEscapesDisplayName() {
    String displayNameField = "common:displayName";
    String displayName = "50% of Megan's files are in C:\\Users\\megan or C:\\Temp\\megans_files";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByDisplayName(displayNameField, displayName),
      "common:displayName ILIKE '50\\% of Megan\\'s files are in C:\\\\Users\\\\megan or C:\\\\Temp\\\\megans\\_files'");
  }

  @Test
  public void buildWhereForAuthItemByNameOrDisplayName() {
    String commonSchemaName = "common";
    String shortId = "shortId";
    String displayNameField = "common:displayName";
    String displayName = "foo";
    String parentCsid = "1234";

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByNameOrDisplayName(commonSchemaName, shortId, displayNameField, displayName, parentCsid),
      "(common:shortIdentifier='shortId' OR common:displayName ILIKE 'foo') AND common:inAuthority='1234'");
  }

  @Test
  public void buildWhereForAuthItemByNameOrDisplayNameWithNullParent() {
    String commonSchemaName = "common";
    String shortId = "shortId";
    String displayNameField = "common:displayName";
    String displayName = "foo";
    String parentCsid = null;

    Assert.assertEquals(
      RefNameServiceUtils.buildWhereForAuthItemByNameOrDisplayName(commonSchemaName, shortId, displayNameField, displayName, parentCsid),
      "(common:shortIdentifier='shortId' OR common:displayName ILIKE 'foo')");
  }
}
