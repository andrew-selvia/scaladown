import cats.data
import cats.data.{NonEmptyChain, NonEmptyList}
import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import laika.api.{MarkupParser, Renderer, Transformer}
import laika.ast._
import laika.bundle.ExtensionBundle
import laika.directive.StandardDirectives.NavigationBuilderConfig
import laika.format.{HTML, Markdown}
import laika.io.implicits._
import laika.markdown.github.GitHubFlavor
import laika.rewrite.link.SlugBuilder

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.Random

object Main extends IOApp {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  def run(args: List[String]): IO[ExitCode] = {
    Blocker[IO].use { blocker =>
      val directoryTransformer = Transformer
        .from(Markdown)
        .to(HTML)
        .using(GitHubFlavor)
        .rendering {
          // https://github.com/planet42/Laika/blob/85d757ee72ebc73c2bfb6ad099e01d2802ca3975/core/shared/src/main/scala/laika/render/HTMLRenderer.scala#L144
          case (fmt, Title(content, opt)) =>
            // <a href="#embed-screenshots-in-apple-product-images">
            //      Embed Screenshots in Apple Product Images
            //    </a>
//            fmt.indent("h1", opt, Seq(SpanLink(List(), ExternalTarget(s"#${opt.id.getOrElse("nah")}"))))
            fmt.rawElement("h1", opt,
              fmt.indentedChildren(List(SpanLink(content, ExternalTarget(s"#${opt.id.getOrElse("")}"))))
              + fmt.newLine
            )
          case (fmt, Header(level, content, opt)) =>
//            fmt.newLine + fmt.element("h"+level.toString, opt,content)
            fmt.rawElement("h"+level, opt,
              fmt.indentedChildren(List(SpanLink(content, ExternalTarget(s"#${opt.id.getOrElse("")}"))))
                + fmt.newLine
            )
          case (fmt, nl: NavigationList) => {
//            return fmt.element("ul", options, content, "id" -> "myUL")


//            def navigationToBulletList (navList: NavigationList): BulletList = {
//
//              val bullet = StringBullet("*")
//
//              def transformItems (items: Seq[NavigationItem]): Seq[BulletListItem] = {
//                items.map { item =>
//                  val target: Paragraph = item match {
//                    case nh: NavigationHeader => Paragraph(nh.title.content, nh.options + Style.navHeader)
//                    case nl: NavigationLink   =>
//                      val styles = if (nl.selfLink) Style.active else NoOpt
//                      Paragraph(Seq(SpanLink(nl.title.content, nl.target)), nl.options + styles)
//                  }
//                  val children = if (item.content.isEmpty) Nil
//                  else Seq(BulletList(transformItems(item.content), bullet))
//                  BulletListItem(target +: children, bullet)
//                }
//              }
//
//              BulletList(transformItems(navList.content), bullet, navList.options)
//            }


            def navigationToBulletList (navList: NavigationList): BulletList = {
              val bullet = StringBullet("*")
              def transformItems (items: Seq[NavigationItem]): Seq[BulletListItem] = {
                items.map { item =>
                  val target: Block = item match {
                    case nh: NavigationHeader =>
//                      Paragraph(nh.title.content, nh.options + Style.navHeader)
                      SpanSequence(nh.title.content, Styles("caret"))
                    case nl: NavigationLink   =>
//                      val styles =
//                        if (nl.selfLink) Style.active
//                        else NoOpt
//                      Paragraph(Seq(SpanLink(nl.title.content, nl.target)), nl.options + styles)
                      SpanSequence(Seq(SpanLink(nl.title.content, nl.target)), if (nl.content.isEmpty) NoOpt else Styles("caret"))

//                      val segments = nl.target.asInstanceOf[InternalTarget].absolutePath.asInstanceOf[SegmentedPath].segments
//                      val segments2 = "journal" +: segments
//                      val part3 = segments2.toNonEmptyList.toList.dropRight(1) :+ s"${segments2.last}.html"
//                      val thing = NonEmptyChain.fromNonEmptyList(NonEmptyList(part3.head, part3.tail))
//                      val target1 = InternalTarget(Path(List("")), SegmentedRelativePath(thing))
//                      SpanSequence(Seq(SpanLink(nl.title.content, target1)), if (nl.content.isEmpty) NoOpt else Styles("caret"))
                  }
                  val children =
                    if (item.content.isEmpty) Nil
                    else Seq(BulletList(transformItems(item.content), bullet, Styles("nested")))
                  BulletListItem(target +: children, bullet)
                }
              }

              BulletList(transformItems(navList.content), bullet, navList.options + Options(Option("myUL"), Set())) // TODO:  I added the last option
            }
            fmt.child(navigationToBulletList(nl))
          }
        }
        .io(blocker)
        .parallel[IO]
        .build
      directoryTransformer
        .fromDirectory("/Users/aselvia/Developer/github.com/AndrewSelvia/journal")
        .toDirectory("/Users/aselvia/Sites/journal")
        .transform
    }.as(ExitCode.Success)
  }





//  def run(args: List[String]): IO[ExitCode] = {
//    Blocker[IO].use { blocker =>
//      val parser = MarkupParser
//        .of(Markdown)
//        .io(blocker)
//        .parallel[IO]
//        .build
//      val htmlRenderer = Renderer.of(HTML).io(blocker).parallel[IO].build
//      val parse = parser.fromDirectory("/Users/aselvia/Developer/github.com/AndrewSelvia/journal").parse
//      parse.flatMap { tree =>
//        println("HELLO")
////        val root = tree.root
////        val htmlOp = htmlRenderer.from(root).toDirectory("/Users/aselvia/Sites/journal").render
////        htmlOp.map(_ => ())
//        IO(ExitCode.Success) // TODO: remove this
//      }
//    }.as(ExitCode.Success)
//  }














//  val transformer = Transformer
//    .from(Markdown)
//    .to(HTML)
//    .using(GitHubFlavor)
//    .build
////  val blocker = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
//  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
//  val directoryTransformer = Transformer
//    .from(Markdown)
//    .to(HTML)
//    .using(GitHubFlavor)
//    .io(blocker)
//    .parallel[IO]
//    .build
//  val res = directoryTransformer
//    .fromDirectory("/Users/aselvia/Developer/github.com/AndrewSelvia/journal")
//    .toDirectory("/Users/aselvia/Sites/journal")
//    .transform

