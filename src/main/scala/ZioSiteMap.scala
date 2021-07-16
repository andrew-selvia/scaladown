import better.files._
import zio._

import java.io.FileWriter
import scala.language.postfixOps
import scala.xml.{Elem, Node, PrettyPrinter, XML}

object ZioSiteMap extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    // TODO: actually take advantage of ZIO
    // TODO: handle args safely
    val inputPath = args.head.toFile
    val outputPath = args(1).toFile
    val writer = new FileWriter(outputPath / "sitemap.xml" toString)
    def urlElement(location: String): Elem =
      <url>
        <loc>{location}</loc>
      </url>
    val urlElements: Seq[Node] =
      inputPath
        .listRecursively
        .filter(_.extension.contains(".xhtml"))
        .map(file =>
          urlElement(file.pathAsString.replace(inputPath.pathAsString, "https://andrew.selvia.com")))
        .toList
        .sortBy(_ \ "loc" toString)
    val urlsetElement: Elem =
      <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      </urlset>
    XML.write(
      writer,
      XML.loadString(new PrettyPrinter(120, 2).format(urlsetElement.copy(child = urlsetElement.child ++ urlElements))),
      "utf-8",
      xmlDecl = true,
      null)
    Task(writer.close()).exitCode
  }
}
