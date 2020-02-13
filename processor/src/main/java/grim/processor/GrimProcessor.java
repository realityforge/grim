package grim.processor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.json.stream.JsonGenerator;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.realityforge.proton.AbstractStandardProcessor;
import org.realityforge.proton.AnnotationsUtil;
import org.realityforge.proton.JsonUtil;
import static javax.tools.Diagnostic.Kind.*;

/**
 * Annotation processor that analyzes grim annotated source and generates rules for
 * which elements should be omitted from compiled javascript under which conditions.
 */
@SuppressWarnings( "Duplicates" )
@SupportedAnnotationTypes( { "grim.annotations.*" } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
@SupportedOptions( { "grim.defer.unresolved", "grim.defer.errors" } )
public final class GrimProcessor
  extends AbstractStandardProcessor
{
  @Nonnull
  static final String BASE_RESOURCE_PATH = "META-INF/grim";
  @Nonnull
  static final String SUFFIX = ".grim.json";
  @Nonnull
  private static final String SENTINEL = "<default>";

  @Nonnull
  @Override
  protected String getIssueTrackerURL()
  {
    return "https://github.com/realityforge/grim/issues";
  }

  @Nonnull
  @Override
  protected String getOptionPrefix()
  {
    return "grim";
  }

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
    processPackages( env, packagesToProcess );
    processTypes( env, typesToProcess.values() );
    errorIfProcessingOverAndInvalidTypesDetected( env );
    return true;
  }

  private void processTypes( @Nonnull final RoundEnvironment env, @Nonnull final Collection<TypeElement> elements )
  {
    for ( final TypeElement element : elements )
    {
      performAction( env, this::processTypeElement, element );
    }
  }

  private void processPackages( @Nonnull final RoundEnvironment env,
                                @Nonnull final Collection<PackageElement> elements )
  {
    for ( final PackageElement element : elements )
    {
      performAction( env, this::processPackageElement, element );
    }
  }

  private void processPackageElement( @Nonnull final PackageElement element )
    throws IOException
  {
    JsonUtil.writeJsonResource( processingEnv, element, toJsonFilename( element ), g -> emitPatterns( element, g ) );
  }

  private void processTypeElement( @Nonnull final TypeElement element )
    throws IOException
  {
    JsonUtil.writeJsonResource( processingEnv, element, toJsonFilename( element ), g -> emitPatterns( element, g ) );
  }

  private void emitPatterns( @Nonnull final TypeElement element, @Nonnull final JsonGenerator g )
  {
    g.writeStartArray();
    processClinits( element, g );
    processTypes( element, g );
    processSymbols( element, g );
    g.writeEnd();
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
    final AnnotationMirror annotation = AnnotationsUtil.findAnnotationByType( element, annotationName );
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
    for ( final AnnotationMirror annotation : AnnotationsUtil.getRepeatingAnnotations( element,
                                                                                       containerAnnotation,
                                                                                       annotationName ) )
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

  private void emitPatterns( @Nonnull final PackageElement element, @Nonnull final JsonGenerator g )
  {
    g.writeStartArray();
    processPatternAnnotations( element, Constants.OMIT_PATTERNS_CLASSNAME, Constants.OMIT_PATTERN_CLASSNAME, g );
    processPatternAnnotations( element, Constants.KEEP_PATTERNS_CLASSNAME, Constants.KEEP_PATTERN_CLASSNAME, g );
    g.writeEnd();
  }

  private void processPatternAnnotations( @Nonnull final PackageElement element,
                                          @Nonnull final String containerAnnotation,
                                          @Nonnull final String annotationName,
                                          @Nonnull final JsonGenerator g )
  {
    final List<AnnotationMirror> annotations =
      AnnotationsUtil.getRepeatingAnnotations( element, containerAnnotation, annotationName );
    for ( final AnnotationMirror annotation : annotations )
    {
      g.writeStartObject();
      if ( Constants.KEEP_PATTERN_CLASSNAME.equals( annotationName ) )
      {
        g.write( "keep", true );
      }
      final String typePattern = AnnotationsUtil.getAnnotationValueValue( annotation, "type" );
      final String actualTypePattern =
        SENTINEL.equals( typePattern ) ?
        "^" + element.getQualifiedName().toString().replace( ".", "\\." ) + "\\..*$" :
        typePattern;
      g.write( "type", actualTypePattern );

      final String symbolPattern = AnnotationsUtil.getAnnotationValueValue( annotation, "symbol" );
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
    final List<AnnotationMirror> annotations =
      AnnotationsUtil.getRepeatingAnnotations( element, containerAnnotation, annotationName );
    for ( final AnnotationMirror annotation : annotations )
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
    final String when = AnnotationsUtil.getAnnotationValueValue( annotation, "when" );
    final String unless = AnnotationsUtil.getAnnotationValueValue( annotation, "unless" );
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
  private String toJsonFilename( @Nonnull final PackageElement element )
  {
    return BASE_RESOURCE_PATH + "/" + packageFilename( element ) + "package-info" + SUFFIX;
  }

  @Nonnull
  private String toJsonFilename( @Nonnull final TypeElement element )
  {
    return BASE_RESOURCE_PATH + "/" + typeName( element ) + SUFFIX;
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
}
