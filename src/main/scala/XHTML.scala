import laika.ast.Element
import laika.factory.{RenderContext, RenderFormat}
import laika.render.{HTMLFormatter, XHTMLFormatter}
import laika.render.epub.XHTMLRenderer

object XHTML extends RenderFormat[HTMLFormatter] {
  override val description = "XHTML"
  val fileSuffix = "xhtml"
  val defaultRenderer: (HTMLFormatter, Element) => String = XHTMLRenderer
  val formatterFactory: RenderContext[HTMLFormatter] => HTMLFormatter = XHTMLFormatter
}
