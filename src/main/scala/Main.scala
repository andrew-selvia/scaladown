import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import laika.api.Transformer
import laika.ast._
import laika.format.Markdown
import laika.io.implicits.ImplicitTextTransformerOps
import laika.markdown.github.GitHubFlavor
import laika.render.HTMLFormatter

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private def link(htmlFormatter: HTMLFormatter, content: Seq[Span], options: Options) =
    htmlFormatter.indentedChildren(List(SpanLink(content, ExternalTarget(s"#${options.id.getOrElse("")}")))) +
      htmlFormatter.newLine
  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use {
        Transformer
          .from(Markdown)
          .to(XHTML)
          .using(GitHubFlavor)
          .using(ScaladownDirectiveRegistry)
          .rendering {
            // https://github.com/planet42/Laika/blob/85d757ee72ebc73c2bfb6ad099e01d2802ca3975/core/shared/src/main/scala/laika/render/HTMLRenderer.scala#L144
            case (htmlFormatter, Title(content, options)) =>
              htmlFormatter.rawElement("h1", options, link(htmlFormatter, content, options))
            case (htmlFormatter, Header(level, content, options)) =>
              htmlFormatter.rawElement(s"h$level", options, link(htmlFormatter, content, options))
          }
          .io(_)
          .parallel[IO]
          .build
          .use {
            _
              .fromDirectory(args.head)
              .toDirectory(args(1))
              .transform
          }
      }
      .as(ExitCode.Success)
}
