require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/single_intermediate_layout'
require 'buildr/gwt'
require 'buildr/jacoco'
require 'buildr/shade'

desc 'grim: Ensure dead code is eliminated'
define 'grim' do
  project.group = 'org.realityforge.grim'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/grim')
  pom.add_developer('realityforge', 'Peter Donald')

  desc 'The annotations'
  define 'annotations' do
    deps = artifacts(:javax_annotation)
    pom.include_transitive_dependencies << deps
    pom.dependency_filter = Proc.new {|dep| dep[:scope].to_s != 'test' && deps.include?(dep[:artifact])}

    compile.with deps

    gwt_enhance(project)

    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  desc 'The assertion library'
  define 'asserts' do
    deps = artifacts(:javax_annotation, :javax_json)
    pom.include_transitive_dependencies << deps
    pom.dependency_filter = Proc.new {|dep| dep[:scope].to_s != 'test' && deps.include?(dep[:artifact])}

    compile.with deps

    test.using :testng

    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  desc 'The Annotation processor'
  define 'processor' do
    deps = artifacts(:javax_annotation, :javax_json)
    pom.include_transitive_dependencies << deps
    pom.dependency_filter = Proc.new {|dep| dep[:scope].to_s != 'test' && deps.include?(dep[:artifact])}

    compile.with :proton_core,
                 deps

    test.using :testng
    test.with :compile_testing,
              Buildr::Util.tools_jar,
              :proton_qa,
              :guava,
              :guava_failureaccess,
              :truth,
              :junit,
              :hamcrest_core,
              project('annotations').package(:jar),
              project('annotations').compile.dependencies

    package(:jar)
    package(:sources)
    package(:javadoc)

    package(:jar).enhance do |jar|
      jar.merge(artifact(:proton_core))
      jar.enhance do |f|
        Buildr::Shade.shade(f,
                            f,
                            'com.google' => 'grim.processor.vendor.google',
                            'org.realityforge.proton' => 'grim.processor.vendor.proton')
      end
    end

    test.using :testng
    test.options[:properties] = { 'grim.fixture_dir' => _('src/test/fixtures') }

    # The generators are configured to generate to here.
    iml.test_source_directories << _('generated/processors/test/java')

    iml.test_source_directories << _('src/test/fixtures/input')
  end

  doc.from(projects(%w(annotations asserts processor))).
    using(:javadoc,
          :windowtitle => 'Grim API Documentation',
          :linksource => true,
          :link => %w(https://docs.oracle.com/javase/8/docs/api)    )

  cleanup_javadocs(project, 'grim')

  iml.excluded_directories << project._('tmp')

  ipr.add_default_testng_configuration(:jvm_args => '-ea -Dgrim.output_fixture_data=false -Dgrim.fixture_dir=processor/src/test/resources')
  ipr.add_testng_configuration('processor',
                               :module => 'processor',
                               :jvm_args => '-ea -Dgrim.output_fixture_data=true -Dgrim.fixture_dir=src/test/fixtures')
  ipr.add_component_from_artifact(:idea_codestyle)
  ipr.add_code_insight_settings
  ipr.add_nullable_manager
  ipr.add_javac_settings('-Xlint:all,-processing,-serial')
end
