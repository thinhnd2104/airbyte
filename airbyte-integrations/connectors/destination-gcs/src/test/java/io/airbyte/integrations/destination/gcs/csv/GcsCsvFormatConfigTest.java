/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.gcs.csv;

import static com.amazonaws.services.s3.internal.Constants.MB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import alex.mojaki.s3upload.StreamTransferManager;
import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.commons.json.Jsons;
import io.airbyte.integrations.destination.gcs.GcsDestinationConfig;
import io.airbyte.integrations.destination.gcs.util.ConfigTestUtils;
import io.airbyte.integrations.destination.s3.S3DestinationConstants;
import io.airbyte.integrations.destination.s3.S3FormatConfig;
import io.airbyte.integrations.destination.s3.csv.S3CsvFormatConfig.Flattening;
import io.airbyte.integrations.destination.s3.util.StreamTransferManagerHelper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GcsCsvFormatConfig")
public class GcsCsvFormatConfigTest {

  @Test
  @DisplayName("Flattening enums can be created from value string")
  public void testFlatteningCreationFromString() {
    assertEquals(Flattening.NO, Flattening.fromValue("no flattening"));
    assertEquals(Flattening.ROOT_LEVEL, Flattening.fromValue("root level flattening"));
    try {
      Flattening.fromValue("invalid flattening value");
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testHandlePartSizeConfig() throws IllegalAccessException {

    final JsonNode config = ConfigTestUtils.getBaseConfig(Jsons.deserialize("{\n"
        + "  \"format_type\": \"CSV\",\n"
        + "  \"flattening\": \"Root level flattening\",\n"
        + "  \"part_size_mb\": 6\n"
        + "}"));

    final GcsDestinationConfig gcsDestinationConfig = GcsDestinationConfig.getGcsDestinationConfig(config);
    ConfigTestUtils.assertBaseConfig(gcsDestinationConfig);

    final S3FormatConfig formatConfig = gcsDestinationConfig.getFormatConfig();
    assertEquals("CSV", formatConfig.getFormat().name());
    assertEquals(6, formatConfig.getPartSize());
    // Assert that is set properly in config
    final StreamTransferManager streamTransferManager = StreamTransferManagerHelper.getDefault(
        gcsDestinationConfig.getBucketName(), "objectKey", null,
        gcsDestinationConfig.getFormatConfig().getPartSize());

    final Integer partSizeBytes = (Integer) FieldUtils.readField(streamTransferManager, "partSize", true);
    assertEquals(MB * 6, partSizeBytes);
  }

  @Test
  public void testHandleAbsenceOfPartSizeConfig() throws IllegalAccessException {

    final JsonNode config = ConfigTestUtils.getBaseConfig(Jsons.deserialize("{\n"
        + "  \"format_type\": \"CSV\",\n"
        + "  \"flattening\": \"Root level flattening\"\n"
        + "}"));

    final GcsDestinationConfig gcsDestinationConfig = GcsDestinationConfig.getGcsDestinationConfig(config);
    ConfigTestUtils.assertBaseConfig(gcsDestinationConfig);

    final StreamTransferManager streamTransferManager = StreamTransferManagerHelper.getDefault(
        gcsDestinationConfig.getBucketName(), "objectKey", null,
        gcsDestinationConfig.getFormatConfig().getPartSize());

    final Integer partSizeBytes = (Integer) FieldUtils.readField(streamTransferManager, "partSize", true);
    assertEquals(MB * S3DestinationConstants.DEFAULT_PART_SIZE_MB, partSizeBytes);
  }

}
