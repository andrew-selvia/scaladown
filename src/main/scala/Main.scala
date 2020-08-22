import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import laika.api.Transformer
import laika.ast._
import laika.format.{HTML, Markdown}
import laika.io.implicits._
import laika.markdown.github.GitHubFlavor

import scala.concurrent.ExecutionContext

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
            fmt.rawElement("h1", opt,
              fmt.indentedChildren(List(SpanLink(content, ExternalTarget(s"#${opt.id.getOrElse("")}"))))
              + fmt.newLine
            )
          case (fmt, Header(level, content, opt)) =>
            fmt.rawElement("h"+level, opt,
              fmt.indentedChildren(List(SpanLink(content, ExternalTarget(s"#${opt.id.getOrElse("")}"))))
                + fmt.newLine
            )
//          case (fmt, SpanLink(content, target, title, opt)) =>
//            def linkAttributes (target: Target, title: Option[String]): Seq[(String, String)] = {
//              val href = target match {
//                case InternalTarget(absolutePath, relPath) =>
//                  // TODO - 0.16 - generalize suffix check
////                  if (relPath.suffix.contains("md") && !relPath.toString.startsWith("..")) s"journal/${fmt.internalLink(relPath)}"
////                  else relPath.toString
//                  s"journal${absolutePath.toString}"
//                case ExternalTarget(url) => url
//              }
//              fmt.optAttributes(
//                "href" -> Some(href),
//                "title" -> title.map(fmt.text)
//              )
//            }
//            fmt.element("a", opt, content, linkAttributes(target, title):_*)
          case (fmt, nl: NavigationList) =>
            def navigationToBulletList (navList: NavigationList): BulletList = {
              val bullet = StringBullet("*")
              def transformItems (items: Seq[NavigationItem]): Seq[BulletListItem] = {
                items.map { item =>
                  val target: Block = item match {
                    case nh: NavigationHeader =>
//                      SpanSequence(nh.title.content, Styles("caret"))
                      SpanSequence(
                        nh.title.content.head match {
                          case text: Text if text.content == "/" => Seq(Text("Journal"))
                          case _ => nh.title.content
                        },
                        Styles("caret"))
                    case nl: NavigationLink   =>
                      SpanSequence(Seq(SpanLink(nl.title.content, nl.target)), if (nl.content.isEmpty) NoOpt else Styles("caret"))
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
        .io(blocker)
        .parallel[IO]
        .build
      directoryTransformer
        .fromDirectory(args.head)
        .toDirectory(args(1))
        .transform
    }.as(ExitCode.Success)
  }
}