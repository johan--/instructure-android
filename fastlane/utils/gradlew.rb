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

# Regular fastlane actions don't provide control over the command name.
# The 'self.step_text' in action.rb is always called before 'self.run'
# so it's not possible to override step_text using parameters set in run.
#
# To fix this, we define and execute a command directly with the correct name.
def gradlew(command)
  raise 'Missing command' unless command

  action_name = "gradlew #{command}"
  command = "#{gradlew_path} #{command}"

  execute_action(action_name) do
   _execute(command: command, print_all: false)
  end # execute_action
end # def gradlew
