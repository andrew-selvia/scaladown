import cats.implicits.catsSyntaxTuple2Semigroupal
import laika.ast._
import laika.directive.Templates
import laika.directive.Templates.dsl._

object DirectoryContentsDirectives {
  lazy val forTemplates: Templates.Directive = Templates.create("directory-contents") {
    (cursor, source).mapN { case (documentCursor, _) =>
      if (isDirectory(documentCursor.target)) {
        val childDocuments =
          documentCursor
            .parent
            .allDocuments
            .filter(isChild(_, documentCursor))
            .map(_.target)
        val bulletFormat = StringBullet("*")
        val bulletListItems =
          childDocuments
            .groupBy(!isDirectory(_)) // group directories and files separately, with directories first
            .flatMap { case (isNotDirectory, groupedChildDocuments) =>
              groupedChildDocuments.map(document => {
                val title =
                  document
                    .content
                    .content
                    .flatMap {
                      case Title(content, _) => content.flatMap {
                        case Text(content, _) => Some(content)
                        case _ => None
                      }.headOption
                      case _ => None
                    }
                    .headOption
                    .getOrElse(document.path.name)

                val (target, replacement) =
                  if (isNotDirectory) {
                    (".md", ".xhtml")
                  } else {
                    ("README.md", "index.xhtml")
                  }
                val path = document.path.relativeTo(documentCursor.path).toString.replace(target, replacement)
                val div = directoryContentsListItemDiv(title, path).toString
                val spacesBeforeDivEndTagInSourceCode = div.split("\n").last.takeWhile(_ == ' ')
                val spacesBeforeDivDesired = "        "
                BulletListItem(Seq(Paragraph(TemplateString(s"\n${
                  div.lines.map(line => {
                    if (line.takeWhile(_ == ' ') == "") s"$spacesBeforeDivDesired$line"
                    else line.replaceFirst(spacesBeforeDivEndTagInSourceCode, spacesBeforeDivDesired)
                  }).toArray.mkString("\n")
                }\n${spacesBeforeDivDesired.drop(2)}"))), bulletFormat)
              })
            }
            .toSeq
        val bulletListId = Some("nav-list-children")
        val bulletList = BulletList(bulletListItems, bulletFormat, Options(bulletListId))
        TemplateElement(bulletList)
      } else {
        TemplateSpanSequence.empty
      }
    }
  }

  // Determines whether thisDocumentCursor is a child of thatDocumentCursor.
  private def isChild(thisDocumentCursor: DocumentCursor, thatDocumentCursor: DocumentCursor) = {
    val thisPosition = thisDocumentCursor.position
    val thatPosition = thatDocumentCursor.position
    // Stores whether thisDocumentCursor is at next depth with respect to thatDocumentCursor.
    // Assume the root directory contains files at the following paths:
    // * /parent1/child1
    // * /parent2/child2
    // In this case, child1 and child2 are both at depth 2, despite having different parents.
    val isAtNextDepth = thisPosition.depth == thatPosition.depth + 1
    // Stores whether thisDocumentCursor is a descendant of thatDocumentCursor.
    // The relationship may not be direct (i.e. thisDocumentCursor could be a *grandchild* of thatDocumentCursor).
    val isDescendant = thisPosition.toSeq.dropRight(1) == thatPosition.toSeq
    isDescendant && isAtNextDepth
  }

  private def isDirectory(pathBase: PathBase): Boolean = pathBase.name == "README.md"
  private def isDirectory(document: Document): Boolean = isDirectory(document.path)

  private def directoryContentsListItemDiv(title: String, path: String) = {
    val isDirectory = path.endsWith("index.xhtml")
    val selector = if (isDirectory) "folder" else "doc"
    <div class="directory-contents-list-item-div">
      <svg:svg class={selector} width={if (isDirectory) "19px" else "15px"} height={if (isDirectory) "15px" else "19px"}>
        <svg:title>{title}</svg:title>
        <svg:use href={s"/assets/images/icons/$selector.fill.svg#Shape"}/>
      </svg:svg>
      <a href={path}>{title}</a>
    </div>
  }
}
