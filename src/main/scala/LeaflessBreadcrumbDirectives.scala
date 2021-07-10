import cats.implicits.catsSyntaxTuple2Semigroupal
import laika.ast.TemplateElement
import laika.directive.Templates
import laika.directive.Templates.dsl._

object LeaflessBreadcrumbDirectives {
  lazy val forTemplates: Templates.Directive = Templates.create("leafless-breadcrumb") {
    (cursor, source).mapN { case (documentCursor, sourceFragment) =>
      TemplateElement(LeaflessBreadcrumbBuilder(sourceFragment).resolve(documentCursor))
    }
  }
}
