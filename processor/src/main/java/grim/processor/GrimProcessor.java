package grim.processor;

import com.google.auto.common.SuperficialValidation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import static javax.tools.Diagnostic.Kind.*;

/**
 * Annotation processor that analyzes grim annotated source and generates rules for
 * which elements should be omitted from compiled javascript under which conditions.
 */
@SuppressWarnings( "Duplicates" )
@SupportedAnnotationTypes( { "grim.annotations.*" } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
public final class GrimProcessor
  extends AbstractProcessor
{
  @Nonnull
  private static final String BASE_RESOURCE_PATH = "META-INF/grim/";
  @Nonnull
  private static final String SUFFIX = ".grim.json";
  @Nonnull
  private static final String SENTINEL = "<default>";

  @FunctionalInterface
  private interface Action
  {
    void call()
      throws Exception;
  }

  @Nonnull
  private HashSet<TypeElement> _deferred = new HashSet<>();

  @Override
  public boolean process( @Nonnull final Set<? extends TypeElement> annotations, @Nonnull final RoundEnvironment env )
  {
    final Set<PackageElement> packagesToProcess = new HashSet<>();
    final Map<String, TypeElement> typesToProcess = new HashMap<>();
    for ( final TypeElement annotation : annotations )
    {
      final String annotationName = annotation.getQualifiedName().toString();
      //noinspection IfCanBeSwitch
      if ( Constants.OMIT_PATTERN_CLASSNAME.equals( annotationName ) ||
           Constants.OMIT_PATTERNS_CLASSNAME.equals( annotationName ) ||
           Constants.KEEP_PATTERN_CLASSNAME.equals( annotationName ) ||
           Constants.KEEP_PATTERNS_CLASSNAME.equals( annotationName ) )
      {
        final Set<? extends Element> elements = env.getElementsAnnotatedWith( annotation );
        for ( final Element element : elements )
        {
          packagesToProcess.add( (PackageElement) element );
        }
      }
      else if ( Constants.OMIT_CLINIT_CLASSNAME.equals( annotationName ) ||
                Constants.OMIT_TYPE_CLASSNAME.equals( annotationName ) ||
                Constants.OMIT_TYPES_CLASSNAME.equals( annotationName ) ||
                Constants.KEEP_CLINIT_CLASSNAME.equals( annotationName ) ||
                Constants.KEEP_TYPE_CLASSNAME.equals( annotationName ) ||
                Constants.KEEP_TYPES_CLASSNAME.equals( annotationName ) )
      {
        final Set<? extends Element> elements = env.getElementsAnnotatedWith( annotation );
        for ( final Element element : elements )
        {
          final TypeElement typeElement = (TypeElement) element;
          typesToProcess.put( typeElement.getQualifiedName().toString(), typeElement );
        }
      }
      else if ( Constants.OMIT_SYMBOLS_CLASSNAME.equals( annotationName ) ||
                Constants.OMIT_SYMBOL_CLASSNAME.equals( annotationName ) ||
                Constants.KEEP_SYMBOLS_CLASSNAME.equals( annotationName ) ||
                Constants.KEEP_SYMBOL_CLASSNAME.equals( annotationName ) )
      {
        final Set<? extends Element> elements = env.getElementsAnnotatedWith( annotation );
        for ( final Element element : elements )
        {
          final TypeElement typeElement = (TypeElement) element.getEnclosingElement();
          typesToProcess.put( typeElement.getQualifiedName().toString(), typeElement );
        }
      }
    }
    processPackages( packagesToProcess );
    processTypeElements( getElementsToProcess( typesToProcess.values() ) );
    if ( env.getRootElements().isEmpty() && !_deferred.isEmpty() )
    {
      _deferred.forEach( this::processingErrorMessage );
      _deferred.clear();
    }
    return true;
  }

  private void processPackages( @Nonnull final Collection<PackageElement> elements )
  {
    for ( final PackageElement element : elements )
    {
      processAction( element, () -> process( element ) );
    }
  }

  private void process( @Nonnull final PackageElement element )
    throws IOException
  {
    writeJsonResource( element, toGrimJsonFilename( element ), g -> processPatterns( element, g ) );
  }

  private void processTypeElements( @Nonnull final Collection<TypeElement> elements )
  {
    for ( final TypeElement element : elements )
    {
      processAction( element, () -> process( element ) );
    }
  }

  private void processAction( @Nonnull final Element element, @Nonnull final Action action )
  {
    try
    {
      action.call();
    }
    catch ( final IOException ioe )
    {
      processingEnv.getMessager().printMessage( ERROR, ioe.getMessage(), element );
    }
    catch ( final Throwable e )
    {
      final StringWriter sw = new StringWriter();
      e.printStackTrace( new PrintWriter( sw ) );
      sw.flush();

      final String message =
        "Unexpected error will running the " + getClass().getName() + " processor. This has " +
        "resulted in a failure to process the code and has left the compiler in an invalid " +
        "state. Please report the failure to the developers so that it can be fixed.\n" +
        " Report the error at: https://github.com/realityforge/grim/issues\n" +
        "\n\n" +
        sw.toString();
      processingEnv.getMessager().printMessage( ERROR, message, element );
    }
  }

  private void process( @Nonnull final TypeElement element )
    throws IOException
  {
    writeJsonResource( element, toGrimJsonFilename( element ), g -> {
      processClinits( element, g );
      processTypes( element, g );
      processSymbols( element, g );
    } );
  }

  private void writeJsonResource( @Nonnull final Element element,
                                  @Nonnull final String filename,
                                  @Nonnull final Consumer<JsonGenerator> action )
    throws IOException
  {
    final Map<String, Object> properties = new HashMap<>();
    properties.put( JsonGenerator.PRETTY_PRINTING, true );
    final JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory( properties );

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final JsonGenerator g = generatorFactory.createGenerator( baos );
    g.writeStartArray();

    action.accept( g );

    g.writeEnd();
    g.close();

    writeResource( filename, formatJson( baos.toString() ), element );
  }

  private void writeResource( @Nonnull final String filename,
                              @Nonnull final String content,
                              @Nonnull final Element element )
    throws IOException
  {
    final FileObject resource =
      processingEnv.getFiler().createResource( StandardLocation.CLASS_OUTPUT, "", filename, element );
    try ( final OutputStream outputStream = resource.openOutputStream() )
    {
      outputStream.write( content.getBytes( StandardCharsets.UTF_8 ) );
    }
    catch ( final IOException e )
    {
      resource.delete();
      throw e;
    }
  }

  private void processClinits( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    processClinit( element, Constants.OMIT_CLINIT_CLASSNAME, g );
    processClinit( element, Constants.KEEP_CLINIT_CLASSNAME, g );
  }

  private void processClinit( @Nonnull final TypeElement element,
                              @Nonnull final String annotationName,
                              @Nonnull final JsonGenerator g )
  {
    final AnnotationMirror annotation = findAnnotationByType( element, annotationName );
    if ( null != annotation )
    {
      g.writeStartObject();
      if ( Constants.KEEP_CLINIT_CLASSNAME.equals( annotationName ) )
      {
        g.write( "keep", true );
      }
      g.write( "type", toTypePattern( element ) );
      g.write( "member", quotedName( "$clinit" ) );
      g.writeEnd();
    }
  }

  private void processTypes( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    processTypeAnnotations( element, Constants.OMIT_TYPES_CLASSNAME, Constants.OMIT_TYPE_CLASSNAME, g );
    processTypeAnnotations( element, Constants.KEEP_TYPES_CLASSNAME, Constants.KEEP_TYPE_CLASSNAME, g );
  }

  private void processTypeAnnotations( @Nonnull final TypeElement element,
                                       @Nonnull final String containerAnnotation,
                                       @Nonnull final String annotationName,
                                       @Nonnull final JsonGenerator g )
  {
    for ( final AnnotationMirror annotation : getRepeatingAnnotations( element, containerAnnotation, annotationName ) )
    {
      g.writeStartObject();
      if ( Constants.KEEP_TYPE_CLASSNAME.equals( annotationName ) )
      {
        g.write( "keep", true );
      }
      g.write( "type", toTypePattern( element ) );
      processConditions( element, annotation, annotationName, g );
      g.writeEnd();
    }
  }

  private void processPatterns( @Nonnull final PackageElement element, @Nonnull final JsonGenerator g )
  {
    processPatternAnnotations( element, Constants.OMIT_PATTERNS_CLASSNAME, Constants.OMIT_PATTERN_CLASSNAME, g );
    processPatternAnnotations( element, Constants.KEEP_PATTERNS_CLASSNAME, Constants.KEEP_PATTERN_CLASSNAME, g );
  }

  private void processPatternAnnotations( @Nonnull final PackageElement element,
                                          @Nonnull final String containerAnnotation,
                                          @Nonnull final String annotationName,
                                          @Nonnull final JsonGenerator g )
  {
    for ( final AnnotationMirror annotation : getRepeatingAnnotations( element, containerAnnotation, annotationName ) )
    {
      g.writeStartObject();
      if ( Constants.KEEP_PATTERN_CLASSNAME.equals( annotationName ) )
      {
        g.write( "keep", true );
      }
      final String typePattern =
        (String) getAnnotationValue( annotation, "type" ).getValue();
      final String actualTypePattern =
        SENTINEL.equals( typePattern ) ?
        "^" + element.getQualifiedName().toString().replace( ".", "\\." ) + "\\..*$" :
        typePattern;
      g.write( "type", actualTypePattern );

      final String symbolPattern = (String) getAnnotationValue( annotation, "symbol" ).getValue();
      if ( !SENTINEL.equals( symbolPattern ) )
      {
        g.write( "member", symbolPattern );
      }
      processConditions( element, annotation, annotationName, g );
      g.writeEnd();
    }
  }

  private void processSymbols( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    for ( final Element child : element.getEnclosedElements() )
    {
      if ( child instanceof ExecutableElement || child instanceof VariableElement )
      {
        processSymbol( element, child, g );
      }
    }
  }

  private void processSymbol( @Nonnull final TypeElement typeElement,
                              @Nonnull final Element element,
                              @Nonnull final JsonGenerator g )
  {
    processSymbolAnnotations( typeElement,
                              element,
                              Constants.OMIT_SYMBOLS_CLASSNAME,
                              Constants.OMIT_SYMBOL_CLASSNAME,
                              g );
    processSymbolAnnotations( typeElement,
                              element,
                              Constants.KEEP_SYMBOLS_CLASSNAME,
                              Constants.KEEP_SYMBOL_CLASSNAME,
                              g );
  }

  private void processSymbolAnnotations( @Nonnull final TypeElement typeElement,
                                         @Nonnull final Element element,
                                         @Nonnull final String containerAnnotation,
                                         @Nonnull final String annotationName,
                                         @Nonnull final JsonGenerator g )
  {
    for ( final AnnotationMirror annotation : getRepeatingAnnotations( element, containerAnnotation, annotationName ) )
    {
      g.writeStartObject();
      if ( Constants.KEEP_SYMBOL_CLASSNAME.equals( annotationName ) )
      {
        g.write( "keep", true );
      }
      g.write( "type", toTypePattern( typeElement ) );
      g.write( "member", getMemberName( element ) );
      processConditions( element, annotation, annotationName, g );
      g.writeEnd();
    }
  }

  @Nonnull
  private String getMemberName( @Nonnull final Element element )
  {
    if ( ElementKind.CONSTRUCTOR == element.getKind() )
    {
      return quotedName( element.getEnclosingElement().getSimpleName().toString() );
    }
    else if ( ElementKind.METHOD == element.getKind() )
    {
      return quotedMethodName( element.getSimpleName().toString() );
    }
    else
    {
      assert ElementKind.FIELD == element.getKind();
      return quotedName( element.getSimpleName().toString() );
    }
  }

  private void processConditions( @Nonnull final Element element,
                                  @Nonnull final AnnotationMirror annotation,
                                  @Nonnull final String annotationName,
                                  @Nonnull final JsonGenerator g )
  {
    final String when = (String) getAnnotationValue( annotation, "when" ).getValue();
    final String unless = (String) getAnnotationValue( annotation, "unless" ).getValue();
    if ( !"".equals( when ) && !"".equals( unless ) )
    {
      processingEnv.getMessager()
        .printMessage( ERROR,
                       annotationName + " annotation incorrectly specifies both a when and unless parameter",
                       element,
                       annotation );
    }
    if ( !"".equals( when ) )
    {
      parseCondition( when, ConditionDescriptor.Operator.EQUALS ).generate( g );
    }
    else if ( !"".equals( unless ) )
    {
      parseCondition( unless, ConditionDescriptor.Operator.NOT_EQUALS ).generate( g );
    }
  }

  @Nonnull
  private ConditionDescriptor parseCondition( @Nonnull final String expression,
                                              @Nonnull final ConditionDescriptor.Operator operator )
  {
    final int split = expression.indexOf( "=" );
    if ( -1 == split )
    {
      return new ConditionDescriptor( expression, "true", operator );
    }
    else
    {
      return new ConditionDescriptor( expression.substring( 0, split ), expression.substring( split + 1 ), operator );
    }
  }

  @Nonnull
  private String toGrimJsonFilename( @Nonnull final PackageElement element )
  {
    return BASE_RESOURCE_PATH + packageFilename( element ) + "package-info" + SUFFIX;
  }

  @Nonnull
  private String toGrimJsonFilename( @Nonnull final TypeElement element )
  {
    return BASE_RESOURCE_PATH + typeName( element ) + SUFFIX;
  }

  @Nonnull
  private String typeName( @Nonnull final Element element )
  {
    final Element enclosingElement = element.getEnclosingElement();
    final String parent =
      enclosingElement instanceof PackageElement ?
      packageFilename( (PackageElement) enclosingElement ) :
      typeName( enclosingElement ) + "$";
    return parent + element.getSimpleName().toString();
  }

  @Nonnull
  private String packageFilename( @Nonnull final PackageElement packageElement )
  {
    return packageElement.getQualifiedName().toString().replace( ".", "/" ) + "/";
  }

  @Nonnull
  private String toTypePattern( @Nonnull final TypeElement element )
  {
    return quotedName( element.getQualifiedName().toString() );
  }

  @Nonnull
  private String quotedName( @Nonnull final String string )
  {
    return "^" + Pattern.quote( string ) + "$";
  }

  @Nonnull
  private String quotedMethodName( @Nonnull final String string )
  {
    // A very common transform in the GWT compiler is to de-virtualize a method. In which case the original method
    // is removed and a new one is created with the $ prefix. So for methods we try to handle this scenario.
    return "^\\$?" + Pattern.quote( string ) + "$";
  }

  private void processingErrorMessage( @Nonnull final TypeElement target )
  {
    processingEnv.getMessager().printMessage( ERROR,
                                              "GrimProcessor unable to process " + target.getQualifiedName() +
                                              " because not all of its dependencies could be resolved. Check for " +
                                              "compilation errors or a circular dependency with generated code.",
                                              target );
  }

  @Nonnull
  private Collection<TypeElement> getElementsToProcess( @Nonnull final Collection<TypeElement> elements )
  {
    final List<TypeElement> deferred = _deferred
      .stream()
      .map( e -> processingEnv.getElementUtils().getTypeElement( e.getQualifiedName() ) )
      .collect( Collectors.toList() );
    _deferred = new HashSet<>();

    final ArrayList<TypeElement> elementsToProcess = new ArrayList<>();
    collectElementsToProcess( elements, elementsToProcess );
    collectElementsToProcess( deferred, elementsToProcess );
    return elementsToProcess;
  }

  private void collectElementsToProcess( @Nonnull final Collection<TypeElement> elements,
                                         @Nonnull final List<TypeElement> elementsToProcess )
  {
    for ( final TypeElement element : elements )
    {
      if ( SuperficialValidation.validateElement( element ) )
      {
        elementsToProcess.add( element );
      }
      else
      {
        _deferred.add( element );
      }
    }
  }

  /**
   * Format the json file.
   * This is horribly inefficient but it is not called very often or with big files so ... meh.
   */
  @Nonnull
  private String formatJson( @Nonnull final String input )
  {
    return
      input
        .replaceAll( "(?m)^ {4}\\{", "  {" )
        .replaceAll( "(?m)^ {4}}", "  }" )
        .replaceAll( "(?m)^ {8}\"", "    \"" )
        .replaceAll( "(?m)^ {8}]", "    ]" )
        .replaceAll( "(?m)^ {12}\\{", "      {" )
        .replaceAll( "(?m)^ {12}}", "      }" )
        .replaceAll( "(?m)^ {16}\"", "        \"" )
        .replaceAll( "(?m)^\n\\[\n", "[\n" ) +
      "\n";
  }

  @SuppressWarnings( "unchecked" )
  @Nonnull
  private List<AnnotationMirror> getRepeatingAnnotations( @Nonnull final Element typeElement,
                                                          @Nonnull final String containerClassName,
                                                          @Nonnull final String annotationClassName )
  {
    final AnnotationValue annotationValue =
      findAnnotationValue( typeElement, containerClassName, "value" );
    if ( null != annotationValue )
    {
      return ( (List<AnnotationValue>) annotationValue.getValue() ).stream().
        map( v -> (AnnotationMirror) v.getValue() ).collect( Collectors.toList() );
    }
    else
    {
      final AnnotationMirror annotation = findAnnotationByType( typeElement, annotationClassName );
      if ( null != annotation )
      {
        return Collections.singletonList( annotation );
      }
      else
      {
        return Collections.emptyList();
      }
    }
  }

  @SuppressWarnings( "SameParameterValue" )
  @Nullable
  private AnnotationValue findAnnotationValue( @Nonnull final Element typeElement,
                                               @Nonnull final String annotationClassName,
                                               @Nonnull final String parameterName )
  {
    final AnnotationMirror mirror = findAnnotationByType( typeElement, annotationClassName );
    if ( null != mirror )
    {
      return findAnnotationValue( mirror, parameterName );
    }
    else
    {
      return null;
    }
  }

  @Nullable
  private AnnotationValue findAnnotationValue( @Nonnull final AnnotationMirror annotation,
                                               @Nonnull final String parameterName )
  {
    final Map<? extends ExecutableElement, ? extends AnnotationValue> values =
      processingEnv.getElementUtils().getElementValuesWithDefaults( annotation );
    final ExecutableElement annotationKey = values.keySet().stream().
      filter( k -> parameterName.equals( k.getSimpleName().toString() ) ).findFirst().orElse( null );
    return values.get( annotationKey );
  }

  @Nonnull
  private AnnotationValue getAnnotationValue( @Nonnull final AnnotationMirror annotation,
                                              @Nonnull final String parameterName )
  {
    final AnnotationValue value = findAnnotationValue( annotation, parameterName );
    assert null != value;
    return value;
  }

  @Nullable
  private AnnotationMirror findAnnotationByType( @Nonnull final Element typeElement,
                                                 @Nonnull final String annotationClassName )
  {
    return typeElement.getAnnotationMirrors().stream().
      filter( a -> a.getAnnotationType().toString().equals( annotationClassName ) ).findFirst().orElse( null );
  }
}
