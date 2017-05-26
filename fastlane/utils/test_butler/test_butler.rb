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

class TestButler
  class << self
    def install_if_missing
      return if MD5.apk_already_installed?(apk_path, device_apk_path)
      _execute('adb uninstall com.linkedin.android.testbutler') rescue nil
      _execute("adb install #{apk_path}")
    end

    private

    def apk_path
      join(__dir__, 'test-butler-app-1.2.0.apk')
    end

    def device_apk_path
      '/data/app/com.linkedin.android.testbutler-1/base.apk'
    end
  end
end
