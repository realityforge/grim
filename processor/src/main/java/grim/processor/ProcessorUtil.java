package grim.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

final class ProcessorUtil
{
  private ProcessorUtil()
  {
  }

  @SuppressWarnings( "unchecked" )
  @Nonnull
  static List<AnnotationMirror> getRepeatingAnnotations( @Nonnull final Elements elements,
                                                         @Nonnull final Element typeElement,
                                                         @Nonnull final String containerClassName,
                                                         @Nonnull final String annotationClassName )
  {
    final AnnotationValue annotationValue =
      findAnnotationValue( elements, typeElement, containerClassName, "value" );
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
  private static AnnotationValue findAnnotationValue( @Nonnull final Elements elements,
                                                      @Nonnull final Element typeElement,
                                                      @Nonnull final String annotationClassName,
                                                      @Nonnull final String parameterName )
  {
    final AnnotationMirror mirror = findAnnotationByType( typeElement, annotationClassName );
    if ( null != mirror )
    {
      return findAnnotationValue( elements, mirror, parameterName );
    }
    else
    {
      return null;
    }
  }

  @Nullable
  private static AnnotationValue findAnnotationValue( @Nonnull final Elements elements,
                                                      @Nonnull final AnnotationMirror annotation,
                                                      @Nonnull final String parameterName )
  {
    final Map<? extends ExecutableElement, ? extends AnnotationValue> values =
      elements.getElementValuesWithDefaults( annotation );
    final ExecutableElement annotationKey = values.keySet().stream().
      filter( k -> parameterName.equals( k.getSimpleName().toString() ) ).findFirst().orElse( null );
    return values.get( annotationKey );
  }

  @Nonnull
  static AnnotationValue getAnnotationValue( @Nonnull final Elements elements,
                                             @Nonnull final AnnotationMirror annotation,
                                             @Nonnull final String parameterName )
  {
    final AnnotationValue value = findAnnotationValue( elements, annotation, parameterName );
    assert null != value;
    return value;
  }

  @Nullable
  static AnnotationMirror findAnnotationByType( @Nonnull final Element typeElement,
                                                @Nonnull final String annotationClassName )
  {
    return typeElement.getAnnotationMirrors().stream().
      filter( a -> a.getAnnotationType().toString().equals( annotationClassName ) ).findFirst().orElse( null );
  }

  @Nonnull
  static PackageElement getPackageElement( @Nonnull final TypeElement element )
  {
    Element enclosingElement = element.getEnclosingElement();
    while ( null != enclosingElement )
    {
      if ( enclosingElement instanceof PackageElement )
      {
        return (PackageElement) enclosingElement;
      }
      enclosingElement = enclosingElement.getEnclosingElement();
    }
    assert false;
    return null;
  }
}
