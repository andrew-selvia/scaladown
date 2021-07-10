import laika.directive.DirectiveRegistry

// http://planet42.github.io/Laika/0.17/05-extending-laika/03-implementing-directives.html
object ScaladownDirectiveRegistry extends DirectiveRegistry {
  val spanDirectives = Seq()
  val blockDirectives = Seq()
  val templateDirectives = Seq(
    LeaflessBreadcrumbDirectives.forTemplates,
    DirectoryContentsDirectives.forTemplates)
  val linkDirectives = Seq()
}
