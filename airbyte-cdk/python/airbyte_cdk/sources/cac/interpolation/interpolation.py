#
# Copyright (c) 2021 Airbyte, Inc., all rights reserved.
#
from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from airbyte_cdk.sources.cac.types import Config, Vars


class Interpolation(ABC):
    @abstractmethod
    def eval(self, input_str: str, vars: "Vars", config: "Config", stream_slice=None, stream_state=None):  # FIXME: declare the output!
        # FIXME: also declare parameter types
        pass