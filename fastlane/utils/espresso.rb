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

class Espresso
  class << self
    # @param [Hash] args
    # @option args [String] :app     REQUIRED App name
    # @option args [String] :name    OPTIONAL Test run name
    # @option args [String] :package REQUIRED App package
    # @option args [String] :runner  REQUIRED JUnit runner
    # @option args [String] :video   OPTIONAL Record video
    def run(args = {})
      valid_apps = [Parent.app, Teacher.app]
      app = args.fetch(:app)
      ui_error "Unknown app #{app}. Must be one of: #{valid_apps.join(', ')}" unless valid_apps.include?(app)

      command_name  = args.fetch(:name, :espresso)
      package       = args.fetch(:package)
      single_class  = args[:class]
      single_method = args[:method]
      if single_class
        # specifying :method requires :class to also be specified
        specific_tests_to_run = "-e class #{package}.#{single_class}"
        specific_tests_to_run << '#' + single_method if single_method
      else
        raise "espresso :method cannot be used without also setting :class" if single_method
      end

      # -w - forces am instrument to wait until the instrumentation terminates before terminating itself.
      # -e - testing options as key/value pairs.
      #      debug false = don't run tests in debug mode.
      #      class = execute all tests cases specified by fully-qualified class name
      #      package = execute all test cases that use this test package name.
      #
      # https://developer.android.com/studio/test/command-line.html
      run_tests_cmd = [
          'adb shell am instrument -w',
          "#{specific_tests_to_run || "-e package #{package}"}",
          '-e debug false',
          args.fetch(:runner)
      ].join(' ')

      cmd_dir = File.expand_path(File.join(__dir__, '..', '..'))

      instrument_file = File.join(cmd_dir, 'fastlane_instrument.txt')
      FileUtils.rm_rf instrument_file

      logcat_file = 'fastlane_logcat.txt'
      FileUtils.rm_rf File.join(cmd_dir, logcat_file) # ensure we don't keep stale logs around.
      logcat_clear_cmd = 'adb logcat -c'
      logcat_save_cmd = %Q(adb logcat -v long -d > "#{logcat_file}")

      # Video recording only works on physical devices.
      # For video recordings on simulators, run the tests on Firebase Test Lab.
      #
      # Note: screenrecord only records for 3 minutes.
      record_video = args[:video]

      mp4_name = 'fastlane_video.mp4'
      mp4_path = %Q("/sdcard/#{mp4_name}")
      FileUtils.rm_rf File.join(cmd_dir, mp4_name) # ensure we don't keep stale videos around.

      if record_video
        remove_screen_record_cmd = %Q(adb shell rm #{mp4_path})
        _spawn_child(remove_screen_record_cmd)
        screen_record_cmd = %Q(adb shell screenrecord #{mp4_path})
        record_process_pid = _spawn_async(screen_record_cmd)
        screen_record_pull_cmd = %Q(adb pull #{mp4_path} .)
      end

      Fastlane::Actions.execute_action(command_name) do
        _execute(logcat_clear_cmd)
        output = _execute(run_tests_cmd)

        # adb shell am instrument will always return exit code 0. Even on failures.
        # Manually parse the output to determine success.
        case output
          when /FAILURES!!!/, /shortMsg=Process crashed/
            _execute(logcat_save_cmd)

            if record_video
              # send Ctrl+C to end recording if the process is still alive.
              Process.kill('INT', record_process_pid) rescue nil
              Process::waitpid(record_process_pid)
              # screen record generates corrupt videos unless we wait a few seconds.
              # the sleep cost is only ever paid once, on failure.
              sleep 2
              _execute(screen_record_pull_cmd)
            end

            File.write(instrument_file, output)

            ui_error('Error running the tests - see the log above')
        end
      end # execute_action
    end # def run
  end
end