  // highlight-js
  //    transformer.transform(Source.fromResource("test.md").mkString) match {
  //      case Right(html) => os.write.over(
  //        os.home/"Sites"/"article-dark.html",
  //        """<style type="text/css">
  //          |:root {
  //          |    font-family: -apple-system;
  //          |    color-scheme: light dark;
  //          |}
  //          |</style>
  //          |
  //          |<meta charset="utf-8">
  //          |
  //          |<link rel="stylesheet" href="highlight/styles/dark.css">
  //          |<script src="highlight/highlight.pack.js"></script>
  //          |<script>hljs.initHighlightingOnLoad();</script>"""
  //          .stripMargin
  //          + html)
  //    }
  //
  //  // highlight-js Nord
  //  transformer.transform(Source.fromResource("test.md").mkString) match {
  //    case Right(html) => os.write.over(
  //      os.home / "Sites" / "article-nord.html",
  //      """<style type="text/css">
  //        |:root {
  //        |    font-family: -apple-system;
  //        |    color-scheme: light dark;
  //        |}
  //        |</style>
  //        |
  //        |<meta charset="utf-8">
  //        |
  //        |<link href="https://unpkg.com/nord-highlightjs@0.1.0/dist/nord.css" rel="stylesheet" type="text/css" />
  //        |<script src="highlight/highlight.pack.js"></script>
  //        |<script>hljs.initHighlightingOnLoad();</script>"""
  //        .stripMargin
  //        + html)
  //  }

  // highlight-js Atom One Dark
  // TODO: notice I'm doing something special for this; I learned how to use SF Mono in the code block (mine.css) has what's necessary; https://webkit.org/blog/10247/new-webkit-features-in-safari-13-1/
  // :root {
  //    font-family: ui-sans-serif;
  //    color-scheme: light dark;
  //}
  //pre > code {
  //    font-family: ui-monospace;
  //}
//  transformer.transform(Source.fromResource("test.md").mkString) match {
//    case Right(html) => os.write.over(
//      os.home / "Sites" / "article-atom-one-dark.html",
//      """<link rel="stylesheet" type="text/css" href="mine.css" media="all">
//        |
//        |<meta charset="utf-8">
//        |
//        |<link href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/10.1.2/styles/atom-one-light.min.css" rel="stylesheet" type="text/css" media="(prefers-color-scheme: light)" />
//        |<link href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/10.1.2/styles/atom-one-dark.min.css" rel="stylesheet" type="text/css" media="(prefers-color-scheme: dark)" />
//        |<script src="highlight/highlight.pack.js"></script>
//        |<script>hljs.initHighlightingOnLoad();</script>"""
//        .stripMargin
//        + html)
//  }

  // prism.js
  // TODO: in order for it to work, go to article.html and change all the code blocks to be "language-scala" rather than just "scala"
  //  transformer.transform(Source.fromResource("test.md").mkString) match {
  //    case Right(html) => os.write.over(
  //      os.home/"Sites"/"article.html",
  //      """<style type="text/css">
  //        |:root {
  //        |    font-family: -apple-system;
  //        |    color-scheme: light dark;
  //        |}
  //        |</style>
  //        |
  //        |<meta charset="utf-8">
  //        |
  //        |<link href="/Users/aselvia/Sites/prism/prism.css" rel="stylesheet" />
  //        |<script src="/Users/aselvia/Sites/prism/prism.js"></script>
  //        |"""
  //        .stripMargin
  //        + html)
  //
  //
  //  }
}