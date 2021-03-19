require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/single_intermediate_layout'
require 'buildr/gwt'
require 'buildr/jacoco'

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
        shaded_jar = (f.to_s + '-shaded')
        a = artifact('org.realityforge.shade:shade-cli:jar:1.0.0')
        a.invoke
        sh "#{Java::Commands.path_to_bin('java')} -jar #{a} --input #{f} --output #{shaded_jar} -rcom.google=grim.processor.vendor.google"
        FileUtils.mv shaded_jar, f.to_s
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
  ipr.add_component_from_artifact(:idea_codestyle)

  ipr.add_component('CompilerConfiguration') do |component|
    component.annotationProcessing do |xml|
      xml.profile(:default => true, :name => 'Default', :enabled => true) do
        xml.sourceOutputDir :name => 'generated/processors/main/java'
        xml.sourceTestOutputDir :name => 'generated/processors/test/java'
        xml.outputRelativeToContentRoot :value => true
      end
    end
  end

  ipr.add_testng_configuration('processor',
                               :module => 'processor',
                               :jvm_args => '-ea -Dgrim.output_fixture_data=true -Dgrim.fixture_dir=src/test/fixtures')

  ipr.add_component('JavacSettings') do |xml|
    xml.option(:name => 'ADDITIONAL_OPTIONS_STRING', :value => '-Xlint:all,-processing,-serial')
  end

  ipr.add_component('JavaProjectCodeInsightSettings') do |xml|
    xml.tag!('excluded-names') do
      xml << '<name>com.sun.istack.internal.NotNull</name>'
      xml << '<name>com.sun.istack.internal.Nullable</name>'
      xml << '<name>org.jetbrains.annotations.Nullable</name>'
      xml << '<name>org.jetbrains.annotations.NotNull</name>'
      xml << '<name>org.testng.AssertJUnit</name>'
    end
  end
  ipr.add_component('NullableNotNullManager') do |component|
    component.option :name => 'myDefaultNullable', :value => 'javax.annotation.Nullable'
    component.option :name => 'myDefaultNotNull', :value => 'javax.annotation.Nonnull'
    component.option :name => 'myNullables' do |option|
      option.value do |value|
        value.list :size => '2' do |list|
          list.item :index => '0', :class => 'java.lang.String', :itemvalue => 'org.jetbrains.annotations.Nullable'
          list.item :index => '1', :class => 'java.lang.String', :itemvalue => 'javax.annotation.Nullable'
        end
      end
    end
    component.option :name => 'myNotNulls' do |option|
      option.value do |value|
        value.list :size => '2' do |list|
          list.item :index => '0', :class => 'java.lang.String', :itemvalue => 'org.jetbrains.annotations.NotNull'
          list.item :index => '1', :class => 'java.lang.String', :itemvalue => 'javax.annotation.Nonnull'
        end
      end
    end
  end
end
