#
# Copyright (c) 2022 Airbyte, Inc., all rights reserved.
#

import jsonschema
from airbyte_cdk.models import AirbyteMessage, ConfiguredAirbyteCatalog, Type
from source_faker import SourceFaker


def test_source_streams():
    source = SourceFaker()
    config = {"count": 1}
    catalog = source.discover(None, config)
    catalog = AirbyteMessage(type=Type.CATALOG, catalog=catalog).dict(exclude_unset=True)
    schemas = [stream["json_schema"] for stream in catalog["catalog"]["streams"]]

    assert len(schemas) == 1
    assert schemas[0]["properties"] == {
        "id": {"type": "number"},
        "created_at": {"type": "string", "format": "date-time", "airbyte_type": "timestamp_without_timezone"},
        "updated_at": {"type": "string", "format": "date-time", "airbyte_type": "timestamp_without_timezone"},
        "job": {"type": "string"},
        "company": {"type": "string"},
        "ssn": {"type": "string"},
        "residence": {"type": "string"},
        "current_location": {"type": "array"},
        "blood_group": {"type": "string"},
        "website": {"type": "array"},
        "username": {"type": "string"},
        "name": {"type": "string"},
        "sex": {"type": "string"},
        "address": {"type": "string"},
        "mail": {"type": "string"},
        "birthdate": {"type": "string", "format": "date"},
    }

    for schema in schemas:
        jsonschema.Draft7Validator.check_schema(schema)


def test_read_random_data():
    source = SourceFaker()
    logger = None
    config = {"count": 10}
    catalog = ConfiguredAirbyteCatalog(
        streams=[{"stream": {"name": "Users", "json_schema": {}}, "sync_mode": "full_refresh", "destination_sync_mode": "overwrite"}]
    )
    state = {}
    iterator = source.read(logger, config, catalog, state)

    record_rows_count = 0
    state_rows_count = 0
    for row in iterator:
        if row.type is Type.RECORD:
            record_rows_count = record_rows_count + 1
        if row.type is Type.STATE:
            state_rows_count = state_rows_count + 1

    assert record_rows_count == 10
    assert state_rows_count == 1


def test_read_with_seed():
    """
    This test asserts that setting a seed always returns the same values
    """

    source = SourceFaker()
    logger = None
    config = {"count": 1, "seed": 100}
    catalog = ConfiguredAirbyteCatalog(
        streams=[{"stream": {"name": "Users", "json_schema": {}}, "sync_mode": "full_refresh", "destination_sync_mode": "overwrite"}]
    )
    state = {}
    iterator = source.read(logger, config, catalog, state)

    records = [row for row in iterator if row.type is Type.RECORD]
    assert records[0].record.data["company"] == "Gibson-Townsend"
    assert records[0].record.data["mail"] == "zamoradenise@yahoo.com"
