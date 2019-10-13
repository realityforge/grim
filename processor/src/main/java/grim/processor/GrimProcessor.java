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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
  private HashSet<TypeElement> _deferred = new HashSet<>();

  @Override
  public boolean process( @Nonnull final Set<? extends TypeElement> annotations, @Nonnull final RoundEnvironment env )
  {
    final Map<String, TypeElement> typesToProcess = new HashMap<>();
    for ( final TypeElement annotation : annotations )
    {
      final String annotationName = annotation.getQualifiedName().toString();
      if ( Constants.OMIT_CLINIT_CLASSNAME.equals( annotationName ) ||
           Constants.OMIT_PATTERN_CLASSNAME.equals( annotationName ) ||
           Constants.OMIT_PATTERNS_CLASSNAME.equals( annotationName ) ||
           Constants.OMIT_TYPE_CLASSNAME.equals( annotationName ) ||
           Constants.OMIT_TYPES_CLASSNAME.equals( annotationName ) )
      {
        final Set<? extends Element> elements = env.getElementsAnnotatedWith( annotation );
        for ( final Element element : elements )
        {
          final TypeElement typeElement = (TypeElement) element;
          typesToProcess.put( typeElement.getQualifiedName().toString(), typeElement );
        }
      }
      else if ( Constants.OMIT_SYMBOLS_CLASSNAME.equals( annotationName ) ||
                Constants.OMIT_SYMBOL_CLASSNAME.equals( annotationName ) )
      {
        final Set<? extends Element> elements = env.getElementsAnnotatedWith( annotation );
        for ( final Element element : elements )
        {
          final TypeElement typeElement = (TypeElement) element.getEnclosingElement();
          typesToProcess.put( typeElement.getQualifiedName().toString(), typeElement );
        }
      }
    }
    processElements( getElementsToProcess( typesToProcess.values() ) );
    if ( env.getRootElements().isEmpty() && !_deferred.isEmpty() )
    {
      _deferred.forEach( this::processingErrorMessage );
      _deferred.clear();
    }
    return true;
  }

  private void processElements( @Nonnull final Collection<TypeElement> elements )
  {
    for ( final TypeElement element : elements )
    {
      try
      {
        process( element );
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
  }

  private void process( @Nonnull final TypeElement element )
    throws IOException
  {
    final FileObject resource = processingEnv.getFiler()
      .createResource( StandardLocation.CLASS_OUTPUT, "", toGrimJsonFilename( element ), element );
    final Map<String, Object> properties = new HashMap<>();
    properties.put( JsonGenerator.PRETTY_PRINTING, true );
    final JsonGeneratorFactory generatorFactory = Json.createGeneratorFactory( properties );

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final JsonGenerator g = generatorFactory.createGenerator( baos );
    g.writeStartArray();

    processOmitClinit( element, g );
    processOmitType( element, g );
    processOmitPattern( element, g );

    g.writeEnd();
    g.close();

    try ( final OutputStream outputStream = resource.openOutputStream() )
    {
      outputStream.write( formatJson( baos.toString() ).getBytes( StandardCharsets.UTF_8 ) );
    }
    catch ( final IOException e )
    {
      resource.delete();
      throw e;
    }
  }

  private void processOmitClinit( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    final AnnotationMirror omitClinit = ProcessorUtil.findAnnotationByType( element, Constants.OMIT_CLINIT_CLASSNAME );
    if ( null != omitClinit )
    {
      g.writeStartObject();
      g.write( "type", toTypePattern( element ) );
      g.write( "member", quotedName( "$clinit" ) );
      g.writeEnd();
    }
  }

  private void processOmitType( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    final List<AnnotationMirror> omitTypes =
      ProcessorUtil.getRepeatingAnnotations( processingEnv.getElementUtils(),
                                             element,
                                             Constants.OMIT_TYPES_CLASSNAME,
                                             Constants.OMIT_TYPE_CLASSNAME );
    for ( final AnnotationMirror annotation : omitTypes )
    {
      g.writeStartObject();
      g.write( "type", toTypePattern( element ) );
      processConditions( element, annotation, "@OmitType", g );
      g.writeEnd();
    }
  }

  private void processOmitPattern( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    final List<AnnotationMirror> omitTypes =
      ProcessorUtil.getRepeatingAnnotations( processingEnv.getElementUtils(),
                                             element,
                                             Constants.OMIT_PATTERNS_CLASSNAME,
                                             Constants.OMIT_PATTERN_CLASSNAME );
    for ( final AnnotationMirror annotation : omitTypes )
    {
      g.writeStartObject();
      g.write( "type", toTypePattern( element ) );
      final String pattern =
        (String) ProcessorUtil.getAnnotationValue( processingEnv.getElementUtils(), annotation, "pattern" ).getValue();
      g.write( "member", pattern );
      processConditions( element, annotation, "@OmitPattern", g );
      g.writeEnd();
    }
  }

  private void processConditions( @Nonnull final Element element,
                                  @Nonnull final AnnotationMirror annotation,
                                  @Nonnull final String annotationName,
                                  @Nonnull final JsonGenerator g )
  {
    final String when =
      (String) ProcessorUtil.getAnnotationValue( processingEnv.getElementUtils(), annotation, "unless" ).getValue();
    final String unless =
      (String) ProcessorUtil.getAnnotationValue( processingEnv.getElementUtils(), annotation, "when" ).getValue();
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
  private String toGrimJsonFilename( @Nonnull final TypeElement element )
  {
    return "META-INF/grim/" + typeName( element ) + ".grim.json";
  }

  @Nonnull
  private String typeName( @Nonnull final Element element )
  {
    final Element enclosingElement = element.getEnclosingElement();
    final String parent;
    if ( enclosingElement instanceof PackageElement )
    {
      parent = ( (PackageElement) enclosingElement ).getQualifiedName().toString().replace( ".", "/" ) + "/";
    }
    else
    {
      parent = typeName( enclosingElement ) + "$";
    }
    return parent + element.getSimpleName().toString();
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
}
