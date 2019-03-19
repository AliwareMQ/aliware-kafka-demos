# -*- encoding: utf-8 -*-

Gem::Specification.new do |gem|
  gem.authors       = ["Hidemasa Togashi", "Masahiro Nakagawa"]
  gem.email         = ["togachiro@gmail.com", "repeatedly@gmail.com"]
  gem.description   = %q{Fluentd plugin for Apache Kafka > 0.8}
  gem.summary       = %q{Fluentd plugin for Apache Kafka > 0.8}
  gem.homepage      = "https://github.com/fluent/fluent-plugin-kafka"
  gem.license       = "Apache-2.0"

  gem.files         = `git ls-files`.split($\)
  gem.executables   = gem.files.grep(%r{^bin/}).map{ |f| File.basename(f) }
  gem.test_files    = gem.files.grep(%r{^(test|spec|features)/})
  gem.name          = "fluent-plugin-kafka"
  gem.require_paths = ["lib"]
  gem.version       = '0.6.5'
  gem.required_ruby_version = ">= 2.1.0"

  gem.add_dependency "fluentd", [">= 0.10.58", "< 2"]
  gem.add_dependency 'ltsv'
  gem.add_dependency 'ruby-kafka', '>= 0.4.1', '< 1.0.0'
  gem.add_development_dependency "rake", ">= 0.9.2"
  gem.add_development_dependency "test-unit", ">= 3.0.8"
end
