import laika.ast.{Block, BlockResolver, DocumentCursor, NavigationBuilderContext, NavigationItem, NavigationList, NoOpt, Options, SpanSequence, Style, TreeCursor}
import laika.parse.SourceFragment

/** A block resolver that replaces itself with a navigation list from the root node of the input tree to the parent of
 * the current document during AST transformations.
 *
 * Serves as the implementation for the leafless breadcrumb directive, but can also be inserted into the AST manually.
 *
 * This is based on the default [[laika.directive.std.BreadcrumbDirectives.BreadcrumbBuilder]] but omits the leaf file
 * from the breadcrumb list in case you don't want to render it in the breadcrumb UI directly above the document title.
 *
 * @author Andrew Selvia
 */
case class LeaflessBreadcrumbBuilder(source: SourceFragment, options: Options = NoOpt) extends BlockResolver {
  type Self = LeaflessBreadcrumbBuilder
  def resolve(documentCursor: DocumentCursor): Block = {
    val navigationBuilderContext = NavigationBuilderContext(documentCursor.path, Set(Style.breadcrumb.styles.head))
    def entriesFor(treeCursor: TreeCursor): Vector[NavigationItem] = {
      val target = treeCursor.target
      val title = target.title.getOrElse(SpanSequence(treeCursor.path.name))
      val navigationItem = navigationBuilderContext.newNavigationItem(
        title,
        target.titleDocument,
        Nil,
        target.targetFormats)
      treeCursor.parent.fold(Vector(navigationItem))(entriesFor(_) :+ navigationItem)
    }
    val navigationItems = entriesFor(documentCursor.parent)
    val parentNavigationItem = navigationItems.last
    val parentNavigationItemWithId = parentNavigationItem.withOptions(Options(Some("parent"), parentNavigationItem.options.styles))
    NavigationList(navigationItems.dropRight(1) :+ parentNavigationItemWithId, Style.breadcrumb)
  }
  def withOptions(options: Options): LeaflessBreadcrumbBuilder = copy(options = options)
  lazy val unresolvedMessage: String = "Unresolved leafless breadcrumb builder"
}
