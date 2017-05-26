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

class MD5
  class << self
    def apk_already_installed?(local_apk_path, device_apk_path)
      md5sum1 = local_apk_md5sum(local_apk_path)
      md5sum2 = installed_apk_md5sum(device_apk_path)
      md5sum1 == md5sum2
    end

    private

    def local_apk_md5sum(apk_path)
      abort "App apk does not exist! #{app_apk_path}" unless File.exist?(apk_path)
      Digest::MD5.hexdigest(File.read(apk_path))
    end

    def installed_apk_md5sum(device_apk_path)
      result = _execute("adb shell md5sum #{device_apk_path}") rescue nil
      result.split.first if result
    end
  end
end
