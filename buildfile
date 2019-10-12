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
    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  desc 'The Annotation processor'
  define 'processor' do
    compile.with :autoservice,
                 :autocommon,
                 :javax_annotation

    test.with :compile_testing,
              Java.tools_jar,
              :truth,
              :junit,
              :hamcrest_core,
              project('annotations').package(:jar),
              project('annotations').compile.dependencies

    package(:jar)
    package(:sources)
    package(:javadoc)

    package(:jar).enhance do |jar|
      jar.enhance do |f|
        shaded_jar = (f.to_s + '-shaded')
        Buildr.ant 'shade_jar' do |ant|
          artifact = Buildr.artifact(:shade_task)
          artifact.invoke
          ant.taskdef :name => 'shade', :classname => 'org.realityforge.ant.shade.Shade', :classpath => artifact.to_s
          ant.shade :jar => f.to_s, :uberJar => shaded_jar do
            ant.relocation :pattern => 'com.google', :shadedPattern => 'grim.processor.vendor.google'
          end
        end
        FileUtils.mv shaded_jar, f.to_s
      end
    end

    test.using :testng
    test.options[:properties] = { 'grim.fixture_dir' => _('src/test/resources') }

    # The generators are configured to generate to here.
    iml.test_source_directories << _('generated/processors/test/java')

    iml.test_source_directories << _('src/test/resources/input')
    iml.test_source_directories << _('src/test/resources/expected')
    iml.test_source_directories << _('src/test/resources/bad_input')
  end

  doc.from(projects(%w(annotations))).
    using(:javadoc,
          :windowtitle => 'Grim API Documentation',
          :linksource => true,
          :link => %w(https://docs.oracle.com/javase/8/docs/api)    )

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
