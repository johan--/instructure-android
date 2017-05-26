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

# https://github.com/fastlane/fastlane/blob/7908e2af585ce859312972bc2bd9e361f4229b86/fastlane/lib/fastlane/fast_file.rb
def fastlane_require(gem_name)
  Fastlane::FastlaneRequire.install_gem_if_needed(gem_name: gem_name, require_gem: true)
end

fastlane_require 'posix-spawn'

def _execute args={}
  args = {command: args} if args.is_a?(String)
  args[:command] = "set -o pipefail && #{args[:command]}" if args[:command].include? '|'

  args[:print_all] = args.fetch(:print_all, true)
  args[:print_command] = args.fetch(:print_command, true)

  # 'fastlane' is the default cwd. change to parent dir.
  dir = args.fetch(:dir, repo_root_dir)
  args.delete(:dir)
  Dir.chdir(dir) do
    FastlaneCore::CommandExecutor.execute args
  end
end

# Set gradle.properties on bitrise.
if bitrise
  # /bin/sh is bash on macOS & dash on Ubuntu
  # pipefail doesn't work on dash so default to bash
  system 'sudo ln -sf bash /bin/sh'

  src = join(gradle_home, 'gradle.properties')
  dst_parent = join(Dir.home, '.gradle')
  dst = join(dst_parent, 'gradle.properties')
  raise "source gradle properties doesn't exist: #{src}'" unless File.exist? src
  FileUtils.mkdir_p dst_parent
  FileUtils.copy src, dst

  puts '~' * 30
  puts 'Created gradle.properties'
  puts dst
  puts File.read dst
  puts '~' * 30

  # Install Ruby deps
  _execute 'apt-get install -y libsqlite3-dev'
  _execute 'gem update --system'
  _execute 'echo "gem: --no-document" >> ~/.gemrc'
end

def clean_bitrise_daemon
  # Remove old daemons. Otherwise bitrise cache will complain.
  FileUtils.rm_rf join(Dir.home, '.gradle', 'daemon') if bitrise
end

def ui_error msg
  FastlaneCore::UI.error "ERROR: #{msg}"
  abort
end

def ui_important msg
  FastlaneCore::UI.important msg
end

def ui_success msg
  FastlaneCore::UI.success msg
end

def ui_message msg
  FastlaneCore::UI.message msg
end

# Invoke execute_action to show up in the Fastlane step report.
def execute_action name, &block
  ::Fastlane::Actions.execute_action(name, &block)
end

def _spawn_child *args
  FastlaneCore::UI.message(args.map(&:to_s).join(' '))
  POSIX::Spawn::Child.new(*args)
end

def _spawn_async *args
  FastlaneCore::UI.message(args.map(&:to_s).join(' '))
  POSIX::Spawn::spawn(*args)
end

def select_app(args)
  case args.intern
  when :parent
    Parent
  when :teacher
    Teacher
  else
    raise "Unknown app: #{args}"
  end
end
