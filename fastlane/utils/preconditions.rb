# Copyright (C) 2017 - present Instructure, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

class Preconditions
  class << self
    def check_adb_on_path
      # don't use which. sometimes adb is on path and which won't find it.
      adb_exists = `adb version`.include? 'version'
      ui_error 'Adb not on path! (adb version)' unless adb_exists
    end

    def check_one_adb_device
      device_count = `adb devices`.scan("\tdevice\n").length

      if device_count != 1
        ui_error "Must have exactly one device (adb devices). Device count: #{device_count}"
      end
    end
  end
end
