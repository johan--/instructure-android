# Copyright (C) 2016 - present Instructure, Inc.
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

module Fastlane
  module Actions

    def self.last_non_merge_git_commit_dict
      return nil if last_non_merge_git_commit_formatted_with('%an').nil?
      {
          author:                  last_non_merge_git_commit_formatted_with('%an'),
          message:                 last_non_merge_git_commit_formatted_with('%B'),
          commit_hash:             last_non_merge_git_commit_formatted_with('%H'),
          abbreviated_commit_hash: last_non_merge_git_commit_formatted_with('%h')
      }
    end

    def self.last_non_merge_git_commit_formatted_with(pretty_format)
      cmd = "git log -1 --no-merges --pretty=#{pretty_format}"
      Actions.sh(cmd, log: false).chomp rescue nil
    end

  end # module Actions
end # module Fastlane
