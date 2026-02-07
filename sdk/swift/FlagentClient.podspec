Pod::Spec.new do |s|
  s.name = 'FlagentClient'
  s.ios.deployment_target = '11.0'
  s.osx.deployment_target = '10.13'
  s.tvos.deployment_target = '11.0'
  s.watchos.deployment_target = '4.0'
  s.version = '0.1.5'
  s.source = { :git => 'git@github.com:OpenAPITools/openapi-generator.git', :tag => 'v0.1.5' }
  s.authors = 'OpenAPI Generator'
  s.license = 'Proprietary'
  s.homepage = 'https://github.com/OpenAPITools/openapi-generator'
  s.summary = 'FlagentClient Swift SDK'
  s.source_files = 'Sources/FlagentClient/**/*.swift'
  s.dependency 'AnyCodable-FlightSchool', '~> 0.6'
end
